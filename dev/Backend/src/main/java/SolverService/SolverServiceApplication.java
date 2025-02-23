package SolverService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("grpcSolver")
@Component
@SpringBootApplication
public class SolverServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolverServiceApplication.class, args);
    }
}
