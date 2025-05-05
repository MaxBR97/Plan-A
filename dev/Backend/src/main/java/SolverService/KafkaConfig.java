package SolverService;

import java.time.Duration;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import DTO.Records.Requests.Commands.KafkaCompileRequestDTO;
import DTO.Records.Requests.Commands.KafkaCompileResponseDTO;
import DTO.Records.Requests.Commands.KafkaSolveRequestDTO;
import DTO.Records.Requests.Commands.KafkaSolveResponseDTO;

@Profile("kafkaSolver")
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Add these two lines
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        config.put(JsonSerializer.TYPE_MAPPINGS, "solveRequest:DTO.Records.Requests.Commands.KafkaSolveRequestDTO,compileRequest:DTO.Records.Requests.Commands.KafkaCompileRequestDTO");
        return config;
    }

@Bean
public Map<String, Object> consumerConfigs() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "solver-client");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    
    // Add these poll rate configurations
    config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);  // Process fewer records per poll
    config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);   // Don't wait to accumulate data
    config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100); // Wait at most 100ms for data
    config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000); // More frequent heartbeats
    
    return config;
}


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public KafkaTemplate<String, KafkaSolveResponseDTO> kafkaSolveTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }

    @Bean
    public KafkaTemplate<String, KafkaCompileResponseDTO> kafkaCompileTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }

    @Bean(name = "kafkaTemplate")
    @Primary
    public KafkaTemplate<String, KafkaCompileResponseDTO> defaultKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }


    @Bean
    public ProducerFactory<String, Object> objectProducerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        ));
    }


    @Bean
    public ProducerFactory<String, KafkaSolveRequestDTO> solveProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ProducerFactory<String, KafkaCompileRequestDTO> compileProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, KafkaSolveResponseDTO> solveConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaSolveResponseDTO.class));
    }

    @Bean
    public ConsumerFactory<String, KafkaCompileResponseDTO> compileConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaCompileResponseDTO.class));
    }


    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaSolveResponseDTO> replyContainerSolve() {
        ContainerProperties containerProps = new ContainerProperties("solve_response");
        containerProps.setPollTimeout(100);
        return new ConcurrentMessageListenerContainer<>(solveConsumerFactory(), containerProps);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaCompileResponseDTO> replyContainerCompile() {
        ContainerProperties containerProps = new ContainerProperties("compile_response");
        containerProps.setPollTimeout(100);
        return new ConcurrentMessageListenerContainer<>(compileConsumerFactory(), containerProps);
    }


    @Bean
public ReplyingKafkaTemplate<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> solveTemplate() {
    ReplyingKafkaTemplate<String, KafkaSolveRequestDTO, KafkaSolveResponseDTO> template = 
        new ReplyingKafkaTemplate<>(solveProducerFactory(), replyContainerSolve());
    
    template.setDefaultReplyTimeout(Duration.ofSeconds(32));
    
    return template;
}

@Bean
public ReplyingKafkaTemplate<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> compileTemplate() {
    ReplyingKafkaTemplate<String, KafkaCompileRequestDTO, KafkaCompileResponseDTO> template = 
        new ReplyingKafkaTemplate<>(compileProducerFactory(), replyContainerCompile());
    
    template.setDefaultReplyTimeout(Duration.ofSeconds(32)); //32 sec
    
    return template;
}

}
