package Model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import java.nio.file.Paths;
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
       // m = new Model(TEST_FILE_PATH);
        return m.getSet(identifier);
    }
    
    private ModelParameter getParameter(Model m, String identifier) throws Exception{
       // m = new Model(TEST_FILE_PATH);
        return m.getParameter(identifier);
    }
    
    private ModelVariable getVariable(Model m, String identifier) throws Exception{
        //m = new Model(TEST_FILE_PATH);
        return m.getVariable(identifier);
    }
    
    private ModelConstraint getConstraint(Model m, String identifier) throws Exception{
        //m = new Model(TEST_FILE_PATH);
        return m.getConstraint(identifier);
    }
    
    private ModelPreference getPreference(Model m, String identifier) throws Exception{
        //m = new Model(TEST_FILE_PATH);
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
    
    @Test
    public void testSetInputForParameter() throws Exception {
        String parameter = "soldiers";
        String correctValueToSet = "12";
        String incorrectValueToSet = "\"myText\"";

        ModelParameter param = getParameter(model, parameter);
        assertNotNull(param);
        assertEquals(param.getType(), ModelPrimitives.INT);

        //Set correct value
        model.setInput(param, correctValueToSet);
        param = getParameter(model, parameter);
        assertEquals( param.getValue(), correctValueToSet);
        assertTrue(model.isCompiling(2));
        
        //Set Incorrect Value
        assertThrows(Exception.class, () -> {
            ModelParameter paramInLambda = getParameter(model, parameter);
            model.setInput(paramInLambda, incorrectValueToSet);
        });
        param = getParameter(model, parameter);
        assertEquals(param.getValue(), correctValueToSet);
        assertTrue(model.isCompiling(2));
    }

    @Test
    public void testSetInputForSet() throws Exception {
        String setName = "Emdot";
        String correctValueToSet[] = new String[] {"\"12\"", "\"Hi\""};
        String incorrectValueToSet[] = new String[] {"12", "Hi","5432.2", ""};

        ModelSet set = getSet(model, setName);
        assertNotNull(set);
        assertEquals(set.getType(), ModelPrimitives.TEXT);

        //Set correct values
        model.setInput(set, correctValueToSet);
        set = getSet(model, setName);
        assertEquals(correctValueToSet.length, set.getElements().size());
        assertArrayEquals(correctValueToSet, set.getElements().toArray());
        assertTrue(model.isCompiling(2));

        //set incorrect values
        assertThrows(Exception.class, () -> {
            ModelSet setInLambda = getSet(model, setName);
            model.setInput(setInLambda, incorrectValueToSet);
        });
        set = getSet(model, setName);
        assertEquals(correctValueToSet.length, set.getElements().size());
        assertArrayEquals(correctValueToSet, set.getElements().toArray());
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
        assertFalse(model.isCompiling(0.00000000001f));
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
        Path solutionPath = Path.of(TEST_FILE_PATH + "SOLUTION");
        Files.deleteIfExists(targetPath);
        Files.deleteIfExists(solutionPath);
    }

}