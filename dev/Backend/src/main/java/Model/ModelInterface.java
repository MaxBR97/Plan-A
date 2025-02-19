package Model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import DataAccess.ModelRepository;
import Exceptions.InternalErrors.BadRequestException;

/**
 * Interface defining the public API for interacting with a mathematical optimization model.
 * This interface provides methods for managing sets, parameters, constraints, preferences,
 * and variables within the model, as well as solving and compiling operations.
 */
public abstract class ModelInterface {

    /**
     * Appends a value to a specified set in the model.
     * 
     * @param set The set to append to
     * @param value The value to append
     * @throws Exception if the value is incompatible with the set's type
     */
    public abstract void appendToSet(ModelSet set, String value) throws Exception;

    /**
     * Removes a value from a specified set in the model.
     * 
     * @param set The set to remove from
     * @param value The value to remove
     * @throws Exception if the value is incompatible with the set's type
     */
    public abstract void removeFromSet(ModelSet set, String value) throws Exception;

    /**
     * Sets the value of a model input (parameter).
     * 
     * @param identifier The input identifier
     * @param value The value to set
     * @throws Exception if the value is incompatible with the input's type
     */
    public abstract void setInput(ModelParameter identifier, String value) throws Exception;

    /**
     * Sets the value of a model input (set).
     * 
     * @param identifier The input identifier
     * @throws Exception if the values are incompatible with the input's type
     */
    public abstract void setInput(ModelSet identifier, String[] values) throws Exception;

    // Get last committed input from zpl file
    public abstract String[] getInput(ModelParameter parameter) throws Exception;

    // Get last committed input from zpl file
    public abstract List<String[]> getInput(ModelSet set) throws Exception;

    
    /**
     * Toggles a model functionality on or off.
     * 
     * @param mf The functionality to toggle
     * @param turnOn true to enable, false to disable
     */
    public abstract void toggleFunctionality(ModelFunctionality mf, boolean turnOn);

    /**
     * Checks if the model compiles successfully.
     * 
     * @param timeout Maximum time in seconds to wait for compilation
     * @return true if compilation succeeds, false otherwise
     */
    public abstract boolean isCompiling(float timeout) throws BadRequestException;

    /**
     * Solves the model and returns the solution.
     * 
     * @param timeout Maximum time in seconds to wait for solving
     * @return Solution object if solving succeeds, null otherwise
     */
    public abstract Solution solve(float timeout, String solutionFileSufix) throws BadRequestException;

    /**
     * Retrieves a set by its identifier.
     * 
     * @param identifier The set identifier
     * @return ModelSet object if found, null otherwise
     */
    public abstract ModelSet getSet(String identifier);

    /**
     * Retrieves a parameter by its identifier.
     * 
     * @param identifier The parameter identifier
     * @return ModelParameter object if found, null otherwise
     */
    public abstract ModelParameter getParameter(String identifier);

    /**
     * Retrieves a constraint by its identifier.
     * 
     * @param identifier The constraint identifier
     * @return ModelConstraint object if found, null otherwise
     */
    public abstract ModelConstraint getConstraint(String identifier);

    /**
     * Retrieves a all constraints loaded in the model
     * @return set of all constraints parsed from the model
     */
    public abstract Collection<ModelConstraint> getConstraints();
    /**
     * Retrieves a preference by its identifier.
     * 
     * @param identifier The preference identifier
     * @return ModelPreference object if found, null otherwise
     */
    public abstract ModelPreference getPreference(String identifier);
    /**
     * Retrieves a all preferences loaded in the model
     * @return set of all preferences parsed from the model
     */
    public abstract Collection<ModelPreference> getPreferences();
    /**
     * Retrieves a variable by its identifier.
     * 
     * @param identifier The variable identifier
     * @return ModelVariable object if found, null otherwise
     */
    abstract public ModelVariable getVariable(String identifier);

    abstract public Collection<ModelVariable> getVariables();

    abstract public Collection<ModelVariable> getVariables(Collection<String> identifiers);


    abstract public Collection<ModelSet> getSets();

    abstract public Collection<ModelParameter> getParameters();

    abstract public ModelComponent getComponent(String identifier);

}