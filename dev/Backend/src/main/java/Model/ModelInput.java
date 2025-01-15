package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
public abstract class ModelInput extends ModelComponent {
    private ModelType myType;
    protected List<ModelSet> setDependencies; // order matters
    protected List<ModelParameter> paramDependencies;
    protected StructureBlock[] myStruct;

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

    ModelInput(String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b, StructureBlock[] struct) {
        super(identifier);
        myType = type;
        setDependencies = a;
        paramDependencies = b;
        this.myStruct = struct;
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

    // Bulk operations
    void clearSetDependencies() {
        setDependencies.clear();
    }

    void clearParamDependencies() {
        paramDependencies.clear();
    }

    void clearAllDependencies() {
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

    // x.getStructure() -> [null]
    // A.getStructure() -> [(anonymous_set,0)] ; anonymous_set.getStructure() -> [null]
    // B.getStructure() -> [(anonymous_set, 0), (anonymous_set,1)] ; anonymous_set.getStructure() -> [null,null]
    // Range.getStructure() -> [(anonymous_set,0)] ; anonymous_set.getStructure() -> [null]
    // C.getStructure() -> [(B,0), (B,1) , (anonymous_set, 0), (anonymous_set,1), (A,0), (Range,0)] ; anonymous_set.getStructure() -> [null,null]
    // D.getStructure() -> [(anonymous_set, 0), (anonymous_set, 1),(anonymous_set, 2), (anonymous_set, 3)] ; anonymous_set.getStrucutre() -> [(anonymous_set2,0), null, (anonymous_set2,1), (x,0)]
    // Range2.getStructure() -> [(anonymous_set,0)] ; anonymous_set.getStructure() -> [null]

    public class StructureBlock {
        final public ModelInput dependency;
        final public int position;

        public StructureBlock(ModelInput dep, int pos){
            this.dependency = dep;
            this.position = pos;
        }
    }

    // the length of the list output must be the length of the tuple held in field 'type', or length 1 if type is a primitive.
    public StructureBlock[] getStructure(){
        if (myStruct != null){
            return myStruct;
        }

        StructureBlock[] ans;
        if(this.myType instanceof ModelPrimitives){
            ans = new StructureBlock[1];
        } else {
            ans = new StructureBlock[((Tuple)this.myType).size()];
        }

        int i = 0;
        for (ModelSet set : setDependencies){
            int j =0;
            for (StructureBlock sb : set.getStructure()){
                ans[i] = new StructureBlock(set, j);
                i++;
                j++;
            }
        }

        myStruct = ans;
        return ans;
    }
}