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
import org.springframework.beans.factory.ObjectProvider;


@Configuration
public class SolverConfiguration {

    @Value("${solver.mode:local}") 
    String mode;

    // @Bean
    // @ConditionalOnProperty(name = "solver.mode", havingValue = "remote-direct")
    // public Solver remoteSolver(@Value("${solver.service.url}") String serviceUrl) {
    //     System.out.println("solver.mode: " + mode);
    //     return new RemoteSolverClient(serviceUrl);
    // }

    @Bean
    @Profile("kafkaSolver")
    @Primary
    public Solver kafkaSolver(ModelRepository modelRepository) {
        System.out.println("Initializing process pool solver for Kafka profile");
        return new SolverService(modelRepository);
    }

    @Bean
    @Profile("!kafkaSolver")
    @Primary
    public Solver defaultSolver(ModelRepository modelRepository) {
        System.out.println("Initializing stream solver for non-Kafka profile");
        return new StreamSolverService(modelRepository);
    }
}