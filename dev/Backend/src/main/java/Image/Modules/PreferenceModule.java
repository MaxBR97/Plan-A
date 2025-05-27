package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.Objects;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
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
import jakarta.persistence.PostLoad;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "preference_module_cost_params",
        joinColumns = {
            @JoinColumn(name = "image_id", referencedColumnName = "image_id"),
            @JoinColumn(name = "module_name", referencedColumnName = "name")
        }
    )
    @Column(name = "param_identifier")
    private Set<String> costParameter;

    public PreferenceModule(Image image, PreferenceModuleDTO dto) throws Exception {
        super(image, dto.moduleName(), dto.description(), 
            dto.inputSets(),
            Stream.concat(dto.inputParams().stream(), dto.costParams().stream()).collect(Collectors.toSet())
        );
        
        // Initialize cost parameters
        costParameter = new HashSet<>();
        for (ParameterDefinitionDTO paramDTO : dto.costParams()) {
            ModelParameter param = image.getModel().getParameter(paramDTO.name());
            costParameter.add(param.getIdentifier());
            param.update(paramDTO);
            param.setCostParameter(true);
        }
        
        this.preferences = new HashMap<>();
        for (String prefDTO : dto.preferences()) {
            ModelPreference preference = image.getModel().getPreference(prefDTO);
            this.preferences.put(prefDTO, preference);
            preference.setModuleName(this.getName());
        }
    }

    protected PreferenceModule() {
        super();
        preferences = new HashMap<String,ModelPreference>();
    }

    public PreferenceModule(Image image ,String name, String description) {
        super(image, name, description);
        preferences = new HashMap<>();
    }

    public PreferenceModule(Image image ,String name, String description, Collection<ModelPreference> preferences, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams, Collection<ModelParameter> coefficients) {
        super(image , name, description,inputSets,Stream.concat(inputParams.stream(), coefficients.stream()).collect(Collectors.toSet()));

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
    public Set<ModelParameter> getCostParameters() {
        return this.costParameter.stream()
            .map(id -> this.inputParams.stream()
                .filter(param -> param.getIdentifier().equals(id))
                .findFirst()
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Transactional
    public void update(PreferenceModuleDTO dto) throws Exception {
        // Update preferences
        for (String prefId : dto.preferences()) {
            ModelPreference modelPreference = image.getModel().getPreference(prefId);
            if (modelPreference == null) {
                throw new IllegalArgumentException("Invalid preference name: " + prefId);
            }
            modelPreference.setModuleName(this.getName());
            preferences.put(prefId, modelPreference);
        }

        // Update input sets
        inputSets.clear();
        for (SetDefinitionDTO setDTO : dto.inputSets()) {
            addSet(image.getModel().getSet(setDTO.name()));
        }

        // Update input parameters and cost parameters
        inputParams.clear();
        costParameter.clear();
        
        // Add all parameters (both input and cost) to inputParams
        for (ParameterDefinitionDTO paramDTO : dto.inputParams()) {
            addParam(image.getModel().getParameter(paramDTO.name()));
        }
        for (ParameterDefinitionDTO paramDTO : dto.costParams()) {
            ModelParameter param = image.getModel().getParameter(paramDTO.name());
            addParam(param);
            param.update(paramDTO);
            param.setCostParameter(true);
            costParameter.add(param.getIdentifier());
        }
    }

    //Gets the input sets including the Bonud set
    public Set<ModelParameter> getInputParameters() {
        Set<ModelParameter> params = new HashSet<>();
        params.addAll(this.getCostParameters());
        params.addAll(super.getInputParams());
        return params;
    }

    @Transactional
    @Override
    public boolean isInput(String id) {
        return super.isInput(id) || costParameter.contains(id);
    }
}
