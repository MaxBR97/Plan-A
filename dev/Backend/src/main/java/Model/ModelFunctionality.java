package Model;

import java.util.List;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModelFunctionality extends ModelComponent {

    public ModelFunctionality(String imageId, String identifier) {
        super(imageId, identifier);
    }

    public ModelFunctionality(String imageId, String name, List<ModelSet> basicSets, List<ModelParameter> basicParams) {
        super(imageId, name ,basicSets,basicParams);
    }

    protected ModelFunctionality(){
        super();
    }
}