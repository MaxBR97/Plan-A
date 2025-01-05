package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
public abstract class ModelInput extends ModelComponent {
    private ModelType myType;
    protected List<ModelSet> setDependencies; // order matters
    protected List<ModelParameter> paramDependencies;

    public ModelInput(String identifier, ModelType type) {
        super(identifier);
        myType = type;
        setDependencies = new LinkedList<>();
        paramDependencies = new LinkedList<>();
    }

    public ModelInput(String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b) {
        super(identifier);
        myType = type;
        setDependencies = a;
        paramDependencies = b;
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

    // Existing methods
    public ModelType getType() {
        return myType;
    }

    public boolean isCompatible(ModelType val) {
        return myType.isCompatible(val);
    }

    public boolean isCompatible(String str) {
        if(myType == ModelPrimitives.UNKNOWN)
            return true;
        return myType.isCompatible(str);
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
    public void addSetDependency(ModelSet dependency) {
        if (dependency != null && !setDependencies.contains(dependency)) {
            setDependencies.add(dependency);
        }
    }

    public void removeSetDependency(ModelSet dependency) {
        setDependencies.remove(dependency);
    }

    // Individual add/remove for parameters
    public void addParamDependency(ModelParameter dependency) {
        if (dependency != null && !paramDependencies.contains(dependency)) {
            paramDependencies.add(dependency);
        }
    }

    public void removeParamDependency(ModelParameter dependency) {
        paramDependencies.remove(dependency);
    }

    // Bulk operations
    public void clearSetDependencies() {
        setDependencies.clear();
    }

    public void clearParamDependencies() {
        paramDependencies.clear();
    }

    public void clearAllDependencies() {
        setDependencies.clear();
        paramDependencies.clear();
    }
}