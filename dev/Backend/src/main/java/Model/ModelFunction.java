package Model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table(name="functions")
public class ModelFunction extends ModelInput {

    @Column(name = "is_cost_func")
    private boolean isCostFunction = false;

    @Transient
    private String value;

    public ModelFunction(){
        super();
    }

    public ModelFunction(String imageId, String identifier, ModelType type) {
       super(imageId, identifier,type);

    }
    
    public ModelFunction(String imageId, String paramName, ModelType type, List<ModelSet> basicSets,
            List<ModelParameter> basicParams, List<ModelFunction> basicFuncs) {
        super(imageId, paramName, type, basicSets, basicParams, basicFuncs);
    }
    @Override
    public boolean isPrimitive(){
        return this.setDependencies.isEmpty() && this.paramDependencies.isEmpty();
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

    public void setCostParameter (boolean isCostFunction) {
        this.isCostFunction = isCostFunction;
    }

    public boolean isCostFunction () {
        return this.isCostFunction;
    }
    
}