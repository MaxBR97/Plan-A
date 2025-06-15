package Acceptance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.test.context.TestPropertySource;

import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Image.SolutionValueDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.DependenciesDTO;
import DTO.Records.Model.ModelDefinition.ModelDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ExceptionDTO;
import DataAccess.ModelRepository;
import groupId.Main;
import groupId.Service;
import DTO.Records.Image.PreferenceModuleDTO;


/*TODO: Add test for creating a preference module
* with a cost param that doesnt appear in the inputSets list.
*/

/*TODO: Add test creating an image with an initial empty input set (set x := {};)
* and try to put correct inputs to it and solve it.
*/

/*TODO: Add test for toggling off a preference module and make sure it behaves properly. 
 * 
 */

 /*TODO: Add test for passing an integer value to float param.
 * 
 */

 /*TODO: 
 * add test for defining a paramter as both in variaablesmodule , preference and constraint module.
 */

 /*TODO: 
 * add a test for defining a paramter as both in variablesmodule and preference's costParam.
 * 
 */

 /*TODO: 
    * add a test for defining a parameter as both inputParam and costParam of a preference module.
 */

 /*TODO: 
  * add a test for defining a paramaeter as input for two different modules.
 */

 /*TODO: 
  * add a test for defining a set as input for two diffeeretn variables.
 */

 /*
  * TODO:
  * add a test for toggling off multiple modules.
  */

  /*TODO:
  * add a test for defining a param as part of a module, reconfigure the image,
  * and make that param part of variables module and try to solve while toggling off the module it used to be part of.
  */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
@ActiveProfiles({"H2mem", "securityAndGateway", "streamSolver"})
@TestPropertySource(properties = {
    "app.file.storage-dir=../Test/Models"
})
public class ServiceTest {
    @LocalServerPort
    private int port;
    static String pathToSimpleExample = Paths.get("src", "test", "resources", "ZimplExamples", "SimpleExample.zpl").toString();
    static String pathToSoldiersExampleProgram3 =  Paths.get("src", "test", "resources","ZimplExamples" ,"SoldiersExampleProgram3.zpl").toString();
    static String pathToComplexSoldiersExampleProgram3 =  Paths.get("src", "test", "resources","ZimplExamples" ,"ComplexSoldiersExampleProgram3.zpl").toString();
    static String pathToLearningParity2 = Paths.get("src", "test", "resources","ZimplExamples" ,"LearningParity2.zpl").toString();
    static String pathToCourseScheduling = Paths.get("src", "test", "resources","ZimplExamples" ,"Course_Scheduler_For_Students.zpl").toString();
    static String pathToEnhancedTravellingSalesmanProblem = Paths.get("src", "test", "resources","ZimplExamples" ,"EnhancedTravellingSalesmanProblem.zpl").toString();

    static RequestsManager requestsManager;
    static String imageName="myImage";
    static String imageDescription="desc";
    @Autowired
    private Service service;
    
    // @Autowired
    // private TestRestTemplate restTemplate;

    @Autowired
    WebClient.Builder webClientBuilder;

    @Autowired
    private ModelRepository modelRepository;

