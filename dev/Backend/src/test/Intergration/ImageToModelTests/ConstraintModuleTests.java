package Intergration.ImageToModelTests;

import Model.Model;
import Model.ModelInterface;
import Utilities.Stubs.ModelStub;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import Model.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ConstraintModuleTests {

    @Mock
    ModelInterface model;
    static String sourcePath = "src/test/Utilities/Stubs/ExampleZimplProgram.zpl";
    static Path tmpDirPath;
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
    public void beforeEach() {
        try {
            model = new Model(sourcePath);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }
    @Test
    public void GivenPath_WhenPathInvalid_ThrowsException() throws IOException {
        String badPath= sourcePath+ "/IDontExist.zpl";
        try {
            model = new ModelStub(badPath);
        } catch (IOException e) {
            fail();
        }
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
            Files.writeString(badZimpl, "\nThis text is appended to zimpl code and make it not compile", StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            model = new Model(badZimpl.toFile().getPath());
            assertFalse(model.isCompiling(1000));
        }
        catch (IOException e){
            fail("IO error in GivenInvalidZimplCode_WhenCompiling_ReturnsFalse: "+ e.getMessage());
        }
    }
    @Test
    public void testSolve(){
        model.solve(50);
        assertFalse(true); // test is not implemented yet because Solution class is not implemented yet
    }
}
