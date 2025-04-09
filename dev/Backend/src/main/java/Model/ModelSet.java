package Model;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="sets")
public class ModelSet extends ModelInput {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumns({
    @JoinColumn(name = "bound_variable_image_id", referencedColumnName = "image_id"),
    @JoinColumn(name = "bound_variable_name", referencedColumnName = "name")
})
protected ModelVariable boundToVariable;



    protected ModelSet(){
        super();
    }

    public ModelSet(String imageId, String identifier, ModelType type) {
        super(imageId, identifier,type);
    }

    public ModelSet(String imageId, String setName, ModelType type, List<ModelSet> basicSets, List<ModelParameter> basicParams, List<ModelFunction> basicFuncs) {
        super(imageId, setName, type, basicSets, basicParams, basicFuncs);
    }


    public ModelSet(String imageId, String setName, List<ModelSet> basicSets, List<ModelParameter> basicParams,List<ModelFunction> basicFuncs, StructureBlock[] resultingStructure) {
        super(imageId, setName,null, basicSets, basicParams, basicFuncs);
            //infer type
            Tuple type = new Tuple();
            int i = 0;
            for(StructureBlock sb : resultingStructure){
                ModelType mt = sb.dependency.getType();
                ModelPrimitives prim = null;
                if (mt instanceof ModelPrimitives)
                    prim = ((ModelPrimitives)mt);
                else if (mt instanceof Tuple)
                    prim = ((Tuple)mt).getTypes().get(sb == null && setName.equals("anonymous_set") ? i : sb.position-1);
                type.append(prim);
                i++;
            }
            super.myType = type;
            super.myStruct = resultingStructure;
    }

    public List<String> getElements() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getDefaultElements() {
        return Collections.unmodifiableList(this.def_values);
    }

    // Get element at specific index
    public String getElement(int index) {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    // Set entire list of elements
    public void setElements(List<String> elements) {
        this.values = elements;
    }

    // Set entire list of elements
    public void setDefaultElements(List<String> elements) {
        this.def_values = elements;
    }

    // Add single element
    public void addElement(String element) {
        if (element != null) {
            this.values.add(element);
        }
    }

    // Remove single element
    public void removeElement(String element) {
        this.values.remove(element);
    }

    // Clear all values
    public void clearElements() {
        this.values.clear();
    }

    // Get size of values
    public int size() {
        return values.size();
    }

    // Check if values is empty
    public boolean isEmpty() {
        return values.isEmpty();
    }


}
