package Image.Modules;

import Model.ModelConstraint;
import Model.ModelFunctionality;
import Model.ModelSet;

import java.util.*;

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

    public Set<ModelSet> getInvolvedSets(){
        HashSet<ModelSet> involvedSets = new HashSet<>();
        for(ModelConstraint constraint : constraints.values()){
            involvedSets.addAll(constraint.getSetDependencies());
        }
        return involvedSets;
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
