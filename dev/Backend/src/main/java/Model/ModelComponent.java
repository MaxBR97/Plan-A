package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class ModelComponent {
    
    @EmbeddedId
    protected ModelComponentId id;

    @Column(name="module_name", insertable=false, updatable=false)
    protected String module_name;

    @Transient
    protected List<ModelSet> setDependencies; // order matters
    @Transient
    protected List<ModelParameter> paramDependencies;
    @Transient
    protected List<ModelFunction> functionDependencies;
    
    //required for JPA
    protected ModelComponent(){
        this.id = new ModelComponentId();
        this.id.setImageId("nullImage");
        this.id.setIdentifier("nullIdentifier");
        this.module_name = "default";
    }

    public ModelComponent(String imageId, String identifier) {
        this.id = new ModelComponentId();
        this.id.setIdentifier(identifier);
        this.setDependencies = new LinkedList<>();
        this.paramDependencies = new LinkedList<>();
        this.functionDependencies = new LinkedList<>();
        this.id.setImageId(imageId);
        this.module_name = "default";
    }

public ModelComponent(String imageId, String identifier, List<ModelSet> setDep, List<ModelParameter> paramDep, List<ModelFunction> funcDep) {
        this.id = new ModelComponentId();    
        this.id.setIdentifier(identifier);
        this.setDependencies = setDep;
        this.paramDependencies = paramDep;
        this.functionDependencies = funcDep;
        this.id.setImageId(imageId);
        this.module_name = "default";
    }

    public void dynamicLoadTransient(ModelComponent mc){
        if(this.setDependencies == null)
            this.setDependencies = mc.setDependencies;
        if(this.paramDependencies == null)
            this.paramDependencies = mc.paramDependencies;
        if(this.functionDependencies == null)
            this.functionDependencies = mc.functionDependencies;
    }

    public String getIdentifier() {
        return this.id.getIdentifier();
    }

    public ModelComponentId getModelComponentId() {
        return this.id;
    }

    public ModelSet findSetDependency(String identifier){
        for( ModelSet s : setDependencies){
            if(s.id.getIdentifier().equals(identifier))
                return s;
        }
        return null;
    }

    public ModelParameter findParamDependency(String identifier){
        for( ModelParameter s : paramDependencies){
            if(s.id.getIdentifier().equals(identifier))
                return s;
        }
        return null;
    }

    public ModelFunction findFuncDependency(String identifier){
        for( ModelFunction s : functionDependencies){
            if(s.id.getIdentifier().equals(identifier))
                return s;
        }
        return null;
    }

    public List<ModelSet> getSetDependencies() {
        return Collections.unmodifiableList(setDependencies);
    }

    public List<ModelParameter> getParamDependencies() {
        return Collections.unmodifiableList(paramDependencies);
    }

    public List<ModelFunction> getFuncDependencies() {
        return Collections.unmodifiableList(functionDependencies);
    }

    // Setters
    public void setSetDependencies(List<ModelSet> dependencies) {
        this.setDependencies = new ArrayList<>(dependencies);
    }

    public void setParamDependencies(List<ModelParameter> dependencies) {
        this.paramDependencies = new ArrayList<>(dependencies);
    }

    public void setFuncDependencies(List<ModelFunction> dependencies) {
        this.functionDependencies = new ArrayList<>(dependencies);
    }

    // Individual add/remove for sets
     void addSetDependency(ModelSet dependency) {
        if (dependency != null && !setDependencies.contains(dependency)) {
            setDependencies.add(dependency);
        }
    }

     void removeSetDependency(ModelSet dependency) {
        setDependencies.remove(dependency);
    }

    // Individual add/remove for parameters
    void addParamDependency(ModelParameter dependency) {
        if (dependency != null && !paramDependencies.contains(dependency)) {
            paramDependencies.add(dependency);
        }
    }
    public boolean isPrimitive(){
        return this.setDependencies.isEmpty() && this.paramDependencies.isEmpty();
    }

    /**
     * Iterates recursively over parameter and set dependencies and collects primitive parameters
     *
     * @param parameters list given to the function and passed down the recursion
     */
    public void getPrimitiveParameters(Set<ModelParameter> parameters) {
        if(this.paramDependencies == null)
            return;
        for(ModelSet set : setDependencies){
            if(!set.isPrimitive())
             set.getPrimitiveParameters(parameters);
        }
        for(ModelParameter parameter : paramDependencies){
            if(!parameters.contains(parameter)) {
                if (parameter.isPrimitive())
                    parameters.add(parameter);
                else parameter.getPrimitiveParameters(parameters);
            }
        }
    }

    /**
     * Iterates recursively over parameter and set dependencies and collects primitive sets
     *
     * @param sets list given to the function and passed down the recursion
     */
    public void getPrimitiveSets(Set<ModelSet> sets) {
        if(this.setDependencies == null)
            return;
        for(ModelSet set : setDependencies){
            if(!sets.contains(set)) {
                if (set.isPrimitive())
                    sets.add(set);
                else set.getPrimitiveSets(sets);
            }
        }
        for(ModelParameter parameter : paramDependencies){
            if(!parameter.isPrimitive())
                parameter.getPrimitiveSets(sets);
        }
    }
    void removeParamDependency(ModelParameter dependency) {
        paramDependencies.remove(dependency);
    }

    public void setModuleName(String name){
        this.module_name = name;
    }

}

