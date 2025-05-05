package SolverService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("kafkaSolver")
@SpringBootApplication(scanBasePackages = {"groupId", "DataAccess", "Model", "Image", " Unit", "Integration", "Acceptance", "DTO", "Exceptions", "Image.Modules","Exceptions.UserErrors","SolverService"})
public class SolverServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolverServiceApplication.class, args);
    }
}

