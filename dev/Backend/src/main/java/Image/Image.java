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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Image.Modules.VariableModule;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import Model.ModelType;
import Model.ModelVariable;
import Model.Solution;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import Exceptions.InternalErrors.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import Model.Tuple;
import Model.ModelPrimitives;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @Column(name = "image_id") 
    private String id;

    @Column(name = "owner")
    private String owner;

    @Column(name = "image_name") 
    private String name;

    @Column(name = "is_public")
    private boolean isPrivate;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "image_solver_scripts", joinColumns = @JoinColumn(name = "image_id"))
    @MapKeyColumn(name = "script_key")
    @Column(name = "script_value")
    private Map<String, String> solverScripts;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "image_saved_solutions", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "solution_name")
    private List<String> savedSolutions;

    @Transient
    private int defaultTimeout = 60;

    @Transient
    private ModelInterface model;

    @Transient
    private static ModelFactory modelFactory;

    protected Image () throws Exception {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new HashMap<>();
        solverScripts = new HashMap<>();
        savedSolutions = new LinkedList<>();
        owner = null;
        isPrivate = true;
    }
    
    public Image(String id, String name, String description, String ownerUser, Boolean isPrivate) throws Exception {
        validateName(name);
        // Description can be null or empty, no validation needed
        
        this.id = id;
        this.name = name;
        this.description = description;
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new HashMap<>();
        VariableModule module = new VariableModule(this, VariableModule.getVariableModuleName(), "");
        variables.put(VariableModule.getVariableModuleName(), module);
        this.model = modelFactory.getModel(id);
        solverScripts = new HashMap<>();
        savedSolutions = new LinkedList<>();
        this.owner = ownerUser;
        this.isPrivate = isPrivate == null ? true : isPrivate;
    }

    private void validateName(String name) throws BadRequestException {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Image name cannot be null or empty");
        }
    }

    public static void setModelFactory(ModelFactory factory){
        modelFactory = factory;
    }

    @PostLoad
    private void initializeTransientFields() throws Exception {
        if (variables == null || variables.isEmpty()) {
            variables = new HashMap<>();
            VariableModule module = new VariableModule(this, VariableModule.getVariableModuleName(), "");
            variables.put(VariableModule.getVariableModuleName(), module);
        }
        this.model = modelFactory.getModel(id, getAllInputSets(), getAllInputParameters());
    }

    private void setModelWithPersistedData() throws Exception {
        for(ModelSet set : getAllInputSets()){
            this.model.setModelComponent(set);
        }
        for(ModelParameter param : getAllInputParameters()){
            this.model.setModelComponent(param);
        }
        this.model.parseSource();
    }

    public void setSolverScripts(Map<String , String> solverScripts) {
        if(solverScripts != null && solverScripts.size() > 0)
            this.solverScripts = solverScripts;
    }

    public Map<String,String> getSolverScripts(){
        if (solverScripts == null || solverScripts.size() == 0)
            this.solverScripts = new HashMap<>(Map.of("default",""));            
        return this.solverScripts;
    }

    
    //TODO: make this correspond to patch semantics - fields that are null - are not updated.
    @Transactional
    public void update(ImageDTO imageDTO) throws Exception {
        // First validate all modules together before making any changes
        validateModulesCompatibility(
            imageDTO.variablesModule(),
            imageDTO.constraintModules(),
            imageDTO.preferenceModules()
        );

        // If validation passed, proceed with the update
        if (imageDTO.imageName() != null) {
            validateName(imageDTO.imageName());
            this.name = imageDTO.imageName();
        }
        if (imageDTO.imageDescription() != null) {
            this.description = imageDTO.imageDescription();
        }
        if (imageDTO.owner() != null) {
            this.owner = imageDTO.owner();
        }
        if (imageDTO.isPrivate() != null) {
            this.setIsPrivate(imageDTO.isPrivate());
        }
        
        if (imageDTO.solverSettings() != null) {
            this.setSolverScripts(imageDTO.solverSettings());
        }
        
        if (imageDTO.variablesModule() != null) {
            this.setVariablesModule(imageDTO.variablesModule());
        }
        
        if (imageDTO.constraintModules() != null) {
            this.getConstraintsModules().clear();
            for (ConstraintModuleDTO constraintModule : imageDTO.constraintModules()) {
                this.addConstraintModule(constraintModule);
            }
        }
        
        if (imageDTO.preferenceModules() != null) {
            this.getPreferenceModules().clear();
            for (PreferenceModuleDTO preferenceModule : imageDTO.preferenceModules()) {
                this.addPreferenceModule(preferenceModule);
            }
        }
    }

    private void validateModulesCompatibility(
        VariableModuleDTO variablesModule,
        Set<ConstraintModuleDTO> constraintModules,
        Set<PreferenceModuleDTO> preferenceModules
    ) {
        // Create maps to track which inputs belong to which modules
        Map<String, List<String>> setOwnership = new HashMap<>();
        Map<String, List<String>> paramOwnership = new HashMap<>();
        Map<String, List<String>> boundSetOwnership = new HashMap<>();
        Map<String, String> moduleNames = new HashMap<>(); // Track module names and their types
        StringBuilder errorMessage = new StringBuilder();
        
        // Check for duplicate module names across all types
        if (constraintModules != null) {
            for (ConstraintModuleDTO module : constraintModules) {
                if (moduleNames.containsKey(module.moduleName())) {
                    throw new RuntimeException("two modules have the same name: " + module.moduleName());
                }
                moduleNames.put(module.moduleName(), "constraint");
            }
        }
        
        if (preferenceModules != null) {
            for (PreferenceModuleDTO module : preferenceModules) {
                if (moduleNames.containsKey(module.moduleName())) {
                    throw new RuntimeException("two modules have the same name: " + module.moduleName());
                }
                moduleNames.put(module.moduleName(), "preference");
            }
        }
        
        // Check variables module
        if (variablesModule != null) {
            // Track regular inputs
            for (SetDefinitionDTO set : variablesModule.inputSets()) {
                setOwnership.computeIfAbsent(set.name(), k -> new ArrayList<>())
                    .add("Variables module");
            }
            for (ParameterDefinitionDTO param : variablesModule.inputParams()) {
                paramOwnership.computeIfAbsent(param.name(), k -> new ArrayList<>())
                    .add("Variables module");
            }
            
            // Track bound sets
            for (VariableDTO var : variablesModule.variablesOfInterest()) {
                if (var.boundSet() != null) {
                    boundSetOwnership.computeIfAbsent(var.boundSet(), k -> new ArrayList<>())
                        .add("Variable '" + var.identifier() + "'");
                }
            }
        }
        
        // Check constraint modules
        if (constraintModules != null) {
            for (ConstraintModuleDTO module : constraintModules) {
                for (SetDefinitionDTO set : module.inputSets()) {
                    setOwnership.computeIfAbsent(set.name(), k -> new ArrayList<>())
                        .add("Constraint module '" + module.moduleName() + "'");
                }
                for (ParameterDefinitionDTO param : module.inputParams()) {
                    paramOwnership.computeIfAbsent(param.name(), k -> new ArrayList<>())
                        .add("Constraint module '" + module.moduleName() + "'");
                }
            }
        }
        
        // Check preference modules
        if (preferenceModules != null) {
            for (PreferenceModuleDTO module : preferenceModules) {
                // Create a list of all parameters used by this module
                List<String> moduleParams = new ArrayList<>();
                
                for (SetDefinitionDTO set : module.inputSets()) {
                    setOwnership.computeIfAbsent(set.name(), k -> new ArrayList<>())
                        .add("Preference module '" + module.moduleName() + "'");
                }
                
                // Track input parameters
                for (ParameterDefinitionDTO param : module.inputParams()) {
                    moduleParams.add(param.name());
                    paramOwnership.computeIfAbsent(param.name(), k -> new ArrayList<>())
                        .add("Preference module '" + module.moduleName() + "'");
                }
                
                // Track cost parameters, but only add to ownership if not already an input parameter
                for (ParameterDefinitionDTO param : module.costParams()) {
                    if (!moduleParams.contains(param.name())) {
                        paramOwnership.computeIfAbsent(param.name(), k -> new ArrayList<>())
                            .add("Preference module '" + module.moduleName() + "' (cost parameter)");
                    }
                }
            }
        }

        // Check for bound set conflicts - but allow a set to be both bound set and input set in variables module
        boundSetOwnership.forEach((setName, variables) -> {
            if (variables.size() > 1) {
                errorMessage.append(String.format("Bound set conflict:%n"));
                errorMessage.append(String.format("- Set '%s' is used as bound set by multiple variables:%n", setName));
                variables.forEach(variable -> errorMessage.append(String.format("  * %s%n", variable)));
            }
            if (setOwnership.containsKey(setName)) {
                List<String> owners = setOwnership.get(setName);
                // Only report conflict if the set is used by modules other than the variables module
                boolean usedByOtherModules = owners.stream()
                    .anyMatch(owner -> !owner.equals("Variables module"));
                
                if (usedByOtherModules) {
                    errorMessage.append(String.format("Bound set conflict:%n"));
                    errorMessage.append(String.format("- Set '%s' is used as bound set by:%n", setName));
                    variables.forEach(variable -> errorMessage.append(String.format("  * %s%n", variable)));
                    errorMessage.append("And is also used as input by:%n");
                    owners.forEach(owner -> errorMessage.append(String.format("  * %s%n", owner)));
                }
            }
        });
        
        // Check set conflicts
        setOwnership.forEach((setName, owners) -> {
            if (owners.size() > 1) {
                // Filter out cases where it's only used by variables module
                List<String> nonVarModuleOwners = owners.stream()
                    .filter(owner -> !owner.equals("Variables module"))
                    .collect(Collectors.toList());
                
                if (nonVarModuleOwners.size() > 0) {
                    errorMessage.append(String.format("Set conflict:%n"));
                    errorMessage.append(String.format("- Set '%s' is used in multiple modules:%n", setName));
                    owners.forEach(owner -> errorMessage.append(String.format("  * %s%n", owner)));
                }
            }
        });
        
        // Check parameter conflicts
        paramOwnership.forEach((paramName, owners) -> {
            if (owners.size() > 1) {
                errorMessage.append(String.format("Parameter conflict:%n"));
                errorMessage.append(String.format("- Parameter '%s' is used in multiple modules:%n", paramName));
                owners.forEach(owner -> errorMessage.append(String.format("  * %s%n", owner)));
            }
        });
        
        if (errorMessage.length() > 0) {
            throw new IllegalArgumentException(
                String.format("Module conflicts detected:%n%s", errorMessage.toString())
            );
        }
    }

    @Transactional
    public String getName(){
        return this.name;
    }

    @Transactional
    public String getDescription(){
        return this.description;
    }

    @Transactional
    public String getOwner(){
        return this.owner;
    }

    @Transactional
    public boolean isPrivate(){
        return this.isPrivate;
    }

    @Transactional
    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    @Transactional
    public void addConstraintModule(String moduleName, String description) {
        constraintsModules.put( moduleName, new ConstraintModule(this, moduleName, description));
    }
    
    @Transactional
    public void addConstraintModule(ConstraintModuleDTO module) throws Exception {
        validateModulesCompatibility(
            RecordFactory.makeDTO(this).variablesModule(),
            Stream.concat(
                getConstraintsModules().values().stream().map(m -> RecordFactory.makeDTO(m)),
                Stream.of(module)
            ).collect(Collectors.toSet()),
            getPreferenceModules().values().stream().map(m -> RecordFactory.makeDTO(m)).collect(Collectors.toSet())
        );
        constraintsModules.put(module.moduleName(), new ConstraintModule(this, module));
    }
    @Transactional
    public void setConstraintsModules(HashMap<String,ConstraintModule> constraintsModules){
        this.constraintsModules = constraintsModules;
    }
    @Transactional
    public void setPreferencesModules(HashMap<String,PreferenceModule> prefs){
        this.preferenceModules = prefs;
    }

    @Transactional
    public void addPreferenceModule(String moduleName, String description) {
        preferenceModules.put(moduleName, new PreferenceModule(this, moduleName, description));
    }

    @Transactional
    public void addPreferenceModule(PreferenceModuleDTO module) throws Exception {
        validateModulesCompatibility(
            RecordFactory.makeDTO(this).variablesModule(),
            getConstraintsModules().values().stream().map(m -> RecordFactory.makeDTO(m)).collect(Collectors.toSet()),
            Stream.concat(
                getPreferenceModules().values().stream().map(m -> RecordFactory.makeDTO(m)),
                Stream.of(module)
            ).collect(Collectors.toSet())
        );
        preferenceModules.put(module.moduleName(), new PreferenceModule(this, module));
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
   
    //Adds a constraint to a Module
    @Transactional
    public void addConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).addConstraint(model.getConstraint(constraint.identifier()));
    }

    

    //Removes a certain constraint from a Module
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
    public void setVariablesModule(VariableModuleDTO moduleDTO) throws Exception {
        validateModulesCompatibility(
            moduleDTO,
            getConstraintsModules().values().stream().map(m -> RecordFactory.makeDTO(m)).collect(Collectors.toSet()),
            getPreferenceModules().values().stream().map(m -> RecordFactory.makeDTO(m)).collect(Collectors.toSet())
        );
        VariableModule module = this.variables.get(VariableModule.getVariableModuleName());
        if (module == null) {
            module = new VariableModule(this, VariableModule.getVariableModuleName(), "");
            this.variables.put(VariableModule.getVariableModuleName(), module);
        }
        module.update(moduleDTO);
    }
    
    @Transactional
    public VariableModule getVariablesModule() {
        return variables.get(VariableModule.getVariableModuleName());
    }

    @Transactional
    public void removePreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).removePreference(model.getPreference(preferenceDTO.identifier()));
    }

    @Transactional
    public Map<String,ModelVariable> getVariables() {
        return getVariablesModule().getVariables();
    }

    
    public SolutionDTO parseSolution(Solution solution) {
        try {
            solution.parseSolution(model, getVariablesModule().getIdentifiers());
        } catch (Exception e) {
            throw new RuntimeException("IO exception while parsing solution file, message: "+ e);
        }
        return RecordFactory.makeDTO(solution);
    }

    public ModelInterface getModel() {
        return this.model;
    }

    public String getId() {
       return this.id;
    }

    @Transactional
    public void setVariablesModule(Map<String, ModelVariable> variables, Collection<String> sets, Collection<String> params) {
        // Create a new module with the model data
        VariableModule module = new VariableModule(
            this,
            VariableModule.getVariableModuleName(),
            ""
        );
        
        // Set the variables
        for (Map.Entry<String, ModelVariable> entry : variables.entrySet()) {
            module.addVariable(entry.getValue());
        }
        
        // Set the input sets
        for (String setName : sets) {
            ModelSet set = model.getSet(setName);
            if (set != null) {
                module.addSet(set);
            }
        }
        
        // Set the input parameters
        for (String paramName : params) {
            ModelParameter param = model.getParameter(paramName);
            if (param != null) {
                module.addParam(param);
            }
        }
        
        // Store the module
        this.variables.put(VariableModule.getVariableModuleName(), module);
    }

    //Involved means all dependency sets of all modules
    public Set<ModelSet> getAllInvolvedSets() {
        Set<ModelSet> allSets = new HashSet<>();

        // Add inputSets from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allSets.addAll(constraintModule.getInvolvedSets());
        }

        // Add inputSets from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allSets.addAll(preferenceModule.getInvolvedSets());
        }
            
        allSets.addAll(getVariablesModule().getInvolvedSets());

        return allSets;
    }

    //Involved means all dependency params of all modules
    public Set<ModelParameter> getAllInvolvedParams() {
        Set<ModelParameter> allParams = new HashSet<>();

        // Add inputParams from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allParams.addAll(constraintModule.getInvolvedParameters());
        }

        // Add inputParams from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allParams.addAll(preferenceModule.getInvolvedParameters());
        }

        allParams.addAll(getVariablesModule().getInvolvedParameters());

        return allParams;
    }

    public Set<ModelSet> getAllInputSets() {
        Set<ModelSet> allSets = new HashSet<>();

        // Add inputSets from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allSets.addAll(constraintModule.getInputSets());
        }

        // Add inputSets from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allSets.addAll(preferenceModule.getInputSets());
        }
            
        allSets.addAll(getVariablesModule().getInputSets());

        return allSets;
    }

    public Set<ModelParameter> getAllInputParameters() {
        Set<ModelParameter> allParams = new HashSet<>();

        // Add inputParams from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allParams.addAll(constraintModule.getInputParams());
        }

        // Add inputParams from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allParams.addAll(preferenceModule.getInputParams());
        }

        allParams.addAll(getVariablesModule().getInputParams());

        return allParams;
    }

    

    public InputDTO getInput() throws Exception {
        Set<String> relevantParams = getAllInputParameters().stream().map((ModelParameter s) -> s.getIdentifier()).collect(Collectors.toSet());
        Set<String> relevantSets = getAllInputSets().stream().map((ModelSet s) -> s.getIdentifier()).collect(Collectors.toSet());
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

    public void configureModelInputs(Set<SetDefinitionDTO> inputSets,
            Set<ParameterDefinitionDTO> inputParams) throws Exception {
        for(SetDefinitionDTO setDTO : inputSets){
            ModelSet set = model.getSet(setDTO.name());
            set.update(setDTO);
        }
        
        for(ParameterDefinitionDTO paramDTO : inputParams){
            ModelParameter param = model.getParameter(paramDTO.name());
            param.update(paramDTO);
        }
    }

    @Transactional
    public void setConstraintsModule(ConstraintModuleDTO moduleDTO) {
        // Create or get the constraint module
        if (this.constraintsModules == null) {
            this.constraintsModules = new HashMap<>();
        }
        
        ConstraintModule module = this.constraintsModules.get(moduleDTO.moduleName());
        if (module == null) {
            module = new ConstraintModule(this, moduleDTO.moduleName(), "");
            this.constraintsModules.put(moduleDTO.moduleName(), module);
        }
        
        try {
            Set<String> setIds = moduleDTO.inputSets().stream()
                .map(SetDefinitionDTO::name)
                .collect(Collectors.toSet());
            Set<String> paramIds = moduleDTO.inputParams().stream()
                .map(ParameterDefinitionDTO::name)
                .collect(Collectors.toSet());
                
            validateInputExclusivity(setIds, paramIds, moduleDTO.moduleName());
            module.update(moduleDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update constraints module: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void setPreferencesModule(PreferenceModuleDTO moduleDTO) {
        // Create or get the preference module
        if (this.preferenceModules == null) {
            this.preferenceModules = new HashMap<>();
        }
        
        PreferenceModule module = this.preferenceModules.get(moduleDTO.moduleName());
        if (module == null) {
            module = new PreferenceModule(this, moduleDTO.moduleName(), "");
            this.preferenceModules.put(moduleDTO.moduleName(), module);
        }
        
        try {
            Set<String> setIds = moduleDTO.inputSets().stream()
                .map(SetDefinitionDTO::name)
                .collect(Collectors.toSet());
            Set<String> paramIds = moduleDTO.inputParams().stream()
                .map(ParameterDefinitionDTO::name)
                .collect(Collectors.toSet());
                
            validateInputExclusivity(setIds, paramIds, moduleDTO.moduleName());
            module.update(moduleDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update preferences module: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void prepareInput(InputDTO input) throws Exception {
        // Set input for sets
        for (Map.Entry<String,List<List<String>>> set : input.setsToValues().entrySet()) {
            List<String> setElements = new LinkedList<>();
            for(List<String> element : set.getValue()) {
                String tuple = ModelType.convertArrayOfAtoms(
                    element.toArray(new String[0]),
                    //TODO: fix a bug by fetching an input set from the modules instead from the parsed file. fix a bug by fetching an input set from the modules instead from the parsed file.
                    model.getSet(set.getKey()).getType()
                );
                setElements.add(tuple);
            }
            model.setInput(model.getSet(set.getKey()), setElements.toArray(new String[0]));
        }

        // Set input for parameters
        for (Map.Entry<String,List<String>> parameter : input.paramsToValues().entrySet()) {
            model.setInput(
                model.getParameter(parameter.getKey()),
                ModelType.convertArrayOfAtoms(
                    parameter.getValue().toArray(new String[0]),
                    model.getParameter(parameter.getKey()).getType()
                )
            );
        }

        // Handle toggled off constraint modules
        for (String constraintModule : input.constraintModulesToggledOff()) {
            Collection<ModelConstraint> constraintsToToggleOff = this.getConstraintsModule(constraintModule)
                .getConstraints()
                .values();
            for(ModelConstraint mc : constraintsToToggleOff) {
                model.toggleFunctionality(model.getConstraint(mc.getIdentifier()), false);
            }
        }

        // Handle toggled off preference modules
        for (String preferenceModule : input.preferenceModulesToggledOff()) {
            Collection<ModelPreference> preferencesToToggleOff = this.getPreferencesModule(preferenceModule)
                .getPreferences()
                .values();
            for(ModelPreference mp : preferencesToToggleOff) {
                model.toggleFunctionality(model.getPreference(mp.getIdentifier()), false);
            }
        }
        model.commentOutToggledFunctionalities();
    }

    public void restoreInput() throws Exception {
        model.restoreToggledFunctionalities();
    }
    
    private void validateInputExclusivity(Set<String> newSetIds, Set<String> newParamIds, String moduleId) throws IllegalArgumentException {
        // Check against constraint modules
        for (Map.Entry<String, ConstraintModule> entry : constraintsModules.entrySet()) {
            if (!entry.getKey().equals(moduleId)) {
                Set<String> conflictingSets = newSetIds.stream()
                    .filter(newSetId -> entry.getValue().isInput(newSetId))
                    .collect(Collectors.toSet());
                
                Set<String> conflictingParams = newParamIds.stream()
                    .filter(newParamId -> entry.getValue().isInput(newParamId))
                    .collect(Collectors.toSet());
                
                if (!conflictingSets.isEmpty() || !conflictingParams.isEmpty()) {
                    throw new IllegalArgumentException(
                        String.format("Input conflict between modules:%n" +
                            "- Constraint module '%s'%n" +
                            "- Attempting to use in module '%s'%n" +
                            "Conflicting inputs:%n" +
                            "%s%s",
                            entry.getKey(),
                            moduleId,
                            conflictingSets.isEmpty() ? "" : String.format("- Sets: %s%n", String.join(", ", conflictingSets)),
                            conflictingParams.isEmpty() ? "" : String.format("- Parameters: %s%n", String.join(", ", conflictingParams)))
                    );
                }
            }
        }

        // Check against preference modules
        for (Map.Entry<String, PreferenceModule> entry : preferenceModules.entrySet()) {
            if (!entry.getKey().equals(moduleId)) {
                Set<String> conflictingSets = newSetIds.stream()
                    .filter(newSetId -> entry.getValue().isInput(newSetId))
                    .collect(Collectors.toSet());
                
                Set<String> conflictingParams = newParamIds.stream()
                    .filter(newParamId -> entry.getValue().isInput(newParamId))
                    .collect(Collectors.toSet());
                
                Set<String> costParamConflicts = newParamIds.stream()
                    .filter(newParamId -> entry.getValue().isInput(newParamId) && entry.getValue().getCostParameters().stream()
                        .anyMatch(param -> param.getIdentifier().equals(newParamId)))
                    .collect(Collectors.toSet());
                
                if (!conflictingSets.isEmpty() || !conflictingParams.isEmpty()) {
                    String conflictType = !costParamConflicts.isEmpty() ? 
                        String.format("Cost parameter conflict:%n- Parameters used as cost parameters in preference module: %s%n", 
                            String.join(", ", costParamConflicts)) :
                        String.format("Input conflict:%n%s%s",
                            conflictingSets.isEmpty() ? "" : String.format("- Sets: %s%n", String.join(", ", conflictingSets)),
                            conflictingParams.isEmpty() ? "" : String.format("- Parameters: %s%n", String.join(", ", conflictingParams)));
                    
                    throw new IllegalArgumentException(
                        String.format("Conflict between modules:%n" +
                            "- Preference module '%s'%n" +
                            "- Attempting to use in module '%s'%n" +
                            "%s",
                            entry.getKey(),
                            moduleId,
                            conflictType)
                    );
                }
            }
        }

        // Check against variables module if this is not the variables module
        if (!VariableModule.getVariableModuleName().equals(moduleId)) {
            VariableModule variablesModule = getVariablesModule();
            Set<String> conflictingSets = newSetIds.stream()
                .filter(newSetId -> variablesModule.isInput(newSetId))
                .collect(Collectors.toSet());
            
            Set<String> boundSetConflicts = newSetIds.stream()
                .filter(newSetId -> variablesModule.getVariables().values().stream()
                    .anyMatch(var -> var.getBoundSet() != null && var.getBoundSet().getIdentifier().equals(newSetId)))
                .collect(Collectors.toSet());
            
            Set<String> conflictingParams = newParamIds.stream()
                .filter(newParamId -> variablesModule.isInput(newParamId))
                .collect(Collectors.toSet());
            
            if (!conflictingSets.isEmpty() || !conflictingParams.isEmpty()) {
                String conflictType = !boundSetConflicts.isEmpty() ? 
                    String.format("Bound set conflict:%n- Sets used as bound sets in variables module: %s%n", 
                        String.join(", ", boundSetConflicts)) :
                    String.format("Input conflict:%n%s%s",
                        conflictingSets.isEmpty() ? "" : String.format("- Sets: %s%n", String.join(", ", conflictingSets)),
                        conflictingParams.isEmpty() ? "" : String.format("- Parameters: %s%n", String.join(", ", conflictingParams)));
                
                throw new IllegalArgumentException(
                    String.format("Conflict between modules:%n" +
                        "- Variables module%n" +
                        "- Attempting to use in module '%s'%n" +
                        "%s",
                        moduleId,
                        conflictType)
                );
            }
        }
    }

    private void validateNoEmptySets() throws BadRequestException {
        ModelInterface model = getModel();
        Collection<ModelSet> sets = model.getSets();
        for (ModelSet set : sets) {
            if (set.isPrimitive() && set.isEmpty()) {
                throw new BadRequestException("Empty set found: " + set.getIdentifier() + ". Empty sets are not allowed.");
            }
        }
    }

    private void validateNoUnknownTypes() throws BadRequestException {
        ModelInterface model = getModel();
        
        // Check sets
        for (ModelSet set : model.getSets()) {
            ModelType type = set.getType();
            if (type == ModelPrimitives.UNKNOWN) {
                throw new BadRequestException("Unknown type found in set: " + set.getIdentifier());
            }
            // If it's a tuple, check each component
            if (type instanceof Tuple) {
                Tuple tupleType = (Tuple) type;
                for (ModelPrimitives componentType : tupleType.getTypes()) {
                    if (componentType == ModelPrimitives.UNKNOWN) {
                        throw new BadRequestException("Unknown type found in tuple component of set: " + set.getIdentifier());
                    }
                }
            }
        }

        // Check parameters
        for (ModelParameter param : model.getParameters()) {
            ModelType type = param.getType();
            if (type == ModelPrimitives.UNKNOWN) {
                throw new BadRequestException("Unknown type found in parameter: " + param.getIdentifier());
            }
            // If it's a tuple, check each component
            if (type instanceof Tuple) {
                Tuple tupleType = (Tuple) type;
                for (ModelPrimitives componentType : tupleType.getTypes()) {
                    if (componentType == ModelPrimitives.UNKNOWN) {
                        throw new BadRequestException("Unknown type found in tuple component of parameter: " + param.getIdentifier());
                    }
                }
            }
        }

        // Check variables
        for (ModelVariable var : model.getVariables()) {
            ModelType type = var.getType();
            if (type == ModelPrimitives.UNKNOWN) {
                throw new BadRequestException("Unknown type found in variable: " + var.getIdentifier());
            }
            // If it's a tuple, check each component
            if (type instanceof Tuple) {
                Tuple tupleType = (Tuple) type;
                for (ModelPrimitives componentType : tupleType.getTypes()) {
                    if (componentType == ModelPrimitives.UNKNOWN) {
                        throw new BadRequestException("Unknown type found in tuple component of variable: " + var.getIdentifier());
                    }
                }
            }
        }
    }

    public void validateCode() throws Exception {
        validateNoEmptySets();
        // validateNoUnknownTypes();
    }
}
