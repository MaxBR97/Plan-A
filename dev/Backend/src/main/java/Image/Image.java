package Image;

import java.io.IOException;
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

import DTO.Factories.RecordFactory;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import Image.Modules.ConstraintModule;
import Image.Modules.PreferenceModule;
import Image.Modules.VariableModule;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelInterface;
import Model.ModelPreference;
import Model.ModelVariable;
import Model.Solution;

public class Image {
    // Note: this implies module names must be unique between user constraints/preferences.
    private final HashMap<String,ConstraintModule> constraintsModules;
    private final HashMap<String,PreferenceModule> preferenceModules;
    private final VariableModule variables;
    private final ModelInterface model;
    private final int defaultTimeout = 60;
    public Image(ModelInterface model) {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new VariableModule();
        this.model = model;
    }
    public Image(String path) throws IOException {
        constraintsModules = new HashMap<>();
        preferenceModules = new HashMap<>();
        variables = new VariableModule();
        this.model = new Model(path);
    }

    //TODO: implement deep copy
    public Image(Image image) {
        this.constraintsModules = new HashMap<>();
        this.preferenceModules = new HashMap<>();
        this.variables = new VariableModule();
        this.model = image.model;
        //Removed nulls to remove warnings, will implement post alpha.
    }

    //will probably have to use an adapter layer, or change types to DTOs
    public void addConstraintModule(ConstraintModule module) {
        constraintsModules.put(module.getName(), module);
    }
    public void addConstraintModule(String moduleName, String description) {
        constraintsModules.put(moduleName, new ConstraintModule(moduleName, description));
    }
    public void addConstraintModule(String moduleName, String description, Collection<String> constraints, Collection<String> inputSets, Collection<String> inputParams) {
        HashSet<ModelConstraint> modelConstraints = new HashSet<>();
        for (String name : constraints) {
            ModelConstraint constraint = model.getConstraint(name);
            Objects.requireNonNull(constraint,"Invalid constraint name in add constraint in image");
            modelConstraints.add(constraint);
        }
        constraintsModules.put(moduleName, new ConstraintModule(moduleName, description, modelConstraints,inputSets,inputParams));
    }
    public void addPreferenceModule(PreferenceModule module) {
        preferenceModules.put(module.getName(), module);
    }
    public void addPreferenceModule(String moduleName, String description) {
        preferenceModules.put(moduleName, new PreferenceModule(moduleName, description));
    }
    public void addPreferenceModule(String moduleName, String description, Collection<String> preferences, Collection<String> inputSets, Collection<String> inputParams) {
        HashSet<ModelPreference> modelPreferences = new HashSet<>();
        for (String name : preferences) {
            ModelPreference preference = model.getPreference(name);
           Objects.requireNonNull(preference,"Invalid preference name in add preference module");
           modelPreferences.add(preference);
        }
        preferenceModules.put(moduleName, new PreferenceModule(moduleName, description, modelPreferences,inputSets,inputParams));
    }
    public ConstraintModule getConstraintModule(String name) {
        return constraintsModules.get(name);
    }
    public PreferenceModule getPreferenceModules(String name) {
        return preferenceModules.get(name);
    }
    public HashMap<String, ConstraintModule> getConstraintsModules() {
        return constraintsModules;
    }
    public HashMap<String, PreferenceModule> getPreferenceModules() {
        return preferenceModules;
    }

    public void addConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).addConstraint(model.getConstraint(constraint.identifier()));
    }
    public void removeConstraint(String moduleName, ConstraintDTO constraint) {
        if(!constraintsModules.containsKey(moduleName))
            throw new IllegalArgumentException("No constraint module with name: " + moduleName);
        constraintsModules.get(moduleName).removeConstraint(model.getConstraint(constraint.identifier()));
    }
    public void addPreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).addPreference(model.getPreference(preferenceDTO.identifier()));
    }
    public void removePreference(String moduleName, PreferenceDTO preferenceDTO) {
        if(!preferenceModules.containsKey(moduleName))
            throw new IllegalArgumentException("No preference module with name: " + moduleName);
        preferenceModules.get(moduleName).removePreference(model.getPreference(preferenceDTO.identifier()));
    }
    public Map<String,ModelVariable> getVariables() {
        return variables.getVariables();
    }
    public ModelVariable getVariable(String name) {
        return variables.get(name);
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
    public void fetchVariables(){
    }
    /*
    public void TogglePreference(String name){
            Objects.requireNonNull(name,"Null value during Toggle Preference in Image");
            model.toggleFunctionality();
    }
    public void ToggleConstraint(String name){
            Objects.requireNonNull(name,"Null value during Toggle Constraint in Image");
            constraintsModules.get(name).ToggleModule();
    }*/
    public SolutionDTO solve(int timeout){
        Solution solution=model.solve(timeout, "SOLUTION");
        try {
            solution.parseSolution(model, variables.getIdentifiers(),variables.getAliases());
        } catch (IOException e) {
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
    @Deprecated
    public String getId() {
        // Do not use this! ID stored in controller, image not aware of its own ID.
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    public void reset(Map<String,ModelVariable> variables, Collection<String> sets, Collection<String> params,Map<String,List<String>> aliases) {
        constraintsModules.clear();
        preferenceModules.clear();
        this.variables.override(variables,sets,params,aliases);
    }
    public Map<String,List<String>> getAliases() {
        return variables.getAliases();
    }
    public Set<String> getAllInvolvedSets() {
        Set<String> allSets = new HashSet<>();

        // Add inputSets from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allSets.addAll(constraintModule.getInputSets());
        }

        // Add inputSets from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allSets.addAll(preferenceModule.getInputSets());
        }

        allSets.addAll(variables.getInputSets());

        return allSets;
    }

    public Set<String> getAllInvolvedParams() {
        Set<String> allParams = new HashSet<>();

        // Add inputParams from each constraint module
        for (ConstraintModule constraintModule : constraintsModules.values()) {
            allParams.addAll(constraintModule.getInputParams());
        }

        // Add inputParams from each preference module
        for (PreferenceModule preferenceModule : preferenceModules.values()) {
            allParams.addAll(preferenceModule.getInputParams());
        }

        allParams.addAll(variables.getInputParams());

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
