package Model;

import java.util.LinkedList;
import java.util.List;

import DataAccess.StringArrayConverter;
import Exceptions.InternalErrors.BadRequestException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class ModelInput extends ModelComponent {

    @Column(name = "my_type")
    @Convert(converter = ModelTypeConverter.class)
    protected ModelType myType;

    @Transient
    protected StructureBlock[] myStruct;

    @Column(name = "alias")
    protected String alias;

    @Column(name = "tags")
    @Convert(converter = StringArrayConverter.class)
    protected String[] tags;

    @Transient
    protected List<String> values;
    

// @ElementCollection
// @CollectionTable(
//     name = "model_parameter_default_values",
//     joinColumns = {
//         @JoinColumn(name = "image_id", referencedColumnName = "image_id"),
//         @JoinColumn(name = "name", referencedColumnName = "name")
//     }
// )
// @Column(name = "default_value")
@Transient
protected List<String> def_values;

    public ModelInput(String imageId, String identifier, ModelType type) {
        super(imageId, identifier, new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        myType = type;
        alias = this.getIdentifier();
        def_values = null;
    }

    public ModelInput(String imageId, String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b, List<ModelFunction> c) {
        super(imageId, identifier,a,b,c);
        myType = type;
        alias = this.getIdentifier();
        def_values = null;
    }

    public ModelInput(){
        super();
        alias = this.getIdentifier();
        def_values = null;
    }

    ModelInput(String imageId, String identifier, ModelType type, List<ModelSet> a, List<ModelParameter> b, List<ModelFunction> c, StructureBlock[] struct) {
        super(imageId, identifier,a,b,c);
        myType = type;
        this.myStruct = struct;
    }

    public void dynamicLoadTransient(ModelInput mc){
        super.dynamicLoadTransient(mc);
        if(this.myStruct == null)
            this.myStruct = mc.myStruct;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias(){
        return this.alias;
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
        boolean identified_set = !this.getIdentifier().equals("anonymous_set");
        boolean exactlyOneSetDescendant = this.setDependencies.size() == 1;
        boolean exactlyZeroParamDescendant = this.paramDependencies.size() == 0;
        boolean exactlyZeroFuncDescendant = this.functionDependencies.size() == 0;
        if(identified_set && exactlyOneSetDescendant && exactlyZeroParamDescendant){
            if(this.setDependencies.get(0).setDependencies.size() == 0 && this.setDependencies.get(0).paramDependencies.size() == 0 && this.setDependencies.get(0).functionDependencies.size() == 0){
                return true;
            }
        }
        return false;
    }

    public String[] getTags(){
        if(tags == null || tags.length == 0){
            tags = myType.typeList().toArray(new String[0]);
        }
        return tags;
    }

    public void setTags(String[] tags) throws Exception{
        if(tags == null)
            throw new BadRequestException("Trying to set tags as null");
        if(tags.length != myType.typeList().size())
            throw new BadRequestException("Trying to set tags of wrong length.");
        this.tags = tags;
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
        } else if (this.getIdentifier().equals("anonymous_set") ){
            StructureBlock[] sbs;
            sbs = new StructureBlock[this.myType.typeList().size()];
            myStruct = sbs;
            return sbs;
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