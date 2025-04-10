package groupId;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
        Image image = new Image(id,command.imageName(),command.imageDescription());
        imageRepository.save(image);

        return RecordFactory.makeDTO(id, image.getModel());
    }

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

        return image.solve(command.timeout());
    }

    @Transactional
    public void overrideImage(ImageConfigDTO imgConfig) throws Exception{
        ImageDTO imageDTO= imgConfig.image();
        Image image = imageRepository.findById(imgConfig.imageId()).get();
        entityManager.merge(image);
        // //TODO: Not good going, should be fixed to do the operation with deleting and re-writing the whole image
        // imageRepository.deleteById(image.getId());
        // imageRepository.flush();
        Objects.requireNonNull(image,"Invalid imageId in image config/override image");
        Map<String, ModelVariable> variables = new HashMap<>();
        ModelInterface model= image.getModel();
        BadRequestException.requireNotNull(imgConfig.image().variablesModule().variablesOfInterest(),"Bad DTO during image config, field in variables in image is null");
        BadRequestException.requireNotNull(imgConfig.image().variablesModule().inputParams(),"Bad DTO during image config, field in variables in image is null");
        BadRequestException.requireNotNull(imgConfig.image().variablesModule().inputSets(),"Bad DTO during image config, field in variables in image is null");
        for(VariableDTO variable:imageDTO.variablesModule().variablesOfInterest()){
            ModelVariable modelVariable=model.getVariable(variable.identifier());
            modelVariable.setTags(variable.tags().toArray(new String[0]));
            modelVariable.setBoundSet(model.getSet(variable.boundSet()));
            Objects.requireNonNull(modelVariable,"Invalid variable name in config/override image");
            variables.put(variable.identifier(),modelVariable);
        }
        image.reset(variables, imageDTO.variablesModule().inputSets().stream().map(SetDefinitionDTO::name).collect(Collectors.toSet()),
                                imageDTO.variablesModule().inputParams().stream().map(ParameterDefinitionDTO::name).collect(Collectors.toSet()));
        for(ConstraintModuleDTO constraintModule:imageDTO.constraintModules()){
            image.addConstraintModule(constraintModule.moduleName(),constraintModule.description(),
                    constraintModule.constraints(),
                    constraintModule.inputSets().stream().map(SetDefinitionDTO::name).collect(Collectors.toSet()),
                    constraintModule.inputParams().stream().map(ParameterDefinitionDTO::name).collect(Collectors.toSet()));
        }
        for (PreferenceModuleDTO preferenceModule:imageDTO.preferenceModules()){
            image.addPreferenceModule(preferenceModule.moduleName(), preferenceModule.description(),
                    preferenceModule.preferences(),
                    preferenceModule.inputSets().stream().map(SetDefinitionDTO::name).collect(Collectors.toSet()),
                    preferenceModule.inputParams().stream().map(ParameterDefinitionDTO::name).collect(Collectors.toSet()),
                    preferenceModule.costParams().stream().map(ParameterDefinitionDTO::name).collect(Collectors.toSet()));
        }
        entityManager.merge(image);
    }

    @Transactional
    public ImageDTO getImage(String id) {
        Image image = imageRepository.findById(id).get();
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
    }

}
