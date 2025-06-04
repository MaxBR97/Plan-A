package Unit.Model;
import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertNotNull;
    import static org.junit.jupiter.api.Assertions.assertNull;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    import static org.junit.jupiter.api.Assertions.fail;
    import java.util.HashSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import Model.*;

import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.io.IOException;

import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.sun.source.tree.AssertTree;

import DataAccess.ModelRepository;
import groupId.Main;
    

@SpringBootTest(classes = Main.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"inMemory-local","securityAndGateway"}) 
public class TypesAndDependencyTests {
   
    private Model model;

    private static String source = "./src/test/Unit/TestFile.zpl";
    private static String TEST_FILE_PATH = "./src/test/Unit/TestFileINSTANCE.zpl";
    private static String sourceId = "TestFileINSTANCE";

    private static HashMap<String,String[]> immidiateSetDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> immidiateParamDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> immidiateFunctionDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> secondDegreeSetDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> secondDegreeParamDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> secondDegreeFunctionDependencies =  new HashMap<String,String[]>();

    private static HashMap<String,Boolean> primitives = new HashMap<>();
    private static HashMap<String,Boolean> isComplexVariable = new HashMap<>();

    private static ModelRepository modelRepository;

    @Autowired
    public void setModelRepository(ModelRepository injectedRepository) {
        modelRepository = injectedRepository;
        // Model.injectRepository(modelRepository);
    }   
    
