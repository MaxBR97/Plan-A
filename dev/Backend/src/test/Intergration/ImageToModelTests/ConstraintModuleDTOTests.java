package Intergration.ImageToModelTests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import DataAccess.ImageRepository;
import DataAccess.ModelRepository;
import Model.Model;
import Model.ModelConstraint;
import Model.ModelInterface;
import Model.ModelPreference;
import Model.Solution;
import groupId.Main;
import jakarta.persistence.EntityManager;

@SpringBootTest(classes = Main.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("inMemory")
public class ConstraintModuleDTOTests {
    static String SimpleCodeExample = """
                param x := 2;
                set mySet := {1,2,3};

                var myVar[mySet];

                subto sampleConstraint:
                    myVar[x] == mySet[1];

                maximize myObjective:
                    1;
            """;
    @Mock
    ModelInterface model;
    static String sourcePath = "src/test/Utilities/ZimplExamples/ExampleZimplProgram.zpl";
    static String sourceId = "ExampleZimplProgram";
    static Path tmpDirPath;

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
    public static void setup(){
        try {
            //System default tmp folder, for now I delete it at end of run, not 100% sure if should
            tmpDirPath= Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        }
        catch (IOException e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    @BeforeEach
    public void beforeEach() throws Exception {
        try {
            model = new Model(sourcePath);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }
    @Test
    public void GivenPath_WhenPathInvalid_ThrowsException() {
        String badPath= sourcePath+ "/IDontExist.zpl";
        //TODO: a custom exception will probably be a better choice, instead of letting IO be thrown upwards
        assertThrows(IOException.class, () -> new Model(badPath));
    }
    //TODO: Writer an easier to work with zimpl example, this one throws warnings
    @Test
    public void GivenZimplCode_WhenFetchingConstraints_FetchCorrect() {
        Collection<ModelConstraint> constraints = model.getConstraints();
        Set<String> actualIdentifiers= Set.of("trivial1","trivial2","trivial3","trivial4","trivial5",
                "Soldier_Not_In_Two_Stations_Concurrently","All_Stations_One_Soldier","minGuardsCons","maxGuardsCons", "minimalSpacingCons");
        Set<String> identifiers = constraints.stream()
                .map(ModelConstraint::getIdentifier)
                .collect(Collectors.toSet());
        assertEquals(10, constraints.size());
        assertEquals(actualIdentifiers, identifiers);
    }
    @Test
    public void GivenZimplCode_WhenFetchingPreferences_FetchCorrect() {
        Collection<ModelPreference> preferences = model.getPreferences();
        Set<String> identifiers = preferences.stream()
                .map(ModelPreference::getIdentifier)
                .collect(Collectors.toSet());
        System.out.println(identifiers);
        assertEquals(3, preferences.size());
        //TODO: Verify what the identifiers should actually be, since preferences don't have a defined name in zimpl
        //assertEquals(actualIdentifiers, identifiers);
    }
    @Test
    public void GivenValidZimplCode_WhenCompiling_ReturnsTrue() {
           assertTrue(model.isCompiling(1000));
    }

    
    @Test
    public void GivenInvalidZimplCode_WhenCompiling_ReturnsFalse() {
        try {
            Path badZimpl = tmpDirPath.resolve("badZimpl.zimpl");
            Files.copy(Path.of(sourcePath), badZimpl, StandardCopyOption.REPLACE_EXISTING);
            badZimpl.toFile().deleteOnExit();
            Files.writeString(badZimpl, "\nThis text is appended to zimpl code and make it not compile;",StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            model = new Model(badZimpl.toFile().getPath());
            assertFalse(model.isCompiling(1000));
        }
        catch (Exception e){
            fail("IO error in GivenInvalidZimplCode_WhenCompiling_ReturnsFalse: "+ e.getMessage());
        }
    }
    @Test
    public void GivenEmptyZimplFIle_WhenCompiling_ReturnsTrue() {
        try {
            Path badZimpl = tmpDirPath.resolve("badZimpl.zimpl");
            Files.copy(Path.of(sourcePath), badZimpl, StandardCopyOption.REPLACE_EXISTING);
            badZimpl.toFile().deleteOnExit();
            Files.writeString(badZimpl, "", StandardOpenOption.WRITE, StandardOpenOption.WRITE);
            model = new Model(badZimpl.toFile().getPath());
            assertFalse(model.isCompiling(1000));
        }
        catch (Exception e){
            fail("IO error in GivenInvalidZimplCode_WhenCompiling_ReturnsFalse: "+ e.getMessage());
        }
    }

    @Test
    public void testSolve(){
        Solution solution= model.solve(1000,"SOLUTION");
        Set<String> vars= model.getVariables().stream().map(ModelVariable -> ModelVariable.getIdentifier()).collect(Collectors.toSet());
        try {
            solution.parseSolution(model,vars);
        }
        catch (IOException e){
            fail(e.getMessage());
        }
    }
}
