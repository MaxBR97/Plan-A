package DataAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import DataAccess.LocalStorage.ModelLocalDiskDAO;
import DataAccess.ModelRepository;
import DataAccess.S3ModelStorageService;

@Configuration
public class ModelStorageConfig {

    @Bean
    @Primary 
    public ModelRepository modelRepository() {
        String storageType = System.getenv("STORAGE_TYPE"); 
        if ("s3".equalsIgnoreCase(storageType)) {
            return new S3ModelStorageService();
        } else {
            return new ModelLocalDiskDAO();
        }
    }
}
