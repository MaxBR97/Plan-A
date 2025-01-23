package Model;

import java.util.List;

public class ModelVariable extends ModelOutput {
    
    public ModelVariable(String identifier) {
        super(identifier);
    }

    public ModelVariable(String ident, List<ModelSet> dep,List<ModelParameter> paramDep){
        super(ident,dep,paramDep);
    }

}