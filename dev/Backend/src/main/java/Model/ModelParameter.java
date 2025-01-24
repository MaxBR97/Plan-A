package Model;

import java.util.Collection;
import java.util.List;

//import parser.FormulationParser.ParamDeclContext;

public class ModelParameter extends ModelInput {
    private String value;

    public ModelParameter(String identifier, ModelType type) {
       super(identifier,type);
    }


    public ModelParameter(String paramName, ModelType type, List<ModelSet> basicSets,
            List<ModelParameter> basicParams) {
        super(paramName, type, basicSets, basicParams);
    }


    public String getValue() {
        return value;
    }
    
    // Set the value
    public void setValue(String value) {
        this.value = value;
    }
    
    // Check if value is present
    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }
    
    // Clear the value
    public void clearValue() {
        this.value = null;
    }

    public boolean isPrimitive(){
        if(this.setDependencies.size() == 0 && this.paramDependencies.size() == 0)
            return true;
        return false;
    }

    
}