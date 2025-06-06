package SolverService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class ScipProcessPool {
    private final BlockingQueue<ScipProcess> availableProcesses;
    private final List<ScipProcess> allProcesses;
    private final int poolSize;
    private static final long PROCESS_WAIT_TIMEOUT = 30; // seconds

    public ScipProcessPool(int poolSize) {
        this.poolSize = poolSize;
        this.availableProcesses = new ArrayBlockingQueue<>(poolSize);
        this.allProcesses = new ArrayList<>(poolSize);
        initializePool();
    }

    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            ScipProcess process = new ScipProcess();
            allProcesses.add(process);
            availableProcesses.offer(process);
        }
    }

    public ScipProcess acquireProcess() throws Exception {
        ScipProcess process = availableProcesses.poll(PROCESS_WAIT_TIMEOUT, TimeUnit.SECONDS);
        if (process == null) {
            throw new Exception("Timeout waiting for available SCIP process");
        }
        
        // Start the process if it's not already running
        if (process.isRunning()) {
            process.exit();
        }
        process.start();
        
        return process;
    }

    public void releaseProcess(ScipProcess process) throws Exception {
        if (process != null) {
            // Clean up the process for reuse
            try {
                
                availableProcesses.offer(process);
            } catch (Exception e) {
                throw new Exception("Failed to release process: " + e.getMessage());
            }
        }
    }

    public void shutdownAll() {
        allProcesses.forEach(process -> {
            try {
                if (process.isRunning()) {
                    process.exit();
                }
            } catch (Exception e) {
                // Log error but continue shutdown
                System.err.println("Error shutting down process: " + e.getMessage());
            }
        });
        allProcesses.clear();
        availableProcesses.clear();
    }
} 