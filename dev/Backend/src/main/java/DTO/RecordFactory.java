package DTO;

import DTO.Records.*;
import Image.Image;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Model.ModelConstraint;
import Model.ModelPreference;
import Model.Solution;
import java.util.HashMap;
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
        return new ConstraintModuleDTO(module.isActive(), module.getName(), module.getDescription(), constraints);
    }
    public static PreferenceModuleDTO makeDTO(PreferenceModule module) {
        if(module == null)
            throw new NullPointerException("Null preference module in DTO mapping");
        HashMap<String, PreferenceDTO> preferences = new HashMap<>();
        for(ModelPreference preference:module.getPreferences().values()){
            preferences.put(preference.getIdentifier(), makeDTO(preference));
        }
        return new PreferenceModuleDTO(module.isActive(), module.getName(), module.getDescription(), preferences);
    }
    public static ImageDTO makeDTO(Image image){
        if(image == null)
            throw new NullPointerException("Null image in DTO mapping");
        HashMap<String, ConstraintModuleDTO> constraints = new HashMap<>();
        HashMap<String, PreferenceModuleDTO> preferences = new HashMap<>();
        for(ConstraintModule module: image.getConstraintsModules().values()){
            constraints.put(module.getName(), makeDTO(module));
        }
        for(PreferenceModule module: image.getPreferenceModules().values()){
            preferences.put(module.getName(), makeDTO(module));
        }
        return new ImageDTO(constraints, preferences);
    }

}
