package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;

public interface SolverServiceInterface {
    public SolutionDTO solve(String zplFile, boolean continueLast) throws Exception;
    public void solveAsync(String zplFile, boolean continueLast) throws Exception;
    public boolean isCompiling(String zplFile);
}
