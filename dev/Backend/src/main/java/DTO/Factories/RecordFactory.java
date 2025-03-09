package DTO.Factories;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.engine.messageinterpolation.el.VariablesELContext;
import org.yaml.snakeyaml.util.Tuple;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Image.SolutionValueDTO;
import DTO.Records.Image.SolutionVariable;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Model.ModelData.ParameterDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.ModelDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Image.Modules.VariableModule;
import Model.ModelConstraint;
import Model.ModelInput;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import Model.ModelVariable;
import Model.Solution;

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
             for(Tuple<List<String>,Double> value:solution.getVariableSolution(variableName)){
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

        Set<SetDefinitionDTO> sets = module.getInputSets().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
        
        Set<ParameterDefinitionDTO> param = module.getInputParams().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
        
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

        Set<SetDefinitionDTO> sets = module.getInputSets().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
        Set<ParameterDefinitionDTO> param = module.getInputParams().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
        
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
        return new SetDefinitionDTO(set.getIdentifier(), Arrays.asList(set.getTags()), set.getType().typeList());
    }

    public static Collection<SetDefinitionDTO> makeDTO(Collection<ModelSet> sets){
        LinkedList<SetDefinitionDTO> setDTOs= new LinkedList<>();
        for(ModelSet set:sets){
            setDTOs.add(makeDTO(set));
        }
        return setDTOs;
    }

    public static ParameterDefinitionDTO makeDTO(ModelParameter parameter){
        return new ParameterDefinitionDTO(parameter.getIdentifier(),parameter.getTags()[0], parameter.getType().toString());
    }
    public static ParameterDTO makeDTO(ModelParameter parameter, String value){
        return new ParameterDTO(makeDTO(parameter), value);
    }

    private static VariableDTO makeDTO(ModelVariable variable) {
        HashSet<ModelSet> sets = new HashSet<>();
        variable.getPrimitiveSets(sets);
        HashSet<ModelParameter> parameters = new HashSet<>();
        variable.getPrimitiveParameters(parameters);
        return new VariableDTO(variable.getIdentifier(),Arrays.asList(variable.getTags()),variable.getType().typeList(),makeDTO(sets,parameters));
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
        VariableModuleDTO variables = makeDTO(image.getVariableModule());
                for(ConstraintModule module: image.getConstraintsModules().values()){
                    constraints.add(makeDTO(module));
                }
                for(PreferenceModule module: image.getPreferenceModules().values()){
                    preferences.add(makeDTO(module));
                }
                
                return new ImageDTO(image.getId(),image.getName(), image.getDescription(), variables, constraints, preferences);
        }
        private static VariableModuleDTO makeDTO(VariableModule module) {
            // Set<VariableDTO> intr = module.getVariables().values()
            //              .stream()
            //              .map(RecordFactory::makeDTO)
            //              .collect(Collectors.toSet());

            // Set<String> params = module.getInputParams();
            // Set<String> sets = module.getInputSets();

            Set<VariableDTO> vars = new HashSet<>();
            Set<SetDefinitionDTO> sets = new HashSet<>();
            Set<ParameterDefinitionDTO> params = new HashSet<>();
            for(ModelVariable var : module.getVariables().values()){
                vars.add(makeDTO(var));
            }
            sets = module.getInputSets().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
            params = module.getInputParams().stream().map(RecordFactory::makeDTO).collect(Collectors.toSet());
        
            return new VariableModuleDTO(vars, sets, params);
        }

            public static ImageResponseDTO makeDTO(String id, Image image){
        return new ImageResponseDTO(id,makeDTO(image));
    }

    public static CreateImageResponseDTO makeDTO(String id, ModelInterface md){
        return new CreateImageResponseDTO(id, makeDTO(md));
    }
    
    private static ModelDTO makeDTO(ModelInterface modelInterface) {
        Set<ConstraintDTO> constraints = new HashSet<>();
        Set<PreferenceDTO> preferences = new HashSet<>();
        //TODO: Variables.
        Set<VariableDTO> variables = new HashSet<>();
        Map<String,List<String>> setTypes = new HashMap<>();
        Map<String,String> paramTypes = new HashMap<>();
        Map<String,List<String>> varTypes = new HashMap<>();
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
            varTypes.putIfAbsent(variable.getIdentifier(),variable.getType().typeList());
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
    public static CreateImageFromFileDTO makeDTO(String name, String description , String code){
        return new CreateImageFromFileDTO(name,description,code);
    }

}
