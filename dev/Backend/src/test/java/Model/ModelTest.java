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


public class ModelTest {
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

    private ModelSet getSet(Model m, String identifier) throws Exception{
        m = new Model(TEST_FILE_PATH);
        return m.getSet(identifier);
    }
    
    private ModelParameter getParameter(Model m, String identifier) throws Exception{
        m = new Model(TEST_FILE_PATH);
        return m.getParameter(identifier);
    }
    
    private ModelVariable getVariable(Model m, String identifier) throws Exception{
        //m = new Model(TEST_FILE_PATH);
        return m.getVariable(identifier);
    }
    
    private ModelConstraint getConstraint(Model m, String identifier) throws Exception{
        m = new Model(TEST_FILE_PATH);
        return m.getConstraint(identifier);
    }
    
    private ModelPreference getPreference(Model m, String identifier) throws Exception{
        m = new Model(TEST_FILE_PATH);
        return m.getPreference(identifier);
    }
    
    
    @BeforeEach
    public void setUp() throws IOException {
        model = new Model(TEST_FILE_PATH);
    }
    
    @Test
    public void testModelConstruction() {
        assertNotNull(model);
        assertTrue(model.isCompiling(3));

    }
    
    @Test
    public void testInvalidFilePath() throws IOException {
        assertThrows(Exception.class, ()->new Model("nonexistent_file6293.txt"));
    }
    
    
    @Test
    public void testSetOperations() throws Exception {
        // Get initial set
        String setName = "Emdot";
        ModelType type = ModelPrimitives.TEXT;

        ModelSet testSet = model.getSet(setName);
        assertNotNull(testSet);
        assertEquals(testSet.identifier,setName);
        assertEquals(testSet.getType(), type);
        

        String addValue = "\"MyValue\"";


        // Test append
        model.appendToSet(testSet, addValue);
        testSet = getSet(model, setName);
        assertTrue(testSet.getElements().contains(addValue));

        assertTrue(model.isCompiling(2));


        // Test remove
        model.removeFromSet(testSet, addValue);
        testSet = getSet(model, setName);
        assertFalse(testSet.getElements().contains(addValue));

        assertTrue(model.isCompiling(2));

    }
    
    // Input Setting Tests
    @Test
    public void testSetParameterInput() throws Exception {
        String parameter = "soldiers";
        String valueToSet = "12";

        ModelParameter param = getParameter(model, parameter);
        assertNotNull(param);
        assertEquals(param.getType(), ModelPrimitives.INT);
        model.setInput(param, valueToSet);
        param = getParameter(model, parameter);
        assertEquals( param.getValue(), valueToSet);

        assertTrue(model.isCompiling(2));

    }
    
    // Functionality Toggle Tests
    @Test
    public void testToggleFunctionality() throws Exception {
        String testConstraint = "trivial1";

        ModelFunctionality mf = getConstraint(model, testConstraint);
        assertNotNull(mf);
        model.toggleFunctionality(mf, false);

        assertTrue(model.isCompiling(2));

        model.toggleFunctionality(mf, true);
        assertNotNull(getConstraint(model, testConstraint));
        assertTrue(model.isCompiling(2));
    }

    @Test
    public void testBasicCompilation(){
        assertFalse(model.isCompiling(0.000000001f));
        assertTrue(model.isCompiling(3));
        try{
        String gibbrish = "gfsgfd;";
        Files.writeString(Path.of(TEST_FILE_PATH), gibbrish, StandardOpenOption.APPEND);
        assertFalse(model.isCompiling(3));
        FileChannel fileChannel = FileChannel.open(Path.of(TEST_FILE_PATH), StandardOpenOption.WRITE);
        long newSize = fileChannel.size() - gibbrish.length();
        fileChannel.truncate(newSize);
        assertTrue(model.isCompiling(3));
        } catch (Exception e){
            assertFalse(true);
        }
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
        assertEquals(2, cxsxs.getSetDependencies().size());
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
    public void testSolve(){
        model.solve(6);
        assertFalse(true); // test is not implemented yet because Solution class is not implemented yet
    }
    
    // Collection Getter Tests
    @Test
    public void testParameterParsing() {
        assertFalse(true); 
        
    }
    @Test
    public void testSetParsing() {
        assertFalse(true); 
    }

    @Test
    public void testConstraintParsing() {
        assertFalse(true); 
    }

    @Test
    public void testPreferenceParsing() {
        assertFalse(true); 
    }
    @Test
    public void testVariableParsing() {
        assertFalse(true); 
    }
    
    // Exception Tests
    @Test
    public void testInvalidSetAppend() throws Exception {
        assertFalse(true); //unimplemented
    }
    
    @Test
    public void testInvalidSetRemove() throws Exception {
        assertFalse(true); //unimplemented
    }
    
    @Test
    public void testInvalidParameterAssignment() throws Exception {
        assertFalse(true); //unimplemented
    }
    


    @AfterAll
    public static void cleanUp() throws IOException {
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
    }

}
