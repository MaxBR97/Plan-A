package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Image.Image;
import Model.ModelVariable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

@Entity
@Table(name = "variable_module")
public class VariableModule {

    @Id
    @Column(name="image_id")
    private String id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    @MapKey(name = "identifier")
    private Map<String, ModelVariable> variables;

    // @ElementCollection
    // @Column(name = "input_set")
    @Transient
    final Set<String> inputSets;

    // @ElementCollection
    // @Column(name = "input_param")
    @Transient
    final Set<String> inputParams;


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
        variables = new HashMap<>();
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }

    public VariableModule(Image image, Map<String, ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
        this.id = image.getId();
        this.variables = new HashMap<>(variables);
        this.inputSets = new HashSet<>(inputSets);
        this.inputParams = new HashSet<>(inputParams);
    }
    public VariableModule(Image image, Set<ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
        this.id = image.getId();
        this.variables = new HashMap<>();
        for(ModelVariable variable: variables) {
            this.variables.put(variable.getIdentifier(),variable);
        }
        this.inputSets = new HashSet<>(inputSets);
        this.inputParams = new HashSet<>(inputParams);
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
    /*
    public void addParam(String name){
        inputParams.add(name);
    }
    public void addSet(String name){
        inputParams.add(name);
    }
    public void addSets(Collection<String> sets){
        inputSets.addAll(sets);
    }
    public void addParams(Collection<String> params){
        inputParams.addAll(params);
    }
    */
}
