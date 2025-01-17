package Model;

import java.util.List;


public class ModelConstraint extends ModelFunctionality {
    
    public ModelConstraint(String identifier) {
        super(identifier);
    }

    ModelConstraint(String constName, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(constName,basicSets,basicParams);
    }
}