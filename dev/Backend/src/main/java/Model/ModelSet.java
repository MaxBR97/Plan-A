package Model;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="sets")
public class ModelSet extends ModelInput {


    @Transient
    private List<String> elements;

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
        return Collections.unmodifiableList(elements);
    }

    // Get element at specific index
    public String getElement(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }

    // Set entire list of elements
    public void setElements(List<String> elements) {
        this.elements = elements;
    }

    // Add single element
    public void addElement(String element) {
        if (element != null) {
            this.elements.add(element);
        }
    }

    // Remove single element
    public void removeElement(String element) {
        this.elements.remove(element);
    }

    // Clear all elements
    public void clearElements() {
        this.elements.clear();
    }

    // Get size of elements
    public int size() {
        return elements.size();
    }

    // Check if elements is empty
    public boolean isEmpty() {
        return elements.isEmpty();
    }


}
