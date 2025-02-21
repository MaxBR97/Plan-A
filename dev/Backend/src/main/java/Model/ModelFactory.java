package Model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import DataAccess.ModelRepository;
import Exceptions.InternalErrors.BadRequestException;

@Service
public class ModelFactory {
    @Value("${model.useRemote:local}")
    private String modelInstance;
    @Value("${model.grpc.host:localhost}")
    private String remoteHost;
    @Value("${model.grpc.port:0}")
    private int remotePort;
    private static ModelRepository modelRepository;

    @Autowired
    public ModelFactory(ModelRepository repo) {
        remotePort = remotePort== 0 ? Integer.parseInt(System.getProperty("server.port", "8080")) : remotePort;
        modelRepository = repo;
    }

    public ModelInterface getModel(String id) throws Exception {
        if(modelInstance.equals("local"))
            return new Model(modelRepository,id);
        else if(modelInstance.equals("remote"))
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
}
