package groupId;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DataAccess.ImageRepository;
import Exceptions.InternalErrors.BadRequestException;
import Image.Image;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelPreference;
import Model.ModelType;
import Model.ModelVariable;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@RestController
public class ImageController {
    //private final Map<UUID,Image> images;
    private final ImageRepository imageRepository;
    private final ModelFactory modelFactory;
    private EntityManager entityManager;
    //should be enabled only when running in desktop app
    private Image currentlyCached;


    @Autowired
    public ImageController(ImageRepository imageRepository, ModelFactory modelFactory, EntityManager em) {
        this.imageRepository = imageRepository;
        this.modelFactory = modelFactory;
        this.entityManager = em;
        Image.setModelFactory(modelFactory);
    }

    //TODO: ensure the code file received does not exceed 8KB
    @Transactional
    public CreateImageResponseDTO createImageFromFile(CreateImageFromFileDTO command) throws Exception {
        String id = UUID.randomUUID().toString();
        modelFactory.uploadNewModel(id,command.code());
        Image image = new Image(
        id,
        command.imageName(),
        command.imageDescription(),
        command.owner(),
        command.isPrivate() == null ? true : command.isPrivate());
        imageRepository.save(image);
        return RecordFactory.makeDTO(id, image.getModel());
    }

    /* TODO: this method gets the model of the image to toggle off constraints and preferences.
      What actually should happen is image toggling off functions should be called, instead of
      directly calling mode methods.
    */
    @Transactional
    public SolutionDTO solve(SolveCommandDTO command) throws Exception {
        Image image = imageRepository.findById(command.imageId()).get();
        ModelInterface model = image.getModel();
        for (Map.Entry<String,List<List<String>>> set : command.input().setsToValues().entrySet()){
            List<String> setElements = new LinkedList<>();
            for(List<String> element : set.getValue()){
                String tuple = ModelType.convertArrayOfAtoms((element.toArray(new String[0])),model.getSet(set.getKey()).getType());
                setElements.add(tuple);
            }
            model.setInput(model.getSet(set.getKey()), setElements.toArray(new String[0]));
        }

        for (Map.Entry<String,List<String>> parameter : command.input().paramsToValues().entrySet()){
            model.setInput(model.getParameter(parameter.getKey()), ModelType.convertArrayOfAtoms(parameter.getValue().toArray(new String[0]), model.getParameter(parameter.getKey()).getType()));
        }

        for (String constraintModule : command.input().constraintModulesToggledOff()){
            Collection<ModelConstraint> constraintsToToggleOff = image.getConstraintsModule(constraintModule).getConstraints().values();
            for(ModelConstraint mc : constraintsToToggleOff){
                model.toggleFunctionality(model.getConstraint(mc.getIdentifier()), false);
            }
        }

        for (String preferenceModule : command.input().preferenceModulesToggledOff()){
            Collection<ModelPreference> preferencesToToggleOff = image.getPreferencesModule(preferenceModule).getPreferences().values();
            for(ModelPreference mp : preferencesToToggleOff){
                model.toggleFunctionality(model.getPreference(mp.getIdentifier()), false);
            }
        }

        return image.solve(command.timeout(), command.solverSettings());
    }

    private ModelInterface currentlySolving;
    public ModelInterface getModelCurrentlySolving(){
        return currentlySolving;
    }

    //TODO: same as with solve(...)
    @Transactional
    public CompletableFuture<SolutionDTO> solveAsync(SolveCommandDTO command, boolean continueLast) throws Exception {
        Image image = currentlyCached != null ? currentlyCached : imageRepository.findById(command.imageId()).get();
        currentlyCached = image;
        ModelInterface model = image.getModel();
        for (Map.Entry<String,List<List<String>>> set : command.input().setsToValues().entrySet()){
            List<String> setElements = new LinkedList<>();
            for(List<String> element : set.getValue()){
                String tuple = ModelType.convertArrayOfAtoms((element.toArray(new String[0])),model.getSet(set.getKey()).getType());
                setElements.add(tuple);
            }
            model.setInput(model.getSet(set.getKey()), setElements.toArray(new String[0]));
        }

        for (Map.Entry<String,List<String>> parameter : command.input().paramsToValues().entrySet()){
            model.setInput(model.getParameter(parameter.getKey()), ModelType.convertArrayOfAtoms(parameter.getValue().toArray(new String[0]), model.getParameter(parameter.getKey()).getType()));
        }

        for (String constraintModule : command.input().constraintModulesToggledOff()){
            Collection<ModelConstraint> constraintsToToggleOff = image.getConstraintsModule(constraintModule).getConstraints().values();
            for(ModelConstraint mc : constraintsToToggleOff){
                model.toggleFunctionality(model.getConstraint(mc.getIdentifier()), false);
            }
        }

        for (String preferenceModule : command.input().preferenceModulesToggledOff()){
            Collection<ModelPreference> preferencesToToggleOff = image.getPreferencesModule(preferenceModule).getPreferences().values();
            for(ModelPreference mp : preferencesToToggleOff){
                model.toggleFunctionality(model.getPreference(mp.getIdentifier()), false);
            }
        }
        currentlySolving = model;
        CompletableFuture<SolutionDTO> futureAns = image.solveAsync(command.timeout(), command.solverSettings(), continueLast);
        return futureAns;
    }

