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

@Service
public class SolverService implements StreamSolver {

    private ScipProcess scipProcess;
    private final String SOLUTION_FILE_SUFFIX = "solution";
    private String id;
    @Autowired
    private ModelRepository modelRepository;

    public SolverService() {
    }

    @Override
    public Solution solve(String zplFile, int timeout, String solverScript) throws Exception {
        return solveAsync(zplFile, timeout, solverScript).get();
    }

    @Override
    public CompletableFuture<Solution> solveAsync(String id, int timeout, String solverScript) throws Exception {
        if(scipProcess != null){
            finish();
        }
        this.id = id;
        scipProcess = new ScipProcess(timeout, modelRepository.getLocalStoreDir().resolve(id+".zpl").toString());
        scipProcess.setStartupSolverSettings(solverScript);
        scipProcess.start();
        
        return getNextSolution(id);
    }

    @Override
    public boolean isCompiling(String zplFile) {
        try{
            return  isCompilingAsync(zplFile).get();
        } catch(Exception e){
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> isCompilingAsync(String zplFile) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public String pollLog() {
        String ans = "";
        String line;
        while((line = scipProcess.pollLog()) != null){
            ans += line + "\n";
        }
        return ans + " \nCurrent Status: " + scipProcess.getStatus();
    }

    @Override
    public CompletableFuture<Solution> continueSolve(int extraTime) throws Exception  {
        int newTimeout = scipProcess.getTimeout() + extraTime;
        scipProcess.setTimeout(newTimeout);
        scipProcess.pipeInput("set limit time " + newTimeout + " optimize ");
        scipProcess.setStatus("solving");
        return getNextSolution(id);
    }   

    private CompletableFuture<Solution> getNextSolution(String id){
        CompletableFuture<Solution> sol = CompletableFuture.supplyAsync(() -> {
            try {
                scipProcess.pipeInput(" write solution tmpSolution ");
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

    private void finish () throws Exception {
        scipProcess.stopProcess();
    }

    private String getSolutionPathToFile(String suffix) throws Exception {
        return modelRepository.getLocalStoreDir().resolve(id+suffix+".zpl").toString();
    }

    private void writeSolution(String id, String content, String suffix) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(id + suffix, inputStream);
        modelRepository.downloadDocument(id + suffix);
    }

}
