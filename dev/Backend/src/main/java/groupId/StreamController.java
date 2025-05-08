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


@RestController
@RequestMapping("/")
public class StreamController {

    private final ImageController controller;
    private final SimpMessagingTemplate messagingTemplate;
    private ModelInterface model;
    private CompletableFuture<SolutionDTO> futureSolution;

    @Autowired
    public StreamController(ImageController imageController, SimpMessagingTemplate messagingTemplate) {
        this.controller = imageController;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Start a new SCIP solving process
     */
    @PostMapping("/solve/start")
    public ResponseEntity<String> startSolve(@RequestBody SolveCommandDTO request) throws Exception {
        futureSolution = controller.solveAsync(request);
        model = controller.getModelCurrentlySolving();
        return ResponseEntity.ok("started");
    }

    /**
     * Pause the solving process
     */
    @PostMapping("/solve/pause")
    public ResponseEntity<String> pauseSolve() throws Exception {
        model.pause();
        return ResponseEntity.ok("pause");
    }

    /**
     * Continue the solving process
     */
    @PostMapping("/solve/continue")
    public ResponseEntity<String> continueSolve() throws Exception {
        model.continueProcess();
        return ResponseEntity.ok("continue");
    }

    /**
     poll solving status
     */
    @PostMapping("/solve/poll")
    public ResponseEntity<String> pollSolve() {
        String ans = model.poll();
        return ResponseEntity.ok(ans);
    }

    /**
     
     */
    @PostMapping("/solve/pollSolution")
    public ResponseEntity<String> pollSolution() throws Exception {
        if(futureSolution.isDone())
            return ResponseEntity.ok(futureSolution.get().toString());
        else   
            return ResponseEntity.ok("not done yet");
    }

    /**
     * Finish the solving process
     */
    @PostMapping("/solve/finish")
    public ResponseEntity<String> finishSolve() throws Exception {
        model.finish();
        return ResponseEntity.ok("finish");
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