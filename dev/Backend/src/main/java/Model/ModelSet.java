package Model;

import java.util.*;
import java.util.stream.Collectors;

public class ModelSet extends ModelInput {
    private List<String> elements;

    public ModelSet(String identifier, ModelType type) {
        super(identifier,type);
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
        if (elements == null) {
            this.elements = new ArrayList<>();
        } else {
            this.elements = new ArrayList<>(elements);
        }
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
