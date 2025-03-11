package Model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

//import parser.FormulationParser.ParamDeclContext;

@Entity
@Table(name="parameters")
public class ModelParameter extends ModelInput {

    @Column(name = "is_cost_param")
    private boolean isCostParameter = false;
    @Transient
    private String value;

    public ModelParameter(){
        super();
    }
    public ModelParameter(String imageId, String identifier, ModelType type) {
       super(imageId, identifier,type);

    }
    
    public ModelParameter(String imageId, String paramName, ModelType type, List<ModelSet> basicSets,
            List<ModelParameter> basicParams) {
        super(imageId, paramName, type, basicSets, basicParams);
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

    public void setCostParameter (boolean isCostParam) {
        this.isCostParameter = isCostParam;
    }

    public boolean isCostParameter () {
        return this.isCostParameter;
    }
    
}