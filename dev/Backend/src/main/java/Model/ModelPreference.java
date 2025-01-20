package Model;

import java.util.List;

public class ModelPreference extends ModelFunctionality {

    public ModelPreference(String identifier) {
        super(identifier);
    }

    ModelPreference(String preferenceName, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(preferenceName,basicSets,basicParams);
    }
    
}