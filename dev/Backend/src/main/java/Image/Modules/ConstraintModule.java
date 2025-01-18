package Image.Modules;

import Model.ModelConstraint;
import Model.ModelSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstraintModule extends Module{
    /**
     * uses common data and logic across all module types (constraints and preferences) from Module
     * a constraint module, holding the user definition for a group of model constraints (a group of subto expressions in zimpl code)
     */

    private final Map<String,ModelConstraint> constraints;

    public ConstraintModule(String name, String description) {
        super(name, description);
        this.constraints = new HashMap<>();
    }
    public Map<String, ModelConstraint> getConstraints() {
        return constraints;
    }
    public ModelConstraint getConstraint(String constraintName) {
        return constraints.get(constraintName);
    }
    public void addConstraint(ModelConstraint constraint){
        constraints.put(constraint.getIdentifier(),constraint);
    }
    public void removeConstraint(ModelConstraint constraint){
        constraints.remove(constraint.getIdentifier());
    }
    public void removeConstraint(String identifier){
        constraints.remove(identifier);
    }
}
