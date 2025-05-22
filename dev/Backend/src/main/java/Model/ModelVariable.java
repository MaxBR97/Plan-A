package Model;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import DTO.Records.Model.ModelDefinition.VariableDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;

@Entity
@Table(name="variables")
public class ModelVariable extends ModelOutput {
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumns({
        @JoinColumn(name = "bound_set_image_id", referencedColumnName = "image_id"),
        @JoinColumn(name = "bound_set_name", referencedColumnName = "name")
    })
    protected ModelSet boundSet;

    @Column(name = "is_binary", nullable = false)
    protected boolean isBinary;
    
    public ModelVariable(String imageId, String identifier, boolean isBinary) {
        super(imageId, identifier);
        this.isComplex = false;
        this.isBinary = isBinary;
    }

    protected ModelVariable(){
        super();
    }

    public ModelVariable(String imageId, String ident, List<ModelSet> dep, List<ModelParameter> paramDep,List<ModelFunction> basicFuncs, ModelType type, boolean isComplex, boolean isBinary) {
        super(imageId, ident,dep,paramDep,basicFuncs,type);
        this.isComplex = isComplex;
        this.isBinary = isBinary;
    }

    @Transactional
    public void update(VariableDTO dto) throws Exception {
        
        if(dto.tags() != null && dto.tags().size() == dto.type().size())
            this.setTags(dto.tags().toArray(new String[0]));
        
        //TODO: this is not possible in the current design, therefore the 
        // VariablesModule sets it. Find a solution (perhaps make ModelComponent hold Model)
        
        // if(dto.boundSet() != null)
        //     setBoundSet(this.image.getModel().getSet(dto.boundSet()));
    }

    public void setBoundSet(ModelSet s){
        this.boundSet = s;
    }

    public ModelSet getBoundSet(){
        return this.boundSet;
    }

    public boolean isBinary(){
        return this.isBinary;
    }

}