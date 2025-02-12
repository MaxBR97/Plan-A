package Model;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class ModelInput extends ModelComponent {
    @Transient
    protected ModelType myType;
    @Transient
    protected StructureBlock[] myStruct;

    public ModelInput(String imageId, String identifier, ModelType type) {
        super(imageId, identifier);
        myType = type;
        setDependencies = new LinkedList<>();
        paramDependencies = new LinkedList<>();
    }

    public ModelInput(String imageId, String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b) {
        super(imageId, identifier);
        myType = type;
    setDependencies = a;
        paramDependencies = b;
    }

    public ModelInput(){
        super();
    }

    ModelInput(String imageId, String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b, StructureBlock[] struct) {
        super(imageId, identifier);
        myType = type;
        setDependencies = a;
        paramDependencies = b;
        this.myStruct = struct;
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

    public boolean isPrimitive(){
        boolean identified_set = !this.identifier.equals("anonymous_set");
        boolean exactlyOneSetDescendant = this.setDependencies.size() == 1;
        boolean exactlyZeroParamDescendant = this.paramDependencies.size() == 0;
        if(identified_set && exactlyOneSetDescendant && exactlyZeroParamDescendant){
            if(this.setDependencies.get(0).setDependencies.size() == 0 && this.setDependencies.get(0).paramDependencies.size() == 0){
                return true;
            }
        }
        return false;
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
    // A.getStructure() -> [(anonymous_set,1)] ; anonymous_set.getStructure() -> [null]
    // B.getStructure() -> [(anonymous_set, 1), (anonymous_set,2)] ; anonymous_set.getStructure() -> [null,null]
    // Range.getStructure() -> [(anonymous_set,1)] ; anonymous_set.getStructure() -> [null]
    // C.getStructure() -> [(B,1), (B,2) , (anonymous_set, 1), (anonymous_set,2), (A,1), (Range,2)] ; anonymous_set.getStructure() -> [null,null]
    // D.getStructure() -> [(anonymous_set, 1), (anonymous_set, 2),(anonymous_set, 3), (anonymous_set, 4)] ; anonymous_set.getStrucutre() -> [(anonymous_set2,1), null, (anonymous_set2,2), (x,1)]
    // Range2.getStructure() -> [(anonymous_set,1)] ; anonymous_set.getStructure() -> [null]

    public static class StructureBlock {
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
        if(this.myType == ModelPrimitives.UNKNOWN){
            //infer
            int count = 0;
            for (ModelSet set : setDependencies){
                count += set.getStructure().length;
            }
            ans = new StructureBlock[count];
        }
        else if(this.myType instanceof ModelPrimitives){
            ans = new StructureBlock[1];
        } else {
            ans = new StructureBlock[((Tuple)this.myType).size()];
        }

        int i = 0;
        for (ModelSet set : setDependencies){
            int j =1;
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