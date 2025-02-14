package Model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="constraints")
public class ModelPreference extends ModelFunctionality {

    public ModelPreference(){
        super();
    }

    public ModelPreference(String imageId, String identifier) {
        super(imageId, identifier);
    }

    ModelPreference(String imageId, String preferenceName, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(imageId, preferenceName,basicSets,basicParams);
    }
    
}