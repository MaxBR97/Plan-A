package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class Solution {
    final String engineMsg;
    final boolean engineRunSuccess;
    boolean parsed;
    final String solutionPath;
    boolean solved;
    /*
    maps variable names to a list of lists with each list holding elements of the solution,
    for example for variable V of type set1 * set2 where set1= {1,2} and set2= {"a","b"}
    an example for a solution is: V -> { [1,"a"], [2, "b"] } note: order matters!
    Since from this point on this is only static data to be shown to the user, only Strings are in use
     */
    final HashMap<String, List<List<String>>> variableSolution;
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
    }
    public Solution(String solutionPath,String engineMsg,boolean engineRunSuccess) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
        variableStructure = new HashMap<>();
        parsed = false;
        this.engineMsg = engineMsg;
        this.engineRunSuccess = engineRunSuccess;
        variableTypes = new HashMap<>();
    }

    public List<String> getVariableTypes(String identifier) {
        return variableTypes.get(identifier);
    }

    //Implement as lazy call or run during initialization?
    public void ParseSolution(ModelInterface model, Set<String> varsToParse) throws IOException {
        variables = model.getVariables();
        for (ModelVariable variable : variables) {
            if (varsToParse.contains(variable.getIdentifier())) {
                variableSolution.put(variable.getIdentifier(), new ArrayList<>());
                variableStructure.put(variable.getIdentifier(), new ArrayList<>());
                variableTypes.put(variable.getIdentifier(), new ArrayList<>());
                //below lines are not solution dependent but problem dependent, will be more efficient to maintain them inside the image
                for (ModelSet modelSet : variable.getDependencies()) {
                    variableTypes.get(variable.getIdentifier()).add(modelSet.getType().toString());
                    for (ModelInput.StructureBlock block : modelSet.getStructure()) {
                        variableStructure.get(variable.getIdentifier()).add(block.dependency.identifier);

                    }
                }
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(solutionPath))) {
            String line;
            boolean solutionSection = false;

            Pattern statusPattern = Pattern.compile("SCIP Status\s+:\s+problem is solved.*optimal solution found");
            Pattern solvingTimePattern = Pattern.compile("Solving Time \\(sec\\)\s+:\s+(\\d+\\.\\d+)");
            Pattern objectiveValuePattern = Pattern.compile("objective value:\\s+(\\d+(\\.\\d+)?)");
            while ((line = reader.readLine()) != null) {
                if (!solutionSection) {
                    // Check for the solved status
                    Matcher statusMatcher = statusPattern.matcher(line);
                    if (statusMatcher.find()) {
                        solved = true;
                    }
                    // Extract solving time
                    Matcher solvingTimeMatcher = solvingTimePattern.matcher(line);
                    if (solvingTimeMatcher.find()) {
                        solvingTime = Double.parseDouble(solvingTimeMatcher.group(1));
                    }
                    // Extract objective value
                    Matcher objectiveMatcher = objectiveValuePattern.matcher(line);
                    if (objectiveMatcher.find()) {
                        objectiveValue = Double.parseDouble(objectiveMatcher.group(1));
                        solutionSection = true; // Objective value is defined right before the solution values section
                    }
                } else {
                    parseSolutionValues(reader,varsToParse);
                }
            }
        }
        parsed=true;
    }
    private void parseSolutionValues(BufferedReader reader, Set<String> varsToParse) throws IOException {
        Pattern variablePattern = Pattern.compile("([a-zA-Z_\\$0-9]+)(#[a-zA-Z0-9\\$#]+(?:\\s+[a-zA-Z0-9\\$#]+)*)\\s+(\\d+)\\s+\\(obj:(\\d+)\\)");
        String line;
        while ((line = reader.readLine()) != null){
            Matcher variableMatcher = variablePattern.matcher(line);
            if (variableMatcher.find()) {
                String variableName = variableMatcher.group(1);
                String values = variableMatcher.group(2);
                int objectiveValue = Integer.parseInt(variableMatcher.group(3));
                if(varsToParse.contains(variableName) && objectiveValue!=0) {
                    //A 0 objective value means the solution part has no effect on the actual max/min expression
                    List<String> variableValues = new ArrayList<>(Arrays.asList(values.split("[#&$]"))); //need a new array to remove dependence
                    variableValues.removeIf(String::isEmpty); // I hate this, but I hate regex even more. removes empty strings
                    variableSolution.get(variableName).add(variableValues);
                }
            }
        }
    }
    public boolean parsed(){
        return parsed;
    }

    public boolean isSolved() {
        return solved;
    }

    public HashMap<String, List<List<String>>> getVariableSolution() {
        return variableSolution;
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
}
