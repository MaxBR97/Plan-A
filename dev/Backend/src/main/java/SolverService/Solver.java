package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import java.util.concurrent.CompletableFuture;
import Model.Solution;

public interface Solver {
    public Solution solve(String fileId, int timeout, String solverScript) throws Exception;
    public CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript) throws Exception;
    public String isCompiling(String fileId, int timeout) throws Exception;
    public CompletableFuture<String> isCompilingAsync(String fileId, int timeout) throws Exception;
    public void shutdown() throws Exception;
}
