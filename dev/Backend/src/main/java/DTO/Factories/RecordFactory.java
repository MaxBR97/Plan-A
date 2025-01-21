package DTO.Factories;

import DTO.Records.Image.*;
import DTO.Records.Model.ModelDefinition.*;
import DTO.Records.Model.ModelData.*;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Model.*;

import java.util.*;

/**
 * DTOs should be created using this class only.
 * To avoid bloat while reducing coupling between the object and its DTO
 */
public class RecordFactory {

    public static SolutionDTO makeDTO(Solution solution) {
        if(solution == null)
            throw new NullPointerException("Null solution in DTO mapping");
        if(!solution.parsed())
            throw new RuntimeException("Solution must be parsed before attempting to convert to DTO.");
        return new SolutionDTO(solution.isSolved(),solution.getVariableSolution(),solution.getVariableStructure(),
                solution.getSolvingTime(), solution.getSolvingTime());
    }
    public static PreferenceDTO makeDTO(ModelPreference preference) {
        if(preference == null)
            throw new NullPointerException("Null preference in DTO mapping");
        return new PreferenceDTO(preference.getIdentifier());
    }
    public static ConstraintDTO makeDTO(ModelConstraint constraint) {
        if(constraint == null)
            throw new NullPointerException("Null constraint in DTO mapping");
        return new ConstraintDTO(constraint.getIdentifier());
    }

    public static ConstraintModuleDTO makeDTO(ConstraintModule module) {
        if(module == null)
            throw new NullPointerException("Null constraint module in DTO mapping");
        HashMap<String, ConstraintDTO> constraints = new HashMap<>();
        for(ModelConstraint constraint:module.getConstraints().values()){
            constraints.put(constraint.getIdentifier(), makeDTO(constraint));
        }
        return new ConstraintModuleDTO(module.isActive(), module.getName(), module.getDescription(),
                constraints,new HashSet<>(makeDTO(module.getInvolvedSets())),new HashSet<>(makeDTO(module.getInvolvedParameters())));
    }
    public static PreferenceModuleDTO makeDTO(PreferenceModule module) {
        if(module == null)
            throw new NullPointerException("Null preference module in DTO mapping");
        HashMap<String, PreferenceDTO> preferences = new HashMap<>();
        for(ModelPreference preference:module.getPreferences().values()){
            preferences.put(preference.getIdentifier(), makeDTO(preference));
        }
        return new PreferenceModuleDTO(module.isActive(), module.getName(), module.getDescription(), preferences,
                new HashSet<>(makeDTO(module.getInvolvedSets())),new HashSet<>(makeDTO(module.getInvolvedParameters())));
    }

    public static SetDefinitionDTO makeDTO(ModelSet set){
        LinkedList<String> dependencies = new LinkedList<>();
        for(ModelSet dependency: set.getSetDependencies()){
            for(ModelInput.StructureBlock block:dependency.getStructure()){
                dependencies.add(block.dependency.getIdentifier());
            }
        }
        return new SetDefinitionDTO(set.getIdentifier(), dependencies, set.getType().toString());
    }

    public static Collection<SetDefinitionDTO> makeDTO(Collection<ModelSet> sets){
        LinkedList<SetDefinitionDTO> setDTOs= new LinkedList<>();
        for(ModelSet set:sets){
            setDTOs.add(makeDTO(set));
        }
        return setDTOs;
    }

    public static ParameterDefinitionDTO makeDTO(ModelParameter parameter){
        return new ParameterDefinitionDTO(parameter.getIdentifier(), parameter.getType().toString());
    }
    public static ParameterDTO makeDTO(ModelParameter parameter, String value){
        return new ParameterDTO(makeDTO(parameter), value);
    }

    private static VariableDTO makeDTO(ModelVariable variable) {
        HashSet<SetDefinitionDTO> dependencies = new HashSet<>();
        for(ModelSet dependency: variable.getSetDependencies()){
            dependencies.add(makeDTO(dependency));
        }
        return new VariableDTO(variable.getIdentifier(),dependencies);
    }
    private static Collection<ParameterDefinitionDTO> makeDTO(Set<ModelParameter> params) {
        LinkedList<ParameterDefinitionDTO> paramDTOs= new LinkedList<>();
        for(ModelParameter param:params){
            paramDTOs.add(makeDTO(param));
        }
        return paramDTOs;
    }
    public static SetDTO makeDTO(ModelSet set, Collection<String> values){
        return new SetDTO(makeDTO(set), values);
    }

    /**
     * Inefficient, maps the whole image, including all its contents into DTOs.
     * should only be called when loading a new Image, not when modifying it.
     */
    public static ImageDTO makeDTO(Image image){
        if(image == null)
            throw new NullPointerException("Null image in DTO mapping");
        HashMap<String, ConstraintModuleDTO> constraints = new HashMap<>();
        HashMap<String, PreferenceModuleDTO> preferences = new HashMap<>();
        HashMap<String, VariableDTO> variables = new HashMap<>();
        for(ConstraintModule module: image.getConstraintsModules().values()){
            constraints.put(module.getName(), makeDTO(module));
        }
        for(PreferenceModule module: image.getPreferenceModules().values()){
            preferences.put(module.getName(), makeDTO(module));
        }
        for(ModelVariable variable: image.getVariables().values()){
            variables.put(variable.getIdentifier(),makeDTO(variable));
        }
        return new ImageDTO(constraints, preferences,variables);
    }
    public static ImageResponseDTO makeDTO(UUID id, boolean compiled, String message, Image image){
        return new ImageResponseDTO(id.toString(),compiled,message,makeDTO(image));
    }


}
