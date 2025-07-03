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
import Exceptions.ZimpleDataIntegrityException;
import Exceptions.ZimpleCompileException;
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
public class SolverService implements Solver {

    private final boolean DEBUG = true;
    private ScipProcessPool scipProcessPool;
    private final String SOLUTION_FILE_SUFFIX = "SOLUTION";
    private ModelRepository modelRepository;

    public SolverService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.scipProcessPool = new ScipProcessPool(30);
    }

    @Override
    public Solution solve(String fileId, int timeout, String solverScript) throws Exception {
        try {
            return solveAsync(fileId, timeout, solverScript).get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;

                throw new RuntimeException("Unexpected error at solver", cause);
        }
        
    }

    @Override
    public CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript) throws Exception {
        return CompletableFuture.<Solution>supplyAsync(() -> {
            ScipProcess process = null;
            long startTime = System.currentTimeMillis();
            try {
                modelRepository.downloadDocument(fileId);
                System.out.println("Acquiring process | fileId: "+fileId);
                process = scipProcessPool.acquireProcess();
                System.out.println("Process acquired | process: "+process.toString());
                // Configure SCIP settings
                process.solverSettings("set timing reading TRUE");
                process.setTimeLimit(timeout);
                if (!solverScript.isEmpty()) {
                    process.solverSettings(solverScript);
                }
                
                // Read the problem file
                process.read(modelRepository.getLocalStoreDir().resolve(fileId+".zpl").toString());
                
                // Start optimization
                process.optimize();

                // Check if we've already exceeded the timeout
                if (System.currentTimeMillis() - startTime > timeout * 1000L) {
                    return new Solution();
                }

                return getNextSolution(fileId, process);
            } catch (Exception e) {
                // Check if the exception is due to timeout
                if (System.currentTimeMillis() - startTime > timeout * 1000L) {
                    return new Solution();
                }
                throw new CompletionException(e);
            } finally {
                if(process != null){
                    try {
                        scipProcessPool.releaseProcess(process);
                    } catch (Exception e) {
                        // Ignore release errors
                    }
                }
            }
        });
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
                modelRepository.downloadDocument(fileId);
                compilationProcess = scipProcessPool.acquireProcess();
                
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
                        scipProcessPool.releaseProcess(compilationProcess);
                        Thread.sleep(10); // Give extra time for file handles to be released
                        System.gc();
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            }
        });
    }

    private Solution getNextSolution(String id, ScipProcess scipProcess) throws Exception {
        if(DEBUG)
            System.out.println("Waiting for solution status to be updated... | process: "+scipProcess.toString());
        int i = 0;
        long startTime = System.currentTimeMillis();
        while (!scipProcess.getStatus().equals("compilation error")
                && !scipProcess.getStatus().equals("integrity error")
                && !scipProcess.getStatus().equals("solved")
                && !scipProcess.getStatus().equals("paused")) {
            
            // Check for timeout
            if (System.currentTimeMillis() - startTime > (scipProcess.getCurrentTimeLimit() * 1000L)+3000) {
                System.out.println("Timeout while waiting for solution, returning empty solution");
                return new Solution();
            }

            Thread.sleep(70);
            i++;
            if(i % 60 == 0 && DEBUG){
                System.out.println("Waiting for solution status to be updated...  current status: " + scipProcess.getStatus() + " | process: "+scipProcess.toString());
            }
        }
        if(DEBUG)
            System.out.println("Finished waiting, status updated:" + scipProcess.getStatus() + " | process: "+scipProcess.toString());

        if (scipProcess.getStatus().equals("integrity error")) {
            throw new ZimpleDataIntegrityException(scipProcess.getCompilationError());
        } else if (scipProcess.getStatus().equals("compilation error")) {
            throw new ZimpleCompileException(scipProcess.getCompilationError(), 800);
        }
        InputStream solutionStream = scipProcess.getSolution();
        writeSolution(id, SOLUTION_FILE_SUFFIX, solutionStream);
        return new Solution(getSolutionPathToFile(id,SOLUTION_FILE_SUFFIX));
    }

    private String getSolutionPathToFile(String fileId, String suffix) throws Exception {
        return modelRepository.getLocalStoreDir().resolve(fileId+suffix+".zpl").toString();
    }

    private void writeSolution(String fileId, String suffix, InputStream inputStream) throws Exception {
        modelRepository.uploadDocument(fileId + suffix, inputStream);
        // modelRepository.downloadDocument(fileId + suffix);
    }

    public void shutdown() throws Exception{
        scipProcessPool.shutdownAll();
    }
}
