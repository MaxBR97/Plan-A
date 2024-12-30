package Model;


public class ModelParameter extends ModelInput {
    private String value;

    public ModelParameter(String identifier, ModelType type) {
       super(identifier,type);
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
    
}