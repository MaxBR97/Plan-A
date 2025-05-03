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

    @Autowired
    public KafkaSolverWorker(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    @KafkaListener(topics = "solve_request", groupId = "solver")
    public KafkaSolveResponseDTO handleSolve(KafkaSolveRequestDTO request) throws Exception {
        ModelInterface model = modelFactory.getModel(request.id(), "local");
        Model.Solution ans = model.solve(request.timeout(), "tmp", request.solverScript());
        String solutionName = (ans != null) ? request.id() + "tmp" : "null";
        return new KafkaSolveResponseDTO(solutionName);
    }

    @KafkaListener(topics = "compile_request", groupId = "solver")
    public KafkaCompileResponseDTO handleCompile(KafkaCompileRequestDTO request) throws Exception {
        ModelInterface model = modelFactory.getModel(request.id(), "local");
        boolean result = model.isCompiling(request.timeout());
        return new KafkaCompileResponseDTO(result);
    }
}
