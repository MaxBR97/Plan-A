package Unit.Solver;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import Model.Solution;
import Model.Solution.SolutionStatus;
import SolverService.StreamSolver;
import groupId.Main;

@SpringBootTest(classes = Main.class)
@ActiveProfiles({"H2mem","securityAndGateway","streamSolver"})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "app.file.storage-dir=../src/test/Unit/Solver"
})
public class SolverTest {
    
    @Autowired
    private StreamSolver solverService;

    private static final int COMPILATION_TIMEOUT_SECONDS = 2;
    private static final int SOLVING_TIMEOUT_SECONDS = 7;

    @BeforeEach
    void setUp() {
        solverService.setContinue(false);
    }

    // Tests that a non-compiling file is correctly identified
    // Expects isCompiling to return false and solve to return an unsolved solution
    @Test
    void test1_NotCompiling() throws Exception {
        String filePath = "NotCompiling";
        String compilationError = solverService.isCompiling(filePath, COMPILATION_TIMEOUT_SECONDS);
        assertTrue(compilationError.equals("*** Error 133: Unknown symbol \"x\""));
        
        CompletableFuture<String> futureResult = solverService.isCompilingAsync(filePath, COMPILATION_TIMEOUT_SECONDS);
        assertTrue(futureResult.get(COMPILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS).equals("*** Error 133: Unknown symbol \"x\""));

        Solution solution = solverService.solve(filePath, SOLVING_TIMEOUT_SECONDS, "").parseSolution();
        assertEquals(SolutionStatus.UNSOLVED, solution.parseSolutionStatus().getSolutionStatus());
    }

    // Tests that an infeasible problem is correctly identified
    // Expects the problem to compile but return an unsolved solution
    @Test
    void test2_Infeasible() throws Exception {
        String filePath = "Infeasible";
        assertTrue(solverService.isCompiling(filePath, COMPILATION_TIMEOUT_SECONDS).equals(""));
        
        CompletableFuture<String> futureResult = solverService.isCompilingAsync(filePath, COMPILATION_TIMEOUT_SECONDS);
        assertTrue(futureResult.get(COMPILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS).equals(""));

        Solution solution = solverService.solve(filePath, SOLVING_TIMEOUT_SECONDS, "").parseSolution();
        assertEquals(SolutionStatus.UNSOLVED, solution.parseSolutionStatus().getSolutionStatus());
    }

    // Tests that a simple TSP problem can be solved to optimality within the time limit
    // Expects the problem to compile and return an optimal solution
    @Test
    void test3_EasyOptimality() throws Exception {
        String filePath = "EasyOptimality";
        assertTrue(solverService.isCompiling(filePath, COMPILATION_TIMEOUT_SECONDS).equals(""));

        Solution solution = solverService.solve(filePath, SOLVING_TIMEOUT_SECONDS, "").parseSolution();
        assertEquals(SolutionStatus.OPTIMAL, solution.getSolutionStatus());
        assertNotNull(solution.getVariableSolution());
        assertTrue(solution.getObjectiveValue() > 0);
    }

    // Tests that a complex TSP problem with 30 cities returns a suboptimal solution within the time limit
    // Expects the problem to compile but return a suboptimal solution due to time constraints
    @Test
    void test4_HardOptimality() throws Exception {
        String filePath = "HardOptimality";
        assertTrue(solverService.isCompiling(filePath, COMPILATION_TIMEOUT_SECONDS).equals(""));

        Solution solution = solverService.solve(filePath, SOLVING_TIMEOUT_SECONDS, "").parseSolution();
        assertEquals(SolutionStatus.SUBOPTIMAL, solution.getSolutionStatus());
        assertNotNull(solution.getVariableSolution());
        assertTrue(solution.getObjectiveValue() > 0);
    }

    // Tests async solving capabilities for the easy TSP problem
    // Ensures async solving works correctly and returns the same results as sync solving
    @Test
    void test5_AsyncSolving() throws Exception {
        String filePath = "EasyOptimality";
        
        CompletableFuture<Solution> futureSolution = solverService.solveAsync(filePath, SOLVING_TIMEOUT_SECONDS, "");
        Solution solution = futureSolution.get(SOLVING_TIMEOUT_SECONDS + 2, TimeUnit.SECONDS).parseSolution();
        
        assertEquals(SolutionStatus.OPTIMAL, solution.getSolutionStatus());
        assertNotNull(solution.getVariableSolution());
        assertTrue(solution.getObjectiveValue() > 0);
    }

    // Tests that the solver can be interrupted and continued
    // Verifies that solving can be paused and resumed without losing progress
    // and that the solution improves (objective value decreases)
    @Test
    void test6_ContinueSolving() throws Exception {
        String filePath = "HardOptimality";
        
        // Start solving
        CompletableFuture<Solution> futureSolution = solverService.solveAsync(filePath, 3, "");
        Solution firstSolution = futureSolution.get(20, TimeUnit.SECONDS).parseSolution();
        double firstObjectiveValue = firstSolution.getObjectiveValue();
        
        // Continue solving
        solverService.setContinue(true);
        CompletableFuture<Solution> continuedSolution = solverService.solveAsync(filePath, SOLVING_TIMEOUT_SECONDS, "");
        Solution secondSolution = continuedSolution.get(SOLVING_TIMEOUT_SECONDS, TimeUnit.SECONDS).parseSolution();
        double secondObjectiveValue = secondSolution.getObjectiveValue();
        
        // Verify solution improved
        assertNotNull(secondSolution.getVariableSolution());
        assertTrue(secondObjectiveValue > 0, "Second solution should have positive objective value");
        assertTrue(secondObjectiveValue < firstObjectiveValue, 
            String.format("Solution should improve: first=%.2f, second=%.2f", 
                firstObjectiveValue, secondObjectiveValue));
    }

    // Tests that solver output can be polled and contains expected information
    // Verifies that the log output contains relevant solving information
    @Test
    void test7_PollOutput() throws Exception {
        String filePath = "HardOptimality";
        
        CompletableFuture<Solution> futureSolution = solverService.solveAsync(filePath, SOLVING_TIMEOUT_SECONDS, "");
        
        boolean foundReadingLog = false;
        boolean foundSolvingLog = false;
        
        // Poll logs for 5 seconds or until we find what we're looking for
        long endTime = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < endTime) {
            String log = solverService.pollLog();
            if (log != null) {
                if (log.contains("reading")) foundReadingLog = true;
                if (log.contains("solving")) foundSolvingLog = true;
            }
            if (foundReadingLog && foundSolvingLog) break;
            Thread.sleep(100);
        }
        
        assertTrue(foundReadingLog, "Should find reading-related log output");
        assertTrue(foundSolvingLog, "Should find solving-related log output");
        
        // Clean up
        futureSolution.get(SOLVING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
