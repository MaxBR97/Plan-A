package Image.Modules;

import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import java.util.*;

import Image.Image;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

// @Entity
// @Table(name = "preference_module")
public class PreferenceModule extends Module{
    /**
     * a preference module, holding the user definition for a group of model preference
     * (a preference is a single expressions in the expression sum in the minimize/maximize expression in zimpl)
     */
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    // @ElementCollection
    // @Column(name = "input_set")
    // @MapKey(name = "name")
    private final Map<String, ModelPreference> preferences;


    public PreferenceModule(Image image ,String name, String description) {
        super(image, name, description);
        preferences = new HashMap<>();

    }
    public PreferenceModule(Image image ,String name, String description, Collection<ModelPreference> preferences, Collection<String> inputSets, Collection<String> inputParams) {
        super(image , name, description,inputSets,inputParams);
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
            constraint.getPrimitiveSets(involvedSets);
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelPreference preference : preferences.values()){
          preference.getPrimitiveParameters(involvedParameters);
        }
        return involvedParameters;
    }

    public ModelPreference getPreference(String name){
        return preferences.get(name);
    }
    public Map<String, ModelPreference> getPreferences() {
        return preferences;
    }
    @Transactional
    public void addPreference(ModelPreference preference){
        preferences.put(preference.getIdentifier(),preference);
    }
    @Transactional
    public void removePreference(ModelPreference preference){
        preferences.remove(preference.getIdentifier());
    }
    @Transactional
    public void removePreference(String identifier){
        preferences.remove(identifier);
    }
}
