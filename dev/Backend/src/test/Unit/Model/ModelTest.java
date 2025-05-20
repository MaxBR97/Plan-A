package Unit.Model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import DataAccess.ModelRepository;
import Model.ModelConstraint;
import Model.ModelFactory;
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
import groupId.Main;
import SolverService.*;

@SpringBootTest(classes = Main.class)
//@ComponentScan(basePackages = {"Model", "DataAccess","DataAccess.LocalStorage", "Image.Modules"})
//@ExtendWith(SpringExtension.class)
//@ActiveProfiles("test") 
//@Transactional
@ActiveProfiles({"H2mem","securityAndGateway"})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) 

public class ModelTest {
    private ModelInterface model;
    private static String source = "src/test/Unit/TestFile.zpl";
    private static String TEST_FILE_PATH = "src/test/Unit/TestFileINSTANCE.zpl";
    private static String SOLVE_FILE_PATH = "src/test/Unit/TestFile2.zpl";
    private static String sourceId = "TestFileINSTANCE";
    private static String sourceSolveId = "TestFile2";
    private static int compilationBaselineTime = 6;
    private static String[][] expectedParameters = {{"Conditioner","10"}, {"soldiers", "9"}, {"absoluteMinimalRivuah", "8"}};
    
    private static ModelRepository modelRepository;
    private static ModelFactory modelFactory;
    private static Solver solverService;

    @Autowired
    public void setModelRepository(ModelFactory factory, Solver solverService) {
        this.modelFactory = factory;
        this.modelRepository = factory.getRepository();
        this.solverService = solverService;
        // Model.injectRepository(modelRepository);
    }

    @BeforeAll
    public static void setUpFile() throws Exception {
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
    public void setUp() throws Exception{
        InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(TEST_FILE_PATH)));
        modelRepository.uploadDocument(sourceId, inputStream);
        InputStream inputStream2 = new ByteArrayInputStream(Files.readAllBytes(Path.of(SOLVE_FILE_PATH)));
        modelRepository.uploadDocument(sourceSolveId, inputStream2);
        inputStream.close();    
        inputStream2.close();
        model = modelFactory.getModel(sourceId);
    }
    
    @Test
    public void testModelConstruction() throws Exception {
        assertNotNull(model);
        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));
    }
    
    @Test
    public void testInvalidFilePath() throws IOException {
        assertThrows(Exception.class, ()->modelRepository.downloadDocument("nonexistent_file6293.txt"));
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

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));


        // Test remove
        model.removeFromSet(testSet, addValue);
        testSet = getSet(model, setName);
        assertFalse(testSet.getElements().contains(addValue));

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));

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

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));

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

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));

    }
    
    // Functionality Toggle Tests
    @Test
    public void testToggleFunctionalityConstraint() throws Exception {
        String testConstraint = "trivial1";

        ModelFunctionality mf = getConstraint(model, testConstraint);
        assertNotNull(mf);
        model.toggleFunctionality(mf, false);

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));

        model.toggleFunctionality(mf, true);
        assertNotNull(getConstraint(model, testConstraint));
        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));
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

        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));

        model.toggleFunctionality(mf, true);
        assertNotNull(getPreference(model, testPreference));
        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));
    }


    @Test
    public void testBasicCompilation() throws Exception{
        assertFalse(solverService.isCompiling(sourceId, 0).equals("Compilation Error"));
        assertTrue(solverService.isCompiling(sourceId, compilationBaselineTime).equals(""));
        try{
        String gibbrish = "gfsgfd;";
        InputStream original = modelRepository.downloadDocument(sourceId);
        InputStream broken = new ByteArrayInputStream(new String(new String(original.readAllBytes()) + gibbrish).getBytes());
        modelRepository.uploadDocument(sourceId, broken);
        assertFalse(solverService.isCompiling(sourceId, compilationBaselineTime).equals("Compilation Error"));
        original.close();
        broken.close();
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
    public void testSolveOptimal(){
        ModelInterface m = null;
        try{
        
        m = modelFactory.getModel(this.sourceSolveId);
        Solution sol = solverService.solve(sourceSolveId, 15, "");
        
        assertNotNull(sol);
        Set<String> stringVariables = m.getVariables().stream()
            .map(ModelVariable::getIdentifier)
            .collect(Collectors.toSet());
        sol.parseSolution(m, stringVariables);
        assertEquals(sol.getSolutionStatus(), Solution.SolutionStatus.OPTIMAL);
        assertEquals(sol.getObjectiveValue() , 1187);
        } catch(Exception e){assertTrue(false,e.getMessage());}
    }

    @Test
    public void testSolveSuboptimal(){
        ModelInterface m = null;
        try{
        
        m = modelFactory.getModel(this.sourceSolveId);
        m.setInput(m.getSet("Stations"),new String[] {"\"North\"","\"West\"","\"South\"" , "\"East\""});
        m.setInput(m.getParameter("soldiers"),"15");
        m.setInput(m.getParameter("degreeOne"),"7");
        m.setInput(m.getParameter("degreeTwo"),"4");
        m.setInput(m.getParameter("absoluteMinimalSpacing"),"0");

        //21k vars~
        //approximately 1-2 seconds reading time
        //approx. 1 seconds presolve time
        //aprox. 137 seconds solve for optimal
        Solution sol = solverService.solve(sourceSolveId, 6, "");
        
        assertNotNull(sol);
        Set<String> stringVariables = m.getVariables().stream()
            .map(ModelVariable::getIdentifier)
            .collect(Collectors.toSet());
        sol.parseSolution(m, stringVariables);
        assertEquals(sol.getSolutionStatus(), Solution.SolutionStatus.SUBOPTIMAL);
        } catch(Exception e){assertTrue(false);}
    }

    @Test
    public void testSolveMemoryOverload(){
        ModelInterface m = null;
        try{
        
        m = modelFactory.getModel(this.sourceSolveId);
        m.setInput(m.getSet("Stations"),new String[] {"\"North\"","\"West\"","\"South\"" , "\"East\"", "\"Center\""});
        m.setInput(m.getParameter("soldiers"),"35");
        m.setInput(m.getParameter("degreeOne"),"7");
        m.setInput(m.getParameter("degreeTwo"),"4");
        m.setInput(m.getParameter("absoluteMinimalSpacing"),"0");

        //reading takes approx 8 sec
        Solution sol = solverService.solve(sourceSolveId, 1,"");
        
        assertNotNull(sol);
        Set<String> stringVariables = m.getVariables().stream()
            .map(ModelVariable::getIdentifier)
            .collect(Collectors.toSet());
        sol.parseSolution(m, stringVariables);
        assertEquals(sol.getSolutionStatus(), Solution.SolutionStatus.UNSOLVED);
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

    // //TODO:The parsing of preferences must be tested further!


    @AfterAll
    public static void cleanUp() throws Exception {
       Path targetPath = Path.of(TEST_FILE_PATH);
       Files.deleteIfExists(targetPath);
       //Files.deleteIfExists(Path.of(targetPath.toString()+"SOLUTION"));
       //Files.deleteIfExists(Path.of("./src/test/Unit/TestFile2.zplSOLUTION"));
    //    int count = 0;
    //    System.gc();
    //    //temporary solution to a synchronization problem - deleteing file while in use
    //    while(true){
    //     try{
    //     Thread.sleep(100);
    //     modelRepository.deleteDocument(sourceId);
    //     modelRepository.deleteDocument(sourceSolveId);
    //     break;
    //     } catch(Exception e){
    //         count++;
    //         if(count == 10)
    //             throw e;
    //     }
    //    }
    }
    

}
