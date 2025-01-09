package groupId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DTO.*;

@RestController
@RequestMapping("/")
public class Service implements ServiceInterface {
    private UserController controller;

    @Autowired 
    public Service(UserController controller) {
        this.controller = controller;
    }
    
    @PostMapping("/images")
    public ResponseEntity<ImageDTO> createImage(@RequestBody CreateImageDTO zplFile) throws IOException {
        ImageDTO response = controller.createImage(zplFile);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/images")
    public ResponseEntity<Void> configureImage(@RequestBody ImageConfigDTO imgConfig){
        controller.configureImage(imgConfig);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/solve")
    public ResponseEntity<SolutionDTO> solve(@RequestBody ImageInputDTO input) {
        SolutionDTO res = controller.solve(input);
        return ResponseEntity.ok(res);
    }
}