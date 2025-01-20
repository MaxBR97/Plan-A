package groupId;

import java.io.IOException;

import DTO.Records.Commands.CreateImageDTO;
import DTO.Records.Commands.ImageConfigDTO;
import DTO.Records.Commands.ImageInputDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@RestController
@RequestMapping("/")
public class Service implements ServiceInterface {
    private final UserController controller;

    @Autowired 
    public Service(UserController controller) {
        this.controller = controller;
    }

    @GetMapping(value = {"/", "/{path:^(?!api|static).*$}/**"})
    public ResponseEntity<Resource> serveHomePage() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(resource);
    }

    
    @PostMapping("/images")
    public ResponseEntity<ImageDTO> createImage(@RequestBody CreateImageDTO zplFile) throws Exception {
        ImageDTO response = controller.createImage(zplFile);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/images")
    public ResponseEntity<Void> configureImage(@RequestBody ImageConfigDTO imgConfig) throws Exception{
        controller.configureImage(imgConfig);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/solve")
    public ResponseEntity<SolutionDTO> solve(@RequestBody ImageInputDTO input) throws Exception {
        SolutionDTO res = controller.solve(input);
        return ResponseEntity.ok(res);
    }

}