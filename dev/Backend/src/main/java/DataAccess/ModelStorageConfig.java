package DataAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import DataAccess.LocalStorage.ModelLocalDiskDAO;
import DataAccess.ModelRepository;
import DataAccess.S3ModelStorageService;


@Configuration
public class ModelStorageConfig {
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public ModelRepository s3ModelRepository(S3ModelStorageService s3ModelStorageService) {
        return s3ModelStorageService;
    }

    @Primary
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local")
    public ModelRepository localModelRepository(ModelLocalDiskDAO modelLocalDiskDAO) {
        return modelLocalDiskDAO;
    }
    
    @Bean
    @ConditionalOnMissingBean(ModelRepository.class)
    public ModelRepository defaultRepository(ModelLocalDiskDAO modelLocalDiskDAO) {
        return modelLocalDiskDAO;
    }
}
