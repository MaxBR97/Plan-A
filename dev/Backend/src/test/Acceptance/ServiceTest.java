package Acceptance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import org.springframework.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.ModelDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.*;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import groupId.Main;
import groupId.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
public class ServiceTest {
    @LocalServerPort
    private int port;

    @Autowired
    private Service service;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateImage() {
        // sample Zimpl code
        CreateImageFromFileDTO body = new CreateImageFromFileDTO(
            """
                param x := 10;
                set mySet := {1,2,3};

                var myVar[mySet];

                subto sampleConstraint:
                    myVar[x] == mySet[1];

                maximize myObjective:
                    1; 
            """
                    );

        
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
              List.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(List.of("mySet"),List.of("x")))),
              List.of(new PreferenceDTO("myObjective", new DependenciesDTO(List.of(),List.of()))),
              List.of(new VariableDTO("myVar", new DependenciesDTO(List.of("mySet"),List.of()))),
              Map.of(
                "mySet","INT",
                "x","INT"
                )
            ));
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().imageId() != null);
        assertEquals(response.getBody().model().constraints(), expected.model().constraints());
        assertEquals(response.getBody().model().preferences(), expected.model().preferences());
        assertEquals(response.getBody().model().variables(), expected.model().variables());
        assertEquals(response.getBody().model().types(), expected.model().types());
        
    }

}