package groupId;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import jakarta.validation.Valid;

//TODO: make all returned status codes to comply with best practices/REST conventions including /api routes
@RestController
@RequestMapping("/")
public class Service implements ServiceInterface {
    private final ImageController controller;

    @Autowired 
    public Service(ImageController controller) {
        this.controller = controller;
    }

    @GetMapping(value = {"/"/*, "/{path:^(?!api|static).*$}/**"*/})
    public ResponseEntity<Resource> serveHomePage() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(resource);
    }
    
    @PostMapping("/images")
    public ResponseEntity<CreateImageResponseDTO> createImage(@Valid @RequestBody CreateImageFromFileDTO data) throws Exception {
        CreateImageResponseDTO response = controller.createImageFromFile(data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/images")
    public ResponseEntity<Void> configureImage(@Valid @RequestBody ImageConfigDTO imgConfig) throws Exception{
        controller.overrideImage(imgConfig);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/solve")
    public ResponseEntity<SolutionDTO> solve(@Valid @RequestBody SolveCommandDTO input) throws Exception {
        SolutionDTO res = controller.solve(input);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/images/{id}/inputs")
    public ResponseEntity<InputDTO> loadImageInput(@PathVariable("id") String imageId) throws Exception {
        InputDTO res = controller.loadLastInput(imageId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<ImageDTO> getImage(@PathVariable("id") String imageId) throws Exception {
        ImageDTO res = controller.getImage(imageId);
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(res);
    }

    //in the future will become get all current user's owned images.
    @GetMapping("/images")
    public ResponseEntity<List<ImageDTO>> getAllImages() throws Exception {
        List<ImageDTO> res = controller.getAllImages();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable("id") String imageId) throws Exception {
        controller.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }

    // @PostMapping("/images/{id}/solutions")
    // public ResponseEntity<Void> saveSolution(
    //         @PathVariable("id") String imageId,
    //         @Valid @RequestBody SolutionDTO solutionDTO) throws Exception {
    //     controller.saveSolution(imageId, solutionDTO);
    //     return ResponseEntity.status(HttpStatus.CREATED).build();
    // }

    // @GetMapping("/images/{id}/solutions/{name}")
    // public ResponseEntity<SolutionDTO> getSavedSolution(
    //     @PathVariable("id") String imageId,
    //     @PathVariable("name") String solutionName) throws Exception {
    //     SolutionDTO solution = controller.getSavedSolution(imageId, solutionName);
    //     if (solution == null) {
    //         return ResponseEntity.notFound().build();
    //     }
    //     return ResponseEntity.ok(solution);
    // }


    // @DeleteMapping("/images/{id}/solutions/{name}")
    // public ResponseEntity<Void> deleteSavedSolution(
    //         @PathVariable("id") String imageId,
    //         @PathVariable("name") String solutionName) throws Exception {
    //     controller.deleteSavedSolution(imageId, solutionName);
    //     return ResponseEntity.noContent().build();
    // }



    //TODO: remove this getter. Temporarily exists to support bad tests.
    public ImageController getImageController() {
        return this.controller;
    }
}