package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import Image.Image;
import Model.ModelConstraint;
import Model.ModelInput;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

@Entity
// @Table(name = "preference_module")
@DiscriminatorValue("PREFERENCE")
public class PreferenceModule extends Module{
    /**
     * a preference module, holding the user definition for a group of model preference
     * (a preference is a single expressions in the expression sum in the minimize/maximize expression in zimpl)
     */
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
    })
    @MapKey(name = "id.identifier")
    private Map<String, ModelPreference> preferences;

    @Transient
    private Set<ModelParameter> costParameter;


    protected PreferenceModule() {
        super();
        preferences = new HashMap<String,ModelPreference>();
    }

    public PreferenceModule(Image image ,String name, String description) {
        super(image, name, description);
        preferences = new HashMap<>();
    }

    public PreferenceModule(Image image ,String name, String description, Collection<ModelPreference> preferences, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams, Collection<ModelParameter> coefficients) {
        super(image , name, description,inputSets,inputParams);
        for(ModelParameter costParam : coefficients){
            this.addParam(costParam);
        }
        this.preferences = new HashMap<>();
        for (ModelPreference constraint : preferences) {
            this.preferences.put(constraint.getIdentifier(), constraint);
            constraint.setModuleName(this.getName());
        }
        for(ModelParameter param : coefficients){
            param.setCostParameter(true);
        }
    }

    private void gatherCostParameters(){
        this.costParameter = new HashSet<>();
        for(ModelParameter param : this.inputParams) {
            if(param.isCostParameter())
                this.costParameter.add(param);
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
            constraint = ((ModelPreference)loadFullComponent(constraint));
            constraint.getPrimitiveSets(involvedSets);
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelPreference preference : preferences.values()){
          preference = ((ModelPreference)loadFullComponent(preference));
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

    @Transactional
    public Set<ModelParameter> getCostParameters(){
        if(costParameter == null)
            gatherCostParameters();
        return this.costParameter;
    }
}
