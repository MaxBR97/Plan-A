package Unit;
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

    private static String source = "/Plan-A/dev/Backend/src/test/Unit/TestFile.zpl";
    private static String TEST_FILE_PATH = "/Plan-A/dev/Backend/src/test/Unit/TestFileINSTANCE.zpl";

    private static HashMap<String,String[]> immidiateSetDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> immidiateParamDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> secondDegreeSetDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[]> secondDegreeParamDependencies =  new HashMap<String,String[]>();
    private static HashMap<String,String[][]> structure =  new HashMap<>();
    
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

        immidiateSetDependencies.put("forTest2", new String[]{"anonymous_set", "S", "anonymous_set", "C", "anonymous_set"});
        immidiateParamDependencies.put("forTest2", new String[]{});
        secondDegreeSetDependencies.put("forTest2",new String[]{"Emdot","Zmanim", "anonymous_set"});
        secondDegreeParamDependencies.put("forTest2", new String[]{"soldiers"});

        immidiateSetDependencies.put("conditioner", new String[]{});
        immidiateParamDependencies.put("conditioner", new String[]{});
        immidiateSetDependencies.put("absoluteMinimalRivuah", new String[]{});
        immidiateParamDependencies.put("absoluteMinimalRivuah", new String[]{});
        immidiateSetDependencies.put("soldiers", new String[]{});
        immidiateParamDependencies.put("soldiers", new String[]{});
        

        immidiateSetDependencies.put("couples", new String[]{"CxSxS"});
        immidiateParamDependencies.put("couples", new String[]{});
        immidiateSetDependencies.put("edge", new String[]{"CxS"});
        immidiateParamDependencies.put("edge", new String[]{});

        immidiateSetDependencies.put("trivial1", new String[]{"CxSxS"});
        immidiateParamDependencies.put("trivial1", new String[]{});
        immidiateSetDependencies.put("trivial5", new String[]{"CxS","Zmanim","CxS","CxSxS"});
        immidiateParamDependencies.put("trivial5", new String[]{});
        immidiateSetDependencies.put("Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit", new String[]{"CxS","CxS"});
        immidiateParamDependencies.put("Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit", new String[]{});
        immidiateSetDependencies.put("Kol_Haemdot_Meshubatsot_Hayal_Ehad", new String[]{"S","CxS"});
        immidiateParamDependencies.put("Kol_Haemdot_Meshubatsot_Hayal_Ehad", new String[]{});

        immidiateSetDependencies.put("rivuah", new String[]{"CxS","S"});
        immidiateParamDependencies.put("rivuah", new String[]{"conditioner"});
        
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
    @ValueSource(strings = {"rivuah"})
    public void testPreferencesDependencies(String id){
        ModelPreference mp = model.getPreference(id);
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
    @ValueSource(strings = {"trivial1","trivial5","Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit","Kol_Haemdot_Meshubatsot_Hayal_Ehad"})
    public void testConstraintsDependencies(String id){
        ModelConstraint mc = model.getConstraint(id);
        assertNotNull(mc);
        assertEquals(mc.getSetDependencies().size(), immidiateSetDependencies.get(id).length);
        for(String setId : immidiateSetDependencies.get(id)){
            assertNotNull(mc.findSetDependency(setId));
        }

        assertEquals(mc.getParamDependencies().size(), immidiateParamDependencies.get(id).length);
        for(String setId : immidiateParamDependencies.get(id)){
            assertNotNull(mc.findParamDependency(setId));
        }
    }

    @Test
    public void testStructureforTest2(){
        ModelSet set = model.getSet("forTest2");
        ModelInput.StructureBlock[] struct = set.getStructure();
        assertTrue(struct.length == 7);
        assertTrue(struct[0].dependency.getIdentifier().equals("anonymous_set") && struct[0].position == 0);
        assertTrue(struct[1].dependency.getIdentifier().equals("S") && struct[1].position == 0);
        assertTrue(struct[2].dependency.getIdentifier().equals("S") && struct[2].position == 1);
        assertTrue(struct[3].dependency.getIdentifier().equals("anonymous_set") && struct[3].position == 0);
        assertTrue(struct[4].dependency.getIdentifier().equals("C") && struct[4].position == 0);
        assertTrue(struct[5].dependency.getIdentifier().equals("anonymous_set") && struct[5].position == 0);
        assertTrue(struct[6].dependency.getIdentifier().equals("anonymous_set") && struct[6].position == 1);
    }

    @Test
    public void testStructureforTest3(){   
        ModelSet set = model.getSet("forTest3");
        ModelInput.StructureBlock[] struct = set.getStructure();
        assertTrue(struct.length == 3);
        assertTrue(struct[0].dependency.getIdentifier().equals("anonymous_set") && struct[0].position == 0);
        assertTrue(struct[1].dependency.getIdentifier().equals("anonymous_set") && struct[1].position == 1);
        assertTrue(struct[2].dependency.getIdentifier().equals("anonymous_set") && struct[2].position == 2);
    }

    
    @ParameterizedTest
    @ValueSource(strings = {"setWithRange","C","S","Zmanim", "Emdot", "CxS", "CxSxS", "forTest2"})
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
            assertTrue(expectedSetFirstDegreeDeps.contains(setDep.getIdentifier()), "set id: "+setDep.getIdentifier());

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
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers"})
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
    @ValueSource(strings = {"conditioner", "absoluteMinimalRivuah", "soldiers"})
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

    @AfterAll
    public static void cleanUp() throws IOException {
        Path targetPath = Path.of(TEST_FILE_PATH);
        Files.deleteIfExists(targetPath);
    }


    
}
