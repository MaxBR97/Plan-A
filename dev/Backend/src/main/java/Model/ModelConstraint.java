package Model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="constraints")
public class ModelConstraint extends ModelFunctionality {
    
    public ModelConstraint(){
        super();
    }

    public ModelConstraint(String imageId, String identifier) {
        super(imageId, identifier);
    }

    ModelConstraint(String imageId, String constName, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(imageId, constName,basicSets,basicParams);
    }
}