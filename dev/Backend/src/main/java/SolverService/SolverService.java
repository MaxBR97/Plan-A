package SolverService;

import java.nio.file.Files;
import java.nio.file.Path;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;

import GRPC.CompilationResult;
import GRPC.ExecutionRequest;
import GRPC.Solution;
import GRPC.SolverServiceGrpc;
import Model.ModelFactory;
import Model.ModelInterface;

@GrpcService
public class SolverService extends SolverServiceGrpc.SolverServiceImplBase {

    ModelFactory modelFactory;

    @Autowired
    public SolverService(ModelFactory modelFact){
        modelFactory = modelFact;
    }

    @Override
    public void isCompiling(ExecutionRequest request, StreamObserver<CompilationResult> responseObserver) {
        String code = request.getCode();
        float timeout = request.getTimeout();
        try{
        ModelInterface model = modelFactory.getModel(request.getId());
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
        String code = request.getCode();
        float timeout = request.getTimeout();
        try{
        ModelInterface model = modelFactory.getModel(request.getId());
        Model.Solution ans = model.solve(timeout, "tmp");
        
        
        Files.readAllBytes(Path.of(model.getSolutionPathToFile("tmp")));
        GRPC.Solution response = Solution.newBuilder()
                .setSolution("")
                .build();
        
        responseObserver.onNext(response);
        } catch (Exception e){
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }

}
