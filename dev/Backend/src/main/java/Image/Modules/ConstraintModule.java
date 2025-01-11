package Image.Modules;

import Model.ModelConstraint;
import Model.ModelSet;

import java.util.HashSet;
import java.util.Set;

public class ConstraintModule extends Module{
    /**
     * uses common data and logic across all module types (constraints and preferences)
     * a constraint module, holding the user definition for a group of model constraints (a group of subto expressions in zimpl code)
     */

    private Set<ModelConstraint> constraints;

    public ConstraintModule(String name, String description) {
        super(name, description);
        this.constraints = new HashSet<>();
    }
}
