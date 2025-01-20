package Utilities.Stubs;

import Model.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ModelStub implements ModelInterface {
    Model RealModel;
    public ModelStub(String sourceFilePath) throws IOException {
        RealModel = new Model(sourceFilePath);
    }

    @Override
    public void appendToSet(ModelSet set, String value) throws Exception {

    }

    @Override
    public void removeFromSet(ModelSet set, String value) throws Exception {

    }

    @Override
    public void setInput(ModelSet identifier, String[] values) throws Exception {

    }

    @Override
    public void setInput(ModelParameter identifier, String value) throws Exception {

    }
    @Override
    public void toggleFunctionality(ModelFunctionality mf, boolean turnOn) {

    }

    @Override
    public boolean isCompiling(float timeout) {
        return false;
    }

    @Override
    public Solution solve(float timeout) {
        return null;
    }

    @Override
    public ModelSet getSet(String identifier) {
        return null;
    }

    @Override
    public ModelParameter getParameter(String identifier) {
        return null;
    }

    @Override
    public ModelConstraint getConstraint(String identifier) {
        return null;
    }

    @Override
    public Collection<ModelConstraint> getConstraints() {
        return List.of();
    }

    @Override
    public ModelPreference getPreference(String identifier) {
        return null;
    }

    @Override
    public Collection<ModelPreference> getPreferences() {
        return List.of();
    }

    @Override
    public ModelVariable getVariable(String identifier) {
        return null;
    }
}
