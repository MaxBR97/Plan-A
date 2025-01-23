package Image.Modules;

import Model.ModelFunctionality;
import Model.ModelParameter;
import Model.ModelSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Module {
    /**
     * Common data and logic across all module types (constraints and preferences)
     */
    public Module(String name, String description) {
        this.name = name;
        this.description = description;
        isActive=true;
    }
    private String name;
    private String description;
    private boolean isActive;
    public abstract Set<ModelSet> getInvolvedSets();
    public abstract Set<ModelParameter> getInvolvedParameters();
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Disables if its on and enables if it's off.
     */
    public void ToggleModule(){
        isActive=!isActive;
    }
    public boolean isActive(){
        return isActive;
    }
}
