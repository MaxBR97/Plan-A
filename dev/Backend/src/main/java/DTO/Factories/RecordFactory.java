package DTO.Factories;

import DTO.Records.Image.*;
import DTO.Records.Model.ModelDefinition.*;
import DTO.Records.Model.ModelData.*;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Model.*;
import org.yaml.snakeyaml.util.Tuple;

import java.util.*;

/**
 * DTOs should be created using this class only.
 * To avoid bloat while reducing coupling between the object and its DTO
 */
public class RecordFactory {

    public static SolutionDTO makeDTO(Solution solution) {
        Objects.requireNonNull(solution,"Null Solution in DTO map");
        if(!solution.parsed())
            throw new RuntimeException("Solution must be parsed before attempting to convert to DTO.");
        if(!solution.isSolved())
            return new SolutionDTO(false,-1,-1,"",new HashMap<>());
        double solvingTime = solution.getSolvingTime();
        double objectiveValue = solution.getObjectiveValue();
        boolean solved = true;
        HashMap<String, SolutionVariable> variables = new HashMap<>();
         for(ModelVariable variable: solution.getVariables()){
             String variableName= variable.getIdentifier();
             Set<SolutionValueDTO> variableValues = new HashSet<>();
             List<String> variableStructure=List.copyOf(solution.getVariableStructure(variableName));
             List<String> variableTypes=List.copyOf(solution.getVariableTypes(variableName));
             for(Tuple<List<String>,Integer> value:solution.getVariableSolution(variableName)){
                variableValues.add(new SolutionValueDTO(value._1(),value._2()));
            }
            variables.put(variableName,new SolutionVariable(variableStructure,variableTypes,variableValues));
         }
        return new SolutionDTO(solved,solvingTime,objectiveValue,"",variables);
    }

    public static PreferenceDTO makeDTO(ModelPreference preference) {
        if(preference == null)
            throw new NullPointerException("Null preference in DTO mapping");
        HashSet<ModelSet> sets = new HashSet<>();
        preference.getPrimitiveSets(sets);
        HashSet<ModelParameter> parameters = new HashSet<>();
        preference.getPrimitiveParameters(parameters);
        return new PreferenceDTO(preference.getIdentifier(),makeDTO(sets,parameters));
    }
    public static ConstraintDTO makeDTO(ModelConstraint constraint) {
        if(constraint == null)
            throw new NullPointerException("Null constraint in DTO mapping");
        HashSet<ModelSet> sets = new HashSet<>();
        constraint.getPrimitiveSets(sets);
        HashSet<ModelParameter> parameters = new HashSet<>();
        constraint.getPrimitiveParameters(parameters);
        return new ConstraintDTO(constraint.getIdentifier(),makeDTO(sets,parameters));
    }

    public static ConstraintModuleDTO makeDTO(ConstraintModule module) {
        if(module == null)
            throw new NullPointerException("Null constraint module in DTO mapping");
        Set<String> constraints = new HashSet<>();
        for(ModelConstraint constraint:module.getConstraints().values()){
            constraints.add(constraint.getIdentifier());
        }

        Set<String> sets = new HashSet<>();
        for(ModelSet s:module.getInvolvedSets()){
            sets.add(s.getIdentifier());
        }
        Set<String> param = new HashSet<>();
        for(ModelParameter p:module.getInvolvedParameters()){
            param.add(p.getIdentifier());
        }
        return new ConstraintModuleDTO(module.getName(), module.getDescription(),
                constraints, sets, param);
    }
    public static PreferenceModuleDTO makeDTO(PreferenceModule module) {
        if(module == null)
            throw new NullPointerException("Null preference module in DTO mapping");
        Set<String> preferences = new HashSet<>();
        for(ModelPreference pref:module.getPreferences().values()){
            preferences.add(pref.getIdentifier());
        }

        Set<String> sets = new HashSet<>();
        for(ModelSet s:module.getInvolvedSets()){
            sets.add(s.getIdentifier());
        }

        Set<String> param = new HashSet<>();
        for(ModelParameter p:module.getInvolvedParameters()){
            param.add(p.getIdentifier());
        }
        return new PreferenceModuleDTO(module.getName(), module.getDescription(),
                preferences, sets, param);
    }

    public static SetDefinitionDTO makeDTO(ModelSet set){
        LinkedList<String> dependencies = new LinkedList<>();
        for(ModelSet dependency: set.getSetDependencies()){
            for(ModelInput.StructureBlock block:dependency.getStructure()){
                dependencies.add(block.dependency.getIdentifier());
            }
        }
        return new SetDefinitionDTO(set.getIdentifier(), dependencies, set.getType().typeList());
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
        HashSet<ModelSet> sets = new HashSet<>();
        variable.getPrimitiveSets(sets);
        HashSet<ModelParameter> parameters = new HashSet<>();
        variable.getPrimitiveParameters(parameters);
        return new VariableDTO(variable.getIdentifier(),makeDTO(sets,parameters));
    }
    private static Collection<ParameterDefinitionDTO> makeDTO(Set<ModelParameter> params) {
        LinkedList<ParameterDefinitionDTO> paramDTOs= new LinkedList<>();
        for(ModelParameter param:params){
            paramDTOs.add(makeDTO(param));
        }
        return paramDTOs;
    }
    public static SetDTO makeDTO(ModelSet set, List<String> values){
        return new SetDTO(makeDTO(set), values);
    }

    