    @Transactional
public void overrideImage(ImageConfigDTO imgConfig) throws Exception {
    ImageDTO imageDTO = imgConfig.image();
    String imageId = imgConfig.imageId();
    
    // Find the image by ID
    Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new BadRequestException("Invalid imageId in image config"));
    
    // Update basic properties
    if (imageDTO.isPrivate() != null) {
        image.setIsPrivate(imageDTO.isPrivate());
    }
    
    // Update solver scripts
    image.setSolverScripts(imageDTO.solverSettings());
    
    // Get the model
    ModelInterface model = image.getModel();
    
    // Validate DTO inputs
    BadRequestException.requireNotNull(imageDTO.variablesModule().variablesOfInterest(), 
            "Bad DTO during image config, variablesOfInterest is null");
    BadRequestException.requireNotNull(imageDTO.variablesModule().inputParams(), 
            "Bad DTO during image config, inputParams is null");
    BadRequestException.requireNotNull(imageDTO.variablesModule().inputSets(), 
            "Bad DTO during image config, inputSets is null");
    
    // Clear existing modules first (since we're doing a full replacement)
    // This properly handles orphanRemoval
    image.getConstraintsModules().clear();
    image.getPreferenceModules().clear();
    
    // Handle variables module
    Map<String, ModelVariable> variables = new HashMap<>();
    for (VariableDTO variable : imageDTO.variablesModule().variablesOfInterest()) {
        ModelVariable modelVariable = model.getVariable(variable.identifier());
        if (modelVariable == null) {
            throw new BadRequestException("Invalid variable name: " + variable.identifier());
        }
        
        modelVariable.setTags(variable.tags().toArray(new String[0]));
        modelVariable.setBoundSet(model.getSet(variable.boundSet()));
        variables.put(variable.identifier(), modelVariable);
    }
    
    // Update input sets and parameters
    Set<String> inputSetNames = imageDTO.variablesModule().inputSets().stream()
            .map(SetDefinitionDTO::name)
            .collect(Collectors.toSet());
    
    Set<String> inputParamNames = imageDTO.variablesModule().inputParams().stream()
            .map(ParameterDefinitionDTO::name)
            .collect(Collectors.toSet());
    
    // Reset variables and configure inputs
    image.reset(variables, inputSetNames, inputParamNames);
    image.configureModelInputs(imageDTO.variablesModule().inputSets(), 
                              imageDTO.variablesModule().inputParams());
    
    // Add constraint modules
    for (ConstraintModuleDTO constraintModule : imageDTO.constraintModules()) {
        image.addConstraintModule(
                constraintModule.moduleName(),
                constraintModule.description(),
                constraintModule.constraints(),
                constraintModule.inputSets().stream()
                        .map(SetDefinitionDTO::name)
                        .collect(Collectors.toSet()),
                constraintModule.inputParams().stream()
                        .map(ParameterDefinitionDTO::name)
                        .collect(Collectors.toSet())
        );
        
        image.configureModelInputs(constraintModule.inputSets(), constraintModule.inputParams());
    }
    
    // Add preference modules
    for (PreferenceModuleDTO preferenceModule : imageDTO.preferenceModules()) {
        image.addPreferenceModule(
                preferenceModule.moduleName(),
                preferenceModule.description(),
                preferenceModule.preferences(),
                preferenceModule.inputSets().stream()
                        .map(SetDefinitionDTO::name)
                        .collect(Collectors.toSet()),
                preferenceModule.inputParams().stream()
                        .map(ParameterDefinitionDTO::name)
                        .collect(Collectors.toSet()),
                preferenceModule.costParams().stream()
                        .map(ParameterDefinitionDTO::name)
                        .collect(Collectors.toSet())
        );
        
        image.configureModelInputs(preferenceModule.inputSets(), preferenceModule.inputParams());
    }
    
    // Save the updated entity
    imageRepository.save(image);
}
    @Transactional
    public ImageDTO getImage(String id) {
        Image image = currentlyCached != null && currentlyCached.getId().equals(id) ? currentlyCached : imageRepository.findById(id).get();
        currentlyCached = image;
        return RecordFactory.makeDTO(image);
    }

    @Transactional
    InputDTO loadLastInput(String imageId) throws Exception {
        return imageRepository.findById(imageId).get().getInput();
    }

    @Transactional
    public List<ImageDTO> getAllImages() {
        List<ImageDTO> ans = new LinkedList<>();
        for(Image image : imageRepository.findAll()){
            ans.add(RecordFactory.makeDTO(image));
        }
        return ans;
    }

    @Transactional
    void deleteImage(String imageId) throws Exception {
        imageRepository.deleteById(imageId);
        System.gc();
        modelFactory.deleteModel(imageId);
        if(currentlyCached.getId().equals(imageId))
            currentlyCached = null;
    }

}
