package groupId;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import Model.Model;
import Model.ModelSet;
import SolverService.GrpcSolverService;
import SolverService.SolverServiceApplication;
import parser.*;

//TODO: Make an order in maven pom file, and make a proper pom hierarchy.

@SpringBootApplication(scanBasePackages = {"groupId", "DataAccess", "Model", "Image", " Unit", "Integration", "Acceptance", "DTO", "Exceptions", "Image.Modules","Exceptions.UserErrors","SolverService"})
@EnableJpaRepositories(basePackages = {"DataAccess", "Model","Image", "Unit","Image.Modules"})
@ComponentScan(basePackages = {"groupId", "DataAccess", "Model", "Image", "Unit","Image.Modules","SolverService"})
@EntityScan(basePackages = {"DataAccess", "Model", "Unit", "groupId", "Image","Image.Modules"})

public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}