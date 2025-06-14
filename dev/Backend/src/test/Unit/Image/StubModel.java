package Unit.Image;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.ModelComponent;
import Model.ModelConstraint;
import Model.ModelFunction;
import Model.ModelFunctionality;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelSet;
import Model.ModelVariable;

public class StubModel extends ModelInterface {
    private final Map<String, ModelSet> sets = new HashMap<>();
    private final Map<String, ModelParameter> params = new HashMap<>();
    private final Map<String, ModelConstraint> constraints = new HashMap<>();
    private final Map<String, ModelPreference> preferences = new HashMap<>();
    private final Map<String, ModelVariable> variables = new HashMap<>();
    private final Map<String, ModelFunction> functions = new HashMap<>();

    @Override
    public void writeSolution(String content, String suffix) throws Exception {
        // Stub implementation
    }

    @Override
    public void writeToSource(String newSource) throws Exception {
        // Stub implementation
    }

    @Override
    public String getSolutionPathToFile(String suffix) throws Exception {
        return "stub_solution_path";
    }

    @Override
    public String getSourcePathToFile() throws Exception {
        return "stub_source_path";
    }

    @Override
    public InputStream getSource() throws Exception {
        return null;
    }

    @Override
    public void parseSource() throws Exception {
        // Stub implementation
    }

    @Override
    public void appendToSet(ModelSet set, String value) throws Exception {
        // Stub implementation
    }

    @Override
    public void removeFromSet(ModelSet set, String value) throws Exception {
        // Stub implementation
    }

    @Override
    public void setInput(ModelParameter identifier, String value) throws Exception {
        // Stub implementation
    }

    @Override
    public void setInput(ModelSet identifier, String[] values) throws Exception {
        // Stub implementation
    }

    @Override
    public String[] getInput(ModelParameter parameter) throws Exception {
        return new String[0];
    }

    @Override
    public List<String[]> getInput(ModelSet set) throws Exception {
        return List.of();
    }

    @Override
    public void toggleFunctionality(ModelFunctionality mf, boolean turnOn) {
        // Stub implementation
    }

    @Override
    public ModelSet getSet(String identifier) {
        return sets.get(identifier);
    }

    @Override
    public ModelParameter getParameter(String identifier) {
        return params.get(identifier);
    }

    @Override
    public ModelFunction getFunction(String identifier) {
        return functions.get(identifier);
    }

    @Override
    public ModelConstraint getConstraint(String identifier) {
        return constraints.get(identifier);
    }

    @Override
    public Collection<ModelConstraint> getConstraints() {
        return constraints.values();
    }

    @Override
    public ModelPreference getPreference(String identifier) {
        return preferences.get(identifier);
    }

    @Override
    public Collection<ModelPreference> getPreferences() {
        return preferences.values();
    }

    @Override
    public ModelVariable getVariable(String identifier) {
        return variables.get(identifier);
    }

    @Override
    public Collection<ModelVariable> getVariables() {
        return variables.values();
    }

    @Override
    public Collection<ModelVariable> getVariables(Collection<String> identifiers) {
        return identifiers.stream()
            .map(variables::get)
            .filter(v -> v != null)
            .toList();
    }

    @Override
    public Collection<ModelSet> getSets() {
        return sets.values();
    }

    @Override
    public Collection<ModelParameter> getParameters() {
        return params.values();
    }

    @Override
    public Collection<ModelFunction> getFunctions() {
        return functions.values();
    }

    @Override
    public ModelComponent getComponent(String mc) {
        return getSet(mc) != null ? getSet(mc) :
               getParameter(mc) != null ? getParameter(mc) :
               getVariable(mc) != null ? getVariable(mc) :
               getConstraint(mc) != null ? getConstraint(mc) :
               getPreference(mc);
    }

    @Override
    public void setModelComponent(ModelComponent mc) throws Exception {
        if (mc instanceof ModelSet) {
            sets.put(mc.getIdentifier(), (ModelSet) mc);
        } else if (mc instanceof ModelParameter) {
            params.put(mc.getIdentifier(), (ModelParameter) mc);
        } else if (mc instanceof ModelFunction) {
            functions.put(mc.getIdentifier(), (ModelFunction) mc);
        } else if (mc instanceof ModelConstraint) {
            constraints.put(mc.getIdentifier(), (ModelConstraint) mc);
        } else if (mc instanceof ModelPreference) {
            preferences.put(mc.getIdentifier(), (ModelPreference) mc);
        } else if (mc instanceof ModelVariable) {
            variables.put(mc.getIdentifier(), (ModelVariable) mc);
        }
    }

    @Override
    public void commentOutToggledFunctionalities() throws Exception {
        // Stub implementation
    }

    @Override
    public void restoreToggledFunctionalities() throws Exception {
        // Stub implementation
    }

    // Helper methods to set up test data
    public void addSet(ModelSet set) {
        sets.put(set.getIdentifier(), set);
    }

    public void addParameter(ModelParameter param) {
        params.put(param.getIdentifier(), param);
    }

    public void addConstraint(ModelConstraint constraint) {
        constraints.put(constraint.getIdentifier(), constraint);
    }

    public void addPreference(ModelPreference preference) {
        preferences.put(preference.getIdentifier(), preference);
    }

    public void addVariable(ModelVariable variable) {
        variables.put(variable.getIdentifier(), variable);
    }

    public void addFunction(ModelFunction function) {
        functions.put(function.getIdentifier(), function);
    }

    @Override
    public void setId(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setId'");
    }

} 