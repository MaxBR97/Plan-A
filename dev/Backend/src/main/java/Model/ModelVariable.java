package Model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="variables")
public class ModelVariable extends ModelOutput {

    @OneToOne(mappedBy = "boundToVariable")
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

}