package groupId;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DataAccess.ImageRepository;
import Exceptions.InternalErrors.BadRequestException;
import Image.Image;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelPreference;
import Model.ModelType;
import Model.ModelVariable;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import SolverService.Solver;
import SolverService.StreamSolver;
import Model.Solution;

@RestController
public class ImageController {
    //private final Map<UUID,Image> images;
    @Autowired
    private final ImageRepository imageRepository;
    @Autowired
    private final ModelFactory modelFactory;
    @Autowired
    private EntityManager entityManager;
    //should be enabled only when running in desktop app
    private Image currentlyCached;
    private StreamSolver solverService;
    private ModelInterface currentlySolving;

    @Autowired
    public ImageController(ImageRepository imageRepository, ModelFactory modelFactory, EntityManager em, StreamSolver solverService) {
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
        modelFactory.uploadNewModel(id,command.code());
        Image image = new Image(
        id,
        command.imageName(),
        command.imageDescription(),
        command.owner(),
        command.isPrivate() == null ? true : command.isPrivate());
        imageRepository.save(image);
        return RecordFactory.makeDTO(id, image.getModel());
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
    public CompletableFuture<SolutionDTO> solveAsync(SolveCommandDTO command, boolean continueLast) throws Exception {
        Image image = currentlyCached != null ? currentlyCached : imageRepository.findById(command.imageId()).get();
        // currentlyCached = image;
        // currentlySolving = image.getModel();
        image.prepareInput(command.input());
        CompletableFuture<Solution> solution = continueLast ? solverService.continueSolve(command.timeout()) : solverService.solveAsync(image.getId(), command.timeout(), command.solverSettings());
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
        Image image = currentlyCached != null && currentlyCached.getId().equals(id) ? currentlyCached : imageRepository.findById(id).get();
        // currentlyCached = image;
        return RecordFactory.makeDTO(image);
    }

    @Transactional
    InputDTO loadLastInput(String imageId) throws Exception {
        return imageRepository.findById(imageId).get().getInput();
    }

    @Transactional
    public List<ImageDTO> getAllImages() {
        List<ImageDTO> ans = new LinkedList<>();
        for(Image image : imageRepository.findAll()){
            ans.add(RecordFactory.makeDTO(image));
        }
        return ans;
    }

    @Transactional
    void deleteImage(String imageId) throws Exception {
        imageRepository.deleteById(imageId);
        System.gc();
        modelFactory.deleteModel(imageId);
        // if(currentlyCached.getId().equals(imageId))
        //     currentlyCached = null;
    }

    public StreamSolver getSolver(){
        return solverService;
    }
}
