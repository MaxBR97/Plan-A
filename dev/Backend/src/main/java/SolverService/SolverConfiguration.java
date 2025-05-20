package SolverService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import SolverService.kafka.ImageSolverService;
import SolverService.kafka.SolverRequest;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

import DataAccess.ModelRepository;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;


@Configuration
public class SolverConfiguration {

    @Value("${solver.mode:NOT_SET}") 
    String mode;

    // @Bean
    // @ConditionalOnProperty(name = "solver.mode", havingValue = "remote-direct")
    // public Solver remoteSolver(@Value("${solver.service.url}") String serviceUrl) {
    //     System.out.println("solver.mode: " + mode);
    //     return new RemoteSolverClient(serviceUrl);
    // }

    @Bean
    @ConditionalOnProperty(name = "solver.mode", havingValue = "remote-kafka")
    public Solver kafkaSolver(KafkaTemplate<String, SolverRequest> kafkaTemplate, 
                             MeterRegistry registry,
                             @Value("${kafka.topic.solver.request}") String requestTopic) {
        System.out.println("solver.mode: " + mode + " injecting kafkaSolver");
        return new ImageSolverService(kafkaTemplate, registry, requestTopic);
    }

    @Bean
    @ConditionalOnProperty(name = "solver.mode", havingValue = "local", matchIfMissing = true)
    public Solver localSolver(ModelRepository modelRepository) {
        System.out.println("solver.mode: " + mode + " injecting localSolver");
        return new SolverService(modelRepository);
    }
}