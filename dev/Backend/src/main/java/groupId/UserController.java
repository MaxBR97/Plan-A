package groupId;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelInput;
import Model.ModelInterface;
import Model.ModelVariable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserController {
private final Map<UUID,Image> images;

@Value("${app.file.storage-dir}")
private String storageDir;


    public UserController(){
        images = new HashMap<>();
    }


    public CreateImageResponseDTO createImageFromFile(String code) throws Exception {
        UUID id= UUID.randomUUID();
        String name = id.toString();
        String jarDir = new File(Main.class.getProtectionDomain()
             .getCodeSource().getLocation().toURI()).getParent();
        Path path = Paths.get(jarDir,storageDir, File.separator+name+".zpl");
        Files.createDirectories(path.getParent());
        Files.writeString(path,code, StandardOpenOption.CREATE);
        Image image=new Image(path.toAbsolutePath().toString());
        images.put(id,image);
        return RecordFactory.makeDTO(id,image.getModel());
    }

    public SolutionDTO solve(SolveCommandDTO command) throws Exception {
        Image image = images.get(UUID.fromString(command.imageId()));
        ModelInterface model = image.getModel();
        for (Map.Entry<String,List<List<String>>> set : command.input().setsToValues().entrySet()){
            List<String> setElements = new LinkedList<>();
            for(List<String> element : set.getValue()){
                String tuple = ModelInput.convertArrayOfAtomsToTuple((element.toArray(new String[0])));
                setElements.add(tuple);
            }
            model.setInput(model.getSet(set.getKey()), setElements.toArray(new String[0]));
        }

        for (Map.Entry<String,List<String>> parameter : command.input().paramsToValues().entrySet()){
            model.setInput(model.getParameter(parameter.getKey()), ModelInput.convertArrayOfAtomsToTuple(parameter.getValue().toArray(new String[0])));
        }

        for (String constraint : command.input().constraintsToggledOff()){
            model.toggleFunctionality(model.getConstraint(constraint), false);
        }

        for (String preference : command.input().preferencesToggledOff()){
            model.toggleFunctionality(model.getPreference(preference), false);
        }

        return image.solve(command.timeout());
    }

    public void overrideImage(ImageConfigDTO imgConfig) {
        ImageDTO imageDTO= imgConfig.image();
        Image image=images.get(UUID.fromString(imgConfig.id()));
        Objects.requireNonNull(image,"Invalid id in image config/override image");
        Map<String, ModelVariable> variables = new HashMap<>();
        ModelInterface model= image.getModel();
        for(String variable:imageDTO.variablesModule().variablesOfInterest()){
            ModelVariable modelVariable=model.getVariable(variable);
            Objects.requireNonNull(modelVariable,"Invalid variable name in config/override image");
            variables.put(variable,modelVariable);
        }
        image.reset(variables, imageDTO.variablesModule().variablesConfigurableSets(),imageDTO.variablesModule().variablesConfigurableParams());
        for(ConstraintModuleDTO constraintModule:imageDTO.constraintModules()){
            image.addConstraintModule(constraintModule.moduleName(),constraintModule.description(),
                    constraintModule.constraints(),constraintModule.inputSets(),constraintModule.inputParams());
        }
        for (PreferenceModuleDTO preferenceModule:imageDTO.preferenceModules()){
            image.addPreferenceModule(preferenceModule.moduleName(), preferenceModule.description(),
                    preferenceModule.preferences(),preferenceModule.inputSets(),preferenceModule.inputParams());
        }
    }
    public Image getImage(String id) {
        return images.get(UUID.fromString(id));
    }
}
