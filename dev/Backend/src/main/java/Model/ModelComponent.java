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

    void removeParamDependency(ModelParameter dependency) {
        paramDependencies.remove(dependency);
    }

}

