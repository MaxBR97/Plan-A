package Model;

import java.util.LinkedList;
import java.util.List;

import DataAccess.StringArrayConverter;
import Exceptions.InternalErrors.BadRequestException;
import Model.ModelInput.StructureBlock;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class ModelOutput extends ModelComponent {
    @Transient
    protected boolean isComplex;
    
    @Column(name = "my_type")
    @Convert(converter = ModelTypeConverter.class)
    protected ModelType myType;

    @Column(name = "alias")
    protected String alias;
    
    @Column(name = "tags")
    @Convert(converter = StringArrayConverter.class)
    protected String[] tags;

    public ModelOutput(String imageId, String identifier) {
        super(imageId, identifier);
    }

    public ModelOutput(String imageId, String ident, List<ModelSet> dep, List<ModelParameter> paramDep, List<ModelFunction> funcDep, ModelType type) {
        super(imageId, ident,dep,paramDep,funcDep);
        isComplex = false;
        myType = type;
        alias = this.getIdentifier();
    }

    protected ModelOutput(){
        super();
        alias = this.getIdentifier();
    }

    public String[] getTags(){
        if(tags == null){
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