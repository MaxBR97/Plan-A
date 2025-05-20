package SolverService.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import DataAccess.ModelRepository;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import SolverService.SolverService;
import Model.Solution;

import org.springframework.context.annotation.Profile;
@Service
@Profile("kafkaSolver")
public class KafkaSolverService implements HealthIndicator {
    private final SolverService solverService;
    private final KafkaTemplate<String, SolverResponse> kafkaTemplate;

    @Value("${kafka.topic.solver.response}")
    private String responseTopic;
    
    @Autowired
    public KafkaSolverService( KafkaTemplate<String, SolverResponse> kafkaTemplate, ModelRepository modelRepository) {
        this.solverService = new SolverService(modelRepository);
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @KafkaListener(topics = "${kafka.topic.solver.request}")
    public void handleRequest(SolverRequest request) {
        System.out.println("Request received at solver: " + request.toString());
        try {
            Object result = switch (request.type()) {
                case SOLVE -> solverService.solve(request.fileId(), request.timeout(), request.solverScript());
                case SOLVE_ASYNC -> solverService.solveAsync(request.fileId(), request.timeout(), request.solverScript()).get();
                case COMPILE_CHECK -> solverService.isCompiling(request.fileId(), request.timeout());
                case COMPILE_CHECK_ASYNC -> solverService.isCompilingAsync(request.fileId(), request.timeout()).get();
            };
            
            SolverResponse response;
            if (result instanceof Solution solution) {
                response = new SolverResponse(request.requestId(), solution, null, true);
            } else if (result instanceof String str) {
                response = new SolverResponse(request.requestId(), null, str, true);
            } else {
                response = new SolverResponse(request.requestId(), null, "Unknown result type", false);
            }
            
            kafkaTemplate.send(responseTopic, response);
            System.out.println("Response sent to Kafka: " + response.toString());

        } catch (Exception e) {
            SolverResponse response = new SolverResponse(
                request.requestId(),
                null,
                "Error processing request: " + e.getMessage(),
                false
            );
            kafkaTemplate.send(responseTopic, response);
            System.out.println("Response sent to Kafka: " + response.toString());
        }
    }
    
    @Override
    public Health health() {
        try {
            // Check if solver service is responsive
            solverService.isCompiling("health-check", 1);
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
} 