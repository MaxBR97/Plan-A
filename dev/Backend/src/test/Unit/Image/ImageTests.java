package Unit.Image;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static String sourcePath = "src/test/Utilities/ZimplExamples/ExampleZimplProgram.zpl";
    private static String TEST_FILE_PATH = "src/test/Utilities/ZimplExamples/ExampleZimplProgramINSTANCE.zpl";
    private static String sourceId = "ExampleZimplProgram";
    
    private static Image image;

    private static ModelRepository modelRepository;
    private static ImageRepository imageRepository;

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;


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
    public void testTest() throws Exception{
        assertTrue(true);
    }

    private void printTable(String tableName) {
        String sql = "SELECT * FROM "+tableName; // Use DESCRIBE or equivalent in your DBMS (works in MySQL, MariaDB, etc.)
        System.out.println("TABLE: "+tableName);
        jdbcTemplate.queryForList(sql).forEach(row -> {
            System.out.println("ROW:"+row);
        });
    }

    
    @Test
    @Transactional
    //@Commit
    public void testSavingToRepository() throws Exception {
        ModelInterface m = image.getModel();
        Set<ModelVariable> vars = m.getVariables().stream().collect(Collectors.toSet());
        
        Collection<String> sets = m.getSets().stream()
        .map(ModelSet::getIdentifier)  
        .collect(Collectors.toList());

        Collection<String> params = m.getParameters().stream()
        .map(ModelParameter::getIdentifier)  
        .collect(Collectors.toSet());

        image.setVariablesModule(vars,sets,params);
        imageRepository.save(image);
        entityManager.flush();
        entityManager.clear();
        Image fetchedIm = imageRepository.findById(sourceId).get();
        assertFalse(fetchedIm == image); // not same runtime instance, but a deep copy
        printTable("images");
        printTable("variable_module");
        printTable("variables");
        assertTrue(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()).size() > 0);
        assertEquals(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()),
                    image.getVariables().keySet().stream().collect(Collectors.toSet()));
        // assertEquals(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()),
        //             image.getVariables().keySet().stream().collect(Collectors.toSet()));
        // assertEquals(fetchedIm.getVariables().keySet().stream().collect(Collectors.toSet()),
        //             image.getVariables().keySet().stream().collect(Collectors.toSet()));
    }

    @AfterAll
    public static void cleanUp() throws Exception {
       Path targetPath = Path.of(TEST_FILE_PATH);
       Files.deleteIfExists(targetPath);
       
       System.gc();
       
        modelRepository.deleteDocument(sourceId);
       
    }


}
