package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import java.util.concurrent.CompletableFuture;
import Model.Solution;

public interface Solver {
    public Solution solve(String zplFile, int timeout, String solverScript) throws Exception;
    public CompletableFuture<Solution> solveAsync(String zplFile, int timeout, String solverScript) throws Exception;
    public boolean isCompiling(String zplFile);
    public CompletableFuture<Boolean> isCompilingAsync(String zplFile);
}
