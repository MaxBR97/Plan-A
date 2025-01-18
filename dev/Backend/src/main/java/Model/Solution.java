package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class Solution {
    String solutionPath;
    boolean solved;
    /*
    maps variable names to a list of lists with each list holding elements of the solution,
    for example for variable V of type set1 * set2 where set1= {1,2} and set2= {"a","b"}
    an example for a solution is: V -> { [1,"a"], [2, "b"] } note: order matters!
    Since from this point on this is only static data to be shown to the user, only Strings are in use
     */
    HashMap<String, List<List<String>>> variableSolution;
    HashMap<String, List<String>> variableStructure;
    double solvingTime;
    double objectiveValue; // the actual numeric value of the expression that was optimized


    //helper fields
    Collection<ModelVariable> variables;

    public Solution(String solutionPath) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
        variableStructure = new HashMap<>();
    }
    //Implement as lazy call or run during initialization?
    public void ParseSolution(ModelInterface model) throws IOException {
        //init all variables with as keys to an empty list
        variables = model.getVariables();
        for (ModelVariable variable : variables) {
            variableSolution.put(variable.getIdentifier(), new ArrayList<>());
            variableStructure.put(variable.getIdentifier(), new ArrayList<>());
            //TODO: figure out how to extract the structure of a variable
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
                    parseSolutionValues(reader);
                }
            }
        }
    }
    private void parseSolutionValues(BufferedReader reader) throws IOException {
        Pattern variablePattern = Pattern.compile("([a-zA-Z_\\$0-9]+)(#[a-zA-Z0-9\\$#]+(?:\\s+[a-zA-Z0-9\\$#]+)*)\\s+(\\d+)\\s+\\(obj:(\\d+)\\)");
        String line;
        while ((line = reader.readLine()) != null){
            Matcher variableMatcher = variablePattern.matcher(line);
            if (variableMatcher.find()) {
                String variableName = variableMatcher.group(1);
                String values = variableMatcher.group(2);
                int objectiveValue = Integer.parseInt(variableMatcher.group(3));
                if(objectiveValue!=0) {
                    //A 0 objective value means the solution part has no effect on the actual max/min expression
                    List<String> variableValues = new ArrayList<>(Arrays.asList(values.split("[#&$]")));
                    variableValues.removeIf(String::isEmpty); // I hate this, but I hate regex even more. removes empty strings
                    variableSolution.get(variableName).add(variableValues);
                }
            }
        }
    }
}
