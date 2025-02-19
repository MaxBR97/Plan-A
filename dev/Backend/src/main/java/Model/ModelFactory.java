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

@Service
public class ModelFactory {
    @Value("${model.useRemote:local}")
    private String modelInstance;
    private static ModelRepository modelRepository;

    @Autowired
    public ModelFactory(ModelRepository repo) {
        modelRepository = repo;
    }

    public ModelInterface getModel(String id) throws Exception{
        if(modelInstance.equals("local"))
            return new Model(modelRepository,id);
        else
            return null;
    }

    public static void uploadNewModel(String documentId, String code) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(documentId, inputStream);
    }

    public ModelRepository getRepository(){
        return modelRepository;
    }
}
