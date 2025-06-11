package Acceptance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

public class ConfigureImageRequestBuilder implements RequestBuilder<ImageConfigDTO> {

    private String imageName;
    private CreateImageResponseDTO createImageResponse;
    private VariableModuleDTO variablesModule;
    private Set<ConstraintModuleDTO> constraintModules = new HashSet<>();
    private Set<PreferenceModuleDTO> preferenceModules = new HashSet<>();

    public ConfigureImageRequestBuilder(String imageName, CreateImageResponseDTO createImageResponse) {
        this.imageName = imageName;
        this.createImageResponse = createImageResponse;
    }

    public ConfigureImageRequestBuilder setVariablesModule(Set<String> vars, Set<String> setInputs, Set<String> paramInputs) {
        Set<VariableDTO> convertedVars = new HashSet<>();
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        for(String s : setInputs) {
            sets.add(getSet(s));
        }
        for(String p : paramInputs) {
            params.add(getParam(p));
        }
        for(String v : vars) {
            convertedVars.add(getVar(v));
        }
        variablesModule = new VariableModuleDTO(convertedVars, sets, params);
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

        for(VariableDTO v : createImageResponse.model().variables()){
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
        variablesModule = tmp;
        return this;
    }
    

    public ConfigureImageRequestBuilder addConstraintsModule(String name, String desc, Set<String> constraints, Set<String> setInputs, Set<String> paramInputs){
        Set<ConstraintModuleDTO> tmp = constraintModules.isEmpty() ? 
            new HashSet<>() : new HashSet<>(constraintModules);
        Set<SetDefinitionDTO> sets = new HashSet<>();
        Set<ParameterDefinitionDTO> params = new HashSet<>();
        for(String s : setInputs){
            sets.add(getSet(s));
        }
        for(String p : paramInputs){
            params.add(getParam(p));
        }
        tmp.add(new ConstraintModuleDTO(name, desc, constraints, sets, params));
        constraintModules = tmp;
        return this;
    }

    public ConfigureImageRequestBuilder addPreferencesModule(String name, String desc, Set<String> prefs, Set<String> setInputs, Set<String> paramInputs, Set<String> costParams){
        Set<PreferenceModuleDTO> tmp = preferenceModules.isEmpty() ? 
            new HashSet<>() : new HashSet<>(preferenceModules);
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
        preferenceModules = tmp;
        return this;
    }
    
    @Override
    public ImageConfigDTO build() {
        ImageDTO imageDTO = new ImageDTO(
            createImageResponse.imageId(),
            imageName,
            null,  // Default description
            null,
            false,
            Map.of(),
            variablesModule,
            constraintModules,
            preferenceModules
        );

        return new ImageConfigDTO(createImageResponse.imageId(), imageDTO);
    }

    private SetDefinitionDTO getSet(String id){
        
        for( Map.Entry<String,List<String>> entry : createImageResponse.model().setTypes().entrySet()){
            if(entry.getKey().equals(id)){
                return new SetDefinitionDTO(id, entry.getValue(),entry.getValue(),id);
            }
        }
        return null;
    }

    private ParameterDefinitionDTO getParam(String id){
        for( Map.Entry<String,String> entry : createImageResponse.model().paramTypes().entrySet()){
            if(entry.getKey().equals(id)){
                return new ParameterDefinitionDTO(id, entry.getValue(),entry.getValue(),id);
            }
        }
        return null;
    }

    private VariableDTO getVar(String id){
        Optional<VariableDTO> match = createImageResponse.model().variables().stream()
        .filter(v -> id.equals(v.identifier()))
        .findFirst();
        return match.get();
    }
    
    public ConfigureImageRequestBuilder updateSetMetadata(String setName, List<String> tags, String alias) {
        // Try to find and update in variables module
        if (variablesModule != null) {
            Set<SetDefinitionDTO> updatedSets = variablesModule.inputSets().stream()
                .map(set -> set.name().equals(setName) ? 
                    new SetDefinitionDTO(setName, tags, set.type(), alias) : set)
                .collect(Collectors.toSet());
            variablesModule = new VariableModuleDTO(
                variablesModule.variablesOfInterest(),
                updatedSets,
                variablesModule.inputParams()
            );
        }

        // Try to find and update in constraint modules
        constraintModules = constraintModules.stream()
            .map(module -> {
                Set<SetDefinitionDTO> updatedSets = module.inputSets().stream()
                    .map(set -> set.name().equals(setName) ? 
                        new SetDefinitionDTO(setName, tags, set.type(), alias) : set)
                    .collect(Collectors.toSet());
                return new ConstraintModuleDTO(
                    module.moduleName(),
                    module.description(),
                    module.constraints(),
                    updatedSets,
                    module.inputParams()
                );
            })
            .collect(Collectors.toSet());

        // Try to find and update in preference modules
        preferenceModules = preferenceModules.stream()
            .map(module -> {
                Set<SetDefinitionDTO> updatedSets = module.inputSets().stream()
                    .map(set -> set.name().equals(setName) ? 
                        new SetDefinitionDTO(setName, tags, set.type(), alias) : set)
                    .collect(Collectors.toSet());
                return new PreferenceModuleDTO(
                    module.moduleName(),
                    module.description(),
                    module.preferences(),
                    updatedSets,
                    module.inputParams(),
                    module.costParams()
                );
            })
            .collect(Collectors.toSet());

        return this;
    }

    public ConfigureImageRequestBuilder updateParameterMetadata(String paramName, String tag, String alias) {
        // Try to find and update in variables module
        if (variablesModule != null) {
            Set<ParameterDefinitionDTO> updatedParams = variablesModule.inputParams().stream()
                .map(param -> param.name().equals(paramName) ? 
                    new ParameterDefinitionDTO(paramName, tag, param.type(), alias) : param)
                .collect(Collectors.toSet());
            variablesModule = new VariableModuleDTO(
                variablesModule.variablesOfInterest(),
                variablesModule.inputSets(),
                updatedParams
            );
        }

        // Try to find and update in constraint modules
        constraintModules = constraintModules.stream()
            .map(module -> {
                Set<ParameterDefinitionDTO> updatedParams = module.inputParams().stream()
                    .map(param -> param.name().equals(paramName) ? 
                        new ParameterDefinitionDTO(paramName, tag, param.type(), alias) : param)
                    .collect(Collectors.toSet());
                return new ConstraintModuleDTO(
                    module.moduleName(),
                    module.description(),
                    module.constraints(),
                    module.inputSets(),
                    updatedParams
                );
            })
            .collect(Collectors.toSet());

        // Try to find and update in preference modules
        preferenceModules = preferenceModules.stream()
            .map(module -> {
                Set<ParameterDefinitionDTO> updatedParams = module.inputParams().stream()
                    .map(param -> param.name().equals(paramName) ? 
                        new ParameterDefinitionDTO(paramName, tag, param.type(), alias) : param)
                    .collect(Collectors.toSet());
                Set<ParameterDefinitionDTO> updatedCostParams = module.costParams().stream()
                    .map(param -> param.name().equals(paramName) ? 
                        new ParameterDefinitionDTO(paramName, tag, param.type(), alias) : param)
                    .collect(Collectors.toSet());
                return new PreferenceModuleDTO(
                    module.moduleName(),
                    module.description(),
                    module.preferences(),
                    module.inputSets(),
                    updatedParams,
                    updatedCostParams
                );
            })
            .collect(Collectors.toSet());

        return this;
    }

    public ConfigureImageRequestBuilder updateVariableMetadata(String varIdentifier, List<String> tags) {
        if (variablesModule != null) {
            Set<VariableDTO> updatedVars = variablesModule.variablesOfInterest().stream()
                .map(var -> var.identifier().equals(varIdentifier) ? 
                    new VariableDTO(
                        varIdentifier,
                        tags,
                        var.type(),
                        var.dep(),
                        var.boundSet(),
                        var.isBinary()
                    ) : var)
                .collect(Collectors.toSet());
            variablesModule = new VariableModuleDTO(
                updatedVars,
                variablesModule.inputSets(),
                variablesModule.inputParams()
            );
        }
        return this;
    }
}
