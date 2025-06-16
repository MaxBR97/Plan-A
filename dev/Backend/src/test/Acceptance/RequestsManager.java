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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RequestsManager.class);
    private final int port;
    private final WebClient webClient;
    private String currentUser;
    private StompSession stompSession;
    private WebSocketStompClient stompClient;

    CreateImageRequestBuilder createImageReq;
    ConfigureImageRequestBuilder configImageReq;

    public RequestsManager(int port, WebClient webClient, String initialUser) {
        this.port = port;
        this.webClient = webClient;
        this.currentUser = initialUser;
        logger.info("Initialized RequestsManager with port: {}, user: {}", port, initialUser);
    }

    public void setUser(String user) {
        this.currentUser = user;
    }

    private WebClient.RequestBodySpec addUserHeader(WebClient.RequestBodySpec request) {
        if (currentUser != null) {
            logger.debug("Adding headers for user: {}", currentUser);
            request.header("X-User-Name", currentUser);
            // Create a test JWT token with the current user's information
            String token = String.format("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6IiVzIiwicm9sZXMiOlsiVVNFUiJdfQ.test-signature", currentUser);
            request.header("Authorization", "Bearer " + token);
        }
        return request;
    }

    private WebClient.RequestHeadersSpec<?> addUserHeader(WebClient.RequestHeadersSpec<?> request) {
        if (currentUser != null) {
            logger.debug("Adding headers for user: {}", currentUser);
            request.header("X-User-Name", currentUser);
            // Create a test JWT token with the current user's information
            String token = String.format("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6IiVzIiwicm9sZXMiOlsiVVNFUiJdfQ.test-signature", currentUser);
            request.header("Authorization", "Bearer " + token);
        }
        return request;
    }

    public ResponseEntity<?> sendCreateImageRequest(CreateImageFromFileDTO body) {
        try {
            logger.info("Sending create image request to /api/images");
            CreateImageResponseDTO result = addUserHeader(webClient.post()
                    .uri("/api/images")
                    .contentType(MediaType.APPLICATION_JSON))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(CreateImageResponseDTO.class)
                    .block();
            logger.info("Create image request successful");
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException ex) {
            logger.error("Create image request failed with status: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            String responseBody = ex.getResponseBodyAsString();
            ExceptionDTO errorResponse;
            try {
                errorResponse = ex.getResponseBodyAs(ExceptionDTO.class);
            } catch (Exception decodeEx) {
                logger.error("Failed to decode error response, creating generic error", decodeEx);
                errorResponse = new ExceptionDTO(
                    ex.getStatusCode().toString(),
                    responseBody != null ? responseBody : "Unknown error"
                );
            }
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        } catch (Exception ex) {
            logger.error("Unexpected error in create image request", ex);
            throw ex;
        }
    }

    public ResponseEntity<?> sendConfigImageRequest(ImageConfigDTO body) {
        try {
            addUserHeader(webClient.patch()
                    .uri("/api/images")
                    .contentType(MediaType.APPLICATION_JSON))
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
            SolutionDTO result = addUserHeader(webClient.post()
                    .uri("/api/solve")
                    .contentType(MediaType.APPLICATION_JSON))
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
            return addUserHeader(webClient.post()
                    .uri("/solve/start")
                    .contentType(MediaType.APPLICATION_JSON))
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
            String result = addUserHeader(webClient.post()
                    .uri("/solve/poll")
                    .contentType(MediaType.APPLICATION_JSON))
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
            return addUserHeader(webClient.post()
                    .uri("/solve/continue")
                    .contentType(MediaType.APPLICATION_JSON))
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
            ImageDTO result = addUserHeader(webClient.get()
                    .uri("/api/images/{id}", imageId)
                    .accept(MediaType.APPLICATION_JSON))
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
            addUserHeader(webClient.delete()
                    .uri("/api/images/{id}", imageId))
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
            List<ImageDTO> result = addUserHeader(webClient.get()
                    .uri("/api/images")
                    .accept(MediaType.APPLICATION_JSON))
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
            InputDTO result = addUserHeader(webClient.get()
                    .uri("/api/images/{id}/inputs", imageId)
                    .accept(MediaType.APPLICATION_JSON))
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
