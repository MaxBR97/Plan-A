package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jna.platform.win32.Wincon.CTRL_C_EVENT;

import Utils.*;

public class ScipController {

    private Process scipProcess;
    private ProcessBuilder processBuilder;
    private List<String> startupCommand;
    private BufferedWriter processInput;
    private BufferedReader processOutput;

    
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
    Pattern pausedPattern = Pattern.compile("SCIP Status\\s+:\\s+solving was interrupted");
    Pattern solvedPattern = Pattern.compile("SCIP Status\\s+:\\s+problem is solved");
    private String solverSettings;
    private int timeout;

    public ScipController(int timeout , String problemSourceFile){
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        
        this.timeout = timeout;

        // Join the commands into a single string
        String commandString = String.join(" ", List.of(
            " set timing reading TRUE ",
            " read " + problemSourceFile + " "
        ));
        
        startupCommand = new LinkedList<>(List.of("scip", "-c", commandString));

        this.solverSettings = "";
    }
    
    // sets solver settings by piping into it a short settings script
    public void setStartupSolverSettings(String solverScript) {
        this.solverSettings =  new String(solverScript);
    }
    
    // starts reading the problem
    public void start() throws Exception {
        // Construct full command string with optional solverSettings
        String fullCommand = startupCommand.get(2) + solverSettings + " set limit time " + timeout + " optimize ";
        System.out.println("FULL SCIP COMMAND: " + fullCommand);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
        
            processBuilder.command("scip", "-c", fullCommand );
            
        } else {
            processBuilder.command("scip", "-c", fullCommand);
        }
    
        scipProcess = processBuilder.start();
        processInput = new BufferedWriter(new OutputStreamWriter(scipProcess.getOutputStream()));
        processOutput = new BufferedReader(new InputStreamReader(scipProcess.getInputStream()));
    }

    public int getTimeout(){
        return this.timeout;
    }
    
    public void setTimeout(int newTimeout){
        this.timeout = newTimeout;
    }
    
    // Pipes the string into the stdin of the solving process
    public void pipeInput(String in) throws Exception{
        processInput.write(in + "\n"); // the linefeed is crucial
        processInput.flush();
    }
    
    // polls the output of scipProcess - starting from the first unread line.
    // the returned String must be up to a new line.
    // The remaining part of a line will be left for the next call of this method.
    //TODO: make a pollLog which polls all available logs at once, instead of one line.
    public String pollLog() {
        try {
            if (processOutput.ready()) {
                String line = processOutput.readLine();
                if (line != null) {
                    updateStatus(line);
                    return line + "\n";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopProcess() throws Exception{
        scipProcess.destroyForcibly();
        scipProcess.waitFor();
    }

    public void setStatus(String status){
        this.processStatus = status;
    }

    private void updateStatus(String line) {
        Matcher readingMatcher = readingPattern.matcher(line);
        Matcher problemReadMatcher = problemReadPattern.matcher(line);
        Matcher presolvingMatcher = presolvingPattern.matcher(line);
        Matcher finishedPresolvingMatcher = finishedPresolvingPattern.matcher(line);
        Matcher pausedMatcher = pausedPattern.matcher(line);
        Matcher solvedMatcher = solvedPattern.matcher(line);

        if (readingMatcher.find()) {
            processStatus = "reading";
        } else if (problemReadMatcher.find()) {
            processStatus = "problem read";
        } else if (presolvingMatcher.find()) {
            processStatus = "presolving";
        } else if (finishedPresolvingMatcher.find()) {
            processStatus = "solving";
        } else if (pausedMatcher.find()) {
            processStatus = "paused";
        } else if (solvedMatcher.find()) {
            processStatus = "solved";
        }
    }

    public String getStatus(){
        return this.processStatus;
    }

    // Flag to track if we've already captured a solution
    private boolean solutionCaptured = false;
    
    /*
    *  What this method does is it wait for the scipProcess to be in "paused" or "solved" status,
    *  then waits 50ms and parses the solution that should already be available.
    *  if the process displays "no solution available" - the solution will be set to null.
    *  This method uses its own reading mechanism to not interfere with pollLog.
    */
    public CompletableFuture<Solution> getSolution() {
        return CompletableFuture.supplyAsync(() -> {
            // try {
            //     // Only proceed if we haven't already captured a solution
            //     // if (solutionCaptured) {
            //     //     return null;
            //     // }
                
            //     // Wait until the process status indicates we should have a solution
            //     while (!processStatus.equals("solved") && !processStatus.equals("paused")) {
            //         if (!scipProcess.isAlive()) {
            //             return null;
            //         }
            //         Thread.sleep(100);
            //     }

            //     // Wait a short time for the solution to be fully available
            //     Thread.sleep(50);
                

               
            // } catch(Exception e){}
            return null;
        });
    }
}