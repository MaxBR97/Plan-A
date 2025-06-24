package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import Image.Image;
import Model.ModelParameter;
import Model.ModelSet;
import Model.ModelVariable;
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
@DiscriminatorValue("VARIABLE")
public class VariableModule extends Module {
    private static final String VARIABLE_MODULE_NAME = "VariableModule";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
    })
    @MapKey(name = "id.identifier")
    private Map<String, ModelVariable> variables;


    public Map<String, ModelVariable> getVariables() {
        return variables;
    }

    public VariableModule(Image image, VariableModuleDTO dto) throws Exception{
        super(image,VARIABLE_MODULE_NAME,"", dto.inputSets(), dto.inputParams());
        this.variables = new HashMap<>();
        for (VariableDTO varDTO : dto.variablesOfInterest()){
            ModelVariable var = image.getModel().getVariable(varDTO.identifier());
            this.variables.put(varDTO.identifier(),var);
            var.update(varDTO);
            if(varDTO.boundSet() != null)
                var.setBoundSet(image.getModel().getSet(varDTO.boundSet()));
        }
    }

    protected VariableModule() {
        super();
        variables = new HashMap<>();
    }

    public VariableModule(Image image, String name, String description) {
        super(image, name, description);
        variables = new HashMap<>();
    }

    public VariableModule(Image image, Map<String, ModelVariable> variables, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams) {
        super(image, VariableModule.getVariableModuleName(), "", inputSets, inputParams);
        this.variables = new HashMap<>(variables);
    }

    // @Transactional
    // public void override(Map<String, ModelVariable> variables, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams) {
    //     this.variables.clear();
    //     this.inputSets.clear();
    //     this.inputParams.clear();
    //     this.variables.putAll(variables);
    //     this.inputSets.addAll(inputSets);
    //     this.inputParams.addAll(inputParams);
    // }

    public static String getVariableModuleName() {
        return VARIABLE_MODULE_NAME;
    }

    @Transactional
    public void clear() {
        variables.clear();
        inputSets.clear();
        inputParams.clear();
    }

    public Set<String> getIdentifiers() {
        return variables.keySet();
    }

    public ModelVariable get(String name) {
        return variables.get(name);
    }

    @Transactional
    public void addVariable(ModelVariable variable) {
        variables.put(variable.getIdentifier(),variable);
    }

    @Override
    public Set<ModelSet> getInvolvedSets() {
        HashSet<ModelSet> involvedSets = new HashSet<>();
        for(ModelVariable var : variables.values()){
            var = ((ModelVariable)loadFullComponent(var));
            var.getPrimitiveSets(involvedSets);
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelVariable var : variables.values()){
            var = ((ModelVariable)loadFullComponent(var));
          var.getPrimitiveParameters(involvedParameters);
        }
        return involvedParameters;
    }

    // public Set<ModelSet> getInputSets() {        
    //     return super.getInputSets();
    // }

    //Gets the input sets including the Bonud set
    public Set<ModelSet> getInputSets() {
        Set<ModelSet> sets = new HashSet<>();
        for(ModelVariable var : this.variables.values()){
            if(var.getBoundSet() != null)
                sets.add(var.getBoundSet());
        }
        sets.addAll(super.getInputSets());
        return sets;
    }
    
    
    @Transactional
    public void update(VariableModuleDTO dto) throws Exception {
        // Update variables of interest
        variables.clear();
        for (VariableDTO varDTO : dto.variablesOfInterest()) {
            ModelVariable modelVar = image.getModel().getVariable(varDTO.identifier());
            if (modelVar == null) {
                throw new IllegalArgumentException("Invalid variable name: " + varDTO.identifier());
            }
            modelVar.update(varDTO);
            //TODO: refactor this line to be done in the ModelVariable class by the update method.
            modelVar.setBoundSet(image.getModel().getSet(varDTO.boundSet()));
            variables.put(varDTO.identifier(), modelVar);
        }

        // Update input sets
        inputSets.clear();
        for (var setDTO : dto.inputSets()) {
            addSet(setDTO);
        }

        // Update input parameters
        inputParams.clear();
        for (var paramDTO : dto.inputParams()) {
            addParam(paramDTO);
        }
    }

    @Transactional
    @Override
    public boolean isInput(String id) {
        return super.isInput(id) || variables.values().stream()
            .anyMatch(var -> var.getBoundSet() != null && var.getBoundSet().getIdentifier().equals(id));
    }
}
