package SolverService.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Profile("kafkaSolver")
public class EmbeddedKafkaConfig {
    
    @Value("${spring.kafka.embedded.topics}")
    private String topics;
    
    @Bean
    @ConditionalOnProperty(name = "spring.kafka.embedded.enabled", havingValue = "true")
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        String[] topicArray = topics.split(",\\s*");
        EmbeddedKafkaZKBroker broker = new EmbeddedKafkaZKBroker(1, false)
            .kafkaPorts(9092);
        broker.brokerProperties(java.util.Map.of(
            "listeners", "PLAINTEXT://localhost:9092",
            "port", "9092"
        ));
        broker.brokerListProperty("spring.kafka.bootstrap-servers");
        broker.afterPropertiesSet();
        return broker;
    }
} 