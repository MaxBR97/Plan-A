package Image;

import Image.Modules.*;

import java.util.HashMap;

public class Image {
    // Note: this implies names must be unique between user constraints/preferences.
    private final HashMap<String,ConstraintModule> constraintsModules;
    private final HashMap<String,PreferenceModule> preferenceModules;

    public Image() {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
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
