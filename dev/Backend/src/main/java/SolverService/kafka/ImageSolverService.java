package SolverService.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import Model.Solution;
import SolverService.Solver;
import org.springframework.context.annotation.Profile;

@Service
@Profile("kafkaSolver")
public class ImageSolverService implements Solver, HealthIndicator {
    private final KafkaTemplate<String, SolverRequest> kafkaTemplate;
    private final Map<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();
    private final Counter requestCounter;
    private final Counter errorCounter;
    
    @Value("${kafka.topic.solver.request}")
    private String requestTopic;
    
    public ImageSolverService(KafkaTemplate<String, SolverRequest> kafkaTemplate, 
                             MeterRegistry registry) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestCounter = registry.counter("solver.requests");
        this.errorCounter = registry.counter("solver.errors");
    }
        
    @CircuitBreaker(name = "solver", fallbackMethod = "fallbackSolve")
    @Override
    public Solution solve(String fileId, int timeout, String solverScript) throws Exception {
        return (Solution) sendRequest(fileId, timeout, solverScript, SolverRequest.RequestType.SOLVE).get();
    }
    
    @CircuitBreaker(name = "solver", fallbackMethod = "fallbackSolveAsync")
    @Override
    public CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript) throws Exception {
        CompletableFuture<Object> objectFuture = sendRequest(fileId, timeout, solverScript, SolverRequest.RequestType.SOLVE_ASYNC);
        CompletableFuture<Solution> solutionFuture = new CompletableFuture<>();
        
        objectFuture.thenAccept(result -> {
            if (result instanceof Solution) {
                solutionFuture.complete((Solution) result);
            } else {
                solutionFuture.completeExceptionally(
                    new RuntimeException("Expected Solution but got " + 
                    (result != null ? result.getClass().getName() : "null"))
                );
            }
        }).exceptionally(ex -> {
            solutionFuture.completeExceptionally(ex);
            return null;
        });
        
        return solutionFuture;
    }
    
    @CircuitBreaker(name = "solver", fallbackMethod = "fallbackIsCompiling")
    @Override
    public String isCompiling(String fileId, int timeout) throws Exception {
        return (String) sendRequest(fileId, timeout, "", SolverRequest.RequestType.COMPILE_CHECK).get();
    }
    
    @CircuitBreaker(name = "solver", fallbackMethod = "fallbackIsCompilingAsync")
    @Override
    public CompletableFuture<String> isCompilingAsync(String fileId, int timeout) throws Exception {
        CompletableFuture<Object> objectFuture = sendRequest(fileId, timeout, "", SolverRequest.RequestType.COMPILE_CHECK_ASYNC);
        CompletableFuture<String> stringFuture = new CompletableFuture<>();
        
        objectFuture.thenAccept(result -> {
            if (result instanceof String) {
                stringFuture.complete((String) result);
            } else {
                stringFuture.completeExceptionally(
                    new RuntimeException("Expected String but got " + 
                    (result != null ? result.getClass().getName() : "null"))
                );
            }
        }).exceptionally(ex -> {
            stringFuture.completeExceptionally(ex);
            return null;
        });
        
        return stringFuture;
    }
    
    private CompletableFuture<Object> sendRequest(String fileId, int timeout, String solverScript, SolverRequest.RequestType type) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        
        requestCounter.increment();
        
        SolverRequest request = new SolverRequest(requestId, fileId, timeout, solverScript, type);
        kafkaTemplate.send(requestTopic, request);
        System.out.println("Request sent to Kafka: " + request.toString());
        
        return future;
    }
    
    @KafkaListener(topics = "${kafka.topic.solver.response}")
    public void handleResponse(SolverResponse response) {
        System.out.println("Response received from Kafka: " + response.toString());
        CompletableFuture<Object> future = pendingRequests.remove(response.requestId());
        if (future != null) {
            if (response.success()) {
                future.complete(response.solution());
            } else {
                errorCounter.increment();
                future.completeExceptionally(new RuntimeException(response.errorMessage()));
            }
        }
    }
    
    // Fallback methods
    private Solution fallbackSolve(String fileId, int timeout, String solverScript, Exception e) {
        errorCounter.increment();
        throw new RuntimeException("Solver service is not available");
    }
    
    private CompletableFuture<Solution> fallbackSolveAsync(String fileId, int timeout, String solverScript, Exception e) {
        errorCounter.increment();
        CompletableFuture<Solution> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Solver service is not available"));
        return future;
    }
    
    private String fallbackIsCompiling(String fileId, int timeout, Exception e) {
        errorCounter.increment();
        return "Compilation check service not available";
    }
    
    private CompletableFuture<String> fallbackIsCompilingAsync(String fileId, int timeout, Exception e) {
        errorCounter.increment();
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Compilation check service not available"));
        return future;
    }
    
    @Override
    public Health health() {
        try {
            // Check Kafka connection
            kafkaTemplate.send(requestTopic, new SolverRequest(
                "health-check",
                "test",
                1,
                "",
                SolverRequest.RequestType.COMPILE_CHECK
            )).get();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }

    @Override
    public void shutdown() throws Exception {
        // TODO Auto-generated method stub
        throw new Exception("Unimplemented method 'shutdown'");
    }
}