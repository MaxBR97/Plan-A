package SolverService;

import java.nio.file.Files;
import java.nio.file.Path;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import DTO.Records.Requests.Commands.KafkaCompileRequestDTO;
import DTO.Records.Requests.Commands.KafkaCompileResponseDTO;
import DTO.Records.Requests.Commands.KafkaSolveRequestDTO;
import DTO.Records.Requests.Commands.KafkaSolveResponseDTO;

import Model.ModelFactory;
import Model.ModelInterface;
@Profile("kafkaSolver")
@Component
public class KafkaSolverWorker {

    private final ModelFactory modelFactory;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaSolverWorker(ModelFactory modelFactory, ObjectMapper objectMapper) {
        this.modelFactory = modelFactory;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "solve_request", groupId = "solver")
    public KafkaSolveResponseDTO handleSolve(String requestJson) throws Exception {
        System.out.println("received solve request!");
        
        // Manually deserialize the request
        KafkaSolveRequestDTO request = deserializeSolveRequest(requestJson);
        
        ModelInterface model = modelFactory.getModel(request.id(), "local");
        Model.Solution ans = model.solve(request.timeout(), "tmp", request.solverScript());
        String solutionName = (ans != null) ? request.id() + "tmp" : "null";
        KafkaSolveResponseDTO ans2 =  new KafkaSolveResponseDTO(solutionName);
        System.out.println("Finished solving, sending message back.");
        return ans2;
    }

    @KafkaListener(topics = "compile_request", groupId = "solver")
    public KafkaCompileResponseDTO handleCompile(String requestJson) throws Exception {
        System.out.println("received compile request!");
        
        // Manually deserialize the request
        KafkaCompileRequestDTO request = deserializeCompileRequest(requestJson);
        
        ModelInterface model = modelFactory.getModel(request.id(), "local");
        boolean result = model.isCompiling(request.timeout());
        return new KafkaCompileResponseDTO(result);
    }
    
    private KafkaSolveRequestDTO deserializeSolveRequest(String json) {
        try {
            return objectMapper.readValue(json, KafkaSolveRequestDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize solve request: " + json, e);
        }
    }
    
    private KafkaCompileRequestDTO deserializeCompileRequest(String json) {
        try {
            return objectMapper.readValue(json, KafkaCompileRequestDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize compile request: " + json, e);
        }
    }
}