package Model;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Model.ModelInput.StructureBlock;
import SolverService.GrpcSolverService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import parser.*;
import parser.FormulationLexer;
import parser.FormulationParser;
import parser.FormulationParser.ExprContext;
import parser.FormulationParser.ParamDeclContext;
import parser.FormulationParser.SetDeclContext;
import parser.FormulationParser.SetDefExprContext;
import parser.FormulationParser.SetDescStackContext;
import parser.FormulationParser.SetExprContext;
import parser.FormulationParser.SetExprStackContext;
import parser.FormulationParser.TupleContext;
import parser.FormulationParser.UExprContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.Policy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.TreeWizard;
import org.springframework.web.bind.annotation.RestController;

import DataAccess.ModelRepository;
import GRPC.CompilationResult;
import static GRPC.CompilationResult.parser;
import GRPC.ExecutionRequest;
import GRPC.SolverServiceGrpc;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

public class ModelProxyGRPC extends ModelInterface {

    private final Model localModel;
    private final String solverHost;
    private final int solverPort;
    private ModelRepository modelRepository;
    private String id;

    public ModelProxyGRPC(ModelRepository repo, String id, String solverHost, int solverPort) throws Exception {
        this.modelRepository = repo;
        this.id = id;
        this.localModel = new Model(repo, id);
        this.solverHost = solverHost;
        this.solverPort = solverPort;
    }

    public ModelProxyGRPC(ModelRepository repo, String id, String solverHost, int solverPort, Set<ModelSet> sets, Set<ModelParameter> params) throws Exception {
        this.modelRepository = repo;
        this.id = id;
        this.localModel = new Model(repo, id,sets, params);
        this.solverHost = solverHost;
        this.solverPort = solverPort;
    }

    @Override
    public void parseSource() throws Exception{
        localModel.parseSource();
    }

    @Override
    public InputStream getSource() throws Exception {
        return localModel.getSource();
    }

    @Override
    public String getSourcePathToFile() throws Exception {
        return localModel.getSourcePathToFile();
    }

    @Override
    public String getSolutionPathToFile(String suffix) throws Exception {
        return localModel.getSolutionPathToFile(suffix);
    }

    @Override
    public void writeToSource(String newSource) throws Exception {
        localModel.writeToSource(newSource);
    }

    @Override
    public void writeSolution(String content, String suffix) throws Exception {
        localModel.writeSolution(content, suffix);
    }

    public void appendToSet(ModelSet set, String value) throws Exception {
        localModel.appendToSet(set, value);
    }

    public void removeFromSet(ModelSet set, String value) throws Exception {
        localModel.removeFromSet(set, value);
    }

    public void setInput(ModelParameter identifier, String value) throws Exception {
        localModel.setInput(identifier, value);
    }

    public void setInput(ModelSet identifier, String[] values) throws Exception {
        localModel.setInput(identifier, values);
    }

    @Override
    public String[] getInput(ModelParameter parameter) throws Exception {
        return localModel.getInput(parameter);
    }

    @Override
    public List<String[]> getInput(ModelSet set) throws Exception {
        return localModel.getInput(set);
    }

    public void toggleFunctionality(ModelFunctionality mf, boolean turnOn) {
        localModel.toggleFunctionality(mf, turnOn);
    }

    //TODO: convert isCompiling and solve to asynchronous calls.
    //TODO: much refactoring and optimisation needed.
    @Override
    public boolean isCompiling(float timeout) throws Exception {
        String serviceBHost = this.solverHost;
        int serviceBPort = this.solverPort;

        ManagedChannel channel = NettyChannelBuilder.forAddress(serviceBHost, serviceBPort)
                .usePlaintext()
                .build();

        SolverServiceGrpc.SolverServiceBlockingStub stub = SolverServiceGrpc.newBlockingStub(channel);

        ExecutionRequest request = ExecutionRequest.newBuilder()
                .setId(id)
                //.setCode(this.originalSource)
                .setTimeout(timeout)
                .build();

        localModel.commentOutToggledFunctionalities();
        CompilationResult response = stub.isCompiling(request);
        localModel.restoreToggledFunctionalities();

        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        return response.getResult();
    }
    
    public Solution solve(float timeout, String solutionFileSufix, String SolverScript) throws Exception {
        String serviceBHost = this.solverHost;
        int serviceBPort = this.solverPort;

        ManagedChannel channel = NettyChannelBuilder.forAddress(serviceBHost, serviceBPort)
                .usePlaintext()
                .build();

        SolverServiceGrpc.SolverServiceBlockingStub stub = SolverServiceGrpc.newBlockingStub(channel);

        ExecutionRequest request = ExecutionRequest.newBuilder()
                .setId(id)
                .setTimeout(timeout)
                .build();

        localModel.commentOutToggledFunctionalities();                
        GRPC.Solution response = stub.solve(request);
        localModel.restoreToggledFunctionalities();
        modelRepository.downloadDocument(response.getSolution());
        if(response.getSolution().equals("null"))
            return null;
        Path pathToSolution = modelRepository.getLocalyCachedFile(response.getSolution());
        Solution sol = new Solution(pathToSolution.toString());

        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        return sol;
    }

    public ModelSet getSet(String identifier) {
        return localModel.getSet(identifier);
    }

    public ModelParameter getParameter(String identifier) {
        return localModel.getParameter(identifier);
    }

    public ModelConstraint getConstraint(String identifier) {
        return localModel.getConstraint(identifier);
    }

    @Override
    public Collection<ModelConstraint> getConstraints() {
        return localModel.getConstraints();
    }

    public ModelPreference getPreference(String identifier) {
        return localModel.getPreference(identifier);
    }

    @Override
    public Collection<ModelPreference> getPreferences() {
        return localModel.getPreferences();
    }

    public ModelVariable getVariable(String identifier) {
        return localModel.getVariable(identifier);
    }

    @Override
    public Collection<ModelVariable> getVariables() {
        return localModel.getVariables();
    }

    @Override
    public Collection<ModelVariable> getVariables(Collection<String> identifiers) {
        return localModel.getVariables(identifiers);
    }

    @Override
    public Collection<ModelSet> getSets() {
        return localModel.getSets();
    }

    @Override
    public Collection<ModelParameter> getParameters() {
        return localModel.getParameters();
    }

    public ModelFunction getFunction(String identifier) {
        return localModel.getFunction(identifier);
    }

    @Override
    public Collection<ModelFunction> getFunctions() {
        return localModel.getFunctions();
    }

    @Override
    public ModelComponent getComponent(String mc) {
        return localModel.getComponent(mc);
    }

    @Override
    public void setModelComponent(ModelComponent mc) throws Exception{
        localModel.setModelComponent(mc);
    }

    @Override
    public CompletableFuture<Solution> solveAsync(float timeout, String suffix, String script) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'solveAsync'");
    }

    @Override
    public String poll() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'poll'");
    }

    @Override
    public void pause() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pause'");
    }

    @Override
    public CompletableFuture<Solution> continueProcess(int extraTimeout) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'continueProcess'");
    }

    @Override
    public void finish() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'finish'");
    }

}
