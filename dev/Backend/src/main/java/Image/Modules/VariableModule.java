package Image.Modules;

import Model.ModelParameter;
import Model.ModelSet;
import Model.ModelVariable;

import java.util.*;

public class VariableModule {

    final Map<String, ModelVariable> variables;
    final Set<String> inputSets;
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

    public VariableModule(Map<String, ModelVariable> variables, Collection<String> inputSets, Collection<String> inputParams) {
        this.variables = new HashMap<>(variables);
        this.inputSets = new HashSet<>(inputSets);
        this.inputParams = new HashSet<>(inputParams);
    }

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

    public ModelVariable get(String name) {
        return variables.get(name);
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
