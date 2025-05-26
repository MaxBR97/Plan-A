package Unit.Image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import DataAccess.ImageRepository;
import DataAccess.ModelRepository;
import Image.Image;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelSet;
import Model.ModelVariable;
import Model.ModelType;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import groupId.Main;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;


@SpringBootTest(classes = Main.class)
@ActiveProfiles({"H2mem", "S3-test", "securityAndGateway"})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ImageTests extends TestWithPersistence {

    private static String sourcePath = "src/main/resources/zimpl/ExampleZimplProgram.zpl";
    private static String TEST_FILE_PATH = "src/main/resources/zimpl/ExampleZimplProgramINSTANCE.zpl";
    private static String sourceId = "ExampleZimplProgram";
    
    private static Image image;

    private static ModelRepository modelRepository;
    private static ImageRepository imageRepository;
    private static ModelFactory modelFactory;

    // Common SetDefinitionDTOs
    SetDefinitionDTO preassign_soldier_shifts = new SetDefinitionDTO(
            "preassign_soldier_shifts",
            Arrays.asList("tag1","tag2","tag3"),
            Arrays.asList("INT","TEXT","INT"),
            "some alias"
    );

        SetDefinitionDTO stations = new SetDefinitionDTO(
            "Stations",
            Arrays.asList("tag1"),
            Arrays.asList("TEXT"),
            "stations"
        );

        SetDefinitionDTO times = new SetDefinitionDTO(
            "Times",
            Arrays.asList("tag1"),
            Arrays.asList("INT"),
            "times alias"
        );

    // Common ParameterDefinitionDTOs
    ParameterDefinitionDTO soldiers = new ParameterDefinitionDTO(
        "soldiers",
        "tag1",
        "INT",
        "soldiers alias"
    );

    ParameterDefinitionDTO weight = new ParameterDefinitionDTO(
        "weight",
        "tag1",
        "INT",
        "weight alias"
    );

    // Common VariableDTOs
    VariableDTO edge = new VariableDTO(
        "edge",
        Arrays.asList("tag1","tag2","tag3"),
        Arrays.asList("INT","TEXT","INT"),
        new DependenciesDTO(Set.of("Stations","Times"), Set.of("soldiers")),
        null,
        true
    );

    ConstraintDTO preassign = new ConstraintDTO(
        "preassign",
        new DependenciesDTO(Set.of(preassign_soldier_shifts.name()), Set.of())
    );

    // Common ConstraintDTOs
    ConstraintDTO trivial4 = new ConstraintDTO(
        "trivial4", 
        new DependenciesDTO(Set.of("Stations","Times"), Set.of("soldiers"))
    );

    ConstraintDTO trivial5 = new ConstraintDTO(
        "trivial5", 
        new DependenciesDTO(Set.of("Stations","Times"), Set.of("soldiers"))
    );

    PreferenceDTO pref1 = new PreferenceDTO(
        "((maxGuards-minGuards)+weight)**3",
        new DependenciesDTO(Set.of(), Set.of("weight"))
    );

    // Common ModuleDTOs
    ConstraintModuleDTO defaultConstraintModule = new ConstraintModuleDTO(
        "myConstraint",
        "Test module",
        Set.of(trivial4.identifier()),
        new HashSet<>(),
        new HashSet<>()
    );

    PreferenceModuleDTO defaultPreferenceModule = new PreferenceModuleDTO(
        "myPreference",
        "Test module",
        Set.of(pref1.identifier()),
        new HashSet<>(),
        new HashSet<>(),
        new HashSet<>()
    );

    VariableModuleDTO defaultVariableModule = new VariableModuleDTO(
        new HashSet<>(),
        new HashSet<>(),
        new HashSet<>()
    );

    @Autowired
    public void setModelRepository(ImageRepository injected1, ModelFactory factory) {
        imageRepository = injected1;
        modelFactory = factory;
        modelRepository = factory.getRepository();
    }

    

    @BeforeAll
    public static void setUpFile() throws Exception {
        Path sourcePath2 = Path.of(sourcePath);
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
        Files.copy(sourcePath2, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @BeforeEach
    public void setUp() throws Exception{
        InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(TEST_FILE_PATH)));
        modelRepository.uploadDocument(sourceId, inputStream);
        inputStream.close();
        image = new Image(sourceId, "Test Image", "Test Description", "testUser", false);
    }
    
    //Generic test for saving an image to the repository.
    @Test
    @Transactional
    public void testSavingToRepository() throws Exception {
        ModelInterface m = image.getModel();
        Set<ModelVariable> vars = m.getVariables().stream()
            .filter((ModelVariable v) -> v.getIdentifier().equals("edge"))
            .collect(Collectors.toSet());
        
        Collection<String> sets = m.getSets().stream()
        .map(ModelSet::getIdentifier)
        .filter((String name) -> name.matches("(Stations|Times)"))  
        .collect(Collectors.toList());

        Collection<String> params = m.getParameters().stream()
        .map(ModelParameter::getIdentifier)
        .filter((String name) -> name.matches("soldiers"))  
        .collect(Collectors.toSet());

        // Create VariableModuleDTO
        Set<VariableDTO> variableDTOs = new HashSet<>();
        Set<SetDefinitionDTO> setDTOs = new HashSet<>();
        Set<ParameterDefinitionDTO> paramDTOs = new HashSet<>();

        // Convert variables to DTOs
        for (ModelVariable var : vars) {
            List<String> types = var.getType().typeList();
            Set<ModelSet> setDeps = new HashSet<>();
            Set<ModelParameter> paramDeps = new HashSet<>();
            var.getPrimitiveSets(setDeps);
            var.getPrimitiveParameters(paramDeps);
            
            variableDTOs.add(new VariableDTO(
                var.getIdentifier(),
                Arrays.asList(var.getTags()),
                types,
                new DependenciesDTO(
                    setDeps.stream().map(ModelSet::getIdentifier).collect(Collectors.toSet()),
                    paramDeps.stream().map(ModelParameter::getIdentifier).collect(Collectors.toSet())
                ),
                var.getBoundSet() != null ? var.getBoundSet().getIdentifier() : null,var.isBinary()
            ));
        }

        // Convert sets to DTOs
        for (String setName : sets) {
            ModelSet set = m.getSet(setName);
            setDTOs.add(new SetDefinitionDTO(
                set.getIdentifier(),
                Arrays.asList(set.getTags()),
                set.getType().typeList(),
                set.getIdentifier()
            ));
        }

        // Convert parameters to DTOs
        for (String paramName : params) {
            ModelParameter param = m.getParameter(paramName);
            paramDTOs.add(new ParameterDefinitionDTO(
                param.getIdentifier(),
                param.getTags()[0],
                param.getType().toString(),
                param.getIdentifier()
            ));
        }

        VariableModuleDTO moduleDTO = new VariableModuleDTO(variableDTOs, setDTOs, paramDTOs);
        image.setVariablesModule(moduleDTO);
        
        imageRepository.save(image);
        commit();
        imageRepository.deleteById(image.getId());
        commit();
        imageRepository.save(image);
        commit();
        Image fetchedIm = imageRepository.findById(sourceId).get();
        assertFalse(fetchedIm == image); // check not same runtime instance, but a deep copy
        printAllTables();
        
        assertTrue(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()).size() > 0);

        assertEquals(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()),
                    image.getVariables().keySet().stream().collect(Collectors.toSet()));

        // Create a ConstraintModuleDTO with the constraints we want to add
        Set<String> constraints = new HashSet<>(Arrays.asList("trivial4", "trivial5"));
        Set<SetDefinitionDTO> inputSets = new HashSet<>();
        Set<ParameterDefinitionDTO> inputParams = new HashSet<>();
        
        ConstraintModuleDTO constraintModuleDTO = new ConstraintModuleDTO("myConstraint", "desc", constraints, inputSets, inputParams);
        image.addConstraintModule(constraintModuleDTO);
        
        imageRepository.save(image);
        commit();
        fetchedIm = imageRepository.findById(sourceId).get();
        assertTrue(fetchedIm.getConstraintsModule("myConstraint") != null);
        imageRepository.delete(fetchedIm);
        commit();
        assertThrows(NoSuchElementException.class, () -> imageRepository.findById(sourceId).get() );
    }

    /*
     * note: the following requirements might change or extend, but this is a good starting point.
     * 
     * glossary:
     * "X depends on A" - X is a ModelFunctionality, or ModelOutput,  A is ModelInput.
     *  X is defined by A directly or indirectly, where A is a "Leaf" ModelInput
     * 
     * "Leaf" - ModelInput which can directly receive inputs, not a composition of other ModelInputs
     * 
     * "A is the input of X" - X is a ModelFunctionality, or ModelOutput,  A is ModelInput.
     *                         A is chosen to dynamically receive inputs from the user (the opposite
     *                          it is hard coded and invisible to the user).
     */


    
    /*
     * constraint X depends on sets A, B
     * constraint Y depends on sets B, C
     * constraint Z depends on set C, param P
     * if const X is a part of a Module, const Y and Z must also be in that module
     */
    @Test
    @Transactional
    public void test_Module_Closure(){

    }

    /*
     * constraint X depends on sets A, B
     * constraint Y depends on sets B, C
     * constraint Z depends on set C, param P
     * if X,Y,Z consist a module, then that module depends on A,B,C,P
     */
    @Test
    @Transactional
    public void test_Module_Dependency_Closure(){

    }

    /*
     * constraint X depends on A, B
     * Variable Y depends on A,C
     * constraint X is part of module M
     * then the input of M may only be B, (A may be the input of Y if chosen.)
     */
    @Test
    @Transactional
    public void test_Variable_Superior_To_Constraint(){

    }

     /*
     * constraint X depends on A, B
     * preference Y depends on A, C
     * X is in module M with input A
     * it is illegal for Y's Module to have A as an input.
     */
    @Test
    @Transactional
    public void test_Constraint_Superior_To_Preference(){

    }

    @Test
    @Transactional
    public void When_Adding_Modules_With_Conflicting_Inputs_Then_Exception_Is_Thrown() throws Exception {
        // Create first module with Stations set and soldiers parameter
        ConstraintModuleDTO module1 = new ConstraintModuleDTO(
            "module1",
            "First module",
            Set.of(trivial4.identifier()),
            Set.of(stations),
            Set.of(soldiers)
        );

        // Successfully add first module
        image.addConstraintModule(module1);

        // Try to create second module with same input set
        ConstraintModuleDTO module2 = new ConstraintModuleDTO(
            "module2",
            "Second module",
            Set.of(trivial5.identifier()),
            Set.of(stations), // Same set as module1
            new HashSet<>()
        );

        // This should throw an IllegalArgumentException
        Exception exception = assertThrows(Exception.class, () -> {
            image.addConstraintModule(module2);
        });
        assertTrue(exception.getMessage().contains("module1"));
        assertTrue(exception.getMessage().contains("module2"));
        assertTrue(exception.getMessage().toLowerCase().contains("constraint"));
        assertTrue(exception.getMessage().contains("Stations"));

        // Try to create third module with same parameter
        ConstraintModuleDTO module3 = new ConstraintModuleDTO(
            "module3",
            "Third module",
            Set.of(trivial5.identifier()),
            new HashSet<>(),
            Set.of(soldiers) // Same parameter as module1
        );

        // This should throw an IllegalArgumentException
        exception = assertThrows(RuntimeException.class, () -> {
            image.addConstraintModule(module3);
        });

        assertTrue(exception.getMessage().contains("module1"));
        assertTrue(exception.getMessage().contains("module3"));
        assertTrue(exception.getMessage().toLowerCase().contains("constraint"));
        assertTrue(exception.getMessage().contains("soldiers"));

        // Verify that only the first module was added
        assertEquals(1, image.getConstraintsModules().size());
        assertTrue(image.getConstraintsModules().containsKey("module1"));
    }

    @Test
    @Transactional
    public void When_Updating_Image_With_Modules_With_Conflicting_Inputs_Then_Exception_Is_Thrown() throws Exception {
        // Create initial state with a variables module using soldiers parameter
        VariableModuleDTO variablesModule = new VariableModuleDTO(
            new HashSet<>(),
            new HashSet<>(),
            Set.of(soldiers)
        );
        
        // Create a constraints module trying to use the same parameter
        ConstraintModuleDTO constraintModule = new ConstraintModuleDTO(
            "constraintModule",
            "Test module",
            Set.of(trivial4.identifier()),
            new HashSet<>(),
            Set.of(soldiers) // Try to use the same parameter
        );
        
        // Create a preferences module also trying to use the same parameter
        PreferenceModuleDTO preferenceModule = new PreferenceModuleDTO(
            "preferenceModule",
            "Test module",
            Set.of("pref1"),
            new HashSet<>(),
            new HashSet<>(),
            Set.of(soldiers) // Use the same parameter as cost parameter
        );
        
        // Create ImageDTO with conflicting modules
        ImageDTO imageDTO = new ImageDTO(
            image.getId(),
            image.getName(),
            image.getDescription(),
            image.getOwner(),
            image.isPrivate(),
            image.getSolverScripts(),
            variablesModule,
            Set.of(constraintModule),
            Set.of(preferenceModule)
        );
        
        // This should throw an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            image.update(imageDTO);
        });
        
        // Verify the exception message contains relevant information
        String message = exception.getMessage();
        assertTrue(message.contains(preferenceModule.moduleName()));
        assertTrue(message.contains(constraintModule.moduleName()));
        assertTrue(message.toLowerCase().contains("preference"));
        assertTrue(message.contains("soldiers"));
        
        // Verify persistence - the original state should remain unchanged
        imageRepository.save(image);
        commit();
        
        Image fetchedImage = imageRepository.findById(image.getId()).get();
        assertEquals(0, fetchedImage.getVariablesModule().getInputParams().size());
        assertFalse(fetchedImage.getVariablesModule().isInput("soldiers"));
        assertTrue(fetchedImage.getConstraintsModules().isEmpty());
        assertTrue(fetchedImage.getPreferenceModules().isEmpty());
    }

    @Test
    @Transactional
    public void When_VariablesModule_BoundSet_Conflicts_With_ConstraintModule_InputSet_Then_Exception_Is_Thrown() throws Exception {
        // Create a variables module with a bound set
        VariableDTO boundEdge = new VariableDTO(
            edge.identifier(),
            edge.tags(),
            edge.type(),
            edge.dep(),
            preassign_soldier_shifts.name(),
            edge.isBinary()
        );
        
        VariableModuleDTO variablesModule = new VariableModuleDTO(
            Set.of(boundEdge),
            Set.of(),
            new HashSet<>()
        );
        image.setVariablesModule(variablesModule);
        
        // Create a constraints module trying to use the same set as input
        ConstraintModuleDTO constraintModule = new ConstraintModuleDTO(
            "myConstraint",
            "Test module",
            Set.of(preassign.identifier()),
            Set.of(preassign_soldier_shifts),
            new HashSet<>()
        );
        
        // Create ImageDTO with the conflicting modules
        ImageDTO imageDTO = new ImageDTO(
            image.getId(),
            image.getName(),
            image.getDescription()+"add this to description",
            image.getOwner(),
            image.isPrivate(),
            image.getSolverScripts(),
            variablesModule,
            Set.of(constraintModule),
            Set.of()
        );
        
        // This should throw an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            image.update(imageDTO);
        });
        
        // Verify the exception message contains relevant information
        String message = exception.getMessage();
        assertTrue(message.contains(preassign_soldier_shifts.name()),"the real message: "+message);
        
        // Verify persistence - the original state should remain unchanged
        imageRepository.save(image);
        commit();
        
        Image fetchedImage = imageRepository.findById(image.getId()).get();
        assertEquals(1, fetchedImage.getVariablesModule().getVariables().size());
        assertEquals(preassign_soldier_shifts.name(), fetchedImage.getVariablesModule().getVariables().get(boundEdge.identifier()).getBoundSet().getIdentifier());
        assertTrue(fetchedImage.getConstraintsModules().isEmpty());
        assertTrue(fetchedImage.getDescription().equals(image.getDescription())); // since the update failed, updating the description should also fail.
    }

    @Test
    @Transactional
    public void When_PreferenceModule_CostParam_Conflicts_With_VariablesModule_InputParam_Then_Exception_Is_Thrown() throws Exception {
        // Create a variables module with an input parameter
        VariableModuleDTO variablesModule = new VariableModuleDTO(
            new HashSet<>(),
            new HashSet<>(),
            Set.of(weight)
        );
        
        // Create a preferences module with the same parameter as cost parameter
        PreferenceModuleDTO preferenceModule = new PreferenceModuleDTO(
            "myPreference",
            "Test module",
            Set.of(pref1.identifier()),
            new HashSet<>(),
            new HashSet<>(),
            Set.of(weight)
        );
        
        // Create ImageDTO with the conflicting modules
        ImageDTO imageDTO = new ImageDTO(
            image.getId(),
            image.getName(),
            image.getDescription(),
            image.getOwner(),
            image.isPrivate(),
            image.getSolverScripts(),
            variablesModule,
            Set.of(),
            Set.of(preferenceModule)
        );
        
        // This should throw an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            image.update(imageDTO);
        });
        
        // Verify the exception message contains relevant information
        String message = exception.getMessage();
        assertTrue(message.contains(preferenceModule.moduleName()));
        assertTrue(message.contains(weight.name()));
        
        // Verify persistence - the original state should remain unchanged
        imageRepository.save(image);
        commit();
        
        Image fetchedImage = imageRepository.findById(image.getId()).get();
        assertEquals(0, fetchedImage.getVariablesModule().getInputParams().size());
        assertFalse(fetchedImage.getVariablesModule().isInput(weight.name()));
        assertTrue(fetchedImage.getPreferenceModules().isEmpty());
    }

    @Test
    @Transactional
    public void When_Image_Updated_Succeed() throws Exception {
        // First create an initial state with some modules
        VariableModuleDTO initialVarModule = new VariableModuleDTO(
            new HashSet<>(Set.of(edge)),
            new HashSet<>(),
            Set.of(soldiers)
        );
        
        // Create initial ImageDTO with constraint module
        ImageDTO initialImageDTO = new ImageDTO(
            image.getId(),
            image.getName(),
            image.getDescription(),
            image.getOwner(),
            image.isPrivate(),
            image.getSolverScripts(),
            initialVarModule,
            Set.of(defaultConstraintModule),
            Set.of()
        );
        
        // Update image with initial state
        image.update(initialImageDTO);
        imageRepository.save(image);
        commit();
        
            // Create update ImageDTO with completely different modules
        VariableModuleDTO newVarModule = new VariableModuleDTO(
            new HashSet<>(Set.of(edge)),
            new HashSet<>(Set.of(stations,times)),
            Set.of()
        );
        
        ImageDTO updateImageDTO = new ImageDTO(
            image.getId(),
            "Updated Name",
            "Updated Description",
            image.getOwner(),
            false, // change privacy
            Map.of("newScript", "script content"),
            newVarModule,
            Set.of(),
            Set.of(defaultPreferenceModule)
        );
        
        // Update the image
        image.update(updateImageDTO);
        imageRepository.save(image);
        commit();
        
        // Verify the update was successful
        Image fetchedImage = imageRepository.findById(image.getId()).get();
        
        // Check basic properties were updated
        assertEquals("Updated Name", fetchedImage.getName());
        assertEquals("Updated Description", fetchedImage.getDescription());
        assertFalse(fetchedImage.isPrivate());
        assertEquals("script content", fetchedImage.getSolverScripts().get("newScript"));
        
        // Check modules were completely replaced
        assertTrue(fetchedImage.getConstraintsModules().isEmpty());
        assertEquals(1, fetchedImage.getPreferenceModules().size());
        assertTrue(fetchedImage.getPreferenceModules().containsKey(defaultPreferenceModule.moduleName()));
        
        // Check variables module was updated
        assertEquals(0, fetchedImage.getVariablesModule().getInputParams().size());
        assertEquals(2, fetchedImage.getVariablesModule().getInputSets().size());
        assertFalse(fetchedImage.getVariablesModule().isInput(weight.name()));
    }

    @AfterAll
    public static void cleanUp() throws Exception {
       Path targetPath = Path.of(TEST_FILE_PATH);
       Files.deleteIfExists(targetPath);
       
       System.gc();
       int count = 0;
       while(count<20){
        try{
            modelRepository.deleteAll();
            Thread.sleep(100);
            break;
        } catch (Exception e){
            count++;
        }
       }
    }


}
