package Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Image.Modules.VariableModule;
import Model.ModelConstraint;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import Model.ModelVariable;
import Model.Solution;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @Column(name = "image_id") 
    private String id;

    @Column(name = "image_name") 
    private String name;

    @Column(name = "image_description") 
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "image_id",insertable=false, updatable=false)
    @MapKey(name = "id.name")
    //@Transient
    private Map<String,ConstraintModule> constraintsModules;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER , orphanRemoval = true)
    @JoinColumn(name = "image_id",insertable=false, updatable=false)
    @MapKey(name = "id.name")
    //@Transient
    private Map<String,PreferenceModule> preferenceModules;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
    @JoinColumn(name = "image_id",insertable=false, updatable=false)
    @MapKey(name = "id.name")
    //@Transient
    private Map<String,VariableModule> variables;

    @Transient
    private int defaultTimeout = 60;

    @Transient
    private ModelInterface model;

    @Transient
    private static ModelFactory modelFactory;
    // public Image(ModelInterface model) {
    //     constraintsModules = new HashMap<>();
    //     preferenceModules = new HashMap<>();
    //     variables = new VariableModule(this, Map.of(), List.of(),List.of());
    //     this.model = model;
    // }

    protected Image () throws Exception {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new HashMap<>();
    }
    
    public Image(String id, String name, String description) throws Exception {
        this.id = id;
        this.name = name;
        this.description = description;
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        setVariableModule(new VariableModule(this, Map.of(), List.of(),List.of()));
        this.model = modelFactory.getModel(id);
    }

    public static void setModelFactory(ModelFactory factory){
        modelFactory = factory;
    }

    @PostLoad
    private void initializeTransientFields() throws Exception {
        if(variables == null || variables.isEmpty())
            setVariableModule(new VariableModule(this, Map.of(), List.of(),List.of()));
        this.model = modelFactory.getModel(id);
    }


    // //will probably have to use an adapter layer, or change types to DTOs
    // @Transactional
    // public void addConstraintModule(ConstraintModule module) {
    //     constraintsModules.put(module.getName(), module);
    // }

    @Transactional
    public String getName(){
        return this.name;
    }

    @Transactional
    public String getDescription(){
        return this.description;
    }
    
    @Transactional
    public void addConstraintModule(String moduleName, String description) {
        constraintsModules.put( moduleName, new ConstraintModule(this, moduleName, description));
    }
    
    @Transactional
    public void addConstraintModule(String moduleName, String description, Collection<String> constraints, Collection<String> inputSets, Collection<String> inputParams) {
        HashSet<ModelConstraint> modelConstraints = new HashSet<>();
        HashSet<ModelSet> sets = new HashSet<>();
        HashSet<ModelParameter> params = new HashSet<>();
        for (String name : constraints) {
            ModelConstraint constraint = model.getConstraint(name);
            Objects.requireNonNull(constraint,"Invalid constraint name in add constraint in image");
            modelConstraints.add(constraint);
        }
        for(String set : inputSets){
            sets.add(model.getSet(set));
        }
        for(String param : inputParams){
            params.add(model.getParameter(param));
        }
        constraintsModules.put(moduleName, new ConstraintModule(this, moduleName, description, modelConstraints, sets, params));
    }
    @Transactional
    public void setConstraintsModules(HashMap<String,ConstraintModule> constraintsModules){
        this.constraintsModules = constraintsModules;
    }
    @Transactional
    public void setPreferencesModules(HashMap<String,PreferenceModule> prefs){
        this.preferenceModules = prefs;
    }

    // @Transactional
    // public void addPreferenceModule(PreferenceModule module) {
    //     preferenceModules.put(module.getName(), module);
    // }

    @Transactional
    public void addPreferenceModule(String moduleName, String description) {
        preferenceModules.put(moduleName, new PreferenceModule(this, moduleName, description));
    }

    @Transactional
    public void addPreferenceModule(String moduleName, String description, Collection<String> preferences, Collection<String> inputSets, Collection<String> inputParams, Collection<String> costParameters) {
        HashSet<ModelPreference> modelPreferences = new HashSet<>();
        HashSet<ModelSet> sets = new HashSet<>();
        HashSet<ModelParameter> params = new HashSet<>();
        HashSet<ModelParameter> costs = new HashSet<>();
        for (String name : preferences) {
            ModelPreference preference = model.getPreference(name);
           Objects.requireNonNull(preference,"Invalid preference name in add preference module");
           modelPreferences.add(preference);
        }
        for(String set : inputSets){
            sets.add(model.getSet(set));
        }
        for(String param : inputParams){
            params.add(model.getParameter(param));
        }
        if(costParameters != null){
            for(String param : costParameters){
                costs.add(model.getParameter(param));
            }
        }
        preferenceModules.put(moduleName, new PreferenceModule(this, moduleName, description, modelPreferences,sets,params,costs));
    }
    @Transactional
    public ConstraintModule getConstraintsModule(String name) {
        return constraintsModules.get(name);
    }
    @Transactional
    public PreferenceModule getPreferencesModule(String name) {
        return preferenceModules.get(name);
    }
    @Transactional
    public Map<String, ConstraintModule> getConstraintsModules() {
        return constraintsModules;
    }
    @Transactional
    public Map<String, PreferenceModule> getPreferenceModules() {
        return preferenceModules;
    }
   

    @Transactional
    public void addConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).addConstraint(model.getConstraint(constraint.identifier()));
    }

    @Transactional
    public void removeConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).removeConstraint(model.getConstraint(constraint.identifier()));
    }

    @Transactional
    public void addPreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).addPreference(model.getPreference(preferenceDTO.identifier()));
    }

    @Transactional
    public void setVariablesModule(Set<ModelVariable> map1, Collection<String> sets, Collection<String> params ){
        HashSet<ModelSet> inputSets = new HashSet<>();
        HashSet<ModelParameter> inputParams = new HashSet<>();
        for(String set : sets){
            inputSets.add(model.getSet(set));
        }
        for(String param : params){
            inputParams.add(model.getParameter(param));
        }
        setVariableModule(new VariableModule(this, map1, inputSets, inputParams));
    }

    @Transactional
    public void removePreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).removePreference(model.getPreference(preferenceDTO.identifier()));
    }
    @Transactional
    public Map<String,ModelVariable> getVariables() {
        return getVariableModule().getVariables();
    }

    
    @Transactional
    public void setVariableModule(VariableModule module){
        if (this.variables == null)
            this.variables = new HashMap<>();
        this.variables.put(VariableModule.getVariableModuleName(), module);
    }
    @Transactional
    public VariableModule getVariableModule(){
        return this.variables.get(VariableModule.getVariableModuleName());
    }

    /*public void addVariable(ModelVariable variable) {
        variables.put(variable.getIdentifier(), variable);
    }
    public void removeVariable(ModelVariable variable) {
        variables.remove(variable.getIdentifier());
    }
    public void removeVariable(String name) {
        variables.remove(name);
    }
    /*
     */

    /*
    public void TogglePreference(String name){
            Objects.requireNonNull(name,"Null value during Toggle Preference in Image");
            model.toggleFunctionality();
    }
    public void ToggleConstraint(String name){
            Objects.requireNonNull(name,"Null value during Toggle Constraint in Image");
            constraintsModules.get(name).ToggleModule();
    }*/
    public SolutionDTO solve(int timeout) throws Exception{
        Solution solution=model.solve(timeout, "SOLUTION");
        try {
            solution.parseSolution(model, getVariableModule().getIdentifiers());
        } catch (Exception e) {
            throw new RuntimeException("IO exception while parsing solution file, message: "+ e);
        }
        return RecordFactory.makeDTO(solution);
        /*Objects.requireNonNull(input,"Input is null in solve method in image");
        for(String constraint:input.constraintsToggledOff()){
            toggleOffConstraint(constraint);
        }
        for(String preference:input.preferencesToggledOff()){
            toggleOffPreference(preference);
        }
        return RecordFactory.makeDTO(model.solve(defaultTimeout));*/
    }

    private void toggleOffConstraint(String name){
        Objects.requireNonNull(name,"Null value during Toggle Preference in Image");
        ModelConstraint constraint=model.getConstraint(name);
        Objects.requireNonNull(constraint,"Invalid constraint name in Toggle Constraint in Image");
        model.toggleFunctionality(constraint, false);
    }
    private void toggleOffPreference(String name){
        Objects.requireNonNull(name,"Null value during Toggle Preference in Image");
        ModelPreference preference=model.getPreference(name);
        Objects.requireNonNull(preference,"Invalid preference name in Toggle Preference in Image");
        model.toggleFunctionality(preference, false);
    }

    public ModelInterface getModel() {
        return this.model;
    }

    public String getId() {
       return this.id;
    }

    @Transactional
    public void reset(Map<String,ModelVariable> variables, Collection<String> sets, Collection<String> params) {
        HashSet<ModelSet> inputSets = new HashSet<>();
        HashSet<ModelParameter> inputParams = new HashSet<>();
        for(String set : sets){
            inputSets.add(model.getSet(set));
        }
        for(String param : params){
            inputParams.add(model.getParameter(param));
        }
        constraintsModules.clear();
        preferenceModules.clear();
        getVariableModule().override(variables,inputSets,inputParams);
    }

    public Set<String> getAllInvolvedSets() {
        Set<String> allSets = new HashSet<>();

        // Add inputSets from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allSets.addAll(constraintModule.getInvolvedSets().stream().map((ModelSet s) -> s.getIdentifier()).collect(Collectors.toSet()));
        }

        // Add inputSets from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allSets.addAll(preferenceModule.getInvolvedSets().stream().map((ModelSet s) -> s.getIdentifier()).collect(Collectors.toSet()));
        }
            
        allSets.addAll(getVariableModule().getInvolvedSets().stream().map((ModelSet s) -> s.getIdentifier()).collect(Collectors.toSet()));

        return allSets;
    }

    public Set<String> getAllInvolvedParams() {
        Set<String> allParams = new HashSet<>();

        // Add inputParams from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allParams.addAll(constraintModule.getInvolvedParameters().stream().map((ModelParameter s) -> s.getIdentifier()).collect(Collectors.toSet()));
        }

        // Add inputParams from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allParams.addAll(preferenceModule.getInvolvedParameters().stream().map((ModelParameter s) -> s.getIdentifier()).collect(Collectors.toSet()));
        }

        allParams.addAll(getVariableModule().getInvolvedParameters().stream().map((ModelParameter s) -> s.getIdentifier()).collect(Collectors.toSet()));

        return allParams;
    }

    

    public InputDTO getInput() throws Exception {
        Set<String> relevantParams = getAllInvolvedParams();
        Set<String> relevantSets = getAllInvolvedSets();
        Map<String, List<List<String>>> setsToValues = new HashMap<>();
        Map<String,List<String>> paramsToValues = new HashMap<>();

        for (String param : relevantParams.toArray(new String[0])) {
            String[] atoms = model.getInput(model.getParameter(param));
            paramsToValues.put(param, List.of(atoms));
        }

        for (String set : relevantSets.toArray(new String[0])) {
            List<String[]> atomsOfElements = model.getInput(model.getSet(set));
            List<List<String>> convertedList = new ArrayList<>();
            for (String[] array : atomsOfElements) {
                convertedList.add(Arrays.asList(array)); // Convert String[] to List<String>
            }
            setsToValues.put(set, convertedList);
        }

        return new InputDTO(setsToValues,paramsToValues,new LinkedList<>(), new LinkedList<>());
    }
}
