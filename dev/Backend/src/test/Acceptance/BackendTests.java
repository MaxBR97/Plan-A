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
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Requests.Commands.*;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import groupId.Main;
import groupId.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
class ServiceTest {
    @LocalServerPort
    private int port;

    @Autowired
    private Service service;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateUser() {
        // sample Zimpl code
        CreateImageFromFileDTO body = new CreateImageFromFileDTO(
            """
                param x := 10;
                set mySet := {1,2,3};
                set composition := mySet * mySet;

                var myVar[composition] binary;

                subto sampleConstraint:
                    myVar[<1,1>] == 0;

            """
                    );

        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create http request with body and headers
        HttpEntity<CreateImageFromFileDTO> request = new HttpEntity<>(body, headers);

        // Send POST request with body
        String url = "http://localhost:" + port + "/images";
        ResponseEntity<ImageResponseDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            ImageResponseDTO.class
        );

        //Expected ImageDTO returned
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO("", new ImageDTO(null, null, null))
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody().id() != null);
        assertEquals(response.getBody().image(). != null);
    }

}