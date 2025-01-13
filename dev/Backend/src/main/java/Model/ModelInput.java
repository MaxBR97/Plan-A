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


    // param w := 20;
    // param  x := 10;
    // set A := {1,2};
    // set B := {<1,2> , <3,4>};
    // set Range := {x .. w};
    // set C := B * {<1,"a">, <2,"b">, <3,"c">} * A * Range;
    // set D := {<i,j> in {<"a","b">, <"c","d">} : <i,2,j,x>};
    // set E := {<i,j> in B : <j,i> * D};
    // set Range2 := {1..5}; 

    // Note - ano
    // A.getStructure() -> [null]
    // Range.getStructure() -> [null]
    // C.getStructure() -> [(B,0), (B,1) , null, null, (A,0), (A,1), (Range,0)]
    // D.getStructure() -> [null, null, null, (x,0)]
    // E.getStructure() -> [(B,1), (B,0), (D,0), (D,1), (D,2), (D,3)]
    // Range2.getStructure() -> [null]

    class StructureBlock {
        ModelInput dependency;
        int position;
    }
    //TODO: implement
    // the length of the list output must be the length of the tuple held in field 'type', or length 1 if type is a primitive.
    public StructureBlock[] getStructure(){
        return null;
    }
}