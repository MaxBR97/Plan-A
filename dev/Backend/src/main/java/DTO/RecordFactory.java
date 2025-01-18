package DTO;

import DTO.Records.Commands.*;
import DTO.Records.Image.*;
import DTO.Records.Model.ModelDefinition.*;
import DTO.Records.Model.ModelData.*;
import Image.Image;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Model.*;
import org.apache.tomcat.util.http.parser.Cookie;

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
    public static ExceptionDTO makeDTO(Exception exception) {
        if(exception == null)
            throw new NullPointerException("Null exception in DTO mapping");
        return new ExceptionDTO(exception.getMessage());
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

    @Deprecated
    public static SetDefinitionDTO makeDTO(ModelSet set){
        LinkedList<String> dependencies = new LinkedList<>();
        LinkedList<DataTypes> types = new LinkedList<>();
        for(ModelSet dependency: set.getSetDependencies()){
            for(Model.ModelInput.StructureBlock block:dependency.getStructure()){
                dependencies.add(block.dependency.getIdentifier());
                //TODO: implement converting types
            }
        }
        return new SetDefinitionDTO(set.getIdentifier(), dependencies, types);
    }

    public static Collection<SetDefinitionDTO> makeDTO(Collection<ModelSet> sets){
        LinkedList<SetDefinitionDTO> setDTOs= new LinkedList<>();
        for(ModelSet set:sets){
            setDTOs.add(makeDTO(set));
        }
        return setDTOs;
    }

    @Deprecated
    public static ParameterDefinitionDTO makeDTO(ModelParameter parameter){
        //TODO: implement converting types
        return null;
    }
    private static VariableDTO makeDTO(ModelVariable variable) {
        HashSet<SetDefinitionDTO> dependencies = new HashSet<>();
        for(ModelSet dependency: variable.getSetDependencies()){
            dependencies.add(makeDTO(dependency));
        }
        return new VariableDTO(variable.getIdentifier(),variable.isComplex(),dependencies);
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
    public static DataTypes makeDTO(ModelPrimitives type) {
        return switch (type) {
            case BINARY -> DataTypes.BINARY;
            case TEXT -> DataTypes.TEXT;
            case UNKNOWN -> DataTypes.UNKNOWN;
            case INT -> DataTypes.INT;
            case INFINITY -> DataTypes.INFINITY;
            case FLOAT -> DataTypes.FLOAT;
        };
    }
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



}
