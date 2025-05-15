package Model;

import java.util.LinkedList;
import java.util.List;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;

//import parser.FormulationParser.ParamDeclContext;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name="parameters")
public class ModelParameter extends ModelInput {

    @Column(name = "is_cost_param")
    private boolean isCostParameter = false;
    

    public ModelParameter(){
        super();
    }
    public ModelParameter(String imageId, String identifier, ModelType type) {
       super(imageId, identifier,type);

    }
    
    public ModelParameter(String imageId, String paramName, ModelType type, List<ModelSet> basicSets,
            List<ModelParameter> basicParams, List<ModelFunction> funcDep) {
        super(imageId, paramName, type, basicSets, basicParams, funcDep);
    }
    @Transactional
    public void update(ParameterDefinitionDTO dto) throws Exception{
        if(dto.alias() != null && !dto.alias().equals(""))
            this.setAlias(dto.alias());
        
        if(dto.tag() != null)
            this.setTags(new String[]{dto.tag()});
    }
    
    @Override
    public boolean isPrimitive(){
        return this.setDependencies.isEmpty() && this.paramDependencies.isEmpty() && this.functionDependencies.isEmpty();
    }


    public String getValue() {
        return values.get(0);
    }
    
    // Set the value
    public void setValue(String value) {
        this.values = List.of(value);
    }

    public String getDefaultValue() {
        return this.def_values.get(0);
    }
    
    // Set the value
    public void setDefaultValue(String value) {
        this.def_values = List.of(value);
    }
    
    // Check if value is present
    public boolean hasValue() {
        return this.values != null && !this.values.isEmpty();
    }
    
    // Clear the value
    public void clearValue() {
        this.values = null;
    }

    public void setCostParameter (boolean isCostParam) {
        this.isCostParameter = isCostParam;
    }

    public boolean isCostParameter () {
        return this.isCostParameter;
    }
    
}