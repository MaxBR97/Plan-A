package groupId;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import Model.Model;
import Model.ModelSet;
import parser.*;


@SpringBootApplication(scanBasePackages = {"groupId", "DataAccess", "Model", "Image", "Unit", "Integration", "Acceptance", "DTO", "Exceptions", "Image.Modules","Exceptions.UserErrors"})
@EnableJpaRepositories(basePackages = {"DataAccess", "Model","Image", "Unit","Image.Modules"})
@ComponentScan(basePackages = {"groupId", "DataAccess", "Model", "Image", "Unit","Image.Modules"})
@EntityScan(basePackages = {"DataAccess", "Model", "Unit", "groupId", "Image","Image.Modules"})
@Profile("inMemory")
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
