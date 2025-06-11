package groupId;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DataAccess.ImageRepository;
import Exceptions.BadRequestException;
import Image.Image;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.Solution;
import SolverService.Solver;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@RestController
public class ImageController {
    //private final Map<UUID,Image> images;
    @Autowired
    private final ImageRepository imageRepository;
    @Autowired
    private final ModelFactory modelFactory;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private Solver solverService;
    private ModelInterface currentlySolving;

    @Autowired
    public ImageController(ImageRepository imageRepository, ModelFactory modelFactory, EntityManager em, Solver solverService) {
        this.imageRepository = imageRepository;
        this.modelFactory = modelFactory;
        this.entityManager = em;
        Image.setModelFactory(modelFactory);
        this.solverService = solverService;
    }

    @Transactional
    public CreateImageResponseDTO createImageFromFile(CreateImageFromFileDTO command) throws Exception {
        if(command.code().length() > 65536 ){ // 8 Memory Pages
            throw new BadRequestException("Code size exceeded 64KB");
        }
        
        String id = UUID.randomUUID().toString();
        modelFactory.uploadNewModel(id, command.code());
        
        try {
            // First check compilation using solver
            String compilationResult = solverService.isCompiling(id, 13);
            if (compilationResult != null && !compilationResult.isEmpty()) {
                throw new BadRequestException("Code compilation failed: " + compilationResult);
            }
            
            // Create image and validate for empty sets
            Image image = new Image(
                id,
                command.imageName(),
                command.imageDescription(),
                command.owner(),
                command.isPrivate() == null ? true : command.isPrivate()
            );
            System.gc();
            // Validate no empty sets
            image.validateCode();
            // If all validation passes, save the image
            imageRepository.save(image);
            return RecordFactory.makeDTO(id, image.getModel());
        } catch (BadRequestException e) {
            // Clean up the model if validation fails
            modelFactory.deleteModel(id);
            throw e;
        } catch (Exception e) {
            // Clean up and wrap any other exceptions
            modelFactory.deleteModel(id);
            throw new BadRequestException("Failed to validate image code: " + e.getMessage());
        }
    }
    
    @Transactional
    public SolutionDTO solve(SolveCommandDTO command) throws Exception {
        Image image = imageRepository.findById(command.imageId()).get();
        image.prepareInput(command.input());
        Solution solution = solverService.solve(image.getId(), command.timeout(), command.solverSettings());
        try {
            image.restoreInput();
        } catch (Exception e) {
            throw new RuntimeException("IO exception while restoring input, message: "+ e);
        }
        return image.parseSolution(solution);
    }

    @Transactional
    public CompletableFuture<SolutionDTO> solveAsync(SolveCommandDTO command) throws Exception {
        Image image = imageRepository.findById(command.imageId()).get();
        image.prepareInput(command.input());
        CompletableFuture<Solution> solution = solverService.solveAsync(image.getId(), command.timeout(), command.solverSettings());
        return solution.thenApply(sol -> {
            try {
                image.restoreInput();
            } catch (Exception e) {
                throw new RuntimeException("IO exception while restoring input, message: "+ e);
            }
            return image.parseSolution(sol);
        });
    }

    @Transactional
    public void updateImage(ImageConfigDTO imgConfig) throws Exception {
        ImageDTO imageDTO = imgConfig.image();
        String imageId = imgConfig.imageId();
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BadRequestException("Invalid imageId in image config"));

        image.update(imageDTO);

        imageRepository.save(image);
    }

    @Transactional
    public ImageDTO getImage(String id) {
        Image image = imageRepository.findById(id).get();
        return RecordFactory.makeDTO(image);
    }

    @Transactional
    InputDTO loadLastInput(String imageId) throws Exception {
        return imageRepository.findById(imageId).get().getInput();
    }

    //Gets only 'ready' images - ones that are configured and ready to be used.
    @Transactional
    public List<ImageDTO> getAllImages() {
        List<ImageDTO> ans = new LinkedList<>();
        for(Image image : imageRepository.findAll()){
            if(image.isConfigured()){
                ans.add(RecordFactory.makeDTO(image));
            }
        }
        return ans;
    }

    @Transactional
    void deleteImage(String imageId) throws Exception {
        imageRepository.deleteById(imageId);
        System.gc();
        modelFactory.deleteModel(imageId);
    }

    public Solver getSolver(){
        return solverService;
    }
}
