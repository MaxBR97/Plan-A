package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import java.util.concurrent.CompletableFuture;
import Model.Solution;

public interface StreamSolver extends Solver {
    public void setContinue(boolean continueSolve);
    public String pollLog();
}