    @BeforeAll
    public static void setUpFile() throws IOException {
        Path sourcePath = Path.of(source);
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        //TRUTH
        immidiateSetDependencies.put("setWithRange",new String[]{"anonymous_set"});
        immidiateParamDependencies.put("setWithRange", new String[]{});
        secondDegreeSetDependencies.put("setWithRange",new String[]{});
        secondDegreeParamDependencies.put("setWithRange", new String[]{"conditioner"});

        immidiateSetDependencies.put("C", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("C", new String[]{});
        secondDegreeSetDependencies.put("C",new String[]{});
        secondDegreeParamDependencies.put("C", new String[]{"soldiers"});

        immidiateSetDependencies.put("CxS", new String[]{"C","S"});
        immidiateParamDependencies.put("CxS", new String[]{});
        secondDegreeSetDependencies.put("CxS",new String[]{"anonymous_set", "Emdot", "Zmanim"});
        secondDegreeParamDependencies.put("CxS", new String[]{});

        immidiateSetDependencies.put("Emdot", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("Emdot", new String[]{});
        secondDegreeSetDependencies.put("Emdot",new String[]{});
        secondDegreeParamDependencies.put("Emdot", new String[]{});

        immidiateSetDependencies.put("Zmanim", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("Zmanim", new String[]{});

        immidiateSetDependencies.put("S", new String[]{"Emdot", "Zmanim"});
        immidiateParamDependencies.put("S", new String[]{});

        immidiateSetDependencies.put("CxSxS", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("CxSxS", new String[]{});
        secondDegreeSetDependencies.put("CxSxS",new String[]{"C","S","S"});
        secondDegreeParamDependencies.put("CxSxS", new String[]{});

        immidiateSetDependencies.put("forTest1", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("forTest1", new String[]{});
        secondDegreeSetDependencies.put("forTest1", new String[]{"C","Emdot","anonymous_set","CxS"});
        secondDegreeParamDependencies.put("forTest1", new String[]{"soldiers"});

        immidiateSetDependencies.put("forTest2", new String[]{"anonymous_set", "S", "anonymous_set", "C", "anonymous_set"});
        immidiateParamDependencies.put("forTest2", new String[]{});
        secondDegreeSetDependencies.put("forTest2",new String[]{"Emdot","Zmanim", "anonymous_set"});
        secondDegreeParamDependencies.put("forTest2", new String[]{"soldiers"});

        immidiateSetDependencies.put("forTest6", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("forTest6", new String[]{});
        secondDegreeSetDependencies.put("forTest6",new String[]{});
        secondDegreeParamDependencies.put("forTest6", new String[]{"soldiers"});

        immidiateSetDependencies.put("forTest7", new String[]{});
        immidiateParamDependencies.put("forTest7", new String[]{"conditioner","soldiers"});

        immidiateSetDependencies.put("forTest8", new String[]{});
        immidiateParamDependencies.put("forTest8", new String[]{"conditioner"});

        immidiateSetDependencies.put("forTest9", new String[]{});
        immidiateParamDependencies.put("forTest9", new String[]{"conditioner"});
        

        immidiateSetDependencies.put("forTest10", new String[]{"forTest3"});
        immidiateParamDependencies.put("forTest10", new String[]{});
       
        immidiateSetDependencies.put("forTest11", new String[]{"C","Zmanim"});
        immidiateParamDependencies.put("forTest11", new String[]{});

        immidiateSetDependencies.put("forTest13", new String[]{});
        immidiateParamDependencies.put("forTest13", new String[]{"conditioner"});

        immidiateSetDependencies.put("forTest14", new String[]{"forTest11"});
        immidiateParamDependencies.put("forTest14", new String[]{});
        

        immidiateSetDependencies.put("Shifts", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("Shifts", new String[]{});
        secondDegreeSetDependencies.put("Shifts",new String[]{"Stations", "Times"});
        secondDegreeParamDependencies.put("Shifts", new String[]{"planTimeRange"});

        immidiateSetDependencies.put("SoldiersToShifts", new String[]{"Soldiers", "anonymous_set"});
        immidiateParamDependencies.put("SoldiersToShifts", new String[]{});
        secondDegreeSetDependencies.put("SoldiersToShifts",new String[]{"anonymous_set","Shifts"});
        secondDegreeParamDependencies.put("SoldiersToShifts", new String[]{});

        
        immidiateSetDependencies.put("FormalTimes", new String[]{"anonymous_set"});
        immidiateParamDependencies.put("FormalTimes", new String[]{});
        immidiateFunctionDependencies.put("FormalTimes", new String[]{});
        secondDegreeSetDependencies.put("FormalTimes",new String[]{"Emdot","Soldiers", "Times"});
        secondDegreeParamDependencies.put("FormalTimes", new String[]{"conditioner"});
        secondDegreeFunctionDependencies.put("FormalTimes", new String[]{"myBool"});

        immidiateSetDependencies.put("conditioner", new String[]{});
        immidiateParamDependencies.put("conditioner", new String[]{});
        immidiateSetDependencies.put("absoluteMinimalRivuah", new String[]{});
        immidiateParamDependencies.put("absoluteMinimalRivuah", new String[]{});
        immidiateSetDependencies.put("soldiers", new String[]{});
        immidiateParamDependencies.put("soldiers", new String[]{});
        immidiateFunctionDependencies.put("soldiers", new String[]{});
        immidiateSetDependencies.put("planTimeRange", new String[]{});
        immidiateParamDependencies.put("planTimeRange", new String[]{"conditioner","soldiers","absoluteMinimalRivuah"});
        immidiateFunctionDependencies.put("planTimeRange", new String[]{"timeDifference"});
        immidiateSetDependencies.put("timeDifference", new String[]{});
        immidiateParamDependencies.put("timeDifference", new String[]{"soldiers"});
        immidiateFunctionDependencies.put("timeDifference", new String[]{});
        
        

        immidiateSetDependencies.put("couples", new String[]{"CxSxS"});
        immidiateParamDependencies.put("couples", new String[]{});
        immidiateSetDependencies.put("edge", new String[]{"CxS"});
        immidiateParamDependencies.put("edge", new String[]{});
        immidiateSetDependencies.put("varForTest1", new String[]{"CxS","anonymous_set", "S","anonymous_set"});
        immidiateParamDependencies.put("varForTest1", new String[]{});

        immidiateSetDependencies.put("condForTest1", new String[]{"Zmanim","CxS","Emdot"});
        immidiateParamDependencies.put("condForTest1", new String[]{"paramForTest1"});
        immidiateSetDependencies.put("condForTest2", new String[]{});
        immidiateParamDependencies.put("condForTest2", new String[]{"paramForTest2"});
        immidiateSetDependencies.put("trivial1", new String[]{"CxSxS"});
        immidiateParamDependencies.put("trivial1", new String[]{});
        immidiateSetDependencies.put("trivial5", new String[]{"CxS","Zmanim","CxS","CxSxS"});
        immidiateParamDependencies.put("trivial5", new String[]{});
        immidiateSetDependencies.put("Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit", new String[]{"CxS","CxS"});
        immidiateParamDependencies.put("Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit", new String[]{});
        immidiateSetDependencies.put("Kol_Haemdot_Meshubatsot_Hayal_Ehad", new String[]{"S","CxS"});
        immidiateParamDependencies.put("Kol_Haemdot_Meshubatsot_Hayal_Ehad", new String[]{});

        immidiateSetDependencies.put("((maxShmirot-minShmirot)+conditioner)**3", new String[]{});
        immidiateParamDependencies.put("((maxShmirot-minShmirot)+conditioner)**3", new String[]{"conditioner"});
        immidiateSetDependencies.put("(minimalRivuah)**2", new String[]{});
        immidiateParamDependencies.put("(minimalRivuah)**2", new String[]{});
        immidiateSetDependencies.put("(sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8", new String[]{"CxS","S"});
        immidiateParamDependencies.put("(sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8", new String[]{});
        immidiateSetDependencies.put("sum <person> in People : (TotalMishmarot[person]**2)", new String[]{"People"});
        immidiateParamDependencies.put("sum <person> in People : (TotalMishmarot[person]**2)", new String[]{});

        primitives.put("C",false);
        primitives.put("CxS",false);
        primitives.put("Zmanim",true);
        primitives.put("conditioner",true);
        primitives.put("soldiers",true);
        primitives.put("setWithRange",false);
        primitives.put("Emdot",true);
        primitives.put("S",false);
        

        isComplexVariable.put("edge",true);
        isComplexVariable.put("minShmirot",false);
        isComplexVariable.put("minimalRivuah",false);
        isComplexVariable.put("varForTest1",true); 
    }

    @BeforeEach
    public void setUp() throws Exception{
        InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(TEST_FILE_PATH)));
        modelRepository.uploadDocument(sourceId, inputStream);
        inputStream.close();
        model = new Model(modelRepository, sourceId);
    }


    @ParameterizedTest
    @ValueSource(strings = {"timeDifference"})

    public void functionDependencyInference(String id) {
        ModelInput var = model.getFunction(id);
        assertNotNull(var);
        assertEquals(var.getSetDependencies().size(), immidiateSetDependencies.get(id).length);
        for(String setId : immidiateSetDependencies.get(id)){
            assertNotNull(var.findSetDependency(setId));
        }

        assertEquals(var.getParamDependencies().size(), immidiateParamDependencies.get(id).length);
        for(String setId : immidiateParamDependencies.get(id)){
            assertNotNull(var.findParamDependency(setId));
        }

        assertEquals(var.getFuncDependencies().size(), immidiateFunctionDependencies.get(id).length);
        for(String setId : immidiateFunctionDependencies.get(id)){
            assertNotNull(var.findFuncDependency(setId));
        }
    }
    


    @ParameterizedTest
    @ValueSource(strings = {"C","CxS","Zmanim","conditioner","soldiers"})
    public void testIsPrimitive(String id){
        ModelInput inp = model.getParameter(id);
        if(inp != null){
            assertEquals(primitives.get(id), inp.isPrimitive());
        } else {
            inp = model.getSet(id);
            assertEquals(primitives.get(id), inp.isPrimitive());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"edge", "couples", "varForTest1"})
    public void setDependencyInference(String id) {
        ModelVariable var = model.getVariable(id);
        assertNotNull(var);
        assertEquals(var.getSetDependencies().size(), immidiateSetDependencies.get(id).length);
        for(String setId : immidiateSetDependencies.get(id)){
            assertNotNull(var.findSetDependency(setId));
        }

        assertEquals(var.getParamDependencies().size(), immidiateParamDependencies.get(id).length);
        for(String setId : immidiateParamDependencies.get(id)){
            assertNotNull(var.findParamDependency(setId));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"edge", "minShmirot","varForTest1","minimalRivuah"})
    public void testIsComplexVariable(String id) {
        ModelVariable var = model.getVariable(id);
        assertNotNull(var);
        assertEquals(var.isComplex(), isComplexVariable.get(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"((maxShmirot-minShmirot)+conditioner)**3" ,"(minimalRivuah)**2",
                            "(sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8",
                            "sum <person> in People : (TotalMishmarot[person]**2)"})
    public void testPreferencesDependencies(String id){
        ModelPreference mp = model.getPreference(id.replaceAll(" ", ""));
        assertNotNull(mp);
        assertEquals(mp.getSetDependencies().size(), immidiateSetDependencies.get(id).length);
        for(String setId : immidiateSetDependencies.get(id)){
            assertNotNull(mp.findSetDependency(setId));
        }

        assertEquals(mp.getParamDependencies().size(), immidiateParamDependencies.get(id).length);
        for(String setId : immidiateParamDependencies.get(id)){
            assertNotNull(mp.findParamDependency(setId));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"trivial1","trivial5","Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit","Kol_Haemdot_Meshubatsot_Hayal_Ehad","condForTest1", "condForTest2"})
    public void testConstraintsDependencies(String id){
        ModelConstraint mc = model.getConstraint(id);
        assertNotNull(mc);
        assertEquals(immidiateSetDependencies.get(id).length, mc.getSetDependencies().size());
        for(String setId : immidiateSetDependencies.get(id)){
            assertNotNull(mc.findSetDependency(setId));
        }  

        assertEquals(immidiateParamDependencies.get(id).length, mc.getParamDependencies().size());
        for(String setId : immidiateParamDependencies.get(id)){
            assertNotNull(mc.findParamDependency(setId));
        }
    }

        
   //The following two tests testStructureforTest2, testStructureforTest3 test a deprecated feature.
    
    // @Test
    // public void testStructureforTest2(){
    //     ModelSet set = model.getSet("forTest2");
    //     ModelInput.StructureBlock[] struct = set.getStructure();
    //     assertTrue(struct.length == 7);
    //     assertTrue(struct[0].dependency.getIdentifier().equals("anonymous_set") && struct[0].position == 1);
    //     assertTrue(struct[1].dependency.getIdentifier().equals("S") && struct[1].position == 1);
    //     assertTrue(struct[2].dependency.getIdentifier().equals("S") && struct[2].position == 2);
    //     assertTrue(struct[3].dependency.getIdentifier().equals("anonymous_set") && struct[3].position == 1);
    //     assertTrue(struct[4].dependency.getIdentifier().equals("C") && struct[4].position == 1);
    //     assertTrue(struct[5].dependency.getIdentifier().equals("anonymous_set") && struct[5].position == 1);
    //     assertTrue(struct[6].dependency.getIdentifier().equals("anonymous_set") && struct[6].position == 2);
    // }

    // @Test
    // public void testStructureforTest3(){   
    //     ModelSet set = model.getSet("forTest3");
    //     ModelInput.StructureBlock[] struct = set.getStructure();
    //     assertTrue(struct.length == 3);
    //     assertTrue(struct[0].dependency.getIdentifier().equals("anonymous_set") && struct[0].position == 1);
    //     assertTrue(struct[1].dependency.getIdentifier().equals("anonymous_set") && struct[1].position == 2);
    //     assertTrue(struct[2].dependency.getIdentifier().equals("anonymous_set") && struct[2].position == 3);
    // }

    
    @ParameterizedTest
    @ValueSource(strings = {"setWithRange","C","S","Zmanim", "Emdot", "CxS", "CxSxS", "forTest2","Shifts","SoldiersToShifts","forTest6", "forTest11", "forTest1", "FormalTimes"})
    public void testDependenciesOfSets(String identifier) {
        boolean hasSecondDegreeDeps = secondDegreeSetDependencies.containsKey(identifier);
        String setName = identifier;
        List<String> expectedSetFirstDegreeDeps = Arrays.asList(immidiateSetDependencies.get(identifier));
        List<String> expectedParamFirstDegreeDeps = Arrays.asList(immidiateParamDependencies.get(identifier));
        List<String> expectedSetDepsSecondDegree = null;
        List<String> expectedParamDepsSecondDegree = null;
        int setDepCount=0;
        int paramDepCount = 0;
        if(hasSecondDegreeDeps){
            expectedSetDepsSecondDegree = Arrays.asList(secondDegreeSetDependencies.get(identifier));
            expectedParamDepsSecondDegree = Arrays.asList(secondDegreeParamDependencies.get(identifier));
        }
        ModelSet set = model.getSet(setName);
        assertEquals(expectedSetFirstDegreeDeps.size(), set.getSetDependencies().size());
        for (ModelSet setDep : set.getSetDependencies()) {
            assertTrue(expectedSetFirstDegreeDeps.contains(setDep.getIdentifier()), "set imageId: "+setDep.getIdentifier());

            if(hasSecondDegreeDeps){
                for(ModelSet dep2 : setDep.getSetDependencies()){
                    assertTrue(expectedSetDepsSecondDegree.get(setDepCount).equals(dep2.getIdentifier()));
                    setDepCount++;
                }
                for(ModelParameter dep2 : setDep.getParamDependencies()){
                    assertTrue(expectedParamDepsSecondDegree.get(paramDepCount).equals(dep2.getIdentifier()));
                    paramDepCount++;
                }
            }
        }
        for (ModelInput paramDep : set.getParamDependencies()) {
            assertTrue(expectedParamFirstDegreeDeps.contains(paramDep.getIdentifier()));

            if(hasSecondDegreeDeps){
                for(ModelSet dep2 : paramDep.getSetDependencies()){
                    assertTrue(expectedSetDepsSecondDegree.get(setDepCount).equals(dep2.getIdentifier()));
                    setDepCount++;
                }
                for(ModelParameter dep2 : paramDep.getParamDependencies()){
                    assertTrue(expectedParamDepsSecondDegree.get(paramDepCount).equals(dep2.getIdentifier()));
                    paramDepCount++;
                }
            }
        }
        if(hasSecondDegreeDeps){
            assertTrue(setDepCount == expectedSetDepsSecondDegree.size());
            assertTrue(paramDepCount == expectedParamDepsSecondDegree.size());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers","planTimeRange","forTest7", "forTest8", "forTest9", "forTest10","forTest13","forTest14"})
    public void testImmidiateSetDependenciesOfParameters(String identifier) {
        String paramName = identifier;
        List<String> expectedDeps = Arrays.asList(immidiateSetDependencies.get(identifier));
        ModelParameter param = model.getParameter(paramName);
        assertEquals(expectedDeps.size(), param.getSetDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            param.findSetDependency(dep));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers","planTimeRange","forTest7", "forTest8", "forTest9", "forTest10","forTest13","forTest14"})
    public void testImmidiateParamDependenciesOfParameters(String identifier) {
        String paramName = identifier;
        List<String> expectedDeps = Arrays.asList(immidiateParamDependencies.get(identifier));
        ModelParameter param = model.getParameter(paramName);
        assertEquals(expectedDeps.size(), param.getParamDependencies().size());
        for (String dep : expectedDeps) {
            assertNotNull(
                            param.findParamDependency(dep));
        }
    }


    //TODO: tests that check types, may be converted to parameterized tests.



    @Test
    public void typeForTest7(){
        ModelInput s = model.getParameter("forTest7");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest8(){
        ModelInput s = model.getParameter("forTest8");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest9(){
        ModelInput s = model.getParameter("forTest9");
        ModelType expectedType = ModelPrimitives.FLOAT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest10(){
        ModelInput s = model.getParameter("forTest10");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest12(){
        ModelInput s = model.getParameter("forTest12");
        ModelType expectedType = ModelPrimitives.FLOAT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest13(){
        ModelInput s = model.getParameter("forTest13");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue(s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest14(){
        ModelInput s = model.getParameter("forTest14");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest1(){
        ModelInput s = model.getSet("forTest1");
        ModelType expectedType =  new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,  ModelPrimitives.INT,ModelPrimitives.INT,ModelPrimitives.INT,ModelPrimitives.INT});
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest15(){
        ModelInput s = model.getSet("forTest15");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,  ModelPrimitives.FLOAT});
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest16(){
        ModelInput s = model.getSet("forTest16");
        ModelType expectedType = ModelPrimitives.FLOAT;
        assertTrue( s.isCompatible(expectedType));
    }

    
    @Test
    public void typeForTest17(){
        ModelInput s = model.getSet("forTest17");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,ModelPrimitives.FLOAT,ModelPrimitives.FLOAT});
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeForTest18(){
        ModelInput s = model.getSet("forTest18");
        ModelType expectedType = ModelPrimitives.FLOAT;
        assertTrue( s.isCompatible(expectedType), "type: "+ s.getType().toString() +" expected: " + expectedType.toString());
    }

    //TODO: set forTest19 := {<x,y> in Times * Soldiers: <huehott(x+y)>}; this works
    // set forTest19 := {<x,y> in Times * Soldiers: huehott(x+y)}; but this doesn't - need to fix and test exactly that.
    @Test
    public void typeForTest19(){
        ModelInput s = model.getSet("forTest19");
        ModelType expectedType =  ModelPrimitives.TEXT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeTimeDifference(){
        ModelInput s = model.getFunction("timeDifference");
        ModelType expectedType = ModelPrimitives.FLOAT;
        assertTrue( s.isCompatible(expectedType));
    }

    @Test
    public void typeMyBool(){
        ModelInput s = model.getFunction("myBool");
        ModelType expectedType = ModelPrimitives.BINARY;
        assertTrue(s.isCompatible(expectedType));
    }

    @Test
    public void typeHueHott(){
        ModelInput s = model.getFunction("huehott");
        ModelType expectedType = ModelPrimitives.TEXT;
        assertTrue(s.isCompatible(expectedType));
    }

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
    public void typeCheckFormalTimes(){
        ModelSet s = model.getSet("FormalTimes");
        ModelType expectedType = new Tuple(new ModelPrimitives[]{ModelPrimitives.TEXT,ModelPrimitives.INT,ModelPrimitives.INT});
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

    @Test
    public void typeCheckforTest1(){
        ModelSet set = model.getSet("forTest1");
        assertTrue(set.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(2));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(3));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(4));
    }

    @Test
    public void typeCheckforTest11(){
        ModelSet set = model.getSet("forTest11");
        assertTrue(ModelPrimitives.INT.isCompatible(set.getType()));
    }

    @Test
    public void typeCheckforTest3(){
        ModelSet set = model.getSet("forTest3");
        assertTrue(set.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(2));
    }
    
    @Test
    public void typeCheckforTest2(){
        ModelSet set = model.getSet("forTest2");
        assertTrue(set.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(2));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(3));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(4));
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(5));
        assertEquals(ModelPrimitives.FLOAT,((Tuple)set.getType()).getTypes().get(6));
    }

    @Test
    public void typeCheckforShifts(){
        ModelSet set = model.getSet("Shifts");
        assertTrue(set.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(2));
    }

    @Test
    public void typeCheckforSoldiersToShifts(){
        ModelSet set = model.getSet("SoldiersToShifts");
        assertTrue(set.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.TEXT,((Tuple)set.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)set.getType()).getTypes().get(2));
    }

    @Test
    public void typeCheckforVarForTest1(){
        ModelVariable var = model.getVariable("varForTest1");
        assertTrue(var.getType() instanceof Tuple);
        assertEquals(ModelPrimitives.INT,((Tuple)var.getType()).getTypes().get(0));
        assertEquals(ModelPrimitives.TEXT,((Tuple)var.getType()).getTypes().get(1));
        assertEquals(ModelPrimitives.INT,((Tuple)var.getType()).getTypes().get(2));
        assertEquals(ModelPrimitives.TEXT,((Tuple)var.getType()).getTypes().get(3));
        assertEquals(ModelPrimitives.TEXT,((Tuple)var.getType()).getTypes().get(4));
        assertEquals(ModelPrimitives.INT,((Tuple)var.getType()).getTypes().get(5));
        assertEquals(ModelPrimitives.INT,((Tuple)var.getType()).getTypes().get(6));
    }

    @Test
    public void typeCheckPrefferedCourses(){
        ModelInput s = model.getParameter("sumOfPrefferedCoursesRatings");
        ModelType expectedType = ModelPrimitives.INT;
        assertTrue(s.isCompatible(expectedType), "type: "+ s.getType().toString() +" expected: " + expectedType.toString());
    }

    @Test
    public void testProjFunc(){
        ModelSet set = model.getSet("forTest4");
        assertEquals("anonymous_set",set.getSetDependencies().get(0).getIdentifier());
        assertEquals(2,set.getSetDependencies().get(0).getStructure().length);
        int[] depPointers = {2,1};
        int i = 0;
        for ( ModelSet.StructureBlock sb :set.getSetDependencies().get(0).getStructure()){
            assertEquals(sb.dependency.getIdentifier(),"forTest3");
            assertEquals(sb.position,depPointers[i++]);
        }
        assertEquals("<TEXT,INT>",set.getSetDependencies().get(0).getType().toString());
    }
    

    // forTest5 point to [forTest4,2] , [anonymous_set,2]
    @Test
    public void testProjFunc2(){
        ModelSet set = model.getSet("forTest5");
        assertEquals("anonymous_set",set.getSetDependencies().get(0).getIdentifier());
        assertEquals(2,set.getSetDependencies().get(0).getStructure().length);

        assertEquals("forTest4",set.getSetDependencies().get(0).getStructure()[0].dependency.getIdentifier());
        assertEquals(2,set.getSetDependencies().get(0).getStructure()[0].position);

        assertEquals("anonymous_set",set.getSetDependencies().get(0).getStructure()[1].dependency.getIdentifier());
        assertEquals(2,set.getSetDependencies().get(0).getStructure()[1].position);

        assertEquals("<INT,TEXT>",set.getSetDependencies().get(0).getType().toString());
    }

    @AfterAll
    public static void cleanUp() throws Exception {
       Path targetPath = Path.of(TEST_FILE_PATH);
       Files.deleteIfExists(targetPath);
       //Files.deleteIfExists(Path.of(targetPath.toString()+"SOLUTION"));
       //Files.deleteIfExists(Path.of("./src/test/Unit/TestFile2.zplSOLUTION"));
       System.gc();
       //temporary solution to a synchronization problem - deleteing file while in use
       
        modelRepository.deleteDocument(sourceId);
        
    }



    
}
