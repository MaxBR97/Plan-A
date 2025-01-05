package Model;

/**
 * Interface defining the public API for interacting with a mathematical optimization model.
 * This interface provides methods for managing sets, parameters, constraints, preferences,
 * and variables within the model, as well as solving and compiling operations.
 */
public interface ModelInterface {
    /**
     * Appends a value to a specified set in the model.
     * 
     * @param set The set to append to
     * @param value The value to append
     * @throws Exception if the value is incompatible with the set's type
     */
    void appendToSet(ModelSet set, String value) throws Exception;

    /**
     * Removes a value from a specified set in the model.
     * 
     * @param set The set to remove from
     * @param value The value to remove
     * @throws Exception if the value is incompatible with the set's type
     */
    void removeFromSet(ModelSet set, String value) throws Exception;

    /**
     * Sets the value of a model input (parameter).
     * 
     * @param identifier The input identifier
     * @param value The value to set
     * @throws Exception if the value is incompatible with the input's type
     */
    void setInput(ModelInput identifier, String value) throws Exception;

    /**
     * Toggles a model functionality on or off.
     * 
     * @param mf The functionality to toggle
     * @param turnOn true to enable, false to disable
     */
    void toggleFunctionality(ModelFunctionality mf, boolean turnOn);

    /**
     * Checks if the model compiles successfully.
     * 
     * @param timeout Maximum time in seconds to wait for compilation
     * @return true if compilation succeeds, false otherwise
     */
    boolean isCompiling(float timeout);

    /**
     * Solves the model and returns the solution.
     * 
     * @param timeout Maximum time in seconds to wait for solving
     * @return Solution object if solving succeeds, null otherwise
     */
    Solution solve(float timeout);

    /**
     * Retrieves a set by its identifier.
     * 
     * @param identifier The set identifier
     * @return ModelSet object if found, null otherwise
     */
    ModelSet getSet(String identifier);

    /**
     * Retrieves a parameter by its identifier.
     * 
     * @param identifier The parameter identifier
     * @return ModelParameter object if found, null otherwise
     */
    ModelParameter getParameter(String identifier);

    /**
     * Retrieves a constraint by its identifier.
     * 
     * @param identifier The constraint identifier
     * @return ModelConstraint object if found, null otherwise
     */
    ModelConstraint getConstraint(String identifier);

    /**
     * Retrieves a preference by its identifier.
     * 
     * @param identifier The preference identifier
     * @return ModelPreference object if found, null otherwise
     */
    ModelPreference getPreference(String identifier);

    /**
     * Retrieves a variable by its identifier.
     * 
     * @param identifier The variable identifier
     * @return ModelVariable object if found, null otherwise
     */
    ModelVariable getVariable(String identifier);
}