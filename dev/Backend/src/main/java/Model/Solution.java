package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Utils.Tuple;

public class Solution {

    public enum SolutionStatus {
        OPTIMAL,
        SUBOPTIMAL,
        UNSOLVED;
    }

    final String engineMsg;
    final boolean engineRunSuccess;
    boolean parsed;
    final String solutionPath;
    SolutionStatus solved;
    /*
    maps variable names to a list of lists with each list holding elements of the solution,
    for example for variable V of type set1 * set2 where set1= {1,2} and set2= {"a","b"}
    an example for a solution is: V -> { [1,"a"], [2, "b"] } note: order matters!
    Since from this point on this is only static data to be shown to the user, only Strings are in use
     */
    final HashMap<String, List<Tuple<List<String>, Double>>> variableSolution;
    final HashMap<String, List<String>> variableStructure;
    final HashMap<String, List<String>> variableTypes;
    double solvingTime;
    double objectiveValue; // the actual numeric value of the expression that was optimized


    //helper fields
    Collection<ModelVariable> variables;

    public Solution(String solutionPath) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
        variableStructure = new HashMap<>();
        parsed = false;
        engineRunSuccess = true;
        engineMsg = "";
        variableTypes = new HashMap<>();
        solved = SolutionStatus.UNSOLVED;
    }

    public Solution() {
        this.solutionPath = null;
        variableSolution = new HashMap<>();
        variableStructure = new HashMap<>();
        parsed = false;
        engineRunSuccess = true;
        engineMsg = "";
        variableTypes = new HashMap<>();
        solved = SolutionStatus.UNSOLVED;
    }

    public Solution(String solutionPath,String engineMsg,boolean engineRunSuccess) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
        variableStructure = new HashMap<>();
        parsed = false;
        this.engineMsg = engineMsg;
        this.engineRunSuccess = engineRunSuccess;
        variableTypes = new HashMap<>();
        solved = SolutionStatus.UNSOLVED;
    }

    public List<String> getVariableTypes(String identifier) {
        return variableTypes.get(identifier);
    }

    /**
     * Parses only the solution status from the solution file and sets the solved field.
     * This is a lightweight alternative to parseSolution when only the status is needed.
     * @return this Solution object for method chaining
     * @throws IOException if there's an error reading the solution file
     */
    public Solution parseSolutionStatus() throws IOException {
        if(this.solutionPath == null) {
            return this;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(solutionPath))) {
            String line;
            Pattern optimalSolutionPattern = Pattern.compile("solution status: optimal solution found");
            Pattern objectiveValuePattern = Pattern.compile("objective value:\\s+(-?\\d+(\\.\\d+)?|[+|-]infinity)");
            
            while ((line = reader.readLine()) != null) {
                Matcher optimalMatcher = optimalSolutionPattern.matcher(line);
                if (optimalMatcher.find()) {
                    solved = SolutionStatus.OPTIMAL;
                    return this;
                }
                
                // If we find an objective value but haven't found "optimal", it's suboptimal
                Matcher objectiveMatcher = objectiveValuePattern.matcher(line);
                if (objectiveMatcher.find()) {
                    if (solved == SolutionStatus.UNSOLVED) {
                        solved = SolutionStatus.SUBOPTIMAL;
                        return this;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Parses the solution file with context about binary variables.
     * For binary variables, only solutions with value 1 are included.
     * For non-binary variables, all solutions are included.
     * @param model The model containing variable information
     * @param varsToParse Set of variable names to parse
     * @throws IOException if there's an error reading the solution file
     */
    public void parseSolution(ModelInterface model, Set<String> varsToParse) throws IOException {
        if(this.solutionPath == null)
            return;
        variables = model.getVariables(varsToParse);
        for (ModelVariable variable : variables) {
            if (varsToParse.contains(variable.getIdentifier())) {
                variableSolution.put(variable.getIdentifier(), new ArrayList<>());
                variableStructure.put(variable.getIdentifier(), new ArrayList<>());
                variableTypes.put(variable.getIdentifier(), new ArrayList<>());
                for (ModelSet modelSet : variable.getSetDependencies()) {
                    variableTypes.get(variable.getIdentifier()).add(modelSet.getType().toString());
                    for (String typeAsBlockName : modelSet.getType().typeList()) {
                        variableStructure.get(variable.getIdentifier()).add(typeAsBlockName);
                    }
                }
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(solutionPath))) {
            String line;
            boolean solutionSection = false;
            Pattern optimalSolutionPattern = Pattern.compile("solution status: optimal solution found");
            Pattern objectiveValuePattern = Pattern.compile("objective value:\\s+(-?\\d+(\\.\\d+)?|[+|-]infinity)");
            while ((line = reader.readLine()) != null) {
                if (!solutionSection) {
                    // Check for the solved status
                    Matcher optimalMatcher = optimalSolutionPattern.matcher(line);
                    if (optimalMatcher.find()) {
                        solved = SolutionStatus.OPTIMAL;
                    }

                    // Extract objective value
                    Matcher objectiveMatcher = objectiveValuePattern.matcher(line);
                    if (objectiveMatcher.find()) {
                        if(this.solved == SolutionStatus.UNSOLVED)
                            solved = SolutionStatus.SUBOPTIMAL;
                        objectiveValue = Double.parseDouble(objectiveMatcher.group(1));
                        solutionSection = true; // Objective value is defined right before the solution values section
                        parseSolutionValues(reader, varsToParse);
                    }
                }
            }
        }
        parsed=true;
    }

    private void parseSolutionValues(BufferedReader reader, Set<String> varsToParse) throws IOException {
        Pattern variablePattern = Pattern.compile("^(.*?)[ \\t]+(-?\\d+\\.?\\d*)[ \\t]+\\(obj:(-?\\d+\\.?\\d*)\\)");
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher variableMatcher = variablePattern.matcher(line);
            if (variableMatcher.find()) {
                String solution = variableMatcher.group(1);
                Double value = Double.parseDouble(variableMatcher.group(2));
                List<String> splitSolution = new LinkedList<>(Arrays.asList(solution.split("[#$]"))); //need a new array to remove dependence
                String variableIdentifier = splitSolution.getFirst();
                splitSolution.removeFirst();
                
                if (varsToParse.contains(variableIdentifier)) {
                    // Find the corresponding variable to check if it's binary
                    ModelVariable var = null;
                    for (ModelVariable v : variables) {
                        if (v.getIdentifier().equals(variableIdentifier)) {
                            var = v;
                            break;
                        }
                    }
                    
                    // Only add the value if:
                    // 1. It's a non-binary variable (add all values)
                    // 2. It's a binary variable with value > 0
                    if (var != null && (!var.isBinary() || (var.isBinary() && value > 0))) {
                        variableSolution.get(variableIdentifier).add(new Tuple<>(splitSolution, value));
                    }
                }
            }
        }
    }

    /**
     * Parses the solution file without requiring Model context.
     * This method extracts all variables and their values directly from the solution file.
     * @return this Solution object for method chaining
     * @throws IOException if there's an error reading the solution file
     */
    public Solution parseSolution() throws IOException {
        if(this.solutionPath == null) {
            return this;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(solutionPath))) {
            String line;
            Pattern statusPattern = Pattern.compile("solution status: (.*)");
            Pattern objectivePattern = Pattern.compile("objective value:\\s+(-?\\d+(\\.\\d+)?|[+|-]infinity)");
            Pattern variablePattern = Pattern.compile("^(.+?)\\s+(-?\\d+(?:\\.\\d+)?|(?:-)?infinity)\\s+\\(obj:(-?\\d+(?:\\.\\d+)?)\\)");

            // Clear existing data
            variableSolution.clear();
            
            while ((line = reader.readLine()) != null) {
                Matcher statusMatcher = statusPattern.matcher(line);
                Matcher objectiveMatcher = objectivePattern.matcher(line);
                Matcher variableMatcher = variablePattern.matcher(line);

                if (statusMatcher.find()) {
                    String status = statusMatcher.group(1);
                    if (status.contains("optimal solution found")) {
                        solved = SolutionStatus.OPTIMAL;
                    } else if (status.contains("infeasible")) {
                        solved = SolutionStatus.UNSOLVED;
                    } else if (status.contains("interrupted")) {
                        solved = SolutionStatus.SUBOPTIMAL;
                    }
                } 
                else if (objectiveMatcher.find()) {
                    objectiveValue = Double.parseDouble(objectiveMatcher.group(1));
                }
                else if (variableMatcher.find()) {
                    String varName = variableMatcher.group(1).trim();
                    double value = Double.parseDouble(variableMatcher.group(2));
                    double objCoeff = Double.parseDouble(variableMatcher.group(3));
                    
                    // Split variable name by # to separate indices
                    String[] parts = varName.split("#");
                    String baseVarName = parts[0];
                    
                    // Initialize list for this variable if not already present
                    variableSolution.putIfAbsent(baseVarName, new ArrayList<>());
                    
                    // Create list of indices (everything after the base variable name)
                    List<String> indices = new ArrayList<>();
                    if (parts.length > 1) {
                        String[] indexParts = parts[1].split("\\$");
                        indices.addAll(Arrays.asList(indexParts));
                    }
                    
                    // Add all values when parsing without model context
                    variableSolution.get(baseVarName).add(new Tuple<>(indices, value));
                }
            }
            parsed = true;
        }
        return this;
    }

    public boolean parsed(){
        return parsed;
    }

    public SolutionStatus getSolutionStatus() {
        return solved;
    }

    public HashMap<String, List<Tuple<List<String>, Double>>> getVariableSolution() {
        return variableSolution;
    }

    public List<Tuple<List<String>, Double>> getVariableSolution(String identifier) {
        return variableSolution.get(identifier);
    }

    public HashMap<String, List<String>> getVariableStructure() {
        return variableStructure;
    }

    public double getSolvingTime() {
        return solvingTime;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public Collection<ModelVariable> getVariables() {
        return variables;
    }

    public List<String> getVariableStructure(String variableName) {
        return variableStructure.get(variableName);
    }
}
