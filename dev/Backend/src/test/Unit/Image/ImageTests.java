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
import DTO.Records.Image.VariableModuleDTO;
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

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;


    private static int numberOfImagesToCreate = 4;
    private static List<Image> images;

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
                var.getBoundSet() != null ? var.getBoundSet().getIdentifier() : null
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
