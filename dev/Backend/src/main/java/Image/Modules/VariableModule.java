package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import Image.Image;
import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import Model.ModelVariable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

@Entity
@DiscriminatorValue("VARIABLE")
public class VariableModule extends Module {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
    })
    @MapKey(name = "id.identifier")
    //@Transient
    private Map<String, ModelVariable> variables;

    @Transient
    private final static String genericName = "VariableModule";

        public Map<String, ModelVariable> getVariables() {
            return variables;
        }
    
        public Set<String> getInputSets() {
            return inputSets;
        }
    
        public Set<String> getInputParams() {
            return inputParams;
        }
    
        public VariableModule() {
           super();
           variables = new HashMap<>();
        }
    
    public VariableModule(Image image, Map<String, ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
       super(image,getVariableModuleName(),"NO DESCRIPTION", inputSets, inputParams);
       this.variables = new HashMap<>();
    }

    public VariableModule(Image image, Set<ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
        super(image,getVariableModuleName(),"NO DESCRIPTION",inputSets,inputParams);
        this.variables = new HashMap<>();
        for(ModelVariable variable: variables) {
            this.variables.put(variable.getIdentifier(),variable);
        }
    }

    public static String getVariableModuleName(){
        return genericName;
    }

    @Transactional
    public void clear() {
        variables.clear();
        inputSets.clear();
        inputParams.clear();
    }
    
    public void override(Map<String, ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
        this.variables.clear();
        this.inputSets.clear();
        this.inputParams.clear();
        this.variables.putAll(variables);
        this.inputSets.addAll(inputSets);
        this.inputParams.addAll(inputParams);
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

    @Transactional
    public void addParam(String name){
                inputParams.add(name);
    }

    @Transactional
    public void addSet(String name){
        inputParams.add(name);
    }

    @Transactional
    public void removeSet(String name){
        inputSets.remove(name);
    }

    @Transactional
    public void removeParam(String name){
                inputSets.remove(name);
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
    
}
