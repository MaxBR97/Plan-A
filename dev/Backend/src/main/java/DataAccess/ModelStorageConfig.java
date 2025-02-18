package DataAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import DataAccess.LocalStorage.ModelLocalDiskDAO;


@Configuration
public class ModelStorageConfig {
    
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public ModelRepository s3ModelRepository(@Value("${cloud.aws.region}") String region,
                                             @Value("${app.file.storage-dir}") String storagePath) {
        return new S3ModelStorageService(storagePath, region);
    }

    @Primary
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local")
    public ModelRepository localModelRepository(@Value("${app.file.storage-dir}") String storagePath) {
        return new ModelLocalDiskDAO(storagePath);
    }

    @Bean
    @ConditionalOnMissingBean(ModelRepository.class)
    public ModelRepository defaultRepository(@Value("${app.file.storage-dir}") String storagePath) {
        return new ModelLocalDiskDAO(storagePath);
    }
}

