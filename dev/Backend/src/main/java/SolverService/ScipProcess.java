package SolverService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
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
    private final boolean DEBUG = true;
    private Process scipProcess;
    private ProcessBuilder processBuilder;
    private BufferedWriter processInput;
    private BlockingQueue<String> circularBuffer;
    private Thread readerThread;
    private volatile boolean isRunning;
    private int currentTimeLimit;
    private String compilationErrorMessage;
    
    /* statuses: 
     * "not started" -  process not started/problem not read. starting status
     * "reading" - reading problem
     * "compilation error" - compilation error is found
     * "integrity error" - integrity error (error 900) is found
     * "problem read" - problem read, but no started solving\presolving
     * "presolving" - in presolving process
     * "solving" - post presolving. identfied by not being in "paused" or "solved" status, and presolving already occoured.
     * "paused" - solving process is interrupted
     * "solved" - solving process came to a conclusion (infeasible/found optimal)
     */
    private String processStatus = "not started";
    
    private final Pattern readingPattern = Pattern.compile("^read problem <.*>$");
    private final Pattern compilationErrorPattern = Pattern.compile("^\\*\\*\\* Error (\\d+):.*$");
    private final Pattern fileErrorPattern = Pattern.compile("^\\*\\*\\* File.*$");
    private final Pattern problemReadPattern = Pattern.compile("^original problem has \\d+ variables \\(\\d+ bin, \\d+ int, \\d+ impl, \\d+ cont\\) and \\d+ constraints$");
    private final Pattern presolvingPattern = Pattern.compile("^presolving:$");
    private final Pattern finishedPresolvingPattern = Pattern.compile("^Presolving Time: .*$");
    private final Pattern pausedPattern = Pattern.compile("SCIP Status\\s+:\\s+solving was interrupted");
    private final Pattern solvedPattern = Pattern.compile("SCIP Status\\s+:\\s+problem is solved");

    public ScipProcess() {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        this.circularBuffer = new ArrayBlockingQueue<>(BUFFER_SIZE);
        this.currentTimeLimit = 0;
        this.compilationErrorMessage = null;
    }

    public void start() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("scip");
        } else {
            processBuilder.command("scip");
        }
        
        scipProcess = processBuilder.start();
        processInput = new BufferedWriter(new OutputStreamWriter(scipProcess.getOutputStream()));
        
        // Start reading from process output
        isRunning = true;
        readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(scipProcess.getInputStream()))) {
                String line = "";
                String lastLine = "";
                while (isRunning && (lastLine = reader.readLine()) != null) {
                    String tmp = line;
                    line = lastLine;
                    lastLine = tmp;
                    // If buffer is full, remove oldest line
                    if (circularBuffer.remainingCapacity() == 0) {
                        circularBuffer.poll();
                    }
                    circularBuffer.offer(line);
                    updateStatus(line);
                    if(processStatus.equals("integrity error") && compilationErrorMessage == null){
                        String nextLine = reader.readLine();
                        if(fileErrorPattern.matcher(nextLine).find()){
                            compilationErrorMessage = lastLine;
                        } else {
                            compilationErrorMessage = lastLine + "\n" + nextLine;
                        }
                        if (circularBuffer.remainingCapacity() == 0) {
                            circularBuffer.poll();
                        }
                        circularBuffer.offer(nextLine);
                        lastLine = line;
                        line = nextLine;
                    }
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

    public void read(String file) throws Exception {
        pipeInput("read " + file);
    }

    public void solverSettings(String settings) throws Exception {
        pipeInput(settings);
    }

    public void setTimeLimit(int seconds) throws Exception {
        this.currentTimeLimit = seconds;
        pipeInput("set limit time " + seconds);
    }

    public int getCurrentTimeLimit() {
        return this.currentTimeLimit;
    }

    public void optimize() throws Exception {
        pipeInput("set write printzeros TRUE optimize");
    }

    public String getStatus() {
        return processStatus;
    }

    public String getCompilationError() {
        return compilationErrorMessage;
    }

    public void exit() throws Exception {
        if(DEBUG)
            System.out.println("Attempting to exit scip process: "+ scipProcess.toString());
        isRunning = false;
        if (readerThread != null) {
            try {
                pipeInput("quit");
                processInput.close();
                readerThread.interrupt();
                readerThread.join(1000); // Wait up to 1 second for reader thread to finish
            } catch (Exception e) {
                if(DEBUG)
                    System.out.println("exception at scip process destroying1: "+ scipProcess.toString());
            }
        }
        if(DEBUG)
            System.out.println("piped quit and closed reader thread, attempting to destroy scip process: "+ scipProcess.toString());
        if (scipProcess != null) {
            try {
                scipProcess.destroyForcibly();
                scipProcess.waitFor();
                if(DEBUG)
                    System.out.println("scip process destroyed: "+ scipProcess.toString());
                Thread.sleep(100); // Give a small delay for file handles to be released
            } catch (Exception e) {
                if(DEBUG)
                    System.out.println("exception at scip process destroying2: "+ scipProcess.toString());
            }
        }
    }

    public String pollLog() {
        return circularBuffer.poll();
    }

    public List<String> pollLogAll() {
        List<String> logs = new ArrayList<>();
        String line;
        while ((line = circularBuffer.poll()) != null) {
            logs.add(line);
        }
        return logs;
    }

    private void pipeInput(String input) throws Exception {
        if(DEBUG)
            System.out.println("Piping input to scip: " + input + " , current scip's status: " + processStatus);
        processInput.write(input + "\n");
        processInput.flush();
    }

    private void updateStatus(String line) throws Exception {
        Matcher readingMatcher = readingPattern.matcher(line);
        Matcher problemReadMatcher = problemReadPattern.matcher(line);
        Matcher presolvingMatcher = presolvingPattern.matcher(line);
        Matcher finishedPresolvingMatcher = finishedPresolvingPattern.matcher(line);
        Matcher pausedMatcher = pausedPattern.matcher(line);
        Matcher solvedMatcher = solvedPattern.matcher(line);
        Matcher compilationErrorMatcher = compilationErrorPattern.matcher(line);
        boolean updated = false;
        if (readingMatcher.find()) {
            updated = true;
            processStatus = "reading";
        } else if (problemReadMatcher.find()) {
            updated = true;
            processStatus = "problem read";
        } else if (presolvingMatcher.find()) {
            updated = true;
            processStatus = "presolving";
        } else if (finishedPresolvingMatcher.find()) {
            updated = true;
            processStatus = "solving";
        } else if (pausedMatcher.find()) {
            updated = true;
            processStatus = "paused";
        } else if (solvedMatcher.find()) {
            updated = true;
            processStatus = "solved";
        } else if (compilationErrorMatcher.find()) {
            updated = true;
            int errorNumber = Integer.parseInt(compilationErrorMatcher.group(1));
            if(errorNumber == 900){
                processStatus = "integrity error";
            }else{
                processStatus = "compilation error";
                compilationErrorMessage = line;
            }
            
        }
        if(DEBUG && updated)
            System.out.println("updated status: " + processStatus + " | process: " + scipProcess.toString());
    }
}