    /**
     * Inefficient, maps the whole image, including all its contents into DTOs.
     * should only be called when loading a new Image, not when modifying it.
     */
    public static ImageDTO makeDTO(Image image){
        if(image == null)
            throw new NullPointerException("Null image in DTO mapping");
        Set< ConstraintModuleDTO> constraints = new HashSet<>();
        Set<PreferenceModuleDTO> preferences = new HashSet<>();
        VariableModuleDTO variables = makeDTO(image.getVariables().values().stream().toList());
                for(ConstraintModule module: image.getConstraintsModules().values()){
                    constraints.add(makeDTO(module));
                }
                for(PreferenceModule module: image.getPreferenceModules().values()){
                    preferences.add(makeDTO(module));
                }
                
                return new ImageDTO(variables, constraints, preferences);
            }
        private static VariableModuleDTO makeDTO(List<ModelVariable> values) {
            Set<String> intr = new HashSet<>();
            Set<String> params = new HashSet<>();
            Set<String> sets = new HashSet<>();
            for(ModelVariable mv : values){
                intr.add(mv.getIdentifier());
                for(ModelSet set : mv.getSetDependencies()){
                    sets.add(set.getIdentifier());
                }
                for(ModelParameter param : mv.getParamDependencies()){
                    params.add(param.getIdentifier());
                }
            }
            return new VariableModuleDTO(intr, sets, params);
        }
            public static ImageResponseDTO makeDTO(UUID id, Image image){
        return new ImageResponseDTO(id.toString(),makeDTO(image));
    }

    public static CreateImageResponseDTO makeDTO(UUID id, ModelInterface md){
        return new CreateImageResponseDTO(id.toString(), makeDTO(md));
    }
    
    private static ModelDTO makeDTO(ModelInterface modelInterface) {
        Set<ConstraintDTO> constraints = new HashSet<>();
        Set<PreferenceDTO> preferences = new HashSet<>();
        //TODO: Variables.
        Set<VariableDTO> variables = new HashSet<>();
        Map<String,List<String>> setTypes = new HashMap<>();
        Map<String,String> paramTypes = new HashMap<>();
        Map<String,String> varTypes = new HashMap<>();
        for(ModelConstraint constraint : modelInterface.getConstraints()){
            HashSet<ModelSet> primitiveSets = new HashSet<>();
            HashSet<ModelParameter> primitiveParameters = new HashSet<>();
            constraint.getPrimitiveSets(primitiveSets);
            constraint.getPrimitiveParameters(primitiveParameters);
            constraints.add(makeDTO(constraint));
            for(ModelSet set : primitiveSets)
                setTypes.putIfAbsent(set.getIdentifier(),set.getType().typeList());
            for(ModelParameter parameter : primitiveParameters)
                paramTypes.putIfAbsent(parameter.getIdentifier(),parameter.getType().toString());
        }
        for(ModelPreference preference : modelInterface.getPreferences()){
            preferences.add(makeDTO(preference));
            HashSet<ModelSet> primitiveSets = new HashSet<>();
            HashSet<ModelParameter> primitiveParameters = new HashSet<>();
            preference.getPrimitiveSets(primitiveSets);
            preference.getPrimitiveParameters(primitiveParameters);
            for(ModelSet set : primitiveSets)
                setTypes.putIfAbsent(set.getIdentifier(),set.getType().typeList());
            for(ModelParameter parameter : primitiveParameters)
                paramTypes.putIfAbsent(parameter.getIdentifier(),parameter.getType().toString());
        }
        for(ModelVariable variable : modelInterface.getVariables()){
            variables.add(makeDTO(variable));
            HashSet<ModelSet> primitiveSets = new HashSet<>();
            HashSet<ModelParameter> primitiveParameters = new HashSet<>();
            variable.getPrimitiveSets(primitiveSets);
            variable.getPrimitiveParameters(primitiveParameters);
            for(ModelSet set : primitiveSets)
                setTypes.putIfAbsent(set.getIdentifier(),set.getType().typeList());
            for(ModelParameter parameter : primitiveParameters)
                paramTypes.putIfAbsent(parameter.getIdentifier(),parameter.getType().toString());
        }
        return new ModelDTO(constraints, preferences, variables, setTypes,paramTypes,varTypes);
    }
    public static DependenciesDTO makeDTO(Set<ModelSet> sets, Set<ModelParameter> parameters){
        Set<String> resS = new HashSet<>();
        Set<String> resP = new HashSet<>();
        for(ModelSet x : sets){
            resS.add(x.getIdentifier());
        }
        for(ModelParameter x : parameters){
            resP.add(x.getIdentifier());
        }
        return new DependenciesDTO(resS, resP);
    }
    public static CreateImageFromFileDTO makeDTO(String code){
        return new CreateImageFromFileDTO(code);
    }

}
