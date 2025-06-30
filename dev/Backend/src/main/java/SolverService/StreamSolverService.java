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
import Exceptions.*;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
public class StreamSolverService implements StreamSolver {

    private final boolean DEBUG = true;
    private ScipProcess scipProcess;
    private final String SOLUTION_FILE_SUFFIX = "SOLUTION";
    private String fileId;
    private ModelRepository modelRepository;
    private boolean continueSolve;

    public StreamSolverService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.continueSolve = false;
    }

    @Override
    public Solution solve(String fileId, int timeout, String solverScript) throws Exception {
        try {
            return solveAsync(fileId, timeout, solverScript).get();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) 
                
            throw (Exception) cause;
            throw new RuntimeException("Unexpected error at solver", cause);
        }
        
    }

    @Override
    public CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript) throws Exception {
        if(continueSolve){
            return continueSolve(timeout);
        }

        if(scipProcess != null){
            finish();
        }

        long startTime = System.currentTimeMillis();
        this.fileId = fileId;
        scipProcess = new ScipProcess();
        scipProcess.start();
        if (DEBUG)
            System.out.println("Scip process started" + " | time: "+ (System.currentTimeMillis() - startTime));
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

        // Check if we've already exceeded the timeout
        if (System.currentTimeMillis() - startTime > timeout * 1000L) {
            return CompletableFuture.completedFuture(new Solution());
        }

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
                
                String status;
                long startTime = System.currentTimeMillis();
                long timeoutMillis = timeout * 1000L;
                
                while (System.currentTimeMillis() - startTime < timeoutMillis) {
                    status = compilationProcess.getStatus();
                    
                    if ("compilation error".equals(status) || "integrity error".equals(status)) {
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
                        Thread.sleep(10); // Give extra time for file handles to be released
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
        if(scipProcess != null){
            return scipProcess.getProgressQuote();
        }
        return "";
    }


    private CompletableFuture<Solution> continueSolve(int extraTime) throws Exception {
        int newTimeLimit = scipProcess.getCurrentTimeLimit() + extraTime;
        scipProcess.setTimeLimit(newTimeLimit);
        scipProcess.optimize();
        return getNextSolution(fileId);
    }   

    private CompletableFuture<Solution> getNextSolution(String id) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                int scipProcessPid = Integer.parseInt(scipProcess.getPid());
                if(DEBUG)
                    System.out.println("Waiting for solution status to be updated...");
                int i = 0;
                while (scipProcess != null && scipProcessPid == Integer.parseInt(scipProcess.getPid()) 
                        && !scipProcess.getStatus().equals("compilation error")
                        && !scipProcess.getStatus().equals("integrity error")
                        && !scipProcess.getStatus().equals("solved")
                        && !scipProcess.getStatus().equals("paused")) {
                    
                    // Check for timeout
                    if (System.currentTimeMillis() - startTime > (scipProcess.getCurrentTimeLimit() * 1000L)+2000) {
                        System.out.println("Timeout while waiting for solution, returning empty solution");
                        return new Solution();
                    }

                    Thread.sleep(70);
                    i++;
                    if(DEBUG && i % 60 == 0){
                        System.out.println("Waiting for solution status to be updated...  current status: " + scipProcess.getStatus());
                    }
                }
                if(DEBUG)
                    System.out.println("Finished waiting, status updated:" + scipProcess.getStatus() + " | time: "+ (System.currentTimeMillis() - startTime));
                if (scipProcess == null || scipProcessPid != Integer.parseInt(scipProcess.getPid())) {
                    throw new BadRequestException("solving process was preempted");
                }
                else if (scipProcess.getStatus().equals("integrity error")) {
                    throw new ZimpleDataIntegrityException(scipProcess.getCompilationError());
                } else if (scipProcess.getStatus().equals("compilation error")) {
                    throw new ZimpleCompileException(scipProcess.getCompilationError(), 800);
                } 
                if(DEBUG)
                    System.out.println("Attempting to write solution to tmpSolution" + " | time: "+ (System.currentTimeMillis() - startTime));
                InputStream solutionStream = scipProcess.getSolution();
                writeSolution(id, SOLUTION_FILE_SUFFIX, solutionStream);
                if(DEBUG)
                    System.out.println("Solution uploaded to repo, deleting tmpSolution" + " | time: "+ (System.currentTimeMillis() - startTime));
                return new Solution(getSolutionPathToFile(SOLUTION_FILE_SUFFIX));
            } catch (Exception e) {
                // Check if the exception is due to timeout
                if (System.currentTimeMillis() - startTime > scipProcess.getCurrentTimeLimit() * 1000L) {
                    return new Solution();
                }
                System.err.println("Error getting solution: " + e.getMessage());
                throw new CompletionException(e);
            }
        });
    }
    
    private void finish() throws Exception {
        scipProcess.exit();
    }

    private String getSolutionPathToFile(String suffix) throws Exception {
        return modelRepository.getLocalStoreDir().resolve(fileId+suffix+".zpl").toString();
    }

    private void writeSolution(String fileId, String suffix, InputStream inputStream) throws Exception {
        modelRepository.uploadDocument(fileId + suffix, inputStream);
        // modelRepository.downloadDocument(fileId + suffix);
    }

    public void shutdown() throws Exception{
        finish();
    }
}