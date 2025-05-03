package SolverService;

import java.nio.file.Files;
import java.nio.file.Path;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import GRPC.CompilationResult;
import GRPC.ExecutionRequest;
import GRPC.Solution;
import GRPC.SolverServiceGrpc;
import Model.ModelFactory;
import Model.ModelInterface;

//TODO: check why grpc starts even when remote is off
@Profile("grpcSolver")
@GrpcService
@Component
public class GrpcSolverService extends SolverServiceGrpc.SolverServiceImplBase {

    ModelFactory modelFactory;

    @Autowired
    public GrpcSolverService(ModelFactory modelFact){
        modelFactory = modelFact;
    }

    @Override
    public void isCompiling(ExecutionRequest request, StreamObserver<CompilationResult> responseObserver) {
        String id = request.getId();
        float timeout = request.getTimeout();
        try{
        ModelInterface model = modelFactory.getModel(id,"local");
        boolean ans = model.isCompiling(timeout);

        CompilationResult response = CompilationResult.newBuilder()
                .setResult(ans)
                .build();
        
        responseObserver.onNext(response);
        } catch (Exception e){
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }

    @Override
    public void solve(ExecutionRequest request, StreamObserver<GRPC.Solution> responseObserver) {
        String id = request.getId();
        float timeout = request.getTimeout();
        String solverScript = request.getSolverScript();
        try{
        ModelInterface model = modelFactory.getModel(id,"local");
        Model.Solution ans = model.solve(timeout, "tmp", solverScript);
        GRPC.Solution response;
        if(ans != null) {
            String content = new String(Files.readAllBytes(Path.of(model.getSolutionPathToFile("tmp"))));
            model.writeSolution(content , "tmp");
            response =  Solution.newBuilder()
            .setSolution(id+"tmp")
            .build();
        } else {
            response =  Solution.newBuilder()
            .setSolution("null")
            .build();
        }
        
        responseObserver.onNext(response);
        } catch (Exception e){
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }

}
