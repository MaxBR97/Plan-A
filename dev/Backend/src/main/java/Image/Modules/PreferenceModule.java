package Image.Modules;

import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;

import java.util.*;

public class PreferenceModule extends Module{
    /**
     * a preference module, holding the user definition for a group of model preference
     * (a preference is a single expressions in the expression sum in the minimize/maximize expression in zimpl)
     */
    private final Map<String, ModelPreference> preferences;




    public PreferenceModule(String name, String description) {
        super(name, description);
        preferences = new HashMap<>();

    }
    public PreferenceModule(String name, String description, Collection<ModelPreference> preferences, Collection<String> inputSets, Collection<String> inputParams) {
        super(name, description,inputSets,inputParams);
        this.preferences = new HashMap<>();
        for (ModelPreference constraint : preferences) {
            this.preferences.put(constraint.getIdentifier(), constraint);
        }

    }
    /**
     * Fetch all ModelSets that are in use in any of the preferences in the module.
     * @return all sets that are part of any preferences in the module
     */
    @Override
    public Set<ModelSet> getInvolvedSets(){
        HashSet<ModelSet> involvedSets = new HashSet<>();
        for(ModelPreference constraint : preferences.values()){
            involvedSets.addAll(constraint.getSetDependencies());
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelPreference preference : preferences.values()){
            involvedParameters.addAll(preference.getParamDependencies());
        }
        return involvedParameters;
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
