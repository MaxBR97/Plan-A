package Model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="variables")
public class ModelVariable extends ModelOutput {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumns({
        @JoinColumn(name = "bound_set_image_id", referencedColumnName = "image_id"),
        @JoinColumn(name = "bound_set_name", referencedColumnName = "name")
    })
    protected ModelSet boundSet;
    
    public ModelVariable(String imageId, String identifier) {
        super(imageId, identifier);
        this.isComplex = false;
    }

    protected ModelVariable(){
        super();
    }

    public ModelVariable(String imageId, String ident, List<ModelSet> dep, List<ModelParameter> paramDep,List<ModelFunction> basicFuncs, ModelType type, boolean isComplex) {
        super(imageId, ident,dep,paramDep,basicFuncs,type);
        this.isComplex = isComplex;
    }

    public void setBoundSet(ModelSet s){
        this.boundSet = s;
    }

    public ModelSet getBoundSet(){
        return this.boundSet;
    }

}