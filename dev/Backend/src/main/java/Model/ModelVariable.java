package Model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="variables")
public class ModelVariable extends ModelOutput {
    
    public ModelVariable(String imageId, String identifier) {
        super(imageId, identifier);
        this.isComplex = false;
    }

    protected ModelVariable(){
        super();
    }

    public ModelVariable(String imageId, String ident, List<ModelSet> dep, List<ModelParameter> paramDep, ModelType type, boolean isComplex) {
        super(imageId, ident,dep,paramDep,type);
        this.isComplex = isComplex;
    }

}