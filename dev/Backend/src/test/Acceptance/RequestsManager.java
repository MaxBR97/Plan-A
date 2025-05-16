package Acceptance;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ExceptionDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RequestsManager {
    int port;
    WebClient webClient;
    private StompSession stompSession;
    private WebSocketStompClient stompClient;

    CreateImageRequestBuilder createImageReq;
    ConfigureImageRequestBuilder configImageReq;

    public RequestsManager(int port, WebClient webClient) {
        this.port = port;
        this.webClient = webClient;
    }

    public ResponseEntity<?> sendCreateImageRequest(CreateImageFromFileDTO body) {
        try {
            CreateImageResponseDTO result = webClient.post()
                    .uri("/images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(CreateImageResponseDTO.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendConfigImageRequest(ImageConfigDTO body) {
        try {
            webClient.patch()
                    .uri("/images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendSolveRequest(SolveCommandDTO body) {
        try {
            SolutionDTO result = webClient.post()
                    .uri("/solve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(SolutionDTO.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendPersistentSolve(SolveCommandDTO body) {
        try {
            return webClient.post()
                    .uri("/solve/start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> ResponseEntity.ok().build())
                    .block();
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendPollPersistentSolve() {
        try {
            String result = webClient.post()
                    .uri("/solve/poll")
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendContinuePersistentSolve(SolveCommandDTO body) {
        try {
            return webClient.post()
                    .uri("/solve/continue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> ResponseEntity.ok().build())
                    .block();
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendGetImageRequest(String imageId) {
        try {
            ImageDTO result = webClient.get()
                    .uri("/images/{id}", imageId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ImageDTO.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendDeleteImageRequest(String imageId) {
        try {
            webClient.delete()
                    .uri("/images/{id}", imageId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendGetAllImagesRequest() {
        try {
            List<ImageDTO> result = webClient.get()
                    .uri("/images")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ImageDTO>>() {})
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }

    public ResponseEntity<?> sendGetInputsRequest(String imageId) {
        try {
            InputDTO result = webClient.get()
                    .uri("/images/{id}/inputs", imageId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(InputDTO.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            ExceptionDTO errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }
    }
}
