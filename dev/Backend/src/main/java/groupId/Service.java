package groupId;

import java.io.IOException;

import DTO.Records.Requests.Commands.CreateImageFromPathDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Service implements ServiceInterface {
    private final UserController controller;

    @Autowired 
    public Service(UserController controller) {
        this.controller = controller;
    }
    
    @PostMapping("/images")
    public ResponseEntity<ImageResponseDTO> createImage(@RequestBody CreateImageFromPathDTO zplFile) throws IOException {
        ImageResponseDTO response = controller.createImageFromPath(zplFile.path());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/images")
    public ResponseEntity<Void> configureImage(@RequestBody ImageConfigDTO imgConfig){
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/solve")
    public ResponseEntity<SolutionDTO> solve(@RequestBody SolveCommandDTO input) {
        SolutionDTO res = controller.solve(input);
        return ResponseEntity.ok(res);
    }
}