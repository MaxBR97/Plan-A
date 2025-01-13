package Image;

import Image.Modules.*;
import Model.ModelInterface;
import Model.ModelVariable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

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
