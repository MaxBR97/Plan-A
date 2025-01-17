package Model;

import java.util.List;
//TODO: functionality can also depend on variables, therefore variable dependency should also be created.
public abstract class ModelFunctionality extends ModelComponent {

    public ModelFunctionality(String identifier) {
        super(identifier);
    }

    public ModelFunctionality(String constName, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(constName,basicSets,basicParams);
    }
}