package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import java.util.concurrent.CompletableFuture;
import Model.Solution;

public interface StreamSolver extends Solver {
    public String pollLog();
    public CompletableFuture<Solution> continueSolve(int timeout) throws Exception;
}
