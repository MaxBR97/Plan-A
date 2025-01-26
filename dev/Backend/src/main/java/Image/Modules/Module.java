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

    private final Set<String> inputSets;
    private final Set<String> inputParams;

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
     //   isActive=true;
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }
    public Module(String name, String description, Collection<String> inputSets, Collection<String> inputParams) {
        this.name = name;
        this.description = description;
        //   isActive=true;
       this.inputSets = new HashSet<>(inputSets);
       this.inputParams = new HashSet<>(inputParams);
    }
    private String name;
    private String description;
   // private boolean isActive;

    /**
     * Changed to be part of ModuleDTO, no longer image's responsibility to get
     */
    @Deprecated
    public abstract Set<ModelSet> getInvolvedSets();
    @Deprecated
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
   /* public void ToggleModule(){
        isActive=!isActive;
    }
    public boolean isActive(){
        return isActive;
    }*/
    public Set<String> getInputSets() {
        return inputSets;
    }

    public Set<String> getInputParams() {
        return inputParams;
    }
}
