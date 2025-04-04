package Acceptance;

import java.util.List;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;

public class RequestsManager {
    int port;
    TestRestTemplate restTemplate;

    CreateImageRequestBuilder createImageReq;
    ConfigureImageRequestBuilder configImageReq;
    
    

    public RequestsManager(int port, TestRestTemplate restTemplate){
        this.port = port;
        this.restTemplate = restTemplate;
    }
    
    

    public ResponseEntity<CreateImageResponseDTO> sendCreateImageRequest(CreateImageFromFileDTO body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<CreateImageFromFileDTO> request = new HttpEntity<>(body, headers);

        // Send POST request with body
        String url = "http://localhost:" + port + "/images";
        ResponseEntity<CreateImageResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                CreateImageResponseDTO.class
        );
        return response;
    }

    public ResponseEntity<Void> sendConfigImageRequest(ImageConfigDTO body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ImageConfigDTO> request2 = new HttpEntity<>(body , headers);
                    // Send PATCH request with body
                    String url2 = "http://localhost:" + port + "/images";
                    ResponseEntity<Void> response2 = restTemplate.exchange(
                            url2,
                            HttpMethod.PATCH,
                            request2,
                            Void.class
                    );  
        return response2;
    }

    public ResponseEntity<SolutionDTO> sendSolveRequest(SolveCommandDTO body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SolveCommandDTO> request3 = new HttpEntity<>(body, headers);
                String url3 = "http://localhost:" + port + "/solve";
                ResponseEntity<SolutionDTO> response3 = restTemplate.exchange(
                        url3,
                        HttpMethod.POST,
                        request3,
                        SolutionDTO.class
                );
        
        return response3;
    }

    public ResponseEntity<ImageDTO> sendGetImageRequest(String imageId){
        HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = "http://localhost:" + port + "/images/" + imageId;
            ResponseEntity<ImageDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ImageDTO.class
            );
    
            return response;
    }

    public ResponseEntity<Void> sendDeleteImageRequest(String imageId){
        
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);
    
            String url = "http://localhost:" + port + "/images/" + imageId;
            ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void.class
            );
    
            return response;
    }

    public ResponseEntity<List<ImageDTO>> sendGetAllImagesRequest() {
        HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
    
            HttpEntity<Void> request = new HttpEntity<>(headers);
    
            String url = "http://localhost:" + port + "/images";
            
            ResponseEntity<List<ImageDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<ImageDTO>>() {}
            );
            return response;
    }

    public ResponseEntity<InputDTO> sendGetInputsRequest(String imageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = "http://localhost:" + port + "/images/" + imageId + "/inputs";
        ResponseEntity<InputDTO> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            InputDTO.class
        );

        return response;
    }
     
}
