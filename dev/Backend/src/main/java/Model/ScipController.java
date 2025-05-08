package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ScipController {

    private Process scipProcess;
    private ProcessBuilder processBuilder;
    private List<String> startupCommand;
    private BufferedWriter processInput;

    //consider StringBuffer instead
    private StringBuilder outputBuffer;
    
    /* statuses: 
     * "not started" -  process not started/problem not read. starting status
     * "reading" - reading problem
     * "problem read" - problem read, but no started solving\presolving
     * "presolving" - in presolving process
     * "solving" - post presolving. identfied by not being in "paused" or "solved" status, and presolving already occoured.
     * "paused" - solving process is interrupted
     * "solved" - solving process came to a conclusion (infeasible/found optimal)
     * 
     * TODO: make an Enum for this instead of holding a status in a string
    */
    private String processStatus = "not started";
    Pattern readingPattern = Pattern.compile("^read problem <.*>$");
    Pattern problemReadPattern = Pattern.compile("^original problem has \\d+ variables (\\d+ bin, \\d+ int, \\d+ impl, \\d+ cont) and \\d+ constraints$");
    Pattern presolvingPattern = Pattern.compile("^presolving:$");
    Pattern finishedPresolvingPattern = Pattern.compile("^Presolving Time: .*$");
    Pattern pausedPattern = Pattern.compile("SCIP Status\s+:\s+solving was interrupted");
    Pattern solvedPattern = Pattern.compile("SCIP Status\s+:\s+problem is solved");

    public ScipController(String problemSourceFile){
        ProcessBuilder processBuilder = new ProcessBuilder(
            
        );
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        startupCommand = new LinkedList<String>(List.of("scip", "-c",
                                                        " set timing reading TRUE ",
                                                        " read " + problemSourceFile
                                                        ));
        outputBuffer = new StringBuilder();
    }

    // sets solver settings by piping into it a short settings script
    public void setStartupSolverSettings(String solverScript){
        startupCommand.add(solverScript);
    }

    // starts reading the problem
    public void start() throws Exception { 
        processBuilder.command(startupCommand);
        scipProcess = processBuilder.start();
    }

    //waits for the process to end
    public int waitForFinish() throws Exception {
        return scipProcess.waitFor();
    }

    // Sends a ctrl+c
    public void sendSigint() throws Exception {
        if (scipProcess != null && scipProcess.isAlive()) {
            // Send SIGINT signal (Ctrl+C)
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processInput.write("\u0003"); // Ctrl+C character
                processInput.flush();
            } else {
                Process interruptProcess = new ProcessBuilder("kill -SIGINT " + scipProcess.pid()).start();
                try {
                    interruptProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Pipes the string into the stdin of the solving process
    public void pipeInput(String in) throws Exception{
        processInput.append(in);
        processInput.flush();
    }
    
    // polls the output of scipProcess - starting from the first unread line.
    // the returned String must be up to a new line.
    // The remaining part of a line will be left for the next call of this method.
    public String pollLog(){
        return null;
    }

    public String getStatus(){
        return this.processStatus;
    }

    /*
    *  What this method does is it wait for the scipProcess to be available for user input,
    *  once it is, it parses the solution.
    *  if the process displays "no solution available" - the solution will be set to null.
    */
    public CompletableFuture<Solution> getSolution(){
        return null;
    } 
}
