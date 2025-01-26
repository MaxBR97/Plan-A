package Model;

import java.util.List;

public class ModelVariable extends ModelOutput {
    
    // public ModelVariable(String identifier) {
    //     super(identifier);
    //     this.isComplex = false;
    // }

    public ModelVariable(String ident, List<ModelSet> dep, List<ModelParameter> paramDep, boolean isComplex) {
        super(ident,dep,paramDep);
        this.isComplex = isComplex;
    }

}