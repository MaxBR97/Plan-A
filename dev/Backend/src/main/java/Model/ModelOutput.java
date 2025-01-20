package Model;

import java.util.*;

public abstract class ModelOutput extends ModelComponent {
    protected List<ModelSet> dependency;
    protected boolean isComplex;

    public ModelOutput(String identifier) {
        super(identifier);
        this.dependency = new ArrayList<>();
    }

    public ModelOutput(String ident, List<ModelSet> dep) {
        super(ident);
        this.dependency = new ArrayList<>(dep);
    }

    /**
     * Gets a copy of the dependencies list
     * @return List of ModelSet dependencies
     */
    public List<ModelSet> getDependencies() {
        return new ArrayList<>(dependency);
    }

    /**
     * Sets the dependencies list to a new list
     * @param dependencies new list of dependencies to set
     */
    public void setDependencies(List<ModelSet> dependencies) {
        this.dependency = new ArrayList<>(dependencies);
    }

    /**
     * Appends a new dependency to the list
     * @param modelSet the ModelSet to append
     * @throws IllegalArgumentException if modelSet is null
     */
    public void appendDependency(ModelSet modelSet) {
        if (modelSet == null) {
            throw new IllegalArgumentException("Cannot append null dependency");
        }
        if (!dependency.contains(modelSet)) {
            dependency.add(modelSet);
        }
    }

    /**
     * Removes a dependency from the list
     * @param modelSet the ModelSet to remove
     * @return true if the dependency was removed, false otherwise
     */
    public boolean removeDependency(ModelSet modelSet) {
        return dependency.remove(modelSet);
    }

    /**
     * Finds a dependency by its identifier
     * @param identifier the identifier to search for
     * @return the ModelSet if found, null otherwise
     */
    public ModelSet findDependency(String identifier) {
        if (identifier == null) {
            return null;
        }
        return dependency.stream()
            .filter(dep -> identifier.equals(dep.getIdentifier()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a ModelSet exists in the dependencies
     * @param modelSet the ModelSet to check
     * @return true if the dependency exists, false otherwise
     */
    public boolean hasDependency(ModelSet modelSet) {
        return dependency.contains(modelSet);
    }

    /**
     * Checks if a dependency exists by identifier
     * @param identifier the identifier to check
     * @return true if a dependency with the given identifier exists, false otherwise
     */
    public boolean hasDependency(String identifier) {
        return findDependency(identifier) != null;
    }

    /**
     * Gets the number of dependencies
     * @return the size of the dependency list
     */
    public int getDependencyCount() {
        return dependency.size();
    }

    /**
     * Clears all dependencies
     */
    public void clearDependencies() {
        dependency.clear();
    }

    /**
     * Checks if this ModelOutput has any dependencies
     * @return true if there are no dependencies, false otherwise
     */
    public boolean isEmpty() {
        return dependency.isEmpty();
    }

    /**
     * @return true if this ModelOutput is complex, false otherwise
     */
    public boolean isComplex() {
        return isComplex;
    }

    /**
     * Sets the complex flag
     * @param complex the new complex value
     */
    public void setComplex(boolean complex) {
        this.isComplex = complex;
    }
}