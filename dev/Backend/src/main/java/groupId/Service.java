package groupId;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
@RequestMapping("/api")
public class Service {
    private final ImageController controller;
    @Autowired 
    public Service(ImageController controller) {
        this.controller = controller;
    }

    // Helper method to get current user's username if authenticated
    private Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .map(auth -> {
                if (auth != null && auth.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) auth.getPrincipal();
                    return jwt.getClaimAsString("preferred_username");
                }
                return null;
            })
            .defaultIfEmpty("Guest");
    }

    // Helper method to get current user's ID if authenticated
    private Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .map(auth -> {
                if (auth != null && auth.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) auth.getPrincipal();
                    return jwt.getSubject();
                }
                return null;
            });
    }

    // Public routes - no authentication required
    @GetMapping("/public/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }

    @GetMapping("/images/search")
    public Mono<ResponseEntity<List<ImageDTO>>> searchPublicImages(String searchPhrase) {
        return getCurrentUsername()
            .flatMap(username -> {
                try {
                    List<ImageDTO> res = controller.searchImages(username, searchPhrase);
                    return Mono.just(ResponseEntity.ok(res));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @PostMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<CreateImageResponseDTO>> createImage(@Valid @RequestBody CreateImageFromFileDTO data) {
        return Mono.zip(getCurrentUsername(), getCurrentUserId())
            .flatMap(tuple -> {
                String username = tuple.getT1();
                String userId = tuple.getT2();
                System.out.println("Creating image for user: " + username + " (ID: " + userId + ")");
                try {
                    CreateImageResponseDTO response = controller.createImageFromFile(username, data);
                    return Mono.just(ResponseEntity.ok(response));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @PatchMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> updateImage(@Valid @RequestBody ImageConfigDTO imgConfig) {
        return getCurrentUsername()
            .flatMap(username -> {
                System.out.println("Updating image for user: " + username);
                try {
                    controller.updateImage(username, imgConfig);
                    return Mono.just(ResponseEntity.ok().<Void>build());
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @PostMapping("/solve")
    public Mono<ResponseEntity<SolutionDTO>> solve(@Valid @RequestBody SolveCommandDTO input) {
        return getCurrentUsername()
            .flatMap(username -> {
                if (username != null) {
                    System.out.println("Solving for authenticated user: " + username);
                } else {
                    System.out.println("Solving for anonymous user");
                }
                try {
                    SolutionDTO res = controller.solve(input);
                    return Mono.just(ResponseEntity.ok(res));
                } catch (Exception e) {
                    return Mono.error(e); // ✅ preserve original exception
                }
            });
    }

    @GetMapping("/images/{id}/inputs")
    public Mono<ResponseEntity<InputDTO>> loadImageInput(@PathVariable("id") String imageId) {
        return getCurrentUsername()
            .flatMap(username -> {
                if (username != null) {
                    System.out.println("Loading inputs for authenticated user: " + username);
                }
                try {
                    InputDTO res = controller.loadLastInput(imageId);
                    return Mono.just(ResponseEntity.ok(res));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @GetMapping("/images/{id}")
    public Mono<ResponseEntity<ImageDTO>> getImage(@PathVariable("id") String imageId) {
        return getCurrentUsername()
            .flatMap(username -> {
                if (username != null) {
                    System.out.println("Getting image for authenticated user: " + username);
                }
                try {
                    ImageDTO res = controller.getImage(username, imageId);
                    if (res == null) {
                        return Mono.just(ResponseEntity.notFound().<ImageDTO>build());
                    }
                    return Mono.just(ResponseEntity.ok(res));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @GetMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<ImageDTO>>> getAllUserImages() {
        return Mono.zip(getCurrentUsername(), getCurrentUserId())
            .flatMap(tuple -> {
                String username = tuple.getT1();
                String userId = tuple.getT2();
                System.out.println("Getting all images for user: " + username + " (ID: " + userId + ")");
                try {
                    List<ImageDTO> res = controller.getAllUserImages(username);
                    return Mono.just(ResponseEntity.ok(res));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @DeleteMapping("/images/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> deleteImage(@PathVariable("id") String imageId) {
        return getCurrentUsername()
            .flatMap(username -> {
                System.out.println("Deleting image for user: " + username);
                try {
                    controller.deleteImage(username, imageId);
                    return Mono.just(ResponseEntity.ok().<Void>build());
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
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