package Model;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import Model.ModelInput.StructureBlock;
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

import DTO.Records.Requests.Commands.KafkaCompileRequestDTO;
import DTO.Records.Requests.Commands.KafkaCompileResponseDTO;
import DTO.Records.Requests.Commands.KafkaSolveRequestDTO;
import DTO.Records.Requests.Commands.KafkaSolveResponseDTO;
import DataAccess.ModelRepository;


public class ModelProxyKafka extends ModelInterface {

    private final Model localModel;

    private ReplyingKafkaTemplate<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> compileTemplate;
    private ReplyingKafkaTemplate<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> solveTemplate;

    private ModelRepository modelRepository;
    private String id;

    public ModelProxyKafka(ModelRepository repo, String id,
    ReplyingKafkaTemplate<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> compileTemplate, 
    ReplyingKafkaTemplate<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> solveTemplate) throws Exception {
        this.modelRepository = repo;
        this.id = id;
        this.compileTemplate = compileTemplate;
        this.solveTemplate = solveTemplate;
        this.localModel = new Model(repo, id);
    }

    public ModelProxyKafka(ModelRepository repo, String id, Set<ModelSet> sets, Set<ModelParameter> params, 
    ReplyingKafkaTemplate<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> compileTemplate, 
    ReplyingKafkaTemplate<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> solveTemplate) throws Exception {
        this.modelRepository = repo;
        this.id = id;
        this.compileTemplate = compileTemplate;
        this.solveTemplate = solveTemplate;
        this.localModel = new Model(repo, id, sets, params);
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

    @Override
    public boolean isCompiling(float timeout) throws Exception {
        localModel.commentOutToggledFunctionalities();

        KafkaCompileRequestDTO requestDTO = new KafkaCompileRequestDTO(id, timeout);
        ProducerRecord<String, KafkaCompileRequestDTO> record = new ProducerRecord<>("compile_request", requestDTO);
        // record.headers().add(KafkaHeaders.REPLY_TOPIC, "compile_response".getBytes(StandardCharsets.UTF_8));
        RequestReplyFuture<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> future = compileTemplate.sendAndReceive(record);

        ConsumerRecord<String, KafkaCompileResponseDTO> response = future.get((long) timeout, TimeUnit.SECONDS);

        localModel.restoreToggledFunctionalities();

        return response.value().result();
    }


    //TODO: If using a remote worker, the local service should not access the model document
    // meanning, should not use: modelRepository.downloadDocument(...).
    @Override
    public Solution solve(float timeout, String solutionFileSuffix, String solverScript) throws Exception {
        
        localModel.commentOutToggledFunctionalities();

        KafkaSolveRequestDTO requestDTO = new KafkaSolveRequestDTO(id, timeout, solverScript);
        ProducerRecord<String, KafkaSolveRequestDTO> record = new ProducerRecord<>("solve_request", requestDTO);
        // record.headers().add(KafkaHeaders.REPLY_TOPIC, "solve_response".getBytes(StandardCharsets.UTF_8));
        RequestReplyFuture<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> future = solveTemplate.sendAndReceive(record);
        System.out.println("Reply Received456" + future.get((long) timeout, TimeUnit.SECONDS).toString());
        ConsumerRecord<String, KafkaSolveResponseDTO> response = future.get((long) timeout, TimeUnit.SECONDS);
        System.out.println("Reply Received456");
        localModel.restoreToggledFunctionalities();

        String solutionName = response.value().solution();
        modelRepository.downloadDocument(solutionName);

        if ("null".equals(solutionName)) {
            return null;
        }

        Path pathToSolution = modelRepository.getLocalyCachedFile(solutionName);
        return new Solution(pathToSolution.toString());
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

    // @Override
    // public CompletableFuture<Solution> solveAsync(float timeout, String suffix, String script) throws Exception {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'solveAsync'");
    // }

    // @Override
    // public String poll() throws Exception {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'poll'");
    // }

    // @Override
    // public void pause() throws Exception {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'pause'");
    // }

    // @Override
    // public CompletableFuture<Solution> continueProcess(int extraTimeout) throws Exception {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'continueProcess'");
    // }

    // @Override
    // public void finish() throws Exception {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'finish'");
    // }
    
    @Override
    public void commentOutToggledFunctionalities() throws Exception {
        localModel.commentOutToggledFunctionalities();
    }

    @Override 
    public void restoreToggledFunctionalities() throws Exception {
        localModel.restoreToggledFunctionalities();
    }
}
