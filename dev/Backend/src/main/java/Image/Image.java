package Image;

import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import Image.Modules.*;
import Model.Model;
import Model.ModelInterface;
import Model.ModelVariable;

import java.io.IOException;
import java.util.HashMap;

public class Image {
    // Note: this implies module names must be unique between user constraints/preferences.
    private final HashMap<String,ConstraintModule> constraintsModules;
    private final HashMap<String,PreferenceModule> preferenceModules;
    private final HashMap<String, ModelVariable> variables;
    private final ModelInterface model;

    public Image(ModelInterface model) {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new HashMap<>();
        this.model = model;
        fetchVariables();
    }
    public Image(String path) throws IOException {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new HashMap<>();
        this.model = new Model(path);
        fetchVariables();
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
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).addConstraint(model.getConstraint(constraint.identifier()));
    }
    public void removeConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).removeConstraint(model.getConstraint(constraint.identifier()));
    }
    public void addPreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).addPreference(model.getPreference(preferenceDTO.identifier()));
    }
    public void removePreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).removePreference(model.getPreference(preferenceDTO.identifier()));
    }
    public HashMap<String,ModelVariable> getVariables() {
        return variables;
    }
    public ModelVariable getVariable(String name) {
        return variables.get(name);
    }
    public void addVariable(ModelVariable variable) {
        variables.put(variable.getIdentifier(), variable);
    }
    public void removeVariable(ModelVariable variable) {
        variables.remove(variable.getIdentifier());
    }
    public void removeVariable(String name) {
        variables.remove(name);
    }
    public void fetchVariables(){
        for(ModelVariable variable: model.getVariables()){
            addVariable(variable);
        }
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
