package Acceptance;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.VariableModuleDTO;
import Image.Modules.PreferenceModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

//import org.springframework.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.ModelDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.*;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import groupId.Main;
import groupId.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
public class ServiceTest {
    @LocalServerPort
    private int port;
    static String SimpleCodeExample = """
                param x := 10;
                set mySet := {1,2,3};

                var myVar[mySet];

                subto sampleConstraint:
                    myVar[x] == mySet[1];

                maximize myObjective:
                    1;
            """;
    @Autowired
    private Service service;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateImage() {
        // sample Zimpl code

        CreateImageFromFileDTO body = new CreateImageFromFileDTO(SimpleCodeExample);

        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create http request with body and headers
        HttpEntity<CreateImageFromFileDTO> request = new HttpEntity<>(body, headers);

        // Send POST request with body
        String url = "http://localhost:" + port + "/images";
        ResponseEntity<CreateImageResponseDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            CreateImageResponseDTO.class
        );

        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
            "some id", new ModelDTO(
              Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(List.of("mySet"),List.of("x")))),
                Set.of(new PreferenceDTO("1", new DependenciesDTO(List.of(),List.of()))),
                Set.of(new VariableDTO("myVar", new DependenciesDTO(List.of("mySet"),List.of()))),
              Map.of(
                "mySet","INT",
                "x","INT"
                )
            ));
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().imageId());
        assertEquals(response.getBody().model().constraints(), expected.model().constraints());
        assertEquals(response.getBody().model().preferences(), expected.model().preferences());
        assertEquals(response.getBody().model().variables(), expected.model().variables());
        assertEquals(response.getBody().model().types(), expected.model().types());
    }
    
    @Test
    public void GivenImageDTO_WhenConfigImage_ImageIsCorrect(){
        /**
         * SET UP
         */
        CreateImageFromFileDTO body = new CreateImageFromFileDTO(SimpleCodeExample);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create http request with body and headers
        HttpEntity<CreateImageFromFileDTO> request = new HttpEntity<>(body, headers);
        // Send POST request with body
        String url = "http://localhost:" + port + "/images";
        ResponseEntity<CreateImageResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                CreateImageResponseDTO.class
        );
        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
                "some id", new ModelDTO(
                Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(List.of("mySet"),List.of("x")))),
                Set.of(new PreferenceDTO("1", new DependenciesDTO(List.of(),List.of()))),
                Set.of(new VariableDTO("myVar", new DependenciesDTO(List.of("mySet"),List.of()))),
                Map.of(
                        "mySet","INT",
                        "x","INT"
                )
        ));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().imageId());
        assertEquals(response.getBody().model().constraints(), expected.model().constraints());
        assertEquals(response.getBody().model().preferences(), expected.model().preferences());
        assertEquals(response.getBody().model().variables(), expected.model().variables());
        assertEquals(response.getBody().model().types(), expected.model().types());
        /**
         * TEST
         */
        Set<ConstraintModuleDTO> constraintModuleDTOs=Set.of(
                new ConstraintModuleDTO("Test module","PeanutButter",
                        Set.of("sampleConstraint"),Set.of("mySet"),Set.of("x")));
        Set<PreferenceModuleDTO> preferenceModuleDTOs=Set.of(
                new PreferenceModuleDTO("Test module","PeanutButter",
                        Set.of("1"),Set.of(),Set.of()));
        VariableModuleDTO variableModuleDTO= new VariableModuleDTO(Set.of("myVar"),Set.of("mySet"),Set.of());
        ImageDTO imageDTO= new ImageDTO(variableModuleDTO,constraintModuleDTOs,preferenceModuleDTOs);
        ImageConfigDTO configDTO= new ImageConfigDTO(response.getBody().imageId(),imageDTO);
        HttpEntity<ImageConfigDTO> request2 = new HttpEntity<>(configDTO, headers);
        // Send PATCH request with body
        String url2 = "http://localhost:" + port + "/images";
        ResponseEntity<Void> response2 = restTemplate.exchange(
                url2,
                HttpMethod.PATCH,
                request2,
                Void.class
        );
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    public void GivenFile_WhenCreateImage_ImageIsCorrect() {
        // sample Zimpl code
        String SimpleCodeExample = "";
        try {
        SimpleCodeExample = Files.readString(Paths.get("./src/test/Acceptance/example.zpl"));
        
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        CreateImageFromFileDTO body = new CreateImageFromFileDTO(SimpleCodeExample);

        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create http request with body and headers
        HttpEntity<CreateImageFromFileDTO> request = new HttpEntity<>(body, headers);

        // Send POST request with body
        String url = "http://localhost:" + port + "/images";
        ResponseEntity<CreateImageResponseDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            CreateImageResponseDTO.class
        );

        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
            "some id", new ModelDTO(
              Set.of(new ConstraintDTO("drisha1", new DependenciesDTO(List.of("People","Emdot"),List.of("shiftTime"))),
              new ConstraintDTO("drisha2", new DependenciesDTO(List.of("Emdot","People"),List.of("shiftTime"))),
              new ConstraintDTO("drisha3", new DependenciesDTO(List.of("People","Emdot"),List.of("shiftTime","restHours"))),
              new ConstraintDTO("drisha4", new DependenciesDTO(List.of("Emdot","People"),List.of("shiftTime")))),
                Set.of(new PreferenceDTO("sum<person>inPeople:(TotalMishmarot[person]**2)", new DependenciesDTO(List.of("People"),List.of()))),
                Set.of(new VariableDTO("Shibutsim", new DependenciesDTO(List.of("People","Emdot"),List.of("shiftTime"))),
                        new VariableDTO("TotalMishmarot", new DependenciesDTO(List.of("People"),List.of()))),
              Map.of(
                "People","TEXT",
                "Emdot","TEXT",
                "shiftTime","INT",
                "restHours","INT",
                // adding the type of variables is a new concept! we didnt use to do it before
                "Shibutsim", "<TEXT,TEXT,INT>",
                "TotalMishmarot", "TEXT"
                )
            ));
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().imageId());
        assertEquals(response.getBody().model().constraints(), expected.model().constraints());
        assertEquals(response.getBody().model().preferences(), expected.model().preferences());
        assertEquals(response.getBody().model().variables(), expected.model().variables());
        assertEquals(response.getBody().model().types(), expected.model().types());
    }
    
}