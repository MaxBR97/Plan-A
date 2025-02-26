package Unit;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import Model.Model;
import Model.ModelConstraint;
import Model.ModelFunctionality;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelPreference;
import Model.ModelPrimitives;
import Model.ModelSet;
import Model.ModelType;
import Model.ModelVariable;
import Model.Solution;
import Model.Tuple;


public class ModelTest {
    private ModelInterface model;

    private static String source = "src/test/Unit/TestFile.zpl";
    private static String TEST_FILE_PATH = "src/test/Unit/TestFileINSTANCE.zpl";
    private static float compilationBaselineTime = 8;
    private static String[][] expectedParameters = {{"Conditioner","10"}, {"soldiers", "9"}, {"absoluteMinimalRivuah", "8"}};
    @BeforeAll
    public static void setUpFile() throws IOException {
        Path sourcePath = Path.of(source);
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private ModelSet getSet(ModelInterface m, String identifier) throws Exception{
        return m.getSet(identifier);
    }
    
    private ModelParameter getParameter(ModelInterface m, String identifier) throws Exception{
        return m.getParameter(identifier);
    }
    
    private ModelVariable getVariable(ModelInterface m, String identifier) throws Exception{
        return m.getVariable(identifier);
    }
    
    private ModelConstraint getConstraint(ModelInterface m, String identifier) throws Exception{
        return m.getConstraint(identifier);
    }
    
    private ModelPreference getPreference(ModelInterface m, String identifier) throws Exception{
        return m.getPreference(identifier);
    }
    
    
    @BeforeEach
    public void setUp() throws IOException {
        model = new Model(TEST_FILE_PATH);
    }
    
    @Test
    public void testModelConstruction() {
        assertNotNull(model);
        assertTrue(model.isCompiling(compilationBaselineTime));
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
        assertEquals(testSet.getIdentifier(),setName);
        assertEquals(testSet.getType(), type);
        

        String addValue = "\"MyValue\"";


        // Test append
        model.appendToSet(testSet, addValue);
        testSet = getSet(model, setName);
        assertTrue(testSet.getElements().contains(addValue));

        assertTrue(model.isCompiling(compilationBaselineTime));


        // Test remove
        model.removeFromSet(testSet, addValue);
        testSet = getSet(model, setName);
        assertFalse(testSet.getElements().contains(addValue));

        assertTrue(model.isCompiling(compilationBaselineTime));

    }
    
    // Input Setting Tests
    @Test
    public void testSetParameterInput() throws Exception {
        String parameter = "soldiers";
        String valueToSet = "12";

        ModelParameter param = getParameter(model, parameter);
        assertNotNull(param);
        Assertions.assertEquals(param.getType(), ModelPrimitives.INT);
        model.setInput(param, valueToSet);
        param = getParameter(model, parameter);
        Assertions.assertEquals( param.getValue(), valueToSet);

        assertTrue(model.isCompiling(compilationBaselineTime));

    }

    @Test
    public void testSetSetInput() throws Exception {
        String set = "forTest3";
        String[] valueToSet = {"<1,\"gsd\",3>", "<54,\"g5h\",3>"};

        ModelSet mySet = getSet(model, set);
        assertNotNull(mySet);
        Assertions.assertTrue(mySet.getType().isCompatible(new Tuple(new ModelPrimitives[]{ModelPrimitives.INT,ModelPrimitives.TEXT,ModelPrimitives.INT})));
        model.setInput(mySet, valueToSet);
        mySet = getSet(model, set);
        Assertions.assertArrayEquals( mySet.getElements().toArray(), valueToSet);

        assertTrue(model.isCompiling(compilationBaselineTime));

    }
    
    // Functionality Toggle Tests
    @Test
    public void testToggleFunctionalityConstraint() throws Exception {
        String testConstraint = "trivial1";

        ModelFunctionality mf = getConstraint(model, testConstraint);
        assertNotNull(mf);
        model.toggleFunctionality(mf, false);

        assertTrue(model.isCompiling(compilationBaselineTime));

        model.toggleFunctionality(mf, true);
        assertNotNull(getConstraint(model, testConstraint));
        assertTrue(model.isCompiling(compilationBaselineTime));
    }

    //TODO: Toggling Preferences doesnt work perfectly, but somewhat works 
    //      on common cases. Better dive in to figure it out later.
    // This input string doesn't work: "sum<person>inPeople:(TotalMishmarot[person]**2)"
    @ParameterizedTest
    @ValueSource(strings = {"((maxShmirot-minShmirot)+conditioner)**3", "(minimalRivuah)**2", "(sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8"})
    public void testToggleFunctionalityPreference(String id) throws Exception {
        String testPreference = id.replaceAll(" ","");

        ModelFunctionality mf = getPreference(model, testPreference);
        assertNotNull(mf);
        model.toggleFunctionality(mf, false);

        assertTrue(model.isCompiling(compilationBaselineTime));

        model.toggleFunctionality(mf, true);
        assertNotNull(getPreference(model, testPreference));
        assertTrue(model.isCompiling(compilationBaselineTime));
    }


    @Test
    public void testBasicCompilation(){
        assertFalse(model.isCompiling(0.00000000001f));
        assertTrue(model.isCompiling(compilationBaselineTime));
        try{
        String gibbrish = "gfsgfd;";
        Files.writeString(Path.of(TEST_FILE_PATH), gibbrish, StandardOpenOption.APPEND);
        assertFalse(model.isCompiling(compilationBaselineTime));
        FileChannel fileChannel = FileChannel.open(Path.of(TEST_FILE_PATH), StandardOpenOption.WRITE);
        long newSize = fileChannel.size() - gibbrish.length();
        fileChannel.truncate(newSize);
        assertTrue(model.isCompiling(compilationBaselineTime));
        } catch (Exception e){
            assertFalse(true);
        }
    }

    @Test
    public void testConvertingAtomsToTuple(){
        String res = ModelType.convertArrayOfAtoms(new String[]{"fdas", "32", "321"},new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,ModelPrimitives.INT,ModelPrimitives.INT}));
        assertTrue(res.equals("<\"fdas\",32,321>"));
    }

    
    @Test
    public void testSolve(){
        Model m = null;
        try{
         m = new Model("./src/test/Unit/TestFile2.zpl");
        Solution sol = m.solve(100,"SOLUTION");
        
        if(sol == null)
            assertFalse(true);
        Set<String> stringVariables = m.getVariables().stream()
            .map(ModelVariable::getIdentifier)
            .collect(Collectors.toSet());
        sol.parseSolution(m, stringVariables, Map.of());
        assertTrue(sol.isSolved());
        assertEquals(sol.getObjectiveValue() , 1187);
        } catch(Exception e){assertTrue(false);}
    }

    @Test
    public void testValidGetInputOfParameter() throws Exception {
        ModelParameter subject = model.getParameter("conditioner");
        assertNotNull(subject);
        assertArrayEquals(new String[]{"10"}, model.getInput(subject));
    }


    @Test
    public void testValidGetInputOfSet() throws Exception {
        ModelSet subject = model.getSet("Emdot");
        assertNotNull(subject);
        List<String[]> expected =  List.of(new String[]{"Shin Gimel"}, new String[]{"Fillbox"});
        int i=0;
        for(String[] element : model.getInput(subject)){
            assertArrayEquals(element, expected.get(i));
            i++;
        }
    }

    @Test
    public void testInvalidGetInputOfSet() throws Exception {
        ModelSet subject = model.getSet("C");
        assertNotNull(subject);
        assertThrows(Exception.class, () -> {
            model.getInput(subject);
        });
    }
    
    //TODO: implement
    @Test
    public void testInvalidSetAppend() throws Exception {
        assertTrue(true); 
    }
    
    //TODO: implement
    @Test
    public void testInvalidSetRemove() throws Exception {
        assertTrue(true); 
    }
    
    //TODO: implement
    @Test
    public void testInvalidParameterAssignment() throws Exception {
        assertTrue(true); 
    }

    @ParameterizedTest
    @ValueSource(strings = {"C","CxS","Zmanim","conditioner","soldiers","minShmirot","maxShmirot","minimalRivuah","varForTest1","((maxShmirot-minShmirot)+conditioner)**3", "(minimalRivuah)**2", "(sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8"})
    public void isParsed(String id){
        id = id.replaceAll(" ", "");
        assertTrue(model.getSet(id) != null || model.getConstraint(id) != null || model.getParameter(id) != null || model.getPreference(id) != null || model.getVariable(id) != null);
    }
    //TODO:The parsing of preferences must be tested further!


    @AfterAll
    public static void cleanUp() throws IOException {
       Path targetPath = Path.of(TEST_FILE_PATH);
       Files.deleteIfExists(targetPath);
       Files.deleteIfExists(Path.of(targetPath.toString()+"SOLUTION"));
       Files.deleteIfExists(Path.of("./src/test/Unit/TestFile2.zplSOLUTION"));

    }

}
