package Acceptance;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate.Param;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;

public class ConfigureImageRequestBuilder implements RequestBuilder{

    CreateImageResponseDTO context;
    ImageDTO imageDTO;

    public ConfigureImageRequestBuilder(String imageName, CreateImageResponseDTO createdImageReponse){
        this.context = createdImageReponse;
        imageDTO = new ImageDTO(context.imageId(),
                              imageName,
                              null,  // description
                              null,  // owner
                              null,  // isPrivate
                              null,  // solverSettings
                              null,  // variablesModule
                              null,  // constraintModules
                              null   // preferenceModules
                              );
        // this.context = createdImageReponse;
        // imageDTO=new ImageDTO(context.imageId(),imageName,null, null, null, null,  new VariableModuleDTO(Set.of(),Set.of(),Set.of()),
        //                                         Set.of(),
        //                                         Set.of());
    }

    public ConfigureImageRequestBuilder setVariablesModule(Set<String> vars, Set<String> setInputs, Set<String> paramInputs){
        Set<VariableDTO> convertedVars = new HashSet<>();
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        for(String s : setInputs){
            sets.add(getSet(s));
        }
        for(String p : paramInputs){
            params.add(getParam(p));
        }
        for(String v : vars){
            convertedVars.add(getVar(v));
        }
        VariableModuleDTO tmp = new VariableModuleDTO(convertedVars, sets, params);
        imageDTO = new ImageDTO(imageDTO.imageId(), imageDTO.imageName(), imageDTO.imageDescription(),
                                imageDTO.owner(),
                                imageDTO.isPrivate(),
                                null,
                                tmp,
                                imageDTO.constraintModules(),
                                imageDTO.preferenceModules()
                    );
        return this;
    }

    //insert all vars, all involved sets and params
    public ConfigureImageRequestBuilder setDefaultVariablesModule(){
        Set<VariableDTO> convertedVars = new HashSet<>();
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        Set<String> vars = new HashSet<>();
        Set<String> setInputs = new HashSet<>();
        Set<String> paramInputs = new HashSet<>();

        for(VariableDTO v : context.model().variables()){
            vars.add(v.identifier());
            setInputs.addAll(v.dep().setDependencies());
            paramInputs.addAll(v.dep().paramDependencies());
        }

        for(String s : setInputs){
            sets.add(getSet(s));
        }
        for(String p : paramInputs){
            params.add(getParam(p));
        }
        for(String v : vars){
            convertedVars.add(getVar(v));
        }
        VariableModuleDTO tmp = new VariableModuleDTO(convertedVars, sets, params);
        imageDTO = new ImageDTO(imageDTO.imageId(), imageDTO.imageName(), imageDTO.imageDescription(),
                                imageDTO.owner(),
                                imageDTO.isPrivate(),
                                null,
                                tmp,
                                imageDTO.constraintModules(),
                                imageDTO.preferenceModules()
                    );
        return this;
    }
    

    public ConfigureImageRequestBuilder addConstraintsModule(String name, String desc, Set<String> constraints, Set<String> setInputs, Set<String> paramInputs){
        Set<ConstraintModuleDTO> tmp = imageDTO.constraintModules() == null ? 
            new HashSet<>() : new HashSet<>(imageDTO.constraintModules());
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        for(String s : setInputs){
            sets.add(getSet(s));
        }
        for(String p : paramInputs){
            params.add(getParam(p));
        }
        tmp.add(new ConstraintModuleDTO(name, desc, constraints, sets, params));
        imageDTO = new ImageDTO(imageDTO.imageId(), imageDTO.imageName(), imageDTO.imageDescription(),
                                imageDTO.owner(),
                                imageDTO.isPrivate(),
                                null,
                                imageDTO.variablesModule(),
                                tmp,
                                imageDTO.preferenceModules()
                    );
        return this;
    }

    public ConfigureImageRequestBuilder addPreferencesModule(String name, String desc, Set<String> prefs, Set<String> setInputs, Set<String> paramInputs, Set<String> costParams){
        Set<PreferenceModuleDTO> tmp = imageDTO.preferenceModules() == null ? 
            new HashSet<>() : new HashSet<>(imageDTO.preferenceModules());
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        Set<ParameterDefinitionDTO> costs = new HashSet<>();
        for(String s : setInputs){
            sets.add(getSet(s));
        }
        for(String p : paramInputs){
            params.add(getParam(p));
        }
        for(String p : costParams){
            costs.add(getParam(p));
        }

        tmp.add(new PreferenceModuleDTO(name, desc, prefs, sets, params, costs));
        imageDTO = new ImageDTO(imageDTO.imageId(), imageDTO.imageName(), imageDTO.imageDescription(),
                                imageDTO.owner(),
                                imageDTO.isPrivate(),
                                null,
                                imageDTO.variablesModule(),
                                imageDTO.constraintModules(),
                                tmp
                    );
        return this;
    }
    
    public ImageConfigDTO build() {
        return new ImageConfigDTO(imageDTO.imageId(),imageDTO);
    }

    private SetDefinitionDTO getSet(String id){
        
        for( Map.Entry<String,List<String>> entry : context.model().setTypes().entrySet()){
            if(entry.getKey().equals(id)){
                return new SetDefinitionDTO(id, entry.getValue(),entry.getValue(),id);
            }
        }
        return null;
    }

    private ParameterDefinitionDTO getParam(String id){
        for( Map.Entry<String,String> entry : context.model().paramTypes().entrySet()){
            if(entry.getKey().equals(id)){
                return new ParameterDefinitionDTO(id, entry.getValue(),entry.getValue(),id);
            }
        }
        return null;
    }

    private VariableDTO getVar(String id){
        Optional<VariableDTO> match = context.model().variables().stream()
        .filter(v -> id.equals(v.identifier()))
        .findFirst();
        return match.get();
    }
    

}
