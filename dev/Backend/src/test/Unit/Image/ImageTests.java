package Unit.Image;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import DataAccess.ImageRepository;
import DataAccess.ModelRepository;
import Image.Image;
import Image.Modules.ConstraintModule;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelSet;
import Model.ModelVariable;
import groupId.Main;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(classes = Main.class)
//@ComponentScan(basePackages = {"Model", "DataAccess","DataAccess.LocalStorage", "Image.Modules"})
//@ExtendWith(SpringExtension.class)
//@ActiveProfiles("test") 
//@Transactional
@TestPropertySource(properties = {
    "storage.type=local",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ImageTests {

    private static String sourcePath = "src/main/resources/zimpl/ExampleZimplProgram.zpl";
    private static String TEST_FILE_PATH = "src/main/resources/zimpl/ExampleZimplProgramINSTANCE.zpl";
    private static String sourceId = "ExampleZimplProgram";
    
    private static Image image;

    private static ModelRepository modelRepository;
    private static ImageRepository imageRepository;

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;


    private static int numberOfImagesToCreate = 4;
    private static List<Image> images;

    @Autowired
    public void setModelRepository(ImageRepository injected1, ModelRepository injected2) {
        imageRepository = injected1;
        modelRepository = injected2;
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
        image = new Image(sourceId, modelRepository);
    }

    @Test
    public void testTest() throws Exception {
        assertTrue(true);
    }

    private void printAllTables() {
        String sql = "SHOW TABLES;";
        jdbcTemplate.queryForList(sql).forEach(row -> {
            System.out.println("ROW:"+row);
        });
    }

    private void printTable(String tableName) {
        String sql = "SELECT * FROM "+tableName; // Use DESCRIBE or equivalent in your DBMS (works in MySQL, MariaDB, etc.)
        System.out.println("TABLE: "+tableName);
        jdbcTemplate.queryForList(sql).forEach(row -> {
            System.out.println("ROW:"+row);
        });
    }

    private void commit(){
        entityManager.flush();
        entityManager.clear();
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

        image.setVariablesModule(vars,sets,params);
        imageRepository.save(image);
        commit();
        Image fetchedIm = imageRepository.findById(sourceId).get();
        assertFalse(fetchedIm == image); // check not same runtime instance, but a deep copy
        printTable("images");
        printTable("modules");
        
        assertTrue(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()).size() > 0);

        assertEquals(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()),
                    image.getVariables().keySet().stream().collect(Collectors.toSet()));

        image.addConstraintModule("myConstraint", "desc", List.of("trivial4","trivial5"), List.of(), List.of());
        imageRepository.save(image);
        commit();
        fetchedIm = imageRepository.findById(sourceId).get();
        assertTrue(fetchedIm.getConstraintModule("myConstraint") != null);
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
       
        modelRepository.deleteDocument(sourceId);
       
    }


}
