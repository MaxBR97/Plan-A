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
import java.util.HashMap;
import java.util.List;
    import java.util.Set;
    
    import org.junit.jupiter.api.AfterAll;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import Model.*;

import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.io.IOException;
    
public class TypesAndDependencyTests {
   
    private Model model;

    private static String source = "/Plan-A/dev/Backend/src/test/java/Model/TestFile.zpl";
    private static String TEST_FILE_PATH = "/Plan-A/dev/Backend/src/test/java/Model/TestFileINSTANCE.zpl";

    private static HashMap<String,String[]> setDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> paramDependencies =  new HashMap<String,String[]>();
    
    @BeforeAll
    public static void setUpFile() throws IOException {
        Path sourcePath = Path.of(source);
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        //TRUTH
        setDependencies.put("setWithRange",new String[]{});
        paramDependencies.put("setWithRange", new String[]{"conditioner"});
        setDependencies.put("C", new String[]{});
        paramDependencies.put("C", new String[]{"soldiers"});
        setDependencies.put("CxS", new String[]{"C","S"});
        paramDependencies.put("CxS", new String[]{});
        setDependencies.put("Emdot", new String[]{"custom_set"});
        paramDependencies.put("Emdot", new String[]{});
        setDependencies.put("Zmanim", new String[]{"custom_set"});
        paramDependencies.put("Zmanim", new String[]{});
        setDependencies.put("S", new String[]{"Emdot", "Zmanim"});
        paramDependencies.put("S", new String[]{});
        setDependencies.put("CxSxS", new String[]{"C", "S", "S"});
        paramDependencies.put("CxSxS", new String[]{});
        setDependencies.put("forTest1", new String[]{"custom_set"});
        paramDependencies.put("forTest1", new String[]{"soldiers"});
        setDependencies.put("forTest2", new String[]{"custom_set", "S", });
        paramDependencies.put("forTest2", new String[]{"soldiers"});

        setDependencies.put("conditioner", new String[]{});
        paramDependencies.put("conditioner", new String[]{});
        setDependencies.put("absoluteMinimalRivuah", new String[]{});
        paramDependencies.put("absoluteMinimalRivuah", new String[]{});
        setDependencies.put("soldiers", new String[]{});
        paramDependencies.put("soldiers", new String[]{});
        

        setDependencies.put("couples", new String[]{"CxSxS"});
        paramDependencies.put("couples", new String[]{});
        setDependencies.put("edge", new String[]{"CxS"});
        paramDependencies.put("edge", new String[]{});
        
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

    
    @ParameterizedTest
    @ValueSource(strings = {"setWithRange","C","S","Zmanim", "Emdot", "CxS", "CxSxS"})
    public void testSetDependenciesOfSets(String identifier) {
        String setName = identifier;
        List<String> expectedDeps = Arrays.asList(setDependencies.get(identifier));
        ModelSet set = model.getSet(setName);
        assertEquals(expectedDeps.size(), set.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            set.findSetDependency(dep));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"setWithRange","C","S","Zmanim", "Emdot", "CxS", "CxSxS"})
    public void testParamDependenciesOfSets(String identifier) {
        String setName = identifier;
        List<String> expectedDeps = Arrays.asList(paramDependencies.get(identifier));
        ModelSet set = model.getSet(setName);
        assertEquals(expectedDeps.size(), set.getParamDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            set.findParamDependency(dep));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers"})
    public void testSetDependenciesOfParameters(String identifier) {
        String paramName = identifier;
        List<String> expectedDeps = Arrays.asList(setDependencies.get(identifier));
        ModelParameter param = model.getParameter(paramName);
        assertEquals(expectedDeps.size(), param.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            param.findSetDependency(dep));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers"})
    public void testParamDependenciesOfParameters(String identifier) {
        String paramName = identifier;
        List<String> expectedDeps = Arrays.asList(paramDependencies.get(identifier));
        ModelParameter param = model.getParameter(paramName);
        assertEquals(expectedDeps.size(), param.getParamDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            param.findParamDependency(dep));
        }
    }

    //TODO: test this case more thoroughly by explicitly check the dependency of each set, instead of just checking NotNull.
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


    //TODO: tests that check types, may be converted to parameterized tests.
    @Test
    public void typeCheckC(){
        ModelSet s = model.getSet("C");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue(s.isCompatible(expectedType));
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
