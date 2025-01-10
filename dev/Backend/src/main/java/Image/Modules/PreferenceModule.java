package Image.Modules;

import Model.ModelConstraint;
import Model.ModelPreference;

import java.util.HashSet;
import java.util.Set;

public class PreferenceModule extends Module{
    /**
     * Common data and logic across all module types (constraints and preferences)
     * a preference module, holding the user definition for a group of model preference
     * (a preference is a single expressions in the expression sum in the minimize/maximize expression in zimpl)
     */
    private Set<ModelPreference> preferences;
    public PreferenceModule(String name, String description) {
        super(name, description);
        preferences = new HashSet<>();
    }
}
