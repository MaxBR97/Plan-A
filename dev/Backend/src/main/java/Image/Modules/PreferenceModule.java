package Image.Modules;

import Model.ModelPreference;

import java.util.HashMap;
import java.util.Map;

public class PreferenceModule extends Module{
    /**
     * Common data and logic across all module types (constraints and preferences)
     * a preference module, holding the user definition for a group of model preference
     * (a preference is a single expressions in the expression sum in the minimize/maximize expression in zimpl)
     */
    private final Map<String, ModelPreference> preferences;
    public PreferenceModule(String name, String description) {
        super(name, description);
        preferences = new HashMap<>();
    }
    public ModelPreference getPreference(String name){
        return preferences.get(name);
    }
    public Map<String, ModelPreference> getPreferences() {
        return preferences;
    }
    public void addPreference(ModelPreference preference){
        preferences.put(preference.getIdentifier(),preference);
    }
    public void removePreference(ModelPreference preference){
        preferences.remove(preference.getIdentifier());
    }
    public void removePreference(String identifier){
        preferences.remove(identifier);
    }
}
