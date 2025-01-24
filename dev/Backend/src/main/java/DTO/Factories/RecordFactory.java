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
            return new SolutionDTO(false,-1,-1,new HashMap<>());
        double solvingTime = solution.getSolvingTime();
        double objectiveValue = solution.getObjectiveValue();
        boolean solved = true;
        HashMap<String, SolutionVariable> variables = new HashMap<>();
         for(ModelVariable variable: solution.getVariables()){
             String variableName= variable.getIdentifier();;
             Set<SolutionValueDTO> variableValues = new HashSet<>();
             List<String> variableStructure=List.copyOf(solution.getVariableStructure(variableName));
             List<String> variableTypes=List.copyOf(solution.getVariableTypes(variableName));
             for(Tuple<List<String>,Integer> value:solution.getVariableSolution(variableName)){
                variableValues.add(new SolutionValueDTO(value._1(),value._2()));
            }
            variables.put(variableName,new SolutionVariable(variableStructure,variableTypes,variableValues));
         }
        return new SolutionDTO(solved,solvingTime,objectiveValue,variables);
    }

    public static PreferenceDTO makeDTO(ModelPreference preference) {
        if(preference == null)
            throw new NullPointerException("Null preference in DTO mapping");
        return new PreferenceDTO(preference.getIdentifier(),makeDTO(preference.getSetDependencies(), preference.getParamDependencies()));
    }
    public static ConstraintDTO makeDTO(ModelConstraint constraint) {
        if(constraint == null)
            throw new NullPointerException("Null constraint in DTO mapping");
        return new ConstraintDTO(constraint.getIdentifier(),makeDTO(constraint.getSetDependencies(), constraint.getParamDependencies()));
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
        return new VariableDTO(variable.getIdentifier(),makeDTO(variable.getSetDependencies(), variable.getParamDependencies()));
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
    
    private static ModelDTO makeDTO(ModelInterface md) {
        Set<ConstraintDTO> a = new HashSet<>();
        Set<PreferenceDTO> b = new HashSet<>();
        Set<VariableDTO> c = new HashSet<>();
        Map<String,String> d = new HashMap<>();
        for(ModelConstraint mc : md.getConstraints()){
            a.add(makeDTO(mc));
            for(ModelSet s : mc.getSetDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
            for(ModelParameter s : mc.getParamDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
        }
        for(ModelPreference mc : md.getPreferences()){
            b.add(makeDTO(mc));
            for(ModelSet s : mc.getSetDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
            for(ModelParameter s : mc.getParamDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
        }
        for(ModelVariable mc : md.getVariables()){
            c.add(makeDTO(mc));
            for(ModelSet s : mc.getSetDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
            for(ModelParameter s : mc.getParamDependencies())
                d.put(s.getIdentifier(),s.getType().toString());
        }
        return new ModelDTO(a, b, c, d);
    }
    public static DependenciesDTO makeDTO(List<ModelSet> s, List<ModelParameter> p){
        List<String> resS = new LinkedList<>();
        List<String> resP = new LinkedList<>();
        for(ModelSet x : s){
            resS.add(x.getIdentifier());
        }
        for(ModelParameter x : p){
            resP.add(x.getIdentifier());
        }
        return new DependenciesDTO(resS, resP);
    }
    public static CreateImageFromFileDTO makeDTO(String code){
        return new CreateImageFromFileDTO(code);
    }


}
