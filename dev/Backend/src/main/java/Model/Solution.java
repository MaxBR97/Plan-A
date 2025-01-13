package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Solution {
    String solutionPath;
    HashMap<String, List<ModelInput>> variableSolution;
    double solvingTime;
    double objectiveValue;
    public Solution(String solutionPath) {
        this.solutionPath = solutionPath;
        variableSolution = new HashMap<>();
    }
    //Implement as lazy call or run during initialization?
    public void ParseSolution(ModelInterface model) throws IOException {
        Collection<ModelVariable> variables= model.getVariables();
        String line;
        boolean parsingVariables = false;
        BufferedReader br = new BufferedReader(new FileReader(solutionPath));
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("objective value")) {
                objectiveValue = Double.parseDouble(line.split(":")[1].trim());
            } else if (line.matches("^[a-zA-Z_\\$]+.*")) {
                parsingVariables = true;
            }

            if (parsingVariables && line.matches("^[a-zA-Z_\\$]+.*")) {
                String[] parts = line.split("\\s+");
                String variable = parts[0];
                double value = Double.parseDouble(parts[1]);
           //     variables.put(variable, value);
            }
        }
    }
}
