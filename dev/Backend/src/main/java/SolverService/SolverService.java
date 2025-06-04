package SolverService;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import DataAccess.ModelRepository;
import Model.Solution;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;

@Service
@Profile("!kafka")
public class SolverService implements StreamSolver {

    private ScipProcess scipProcess;
    private final String SOLUTION_FILE_SUFFIX = "SOLUTION";
    private String fileId;
    private ModelRepository modelRepository;
    private boolean continueSolve;

    public SolverService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.continueSolve = false;
    }

    @Override
    public Solution solve(String fileId, int timeout, String solverScript) throws Exception {
        return solveAsync(fileId, timeout, solverScript).get();
    }

    @Override
    public CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript) throws Exception {
        if(continueSolve){
            return continueSolve(timeout);
        }

        if(scipProcess != null){
            finish();
        }
        
        this.fileId = fileId;
        scipProcess = new ScipProcess();
        scipProcess.start();
        
        // Configure SCIP settings
        scipProcess.solverSettings("set timing reading TRUE");
        scipProcess.setTimeLimit(timeout);
        if (!solverScript.isEmpty()) {
            scipProcess.solverSettings(solverScript);
        }
        
        // Read the problem file
        scipProcess.read(modelRepository.getLocalStoreDir().resolve(fileId+".zpl").toString());
        
        // Start optimization
        scipProcess.optimize();

        return getNextSolution(fileId);
    }

    @Override
    public void setContinue(boolean continueSolve) {
        this.continueSolve = continueSolve;
    }

    @Override
    public String isCompiling(String fileId, int timeout) {
        try{
            return isCompilingAsync(fileId, timeout).get();
        } catch(Exception e){
            return "Compilation Error";
        }
    }

    @Override   
    public CompletableFuture<String> isCompilingAsync(String fileId, int timeout) {
        return CompletableFuture.supplyAsync(() -> {
            ScipProcess compilationProcess = null;
            try {
                // Stop any existing process
                if (scipProcess != null) {
                    finish();
                }
                
                // Start new process for compilation check
                compilationProcess = new ScipProcess();
                compilationProcess.start();
                
                // Read the problem file
                String problemFile = modelRepository.getLocalStoreDir().resolve(fileId+".zpl").toString();
                compilationProcess.read(problemFile);
                
                // Check status every 100ms
                String status;
                long startTime = System.currentTimeMillis();
                long timeoutMillis = timeout * 1000L;
                
                while (System.currentTimeMillis() - startTime < timeoutMillis) {
                    status = compilationProcess.getStatus();
                    
                    if ("compilation error".equals(status)) {
                        String errorMsg = compilationProcess.getCompilationError();
                        return errorMsg != null ? errorMsg : "Compilation Error";
                    } else if (!"not started".equals(status) && !"reading".equals(status)) {
                        return "";
                    }
                    
                    Thread.sleep(100);
                }
                
                return "Timeout while checking compilation";
                
            } catch (Exception e) {
                return "Error checking compilation: " + e.getMessage();
            } finally {
                // Ensure process is cleaned up
                if (compilationProcess != null) {
                    try {
                        System.gc();
                        compilationProcess.exit();
                        Thread.sleep(200); // Give extra time for file handles to be released
                        System.gc();
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            }
        });
    }

    @Override
    public String pollLog() {
        List<String> logs = scipProcess.pollLogAll();
        if (logs.isEmpty()) {
            return " \nCurrent Status: " + scipProcess.getStatus();
        }
        return String.join("\n", logs) + " \nCurrent Status: " + scipProcess.getStatus();
    }

    private CompletableFuture<Solution> continueSolve(int extraTime) throws Exception {
        int newTimeLimit = scipProcess.getCurrentTimeLimit() + extraTime;
        scipProcess.setTimeLimit(newTimeLimit);
        scipProcess.optimize();
        return getNextSolution(fileId);
    }   

    private CompletableFuture<Solution> getNextSolution(String id){
        CompletableFuture<Solution> sol = CompletableFuture.supplyAsync(() -> {
            try {
                scipProcess.solverSettings("write solution tmpSolution");
                File tmpFile = new File(Path.of(".","tmpSolution").toString());
                while(!tmpFile.exists()){
                    Thread.sleep(70);
                }
                Thread.sleep(100);
                String tmp = new String(Files.readAllBytes(Path.of(".", "tmpSolution")));
                writeSolution(id, tmp, SOLUTION_FILE_SUFFIX);
                Files.delete(Path.of(".", "tmpSolution"));
                return new Solution(getSolutionPathToFile(SOLUTION_FILE_SUFFIX));
            } catch (Exception e) {
                return null;
            }
        });
        return sol;
    }

    private void finish() throws Exception {
        scipProcess.exit();
    }

    private String getSolutionPathToFile(String suffix) throws Exception {
        return modelRepository.getLocalStoreDir().resolve(fileId+suffix+".zpl").toString();
    }

    private void writeSolution(String fileId, String content, String suffix) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(fileId + suffix, inputStream);
        modelRepository.downloadDocument(fileId + suffix);
    }
}
