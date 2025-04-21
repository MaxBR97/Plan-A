package Model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import DataAccess.ModelRepository;
import Exceptions.InternalErrors.BadRequestException;

@Service
public class ModelFactory {
    
    private String modelInstance;
    
    private String remoteHost;
    
    private int remotePort;
    private static ModelRepository modelRepository;
    private static Environment environment;
    @Autowired
    public ModelFactory(ModelRepository repo, 
                        @Value("${grpc.server.port:0}") int port,
                        @Value("${grpc.server.address:127.0.0.1}") String host,
                        @Value("${server.port:4000}") int serverPort, 
                        Environment env) {
        environment = env;
        String remote = env.acceptsProfiles(Profiles.of("grpcSolver")) ? "grpcSolver" : "local";
        modelInstance = remote;
        remoteHost = host;
        remotePort = port;
        
        if (remote.equals("grpcSolver")) {
            remotePort = (remotePort == 0) ? serverPort : remotePort;
        }

        modelRepository = repo;
    }

    public ModelInterface getModel(String id) throws Exception {
        
        return getModel(id,modelInstance, Set.of(), Set.of());
    }

    public ModelInterface getModel(String id, Set<ModelSet> sets, Set<ModelParameter> params) throws Exception {
        
        return getModel(id,modelInstance, sets, params);
    }

    public ModelInterface getModel(String id, String role, Set<ModelSet> sets, Set<ModelParameter> params) throws Exception {
        
        if(role.equals("local"))
            return new Model(modelRepository,id,sets,params);
        else if(role.equals("grpcSolver"))
            return new ModelProxy(modelRepository,id,remoteHost,remotePort,sets,params);
        throw new BadRequestException(" invalid configuration of model");
    }

    public ModelInterface getModel(String id, String role) throws Exception {
        
        if(role.equals("local"))
            return new Model(modelRepository,id);
        else if(role.equals("grpcSolver"))
            return new ModelProxy(modelRepository,id,remoteHost,remotePort);
        throw new BadRequestException(" invalid configuration of model");
    }

    public static void uploadNewModel(String documentId, String code) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(documentId, inputStream);
    }

    public ModelRepository getRepository(){
        return modelRepository;
    }

    public void deleteModel(String imageId) throws Exception {
        modelRepository.deleteDocument(imageId);
    }
}
