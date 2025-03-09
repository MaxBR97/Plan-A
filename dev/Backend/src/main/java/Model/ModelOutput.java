package Model;

import java.util.LinkedList;
import java.util.List;

import Model.ModelInput.StructureBlock;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class ModelOutput extends ModelComponent {
    @Transient
    protected boolean isComplex;
    @Transient
    protected ModelType myType;
    @Transient
    protected String[] tags;

    public ModelOutput(String imageId, String identifier) {
        super(imageId, identifier);
    }

    public ModelOutput(String imageId, String ident, List<ModelSet> dep, List<ModelParameter> paramDep, ModelType type) {
        super(imageId, ident,dep,paramDep);
        isComplex = false;
        myType = type;
    }

    protected ModelOutput(){
        super();
    }

    public String[] getTags(){
        if(tags == null){
            tags = myType.typeList().toArray(new String[0]);
        }
        return tags;
    }


    /**
     * @return true if this ModelOutput is complex, false otherwise
     */
    public boolean isComplex() {
        return isComplex;
    }

    public StructureBlock[] getStructure(){
        List<StructureBlock> sb = new LinkedList<>();
        for(ModelSet s : this.setDependencies){
            sb.addAll(List.of(s.getStructure()));
        }

        return ((StructureBlock[])sb.toArray());
    }

    public ModelType getType() {
        return this.myType;
    }
}