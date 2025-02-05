package Model;

import java.util.*;

public abstract class ModelComponent {
    protected String identifier;
    protected List<ModelSet> setDependencies; // order matters
    protected List<ModelParameter> paramDependencies;
    
    public ModelComponent(String identifier) {
        this.identifier = identifier;
        this.setDependencies = new LinkedList<>();
        this.paramDependencies = new LinkedList<>();
    }

public ModelComponent(String identifier, List<ModelSet> setDep, List<ModelParameter> paramDep) {
        this.identifier = identifier;
        this.setDependencies = setDep;
        this.paramDependencies = paramDep;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ModelSet findSetDependency(String identifier){
        for( ModelSet s : setDependencies){
            if(s.identifier.equals(identifier))
                return s;
        }
        return null;
    }

    public ModelParameter findParamDependency(String identifier){
        for( ModelParameter s : paramDependencies){
            if(s.identifier.equals(identifier))
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

    // Setters
    public void setSetDependencies(List<ModelSet> dependencies) {
        this.setDependencies = new ArrayList<>(dependencies);
    }

    public void setParamDependencies(List<ModelParameter> dependencies) {
        this.paramDependencies = new ArrayList<>(dependencies);
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

}

