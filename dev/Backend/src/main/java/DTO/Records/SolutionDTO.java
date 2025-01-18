package DTO.Records;

import java.util.HashMap;
import java.util.List;

/**
 * @param variableSolution maps variable names to a list of lists with each list holding elements of the solution,
 *                         for example for variable V of type set1 * set2 where set1= {1,2} and set2= {"a","b"}
 *                         an example for a solution is: V -> { [1,"a"], [2, "b"] } note: order matters!
 *                         Since from this point on this is only static data to be shown to the user, only Strings are in use
 * @param variableStructure structure of all visible variables, maps their name to a list of set names. note: order matters!
 * @param solvingTime time it took the engine to solve the problem
 * @param solved false if there is no solution for any reason (compilation/infeasibility),
 *              maps may hold trash/incomplete values in case of false.
 * @param objectiveValue   the actual numeric value of the expression that was optimized
 */
public record SolutionDTO(boolean solved, HashMap<String, List<List<String>>> variableSolution,
                          HashMap<String, List<String>> variableStructure, double solvingTime, double objectiveValue) {
}
