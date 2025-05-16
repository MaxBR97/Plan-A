package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;

public interface StreamSolverServiceInterface extends SolverServiceInterface {
    public String poll();
    public void continueSolve(int timeout);
}
