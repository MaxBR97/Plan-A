package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class Solution {
    String solutionPath;
    boolean solved;
    HashMap<String, List<ModelInput>> variableSolution;
    double solvingTime;
    double objectiveValue;
    public Solution(String solutionPath) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
    }
    //Implement as lazy call or run during initialization?
    public void ParseSolution(ModelInterface model) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(solutionPath))) {
            String line;
            boolean solutionSection = false;

            Pattern statusPattern = Pattern.compile("SCIP Status\s+:\s+problem is solved.*optimal solution found");
            Pattern solvingTimePattern = Pattern.compile("Solving Time \\(sec\\)\s+:\s+(\\d+\\.\\d+)");
            Pattern objectiveValuePattern = Pattern.compile("objective value:\s+(\\d+\\.\\d+)");
            Pattern variablePattern = Pattern.compile("([a-zA-Z_\\$0-9]+)(#[a-zA-Z0-9\\$#]+(?:\\s+[a-zA-Z0-9\\$#]+)*)\\s+(\\d+)\\s+\\(obj:(\\d+)\\)");
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
                    }

                    // Start parsing variables after the "objective value" line
                    if (line.startsWith("objective value")) {
                        solutionSection = true;
                    }
                } else {
                    // Parse variables and their values
                    Matcher variableMatcher = variablePattern.matcher(line);
                    if (variableMatcher.find()) {
                        String variableName = variableMatcher.group(1);
                        String values = variableMatcher.group(2);
                        String objective = variableMatcher.group(3);
//                        if (value == 1) { // Only include variables with a value of 1
//                            ModelInput input = mapVariableToModelInput(variableName, model);
//                            if (input != null) {
//                                variableSolution.computeIfAbsent(variableName, k -> new ArrayList<>()).add(input);
//                            }
                  //      }
                    }
                }
            }
        }
    }

    private ModelInput mapVariableToModelInput(String variableName, ModelInterface model) {
        // Extract variable structure based on the model
        for (ModelVariable var : model.getVariables()) {
            //TODO: THIS IS FOR DEBUG
            ModelSet set = var.getDependencies().getFirst();
            ModelInput.StructureBlock[] structure = set.getStructure();

            String[] components = variableName.split("#");
            if (components.length == structure.length) {
                boolean matches = true;
                for (int i = 0; i < structure.length; i++) {
                    String component = components[i];
                    String expected = structure[i].dependency.getIdentifier();
                    if (!component.startsWith(expected)) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return new ModelSet(variableName,new Tuple(), List.of(set), List.of());
                }
            }
        }
        return null;
    }
}
