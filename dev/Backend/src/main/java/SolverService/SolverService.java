package SolverService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import GRPC.CompilationResult;
import GRPC.ExecutionRequest;
import GRPC.SolverServiceGrpc;

@GrpcService
public class SolverService extends SolverServiceGrpc.SolverServiceImplBase {

    @Override
    public void isCompiling(ExecutionRequest request, StreamObserver<CompilationResult> responseObserver) {
        CompilationResult response = CompilationResult.newBuilder()
                .setResult(true)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    
}
