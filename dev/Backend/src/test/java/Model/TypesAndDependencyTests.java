package Model;
import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertNotNull;
    import static org.junit.jupiter.api.Assertions.assertNull;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    import static org.junit.jupiter.api.Assertions.fail;
    
    import java.io.IOException;
    
    import java.nio.channels.FileChannel;
    import java.nio.file.Files;
    import java.nio.file.OpenOption;
    import java.nio.file.StandardCopyOption;
    import java.nio.file.StandardOpenOption;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.List;
    import java.util.Set;
    
    import org.junit.jupiter.api.AfterAll;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.params.provider.NullAndEmptySource;
    import org.springframework.boot.test.context.SpringBootTest;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.io.IOException;
    
public class TypesAndDependencyTests {
   
    private Model model;

    private static String source = "/Plan-A/dev/Backend/src/test/java/Model/TestFile.zpl";
    private static String TEST_FILE_PATH = "/Plan-A/dev/Backend/src/test/java/Model/TestFileINSTANCE.zpl";

    private static String[][] expectedParameters = {{"Conditioner","10"}, {"soldiers", "9"}, {"absoluteMinimalRivuah", "8"}};
    
    @BeforeAll
    public static void setUpFile() throws IOException {
        Path sourcePath = Path.of(source);
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @BeforeEach
    public void setUp() throws IOException {
        model = new Model(TEST_FILE_PATH);
    }

    @Test
    public void setDependencyInference() {
        String var = "couples";
        String dependency = "CxSxS";
        assertEquals(1,model.getVariable(var).getDependencies().size());
        assertEquals(dependency,model.getVariable(var).findDependency(dependency).getIdentifier());
    }

        @Test
    public void testEdgeVariableDependency() {
        String var = "edge";
        String dependency = "CxS";
        assertEquals(1, model.getVariable(var).getDependencies().size());
        assertEquals(dependency, model.getVariable(var).findDependency(dependency).getIdentifier());
    }

    @Test
    public void testCoupleVariableDependency() {
        String var = "couples";
        String dependency = "CxSxS";
        assertEquals(1, model.getVariable(var).getDependencies().size());
        assertEquals(dependency, model.getVariable(var).findDependency(dependency).getIdentifier());
    }

    // Test set dependencies
    @Test
    public void testCxSDependencies() {
        String setName = "CxS";
        List<String> expectedDeps = Arrays.asList("C", "S");
        ModelSet set = model.getSet(setName);
        assertEquals(2, set.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            set.findSetDependency(dep));
        }
    }

    @Test
    public void testSDependencies() {
        String setName = "S";
        List<String> expectedDeps = Arrays.asList("Emdot", "Zmanim");
        ModelSet set = model.getSet(setName);
        assertEquals(2, set.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            set.findSetDependency(dep));
        }
    }

    @Test
    public void testCxSxSDependencies() {
        String setName = "CxSxS";
        List<String> expectedDeps = Arrays.asList("C", "S", "S");
        ModelSet set = model.getSet(setName);
        assertEquals(3  , set.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            set.findSetDependency(dep));
        }
    }

    // Test set definitions with ranges
    @Test
    public void testCDependencyOnSoldiers() {
        String setName = "C";
        ModelSet set = model.getSet(setName);
        ModelParameter soldiers = model.getParameter("soldiers");
        assertNotNull(set.findParamDependency("soldiers"));
    }

    @Test
    public void testCSetDependencyOnSoldiers() {
        String setName = "C";
        ModelSet set = model.getSet(setName);
        assertNotNull(set.findSetDependency("anonymous_range"));
    }

    @Test
    public void testEmdotSetDependencyOnSoldiers() {
        String setName = "Emdot";
        ModelSet set = model.getSet(setName);
        assertNotNull(set.findSetDependency("anonymous"));
    }

    @Test
    public void testZmanimSetDependencyOnSoldiers() {
        String setName = "Zmanim";
        ModelSet set = model.getSet(setName);
        assertNotNull(set.findSetDependency("anonymous"));
    }

    // Test cross product dependencies
    @Test
    public void testSCrossProductStructure() {
        ModelSet s = model.getSet("S");
        assertEquals(2, s.getSetDependencies().size());
    }

    @Test
    public void testCxSCrossProductStructure() {
        ModelSet cxs = model.getSet("CxS");
        assertEquals(2, cxs.getSetDependencies().size());
    }

    @Test
    public void testCxSxSCrossProductStructure() {
        ModelSet cxsxs = model.getSet("CxSxS");
        assertEquals(3, cxsxs.getSetDependencies().size());
    }

    // Test parameter dependencies
    @Test
    public void testParameterIndependence() {
        List<String> params = Arrays.asList("conditioner", "soldiers", "absoluteMinimalRivuah");
        for (String param : params) {
            ModelParameter parameter = model.getParameter(param);
            assertEquals(
                        0, parameter.getParamDependencies().size());
        }
    }

    // Test recursive dependencies
    @Test
    public void testRecursiveDependencies() {
        ModelVariable couples = model.getVariable("couples");
        ModelSet cxsxs = couples.findDependency("CxSxS");
        assertNotNull(cxsxs);
        
        ModelSet c = cxsxs.findSetDependency("C");
        ModelSet s = cxsxs.findSetDependency("S");
        assertNotNull(c);
        assertNotNull(s);
        
        ModelSet emdot = s.findSetDependency("Emdot");
        ModelSet zmanim = s.findSetDependency("Zmanim");
        assertNotNull(emdot);
        assertNotNull(zmanim);
    }

    @Test
    public void typeCheckC(){
        ModelSet s = model.getSet("C");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeCheckZmanim(){
        ModelSet s = model.getSet("Zmanim");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeEmdot(){
        ModelSet s = model.getSet("Emdot");
        ModelType expectedType = ModelPrimitives.TEXT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeCheckS(){
        ModelSet s = model.getSet("S");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,ModelPrimitives.INT});
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeCheckCxS(){
        ModelSet s = model.getSet("CxS");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.INT,ModelPrimitives.TEXT,ModelPrimitives.INT});
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeCheckCxSxS(){
        ModelSet s = model.getSet("CxSxS");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.INT,ModelPrimitives.TEXT,ModelPrimitives.INT,ModelPrimitives.TEXT,ModelPrimitives.INT});
        assertTrue( s.isCompatible(expectedType));
    }
    

    @AfterAll
    public static void cleanUp() throws IOException {
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
    }


    
}
