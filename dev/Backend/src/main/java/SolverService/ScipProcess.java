package SolverService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import Model.Solution;

import static com.sun.jna.platform.win32.Wincon.CTRL_C_EVENT;

import Utils.*;

public class ScipProcess {
    private static final int BUFFER_SIZE = 10000; // Number of lines to keep

    private Process scipProcess;
    private ProcessBuilder processBuilder;
    private List<String> startupCommand;
    private BufferedWriter processInput;
    private BlockingQueue<String> circularBuffer;
    private Thread readerThread;
    private volatile boolean isRunning;

    
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
    private final Pattern readingPattern = Pattern.compile("^read problem <.*>$");
    private final Pattern problemReadPattern = Pattern.compile("^original problem has \\d+ variables (\\d+ bin, \\d+ int, \\d+ impl, \\d+ cont) and \\d+ constraints$");
    private final Pattern presolvingPattern = Pattern.compile("^presolving:$");
    private final Pattern finishedPresolvingPattern = Pattern.compile("^Presolving Time: .*$");
    private final Pattern pausedPattern = Pattern.compile("SCIP Status\\s+:\\s+solving was interrupted");
    private final Pattern solvedPattern = Pattern.compile("SCIP Status\\s+:\\s+problem is solved");
    private String solverSettings;
    private int timeout;

    public ScipProcess(int timeout, String problemSourceFile) {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        
        this.timeout = timeout;
        this.circularBuffer = new ArrayBlockingQueue<>(BUFFER_SIZE);
        
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
        String fullCommand = startupCommand.get(2) + solverSettings + " set limit time " + timeout + " optimize ";
        System.out.println("FULL SCIP COMMAND: " + fullCommand);
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("scip", "-c", fullCommand);
        } else {
            processBuilder.command("scip", "-c", fullCommand);
        }
    
        scipProcess = processBuilder.start();
        processInput = new BufferedWriter(new OutputStreamWriter(scipProcess.getOutputStream()));
        
        // Start reading from process output
        isRunning = true;
        readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(scipProcess.getInputStream()))) {
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    // If buffer is full, remove oldest line
                    if (circularBuffer.remainingCapacity() == 0) {
                        circularBuffer.poll();
                    }
                    circularBuffer.offer(line);
                    updateStatus(line);
                }
            } catch (Exception e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
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
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = circularBuffer.poll()) != null) {
            result.append(line).append("\n");
        }
        return result.length() > 0 ? result.toString() : null;
    }

    public void stopProcess() throws Exception {
        isRunning = false;
        if (readerThread != null) {
            readerThread.interrupt();
        }
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
    
}