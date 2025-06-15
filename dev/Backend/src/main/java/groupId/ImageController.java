package groupId;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.el.util.ConcurrentCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    private Map<String,Boolean> solvingRequests;
    private final ReentrantReadWriteLock imageLock;
    private final ScheduledExecutorService garbageCollector;
    private static final int GC_INTERVAL_SECONDS = 5;

    @Autowired
    public ImageController(ImageRepository imageRepository, ModelFactory modelFactory, EntityManager em, Solver solverService) {
        this.imageRepository = imageRepository;
        this.modelFactory = modelFactory;
        this.entityManager = em;
        Image.setModelFactory(modelFactory);
        this.solverService = solverService;
        this.solvingRequests = new ConcurrentHashMap<>();
        this.imageLock = new ReentrantReadWriteLock();
        
        // Initialize and start the garbage collector
        this.garbageCollector = Executors.newSingleThreadScheduledExecutor();
        this.garbageCollector.scheduleAtFixedRate(this::collectGarbage, GC_INTERVAL_SECONDS, GC_INTERVAL_SECONDS, TimeUnit.SECONDS);
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
            String compilationResult = solverService.isCompiling(id, 17);
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
            try{
                modelFactory.deleteModel(id);
            } catch (Exception exception) {
                System.err.println("Error in deleting model: " + e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            // Clean up and wrap any other exceptions
            try{
                modelFactory.deleteModel(id);
            } catch (Exception exception) {
                System.err.println("Error in deleting model: " + e.getMessage());
            }
            throw new BadRequestException("Failed to validate image code: " + e.getMessage());
        }
    }
    
    @Transactional
    public SolutionDTO solve(SolveCommandDTO command) throws Exception {
        long start = System.currentTimeMillis();
        Image image = imageRepository.findById(command.imageId()).get();
        long imageFoundTime = System.currentTimeMillis() - start;
        String solveRequest = image.prepareInput(command.input());
        solvingRequests.put(solveRequest, false);
        long inputPreparedTimes = System.currentTimeMillis() - start;
        Solution solution = solverService.solve(solveRequest, command.timeout(), command.solverSettings());
        long inputSolveTime = System.currentTimeMillis() - start;
        long inputRestoredTime = 0;
        solvingRequests.put(solveRequest, true);
        SolutionDTO solutionDTO = image.parseSolution(solution);
        long solutionParsedTime = System.currentTimeMillis() - start;
        System.out.println("Image found time: " + imageFoundTime + " | Input prepared time: " + inputPreparedTimes + " | Solve time: " + inputSolveTime + " | Input restored time: " + inputRestoredTime + " | Solution parsed time: " + solutionParsedTime);
        return solutionDTO;
    }

    @Transactional
    public CompletableFuture<SolutionDTO> solveAsync(SolveCommandDTO command) throws Exception {
        Image image = imageRepository.findById(command.imageId()).get();
        String solveRequest = image.prepareInput(command.input());
        solvingRequests.put(solveRequest, false);
        CompletableFuture<Solution> solution = solverService.solveAsync(solveRequest, command.timeout(), command.solverSettings());
        return solution.thenApply(sol -> {
            solvingRequests.put(solveRequest, true);
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ImageDTO getImage(String id) {
        imageLock.readLock().lock();
        try {
            Image image = imageRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Image not found"));
            return RecordFactory.makeDTO(image);
        } finally {
            imageLock.readLock().unlock();
        }
    }

    @Transactional
    InputDTO loadLastInput(String imageId) throws Exception {
        return imageRepository.findById(imageId).get().getInput();
    }

    //Gets only 'ready' images - ones that are configured and ready to be used.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ImageDTO> getAllImages() {
        imageLock.readLock().lock();
        try {
            List<ImageDTO> ans = new LinkedList<>();
            for(Image image : imageRepository.findAll()){
                if(image.isConfigured()){
                    ans.add(RecordFactory.makeDTO(image));
                }
            }
            return ans;
        } finally {
            imageLock.readLock().unlock();
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteImage(String imageId) throws Exception {
        imageLock.writeLock().lock();
        try {
            imageRepository.deleteById(imageId);
            System.gc();
            modelFactory.deleteModel(imageId);
        } finally {
            imageLock.writeLock().unlock();
        }
    }

    public Solver getSolver(){
        return solverService;
    }

    // Add a shutdown method to clean up resources
    public void shutdown() {
        if (garbageCollector != null) {
            garbageCollector.shutdown();
            try {
                if (!garbageCollector.awaitTermination(60, TimeUnit.SECONDS)) {
                    garbageCollector.shutdownNow();
                }
            } catch (InterruptedException e) {
                garbageCollector.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void collectGarbage() {
        try {
            for (Map.Entry<String, Boolean> entry : solvingRequests.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    String solveRequest = entry.getKey();
                    try {
                        // Image image = imageRepository.findById(solveRequest.split("\\.")[0]).get();
                        // image.restoreInput(solveRequest);
                        modelFactory.getRepository().deleteDocument(solveRequest);
                        modelFactory.getRepository().deleteDocument(solveRequest+"SOLUTION");
                        solvingRequests.remove(solveRequest);
                    } catch (Exception e) {
                        // Log the error but continue with other requests
                        // System.err.println("Failed to delete temporary file " + solveRequest + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in garbage collector: " + e.getMessage());
        }
    }
}