    @BeforeEach
    public void initilize() throws IOException {
        WebClient webClient = webClientBuilder
        .baseUrl("http://localhost:" + port)
        .build();
        requestsManager = new RequestsManager(port, webClient);
        
        // Ensure test directories exist
        Path modelsDir = Paths.get("..","Test", "Models").toAbsolutePath();
        try {
            Files.createDirectories(modelsDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test directories", e);
        }
        try{
            Thread.sleep(1000);
        }catch(Exception e){
            throw new RuntimeException("Failed to sleep", e);
        }
    }
    
    // Tests creation of an image from a simple code example, verifying that the model 
    // structure (constraints, preferences, variables, etc.) is correctly parsed
    @Test
    public void testCreateImage() {
        CreateImageFromFileDTO body = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(body);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
            "some imageId", new ModelDTO(
              Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(Set.of(),Set.of("x"))),
                    new ConstraintDTO("optionalConstraint", new DependenciesDTO(Set.of(),Set.of()))),
                Set.of(new PreferenceDTO("coefficient*myVar[3]", new DependenciesDTO(Set.of(),Set.of("coefficient"))),new PreferenceDTO("myVar[1]", new DependenciesDTO(Set.of(),Set.of()))),
                Set.of(new VariableDTO("myVar",List.of("myVar_1"),List.of("INT"), new DependenciesDTO(Set.of("mySet"),Set.of()),null,false)),
                Map.of("mySet",List.of("INT")),
                Map.of("x","INT", "coefficient","INT"),
                Map.of("myVar",List.of("INT"))));

        assertNotNull(result.imageId());
        assertEquals(result.model().constraints(), expected.model().constraints());
        assertEquals(result.model().preferences(), expected.model().preferences());
        assertEquals(result.model().variables(), expected.model().variables());
        assertEquals(result.model().setTypes(), expected.model().setTypes());
        assertEquals(result.model().paramTypes(), expected.model().paramTypes());
        assertEquals(result.model().varTypes(), expected.model().varTypes());
    }
    
    // Tests image configuration with variables, constraints, and preferences modules,
    // verifying that all components are correctly set and maintained
    @Test
    public void GivenImageDTO_WhenConfigImage_ImageIsCorrect() {
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

        ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
            .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
            .addConstraintsModule("Test module const", "PeanutButter", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
            .addPreferencesModule("Test module pref", "PeanutButter", Set.of("coefficient*myVar[3]"),Set.of(),Set.of(),Set.of())
            .build();

        ResponseEntity<?> configResponse = requestsManager.sendConfigImageRequest(configImage);
        expectSuccess(configResponse, Void.class);

        // Get the configured image and verify its structure
        ResponseEntity<?> getResponse = requestsManager.sendGetImageRequest(result.imageId());
        ImageDTO configuredImage = expectSuccess(getResponse, ImageDTO.class);

        // Verify each part of the configuration
        assertEquals(imageName, configuredImage.imageName());
        assertEquals(imageDescription, configuredImage.imageDescription());
        
        // Verify variables module
        assertNotNull(configuredImage.variablesModule());
        assertEquals(Set.of("myVar"), configuredImage.variablesModule().variablesOfInterest().stream()
            .map(v -> v.identifier())
            .collect(Collectors.toSet()));
        assertEquals(Set.of("mySet"), configuredImage.variablesModule().inputSets().stream()
            .map(s -> s.name())
            .collect(Collectors.toSet()));
        assertTrue(configuredImage.variablesModule().inputParams().isEmpty());

        // Verify constraints module
        assertEquals(1, configuredImage.constraintModules().size());
        var constModule = configuredImage.constraintModules().iterator().next();
        assertEquals("Test module const", constModule.moduleName());
        assertEquals("PeanutButter", constModule.description());
        assertEquals(Set.of("sampleConstraint"), constModule.constraints());
        assertEquals(Set.of(), constModule.inputSets().stream()
            .map(s -> s.name())
            .collect(Collectors.toSet()));
        assertEquals(Set.of("x"), constModule.inputParams().stream()
            .map(p -> p.name())
            .collect(Collectors.toSet()));

        // Verify preferences module
        assertEquals(1, configuredImage.preferenceModules().size());
        var prefModule = configuredImage.preferenceModules().iterator().next();
        assertEquals("Test module pref", prefModule.moduleName());
        assertEquals("PeanutButter", prefModule.description());
        assertEquals(Set.of("coefficient*myVar[3]"), prefModule.preferences());
        assertTrue(prefModule.inputSets().isEmpty());
        assertTrue(prefModule.inputParams().isEmpty());
        assertTrue(prefModule.costParams().isEmpty());
    }

    // Tests image creation from a file, verifying that complex model structures with
    // multiple constraints, preferences and variables are correctly parsed
    @Test
    public void GivenFile_WhenCreateImage_ImageIsCorrect() {
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Paths.get("./src/test/Acceptance/example.zpl")).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
            "some imageId", new ModelDTO(
              Set.of(new ConstraintDTO("drisha1", new DependenciesDTO(Set.of("People","Emdot"),Set.of("shiftTime"))),
              new ConstraintDTO("drisha2", new DependenciesDTO(Set.of("Emdot","People"),Set.of("shiftTime"))),
              new ConstraintDTO("drisha3", new DependenciesDTO(Set.of("People","Emdot"),Set.of("shiftTime","restHours"))),
              new ConstraintDTO("drisha4", new DependenciesDTO(Set.of("Emdot","People"),Set.of("shiftTime")))),
                Set.of(new PreferenceDTO("sum<person>inPeople:(TotalMishmarot[person]**2)", new DependenciesDTO(Set.of("People"),Set.of()))),
                Set.of(new VariableDTO("Shibutsim",List.of("Shibutsim_1","Shibutsim_2","Shibutsim_3"),List.of("TEXT","TEXT","INT") , new DependenciesDTO(Set.of("People","Emdot"),Set.of("shiftTime")),null,true),
                        new VariableDTO("TotalMishmarot", List.of("TotalMishmarot_1"),List.of("TEXT"),new DependenciesDTO(Set.of("People"),Set.of()),null,false)),
              Map.of(
                "People",List.of("TEXT"),
                "Emdot",List.of("TEXT")),
                Map.of("shiftTime","INT",
                "restHours","INT"),
                      Map.of("Shibutsim",List.of("TEXT","TEXT","INT"),
                                "TotalMishmarot",List.of("TEXT"))));

        assertNotNull(result.imageId());
        assertEquals(result.model().constraints(), expected.model().constraints());
        assertEquals(result.model().preferences(), expected.model().preferences());
        assertEquals(result.model().variables(), expected.model().variables());
        assertEquals(result.model().setTypes(), expected.model().setTypes());
        assertEquals(result.model().paramTypes(), expected.model().paramTypes());
        assertEquals(result.model().varTypes(), expected.model().varTypes());
    }

        @ParameterizedTest
        @ValueSource(strings = {
                                "src\\test\\resources\\ZimplExamples\\SoldiersExampleProgram2.zpl",
                                "src\\test\\resources\\ZimplExamples\\LearningParity2.zpl",
                                "src\\test\\resources\\ZimplExamples\\ComplexSoldiersExampleProgram.zpl",
                                "src\\test\\resources\\ZimplExamples\\ComplexSoldiersExampleProgram4.zpl",
                                "src\\test\\resources\\ZimplExamples\\Course_Scheduler_For_Students.zpl",
                                "src\\test\\resources\\ZimplExamples\\EnhancedTravellingSalesmanProblem.zpl"
                            })
        public void testSuccessfulCreationOfDifferentImages(String pathStringToFile){
            CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription,"Max",true,Paths.get(pathStringToFile)).build();
            CreateImageResponseDTO responseCreateImage = expectSuccess(requestsManager.sendCreateImageRequest(createImage),CreateImageResponseDTO.class);
        }

        // Tests basic solving functionality with a simple model, verifying that the
        // solution matches expected values
        @Test
        public void testSolve_Simple() {
            try {
                CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
                CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of("x"))
                .build();
            expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

            SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .build();
            SolutionDTO solution = expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);

                assertEquals(Set.of(new SolutionValueDTO(List.of("3"),5),
                                    new SolutionValueDTO(List.of("1"),5),
                                    new SolutionValueDTO(List.of("2"),0))
                                    , solution.solution().get("myVar").solutions());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    
        // Tests the retrieval of stored inputs for an image.
        @Test
        public void testLoadImageInput() {
            // create Image
        CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , Path.of(pathToSimpleExample)).build();
        CreateImageResponseDTO responseCreateImage = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);
        
        ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage)
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
                .addConstraintsModule("MyConst", "", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
                .addPreferencesModule("MyPref", "desc", Set.of("coefficient*myVar[3]"),Set.of(),Set.of(),Set.of())
                .build();

        Void responseConfigImage = expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);
    
        InputDTO expected = new InputDTO(
        Map.of(
            "mySet", List.of(List.of("1"), List.of("2"),List.of("3"),List.of("4"))
        ),
        Map.of(
            "x", List.of("10")
        ),
        List.of(), 
        List.of()  
        );
    
        InputDTO result = expectSuccess(requestsManager.sendGetInputsRequest(responseCreateImage.imageId()), InputDTO.class);    
        assertNotNull(result);
        assertEquals(result.setsToValues(),expected.setsToValues());
        assertEquals(result.paramsToValues(),expected.paramsToValues());
        assertEquals(result.constraintModulesToggledOff(),expected.constraintModulesToggledOff());
        assertEquals(result.preferenceModulesToggledOff(),expected.preferenceModulesToggledOff());
        }
        
    // Tests the retrieval of all images, verifying that multiple images can be
    // created and listed correctly
    //TODO: Test passes when run alone, but fails when run with other tests.
    // uncomment test after solving the issue.
    // @Test
    // public void getImagesTest() {
    //     // Use unique names for this test
    //     String testImage1Name = "getImagesTest_image1_" + System.currentTimeMillis();
    //     String testImage2Name = "getImagesTest_image2_" + System.currentTimeMillis();
        
    //     // Get initial count of images
    //     List<ImageDTO> initialImages = expectSuccess(requestsManager.sendGetAllImagesRequest(), List.class);
    //     int initialCount = initialImages.size();

    //     // Create first image with unique name
    //     CreateImageFromFileDTO createImage1 = new CreateImageRequestBuilder(
    //         testImage1Name, 
    //         imageDescription, 
    //         "none", 
    //         false, 
    //         SimpleCodeExample
    //     ).build();
    //     expectSuccess(requestsManager.sendCreateImageRequest(createImage1), CreateImageResponseDTO.class);

    //     CreateImageFromFileDTO createImage2 = new CreateImageRequestBuilder(
    //         testImage2Name,
    //         imageDescription,
    //         "none", 
    //         false,
    //         """
    //         set S := {<1,"a">, <2,"bs">};
    //         minimize this:
    //             300;
    //         """
    //     ).build();
    //     expectSuccess(requestsManager.sendCreateImageRequest(createImage2), CreateImageResponseDTO.class);

    //     // Verify we added exactly 2 new images
    //     List<ImageDTO> images = expectSuccess(requestsManager.sendGetAllImagesRequest(), List.class);
    //     assertEquals(initialCount + 2, images.size(), "Expected exactly 2 new images to be added");

    //     boolean foundImage1 = false;
    //     boolean foundImage2 = false;
    //     for (ImageDTO image : images) {
    //         if (image.imageName().equals(testImage1Name)) foundImage1 = true;
    //         if (image.imageName().equals(testImage2Name)) foundImage2 = true;
    //     }
    //     assertTrue(foundImage1, "First test image not found");
    //     assertTrue(foundImage2, "Second test image not found");
    // }

    // Tests image deletion functionality, verifying proper deletion and appropriate
    // error responses for invalid operations
    @Test
    public void deleteImageTest() {
        // Create an image first
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
        CreateImageResponseDTO createResult = expectSuccess(response, CreateImageResponseDTO.class);
        String imageId = createResult.imageId();
        
        // Verify image exists
        ResponseEntity<?> getResponse = requestsManager.sendGetImageRequest(imageId);
        expectSuccess(getResponse, ImageDTO.class);

        System.gc();

        // Delete the image
        ResponseEntity<?> deleteResponse = requestsManager.sendDeleteImageRequest(imageId);
        expectSuccess(deleteResponse, Void.class);

        // Try to get the deleted image - should fail
        ResponseEntity<?> getDeletedResponse = requestsManager.sendGetImageRequest(imageId);
        expectError(getDeletedResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Tests solving with constraint toggling, verifying that constraints can be
    // selectively disabled during solving
    @Test
    public void testSolve_WithToggleOff() {
        try {
            // Use unique names for this test
            String testImageName = "toggleTest";
            String testConst1 = "MyConst1";
            String testPref1 = "MyPref1";
            String testPref2 = "MyPref2";
            
            CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(testImageName, imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
            CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(testImageName, result)
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of("x"))
                .addConstraintsModule(testConst1, "", Set.of("optionalConstraint"), Set.of(), Set.of())
                .addPreferencesModule(testPref1, "desc", Set.of("coefficient*myVar[3]"),Set.of(),Set.of(),Set.of("coefficient"))
                .addPreferencesModule(testPref2, "desc", Set.of("myVar[1]"),Set.of(),Set.of(),Set.of())
                .build();

            expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

            // Case 1: Toggle off constraint, coefficient=10, x=10, expect myVar[3]=10
            SolveCommandDTO solveRequest1 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .setParamInput("coefficient", List.of("10"))
                .addToggleOffConstraintModule(testConst1)
                .build();   
            SolutionDTO solution1 = expectSuccess(requestsManager.sendSolveRequest(solveRequest1), SolutionDTO.class);
            assertEquals(Set.of(new SolutionValueDTO(List.of("3"),10),
                                new SolutionValueDTO(List.of("1"),0),
                                new SolutionValueDTO(List.of("2"),0)), solution1.solution().get("myVar").solutions());

            // Case 2: Toggle off pref1, coefficient=10, x=10, expect myVar[1]=10
            SolveCommandDTO solveRequest2 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .setParamInput("coefficient", List.of("10"))
                .addToggleOffPreferenceModule(testPref1)
                .build();   
            SolutionDTO solution2 = expectSuccess(requestsManager.sendSolveRequest(solveRequest2), SolutionDTO.class);
            assertEquals(Set.of(new SolutionValueDTO(List.of("1"),10),
                                new SolutionValueDTO(List.of("2"),0),
                                new SolutionValueDTO(List.of("3"),0)), solution2.solution().get("myVar").solutions());

            // Case 3: Toggle off pref2, coefficient=-5, x=10, expect myVar[3]=0 (not in result)
            SolveCommandDTO solveRequest3 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .setParamInput("coefficient", List.of("-5"))
                .addToggleOffPreferenceModule(testPref2)
                .build();   
            SolutionDTO solution3 = expectSuccess(requestsManager.sendSolveRequest(solveRequest3), SolutionDTO.class);
            assertTrue(solution3.solution().get("myVar").solutions().contains(new SolutionValueDTO(List.of("3"),0)),
             "check that myVar[3] value is 0");

            // Case 4: Toggle off constraint and pref2, coefficient=10, x=10, expect myVar[3]=10
            SolveCommandDTO solveRequest4 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .setParamInput("coefficient", List.of("10"))
                .addToggleOffConstraintModule(testConst1)
                .addToggleOffPreferenceModule(testPref2)
                .build();   
            SolutionDTO solution4 = expectSuccess(requestsManager.sendSolveRequest(solveRequest4), SolutionDTO.class);
            assertEquals(Set.of(new SolutionValueDTO(List.of("3"),10),
                                new SolutionValueDTO(List.of("1"),0),
                                new SolutionValueDTO(List.of("2"),0)), solution4.solution().get("myVar").solutions());

            // Case 5: Toggle off all modules, coefficient=10, x=10, expect any solution
            SolveCommandDTO solveRequest5 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .setParamInput("coefficient", List.of("10"))
                .addToggleOffConstraintModule(testConst1)
                .addToggleOffPreferenceModule(testPref1)
                .addToggleOffPreferenceModule(testPref2)
                .build();   
            SolutionDTO solution5 = expectSuccess(requestsManager.sendSolveRequest(solveRequest5), SolutionDTO.class);
            assertNotNull(solution5.solution().get("myVar").solutions(), "Should return some solution");
            assertTrue(!solution5.solution().get("myVar").solutions().isEmpty(), "Should return non-empty solution");

            // Case 6: Toggle off constraint, coefficient=10, x=-10, expect infeasible
            SolveCommandDTO solveRequest6 = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("-10"))
                .setParamInput("coefficient", List.of("10"))
                .addToggleOffConstraintModule(testConst1)
                .build();   
            SolutionDTO solution6 = expectSuccess(requestsManager.sendSolveRequest(solveRequest6), SolutionDTO.class);
            assertTrue(solution6.solution() == null || solution6.solution().isEmpty(), "Solution should be unsolved/infeasible");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // Tests the system's ability to handle and solve large, complex models with
    // predefined inputs
    @Test
    public void testUploadingHeavyImageAndSolveWithInputs() {
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToComplexSoldiersExampleProgram3))
            .build();
        CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

        ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
            .setDefaultVariablesModule()
            .build();
        expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

        InputDTO inputs = expectSuccess(requestsManager.sendGetInputsRequest(result.imageId()), InputDTO.class);

        SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(result)
            .setInput(inputs)
            .setTimeout(16)
            .build();
        expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);
    }

    // Tests the system's ability to handle multiple different programs in sequence,
    // verifying proper creation, configuration and solving for each
    @Test
    public void testUploadMultipleProgramsAndSolve() {
        List<String> uploadAll = List.of(
            pathToSoldiersExampleProgram3,
            pathToLearningParity2,
            pathToComplexSoldiersExampleProgram3,
            pathToCourseScheduling,
            pathToEnhancedTravellingSalesmanProblem
        );
        List<String> respectiveImageIds = new LinkedList<>();

        for(String program : uploadAll) {
            CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(program))
                .build();
            CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);
            respectiveImageIds.add(result.imageId());

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
                .setDefaultVariablesModule()
                .build();
            expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);
        }

        for(String id : respectiveImageIds) {
            InputDTO inputs = expectSuccess(requestsManager.sendGetInputsRequest(id), InputDTO.class);

            SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(id)
                .setInput(inputs)
                .setTimeout(15)
                .build();
            expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);
        }
    }

    // Basic persistent solve test with HTTP polling, verifying that the solve process
    // is correctly polled and that the correct messages are received
    @Test
    public void testPersistentSolveWithHTTPPolling() {
        List<String> polledMessages = new LinkedList<>();
        
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Path.of(pathToSimpleExample)).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);   

        // Configure image with variables module containing myVar and mySet
        ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
            .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
            .build();
        expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

        // Create solve request with input for mySet
        SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(result.imageId())
            .setSetInput("mySet", List.of(List.of("1"), List.of("2"), List.of("3")))
            .setParamInput("x", List.of("10"))
            .build();
        // Start persistent solve
        expectSuccess(requestsManager.sendPersistentSolve(solveRequest), Void.class);

        // Wait and poll
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ResponseEntity<?> pollResponse = requestsManager.sendPollPersistentSolve();
        // expectSuccess(pollResponse, String.class);
        // polledMessages.add(pollResponse.getBody().toString());
        expectSuccess(requestsManager.sendContinuePersistentSolve(solveRequest), Void.class);
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ResponseEntity<?> secondPollResponse = requestsManager.sendPollPersistentSolve();
        // expectSuccess(secondPollResponse, String.class);
        // polledMessages.add(secondPollResponse.getBody().toString());

        // // Verify poll messages at end
        // String firstPoll = polledMessages.get(0);
        // String secondPoll = polledMessages.get(1);

        // assertTrue(firstPoll.contains("read problem"), "First poll should contain 'read problem', got: " + firstPoll);
        // assertTrue(firstPoll.contains("presolving:"), "First poll should contain 'presolving:', got: " + firstPoll); 
        // assertTrue(firstPoll.contains("SCIP Status"), "First poll should contain 'SCIP Status', got: " + firstPoll);
        // assertTrue(secondPoll.contains("problem is already solved"), "Second poll should indicate problem is solved, got: " + secondPoll);
    }

    @Test
    public void testCourseSchedulerConfiguration() {
        // Create image from Course Scheduler file
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName, imageDescription, "none", false, Path.of(pathToCourseScheduling))
            .build();
        CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

        // Configure image with variables module and preference modules
        ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
            .setVariablesModule(
                Set.of("take_course", "choose_group", "day_has_class", "first_activity_of_the_day", "total_points", "assignment_presentation", "assignment_presentation_formatted"),
                Set.of("CourseData", "Weekdays", "CourseSchedule"),
                Set.of()
            )
            .addPreferencesModule(
                "Points Preference",
                "Minimize deviation from target points",
                Set.of("weight_points * abs(total_points - target_points)".replaceAll("\\s+", "")),
                Set.of(),
                Set.of("target_points"),
                Set.of("weight_points")
            )
            .addPreferencesModule(
                "Days Preference",
                "Minimize active days",
                Set.of("weight_days * (sum <w> in Weekdays: day_has_class[w])".replaceAll("\\s+", "")),
                Set.of(),
                Set.of(),
                Set.of("weight_days")
            )
            .addPreferencesModule(
                "Early Start Preference",
                "Prefer early start times",
                Set.of("weight_day_start_early * (sum <c,g,w> in proj(CourseSchedule, <1,2,5>): (first_activity_of_the_day[c,g,w] * (min <c2,g2,t2,st2,wd2,sh2,eh2> in CourseSchedule | c == c2 and g == g2 and w == wd2: sh2)))".replaceAll("\\s+", "")),
                Set.of(),
                Set.of(),
                Set.of("weight_day_start_early")
            )
            .addPreferencesModule(
                "Preferred Courses",
                "Maximize preferred courses",
                Set.of("((-1 * weight_preffered_courses) * (sum <c> in Courses: (take_course[c] * getCourseRating(c)/sumOfPrefferedCoursesRatings)))".replaceAll("\\s+", "") ),
                Set.of("preffered_courses"),
                Set.of(),
                Set.of("weight_preffered_courses")
            )
            .addPreferencesModule(
                "Preferred Teachers",
                "Maximize preferred teachers",
                Set.of("((-1 * weight_preffered_teachers) * (sum <c> in Courses: (take_course[c] * getTeacherRating(c)/sumOfPrefferedTeachersRatings)))".replaceAll("\\s+", "")),
                Set.of("preffered_teachers"),
                Set.of(),
                Set.of("weight_preffered_teachers")
            )
            .build();

        expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

        // Get the configured image and verify its structure
        ResponseEntity<?> getResponse = requestsManager.sendGetImageRequest(result.imageId());
        ImageDTO configuredImage = expectSuccess(getResponse, ImageDTO.class);

        // Verify image basic info
        assertEquals(imageName, configuredImage.imageName());
        assertEquals(imageDescription, configuredImage.imageDescription());

        // Verify variables module
        assertNotNull(configuredImage.variablesModule());
        assertEquals(
            Set.of("take_course", "choose_group", "day_has_class", "first_activity_of_the_day", "total_points", "assignment_presentation", "assignment_presentation_formatted"),
            configuredImage.variablesModule().variablesOfInterest().stream()
                .map(v -> v.identifier())
                .collect(Collectors.toSet())
        );
        assertEquals(
            Set.of("CourseData", "Weekdays", "CourseSchedule"),
            configuredImage.variablesModule().inputSets().stream()
                .map(s -> s.name())
                .collect(Collectors.toSet())
        );
        assertTrue(configuredImage.variablesModule().inputParams().isEmpty());

        // Verify no constraint modules
        assertTrue(configuredImage.constraintModules().isEmpty());

        // Verify preference modules
        assertEquals(5, configuredImage.preferenceModules().size());
        
        // Helper function to find preference module by name
        java.util.function.Function<String, PreferenceModuleDTO> findModule = 
            name -> configuredImage.preferenceModules().stream()
                .filter(m -> m.moduleName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Module " + name + " not found"));

        // Verify Points Preference module
        var pointsModule = findModule.apply("Points Preference");
        assertEquals(Set.of("weight_points * abs(total_points - target_points)".replaceAll("\\s+", "")), pointsModule.preferences());
        assertTrue(pointsModule.inputSets().isEmpty());
        assertEquals(Set.of("target_points", "weight_points"), pointsModule.inputParams().stream().map(p -> p.name()).collect(Collectors.toSet()));
        assertEquals(Set.of("weight_points"), pointsModule.costParams().stream().map(p -> p.name()).collect(Collectors.toSet()));

        // Verify Days Preference module
        var daysModule = findModule.apply("Days Preference");
        assertEquals(Set.of("weight_days * (sum <w> in Weekdays: day_has_class[w])".replaceAll("\\s+", "")), daysModule.preferences());
        assertTrue(daysModule.inputSets().isEmpty());
        assertEquals(Set.of( "weight_days"), daysModule.inputParams().stream().map(p -> p.name()).collect(Collectors.toSet()));
        assertEquals(Set.of("weight_days"), daysModule.costParams().stream().map(p -> p.name()).collect(Collectors.toSet()));

        // Verify Early Start Preference module
        var earlyStartModule = findModule.apply("Early Start Preference");
        assertEquals(Set.of("weight_day_start_early * (sum <c,g,w> in proj(CourseSchedule, <1,2,5>): (first_activity_of_the_day[c,g,w] * (min <c2,g2,t2,st2,wd2,sh2,eh2> in CourseSchedule | c == c2 and g == g2 and w == wd2: sh2)))".replaceAll("\\s+", "")), earlyStartModule.preferences());
        assertTrue(earlyStartModule.inputSets().isEmpty());
        assertEquals(Set.of("weight_day_start_early"), earlyStartModule.inputParams().stream().map(p -> p.name()).collect(Collectors.toSet()));
        assertEquals(Set.of("weight_day_start_early"), earlyStartModule.costParams().stream().map(p -> p.name()).collect(Collectors.toSet()));

        // Verify Preferred Courses module
        var coursesModule = findModule.apply("Preferred Courses");
        assertEquals(Set.of("((-1 * weight_preffered_courses) * (sum <c> in Courses: (take_course[c] * getCourseRating(c)/sumOfPrefferedCoursesRatings)))".replaceAll("\\s+", "")), coursesModule.preferences());
        assertEquals(Set.of("preffered_courses"), coursesModule.inputSets().stream().map(s -> s.name()).collect(Collectors.toSet()));
        assertEquals(Set.of("weight_preffered_courses"), coursesModule.costParams().stream().map(p -> p.name()).collect(Collectors.toSet()));

        // Verify Preferred Teachers module
        var teachersModule = findModule.apply("Preferred Teachers");
        assertEquals(Set.of("((-1 * weight_preffered_teachers) * (sum <c> in Courses: (take_course[c] * getTeacherRating(c)/sumOfPrefferedTeachersRatings)))".replaceAll("\\s+", "")), teachersModule.preferences());
        assertEquals(Set.of("preffered_teachers"), teachersModule.inputSets().stream().map(s -> s.name()).collect(Collectors.toSet()));
        assertEquals(Set.of("weight_preffered_teachers"), teachersModule.costParams().stream().map(p -> p.name()).collect(Collectors.toSet()));
    }

    @Test
    public void testImageCreationWithCodeValidation() {
        // Test Case 1: Invalid code with empty set
        String codeWithEmptySet = """
            set a := {1};
            set x := {};
            param y := 5;
            var z;
            subto c1: z >= 0;
            minimize obj: z;
        """;
        CreateImageFromFileDTO invalidRequest = new CreateImageRequestBuilder(
            "invalidImage",
            "Image with empty set",
            "testUser",
            false,
            codeWithEmptySet
        ).build();
        ResponseEntity<?> invalidResponse = requestsManager.sendCreateImageRequest(invalidRequest);
        ExceptionDTO error = expectError(invalidResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(error.msg().toLowerCase().contains("empty set"), "Error should mention empty set, but was : " + error.msg());

        // Test Case 2: Invalid code that doesn't compile
        String nonCompilingCode = """
            set y := {};    
            set x := {1,2,3;    
            var z;
            subto c1: z >= 0;
            minimize obj: z;
        """;
        CreateImageFromFileDTO nonCompilingRequest = new CreateImageRequestBuilder(
            "invalidImage2",
            "Image with non-compiling code",
            "testUser",
            false,
            nonCompilingCode
        ).build();
        ResponseEntity<?> nonCompilingResponse = requestsManager.sendCreateImageRequest(nonCompilingRequest);
        ExceptionDTO compileError = expectError(nonCompilingResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(compileError.msg().toLowerCase().contains("compilation"), "Error should mention compilation failure, but was: " + compileError.msg());

        // // Test Case 3: Code with unknown type
        // String codeWithUnknownType = """
        //     set x := {1,2,3};
        //     set y := {<a,b> in x * x : a*b}; 
        //     param z := 5;
        //     var w;
        //     minimize obj: w;
        // """;
        // CreateImageFromFileDTO unknownTypeRequest = new CreateImageRequestBuilder(
        //     "invalidImage3",
        //     "Image with unknown type",
        //     "testUser",
        //     false,
        //     codeWithUnknownType
        // ).build();
        // ResponseEntity<?> unknownTypeResponse = requestsManager.sendCreateImageRequest(unknownTypeRequest);
        // ExceptionDTO unknownTypeError = expectError(unknownTypeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        // assertTrue(unknownTypeError.msg().toLowerCase().contains("unknown type"), 
        //     "Error should mention unknown type, but was: " + unknownTypeError.msg());

        // Test Case 4: Valid code
        String validCode = """
            set x := {1,2,3};
            param y := 5;
            var z[x] integer;
            subto c1: z[1] >= 0;
            minimize obj: z[1];
        """;
        CreateImageFromFileDTO validRequest = new CreateImageRequestBuilder(
            "validImage",
            "Image with valid code",
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> validResponse = requestsManager.sendCreateImageRequest(validRequest);
        CreateImageResponseDTO validResult = expectSuccess(validResponse, CreateImageResponseDTO.class);
        assertNotNull(validResult.imageId(), "Valid code should create an image with ID");
    }

    @Test
    public void testImageNameAndDescriptionValidation() {
        String validCode = """
            set x := {1,2,3};
            param y := 5;
            var z[x] integer;
            minimize obj: z[1];
        """;

        // Test Case 1: Null image name
        CreateImageFromFileDTO nullNameRequest = new CreateImageRequestBuilder(
            null,
            "Valid description",
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> nullNameResponse = requestsManager.sendCreateImageRequest(nullNameRequest);
        ExceptionDTO nullNameError = expectError(nullNameResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(nullNameError.msg().toLowerCase().contains("name"), "Error should mention invalid name");

        // Test Case 2: Empty image name
        CreateImageFromFileDTO emptyNameRequest = new CreateImageRequestBuilder(
            "",
            "Valid description",
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> emptyNameResponse = requestsManager.sendCreateImageRequest(emptyNameRequest);
        ExceptionDTO emptyNameError = expectError(emptyNameResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(emptyNameError.msg().toLowerCase().contains("name"), "Error should mention invalid name");

        // Test Case 3: Null description (should be allowed)
        CreateImageFromFileDTO nullDescRequest = new CreateImageRequestBuilder(
            "validName",
            null,
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> nullDescResponse = requestsManager.sendCreateImageRequest(nullDescRequest);
        CreateImageResponseDTO nullDescResult = expectSuccess(nullDescResponse, CreateImageResponseDTO.class);
        assertNotNull(nullDescResult.imageId(), "Image should be created with null description");

        // Test Case 4: Empty description (should be allowed)
        CreateImageFromFileDTO emptyDescRequest = new CreateImageRequestBuilder(
            "validName",
            "",
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> emptyDescResponse = requestsManager.sendCreateImageRequest(emptyDescRequest);
        CreateImageResponseDTO emptyDescResult = expectSuccess(emptyDescResponse, CreateImageResponseDTO.class);
        assertNotNull(emptyDescResult.imageId(), "Image should be created with empty description");

        // Test Case 5: Valid name and description
        CreateImageFromFileDTO validRequest = new CreateImageRequestBuilder(
            "validName",
            "Valid description",
            "testUser",
            false,
            validCode
        ).build();
        ResponseEntity<?> validResponse = requestsManager.sendCreateImageRequest(validRequest);
        CreateImageResponseDTO validResult = expectSuccess(validResponse, CreateImageResponseDTO.class);
        assertNotNull(validResult.imageId(), "Image should be created with valid name and description");

        // Test Case 6: Update with invalid name
        ImageDTO invalidUpdateDTO = new ImageDTO(
            validResult.imageId(),
            "",  // invalid empty name
            "Valid description",
            "testUser",
            false,
            Map.of(),  // empty solver settings
            null,  // no variables module
            Set.of(),  // empty constraint modules
            Set.of()   // empty preference modules
        );
        ImageConfigDTO invalidUpdateRequest = new ImageConfigDTO(
            validResult.imageId(),
            invalidUpdateDTO
        );
        ResponseEntity<?> invalidUpdateResponse = requestsManager.sendConfigImageRequest(invalidUpdateRequest);
        ExceptionDTO invalidUpdateError = expectError(invalidUpdateResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(invalidUpdateError.msg().toLowerCase().contains("name"), "Error should mention invalid name");
    }

    @Test
    public void testUpdateAndPersistTagsAndAliases() {
        // Create an image with a model that has sets and parameters for different modules
        String code = """
            set varModuleSet := {<1,2>, <2,3>, <3,4>};
            set constModuleSet := {4,5,6};
            set prefModuleSet := {7,8,9};
            param varModuleParam := 5;
            param constModuleParam := 10;
            param prefModuleParam := 15;
            var myVar[{1 .. card(varModuleSet) * varModuleParam }] integer;
            var complexVar[constModuleSet] integer;
            subto const1: myVar[1] + card(constModuleSet) <= constModuleParam;
            minimize obj: prefModuleParam * (myVar[1] + card(prefModuleSet)) + 1;
        """;

        // Create the image
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(
            "tagsAndAliasesTest",
            "Testing tags and aliases persistence across modules",
            "testUser",
            false,
            code
        ).build();
        CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

        // Configure the image with modules and their respective sets/parameters
        ImageConfigDTO configImage = new ConfigureImageRequestBuilder("tagsAndAliasesTest", result)
            // Variables module with its exclusive set and parameter
            .setVariablesModule(
                Set.of("myVar"),
                Set.of("varModuleSet"),
                Set.of("varModuleParam")
            )
            // Constraint module with its exclusive set and parameter
            .addConstraintsModule(
                "ConstraintModule",
                "Test constraints",
                Set.of("const1"),
                Set.of("constModuleSet"),
                Set.of("constModuleParam")
            )
            // Preference module with its exclusive set and parameter
            .addPreferencesModule(
                "PreferenceModule",
                "Test preferences",
                Set.of("prefModuleParam*(myVar[1]+card(prefModuleSet))"),
                Set.of("prefModuleSet"),
                Set.of(),
                Set.of("prefModuleParam")
            )
            // Update metadata for sets in different modules
            .updateSetMetadata("varModuleSet", List.of("VarSet", "Basic"), "SimpleVarSet")
            .updateSetMetadata("constModuleSet", List.of("ConstSet"), "ConstraintSet")
            .updateSetMetadata("prefModuleSet", List.of("PrefSet"), "PreferenceSet")
            // Update metadata for parameters in different modules
            .updateParameterMetadata("varModuleParam", "VarParam", "SimpleParam")
            .updateParameterMetadata("constModuleParam", "ConstParam", "ConstraintParam")
            .updateParameterMetadata("prefModuleParam", "PrefParam", "PreferenceParam")
            .build();

        // Send the update request
        expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

        // Fetch the image and verify persistence
        ResponseEntity<?> getResponse = requestsManager.sendGetImageRequest(result.imageId());
        ImageDTO fetchedImage = expectSuccess(getResponse, ImageDTO.class);

        // Verify sets in variables module
        var varModuleSet = fetchedImage.variablesModule().inputSets().stream()
            .filter(s -> s.name().equals("varModuleSet"))
            .findFirst()
            .orElseThrow();
        assertEquals(List.of("VarSet", "Basic"), varModuleSet.tags());
        assertEquals("SimpleVarSet", varModuleSet.alias());

        // Verify sets in constraint module
        var constModuleSet = fetchedImage.constraintModules().stream()
            .flatMap(m -> m.inputSets().stream())
            .filter(s -> s.name().equals("constModuleSet"))
            .findFirst()
            .orElseThrow();
        assertEquals(List.of("ConstSet"), constModuleSet.tags());
        assertEquals("ConstraintSet", constModuleSet.alias());

        // Verify sets in preference module
        var prefModuleSet = fetchedImage.preferenceModules().stream()
            .flatMap(m -> m.inputSets().stream())
            .filter(s -> s.name().equals("prefModuleSet"))
            .findFirst()
            .orElseThrow();
        assertEquals(List.of("PrefSet"), prefModuleSet.tags());
        assertEquals("PreferenceSet", prefModuleSet.alias());

        // Verify parameters in variables module
        var varModuleParam = fetchedImage.variablesModule().inputParams().stream()
            .filter(p -> p.name().equals("varModuleParam"))
            .findFirst()
            .orElseThrow();
        assertEquals("VarParam", varModuleParam.tag());
        assertEquals("SimpleParam", varModuleParam.alias());

        // Verify parameters in constraint module
        var constModuleParam = fetchedImage.constraintModules().stream()
            .flatMap(m -> m.inputParams().stream())
            .filter(p -> p.name().equals("constModuleParam"))
            .findFirst()
            .orElseThrow();
        assertEquals("ConstParam", constModuleParam.tag());
        assertEquals("ConstraintParam", constModuleParam.alias());

        // Verify parameters in preference module
        var prefModuleParam = fetchedImage.preferenceModules().stream()
            .flatMap(m -> m.inputParams().stream())
            .filter(p -> p.name().equals("prefModuleParam"))
            .findFirst()
            .orElseThrow();
        assertEquals("PrefParam", prefModuleParam.tag());
        assertEquals("PreferenceParam", prefModuleParam.alias());
    }

    private <T> T expectSuccess(ResponseEntity<?> response, Class<T> expectedType) {
        if (response.getBody() instanceof ExceptionDTO) {
            ExceptionDTO error = (ExceptionDTO) response.getBody();
            fail("Expected successful response but got error: " + error.msg());
        }
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected OK status but got: " + response.getStatusCode());
        if (expectedType == Void.class) {
            return null;
        }
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(expectedType.isInstance(response.getBody()), 
            "Expected " + expectedType.getSimpleName() + " but got: " + 
            (response.getBody() != null ? response.getBody().getClass().getSimpleName() : "null"));
        return expectedType.cast(response.getBody());
    }

    private ExceptionDTO expectError(ResponseEntity<?> response, HttpStatus expectedStatus) {
        assertEquals(expectedStatus, response.getStatusCode(), "Expected " + expectedStatus + " but got: " + response.getStatusCode());
        assertNotNull(response.getBody(), "Error response body should not be null");
        assertTrue(response.getBody() instanceof ExceptionDTO, 
            "Expected ExceptionDTO but got: " + 
            (response.getBody() != null ? response.getBody().getClass().getSimpleName() : "null"));
        return (ExceptionDTO) response.getBody();
    }

    @AfterEach
    @Transactional
    public void cleanUp() throws Exception {
        System.gc();
        
        // More aggressive cleanup with multiple retries
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                // Clear model repository
                modelRepository.deleteAll();
                
                // Clean up test files
                Path modelsDir = Paths.get("..","Test", "Models").toAbsolutePath();
                if (Files.exists(modelsDir)) {
                    Files.walk(modelsDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
                
                Thread.sleep(100); // Small delay between cleanup steps
                break;
            } catch (Exception e) {
                if (attempt == 2) {
                    throw new RuntimeException("Failed to clean up after 3 attempts", e);
                }
                Thread.sleep(200 * (attempt + 1)); // Exponential backoff
            }
        }
    }

}