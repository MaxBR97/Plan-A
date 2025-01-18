package Image;

import DTO.Records.ConstraintDTO;
import DTO.Records.PreferenceDTO;
import Image.Modules.*;
import Model.ModelInterface;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class Image {
    // Note: this implies names must be unique between user constraints/preferences.
    private final HashMap<String,ConstraintModule> constraintsModules;
    private final HashMap<String,PreferenceModule> preferenceModules;
    //private final Set<ModelVariable> Variables;
    private final ModelInterface model;

    public Image(ModelInterface model) {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        this.model = model;
        LinkedHashSet<String> constraints = new LinkedHashSet<>();
    }
    //will probably have to use an adapter layer, or change types to DTOs
    public void addConstraintModule(ConstraintModule module) {
        constraintsModules.put(module.getName(), module);
    }
    public void addPreferenceModule(PreferenceModule module) {
        preferenceModules.put(module.getName(), module);
    }
    public ConstraintModule getConstraintModule(String name) {
        return constraintsModules.get(name);
    }
    public PreferenceModule getPreferenceModules(String name) {
        return preferenceModules.get(name);
    }

    public HashMap<String, ConstraintModule> getConstraintsModules() {
        return constraintsModules;
    }

    public HashMap<String, PreferenceModule> getPreferenceModules() {
        return preferenceModules;
    }
    public void addConstraint(String moduleName, ConstraintDTO constraint) {
        constraintsModules.get(moduleName).addConstraint(model.getConstraint(constraint.identifier()));
    }
    public void removeConstraint(String moduleName, ConstraintDTO constraint) {
        constraintsModules.get(moduleName).removeConstraint(model.getConstraint(constraint.identifier()));
    }
    public void addPreference(String moduleName, PreferenceDTO preferenceDTO) {
        preferenceModules.get(moduleName).addPreference(model.getPreference(preferenceDTO.identifier()));
    }
    public void removePreference(String moduleName, PreferenceDTO preferenceDTO) {
        preferenceModules.get(moduleName).removePreference(model.getPreference(preferenceDTO.identifier()));
    }
    public void TogglePreference(String name){
        if(preferenceModules.get(name) == null)
            throw new IllegalArgumentException("No such preference module");
        else preferenceModules.get(name).ToggleModule();
    }
    public void ToggleConstraint(String name){
        if(constraintsModules.get(name) == null)
            throw new IllegalArgumentException("No such constraint module");
        else constraintsModules.get(name).ToggleModule();
    }
}
