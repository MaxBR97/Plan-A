package groupId;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import Model.ModelInterface;
import jakarta.validation.Valid;
import SolverService.SolverService;
import SolverService.Solver;
import SolverService.StreamSolver;

@RestController
@RequestMapping("/")
public class StreamController {

    private final ImageController controller;
    private final SimpMessagingTemplate messagingTemplate;
    private StreamSolver model;
    private CompletableFuture<SolutionDTO> futureSolution;

    @Autowired
    public StreamController(ImageController imageController, SimpMessagingTemplate messagingTemplate) {
        this.controller = imageController;
        this.messagingTemplate = messagingTemplate;
    }


    @PostMapping("/solve/start")
    public ResponseEntity<SolutionDTO> startSolve(@RequestBody SolveCommandDTO request) throws Exception {
        futureSolution = controller.solveAsync(request, false);
        model = controller.getSolver();
        return ResponseEntity.ok(futureSolution.get());
    }

    /**
     * Continue the solving process
     */
    @PostMapping("/solve/continue")
    public ResponseEntity<SolutionDTO> continueSolve(@RequestBody SolveCommandDTO request) throws Exception {
        if(request != null && request.timeout() > 0)
            futureSolution = controller.solveAsync(request,true);
        else
            futureSolution = controller.solveAsync(request,true);
        return ResponseEntity.ok(futureSolution.get());
    }

    /**
     poll solving status
     */
    @PostMapping("/solve/poll")
    public ResponseEntity<String> pollLog() throws Exception{
        String ans = model.pollLog();
        return ResponseEntity.ok(ans);
    }

    // @Scheduled(fixedRate = 1000)
    // public void sendCustomMessage() {
    //     // Create a random number between 0 and 100.
    //     Random random = new Random();
    //     int randomNumber = random.nextInt(101);

    //     // Construct a custom message.
    //     String customMessage = "Random number: " + randomNumber + ", Model: " + model;

    //     messagingTemplate.convertAndSend("/topic", customMessage);
    // }
}