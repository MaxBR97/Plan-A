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
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
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
    static String SimpleCodeExample = """
          set mySet := {7,6,4};
          param x := 10;
         
          var myVar[mySet] >= 0;
        
          subto sampleConstraint:
              myVar[1] + myVar[2] + myVar[3] == x;
            
          subto optionalConstraint:
              myVar[3] <= 5;

          maximize myObjective:
              myVar[3];
            """;

    static String pathToSoldiersExampleProgram2 =  Paths.get("..", "..", "ZimplExamplePrograms", "SoldiersExampleProgram2.zpl").toString();
    static String pathToSoldiersExampleProgram3 =  Paths.get("..", "..","ZimplExamplePrograms","SoldiersExampleProgram3.zpl").toString();
    static String pathToComplexSoldiersExampleProgram3 =  Paths.get("..","..", "ZimplExamplePrograms","ComplexSoldiersExampleProgram3.zpl").toString();
    static String pathToLearningParity2 = Paths.get("..", "..", "ZimplExamplePrograms", "LearningParity2.zpl").toString();
    static String pathToComplexSoldiersExampleProgram = Paths.get("..","..","ZimplExamplePrograms","ComplexSoldiersExampleProgram.zpl").toString();
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
        CreateImageFromFileDTO body = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(body);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

        //Expected response
        CreateImageResponseDTO expected = new CreateImageResponseDTO(
            "some imageId", new ModelDTO(
              Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(Set.of(),Set.of("x"))),
                    new ConstraintDTO("optionalConstraint", new DependenciesDTO(Set.of(),Set.of()))),
                Set.of(new PreferenceDTO("myVar[3]", new DependenciesDTO(Set.of(),Set.of()))),
                Set.of(new VariableDTO("myVar",List.of("INT"),List.of("INT"), new DependenciesDTO(Set.of("mySet"),Set.of()),null,false)),
                Map.of("mySet",List.of("INT")),
                Map.of("x","INT"),
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
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
        ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
        CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

        ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
            .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
            .addConstraintsModule("Test module const", "PeanutButter", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
            .addPreferencesModule("Test module pref", "PeanutButter", Set.of("myVar[3]"),Set.of(),Set.of(),Set.of())
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
        assertEquals(Set.of("myVar[3]"), prefModule.preferences());
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
                Set.of(new VariableDTO("Shibutsim",List.of("TEXT","TEXT","INT"),List.of("TEXT","TEXT","INT") , new DependenciesDTO(Set.of("People","Emdot"),Set.of("shiftTime")),null,true),
                        new VariableDTO("TotalMishmarot", List.of("TEXT"),List.of("TEXT"),new DependenciesDTO(Set.of("People"),Set.of()),null,false)),
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
        @ValueSource(strings = {"..\\..\\ZimplExamplePrograms\\SoldiersExampleProgram2.zpl",
                                "..\\..\\ZimplExamplePrograms\\LearningParity2.zpl",
                                "..\\..\\ZimplExamplePrograms\\ComplexSoldiersExampleProgram.zpl"})
        public void testSuccessfulCreationOfDifferentImages(String pathStringToFile){
            CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription,"Max",true,Paths.get(pathStringToFile)).build();
            CreateImageResponseDTO responseCreateImage = expectSuccess(requestsManager.sendCreateImageRequest(createImage),CreateImageResponseDTO.class);
        }

        // Tests basic solving functionality with a simple model, verifying that the
        // solution matches expected values
        @Test
        public void testSolve_Simple() {
            try {
                CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
                CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
                .setVariablesModule(Set.of("myVar"), Set.of(), Set.of())
                .build();
            expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

            SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .build();
            SolutionDTO solution = expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);

                assertEquals(Set.of(new SolutionValueDTO(List.of("3"),5), new SolutionValueDTO(List.of("2"),5)), solution.solution().get("myVar").solutions());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    
        // Tests the retrieval of stored inputs for an image.
        @Test
        public void testLoadImageInput() {
            // create Image
        CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
        CreateImageResponseDTO responseCreateImage = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);
        
        ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage)
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
                .addConstraintsModule("MyConst", "", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
                .addPreferencesModule("MyPref", "desc", Set.of("myVar[3]"),Set.of(),Set.of(),Set.of())
                .build();

        Void responseConfigImage = expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);
    
        InputDTO expected = new InputDTO(
        Map.of(
            "mySet", List.of(List.of("7"), List.of("6"),List.of("4"))
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
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
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
            CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
            CreateImageResponseDTO result = expectSuccess(requestsManager.sendCreateImageRequest(createImage), CreateImageResponseDTO.class);

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
                .setVariablesModule(Set.of("myVar"), Set.of(), Set.of())
                .addConstraintsModule("constraintToRemove", "", Set.of("optionalConstraint"), Set.of(), Set.of())
                .build();

            expectSuccess(requestsManager.sendConfigImageRequest(configImage), Void.class);

            SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(result)
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .addToggleOffConstraintModule("constraintToRemove")
                .build();
            SolutionDTO solution = expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);

            assertEquals(Set.of(new SolutionValueDTO(List.of("3"),10)), solution.solution().get("myVar").solutions());
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
            .build();
        expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);
    }

    // Tests the system's ability to handle multiple different programs in sequence,
    // verifying proper creation, configuration and solving for each
    @Test
    public void testUploadMultipleProgramsAndSolve() {
        List<String> uploadAll = List.of(
            pathToSoldiersExampleProgram2,
            pathToSoldiersExampleProgram3,
            pathToLearningParity2
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
                .build();
            expectSuccess(requestsManager.sendSolveRequest(solveRequest), SolutionDTO.class);
        }
    }

    // Basic persistent solve test with HTTP polling, verifying that the solve process
    // is correctly polled and that the correct messages are received
    @Test
    public void testPersistentSolveWithHTTPPolling() {
        List<String> polledMessages = new LinkedList<>();
        
        CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
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
        ResponseEntity<?> pollResponse = requestsManager.sendPollPersistentSolve();
        expectSuccess(pollResponse, String.class);
        polledMessages.add(pollResponse.getBody().toString());

        expectSuccess(requestsManager.sendContinuePersistentSolve(solveRequest), Void.class);
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ResponseEntity<?> secondPollResponse = requestsManager.sendPollPersistentSolve();
        expectSuccess(secondPollResponse, String.class);
        polledMessages.add(secondPollResponse.getBody().toString());

        // Verify poll messages at end
        String firstPoll = polledMessages.get(0);
        String secondPoll = polledMessages.get(1);

        assertTrue(firstPoll.contains("read problem"), "First poll should contain 'read problem', got: " + firstPoll);
        assertTrue(firstPoll.contains("presolving:"), "First poll should contain 'presolving:', got: " + firstPoll); 
        assertTrue(firstPoll.contains("SCIP Status"), "First poll should contain 'SCIP Status', got: " + firstPoll);
        assertTrue(secondPoll.contains("problem is already solved"), "Second poll should indicate problem is solved, got: " + secondPoll);
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
        int count = 0;
        while(count<20){
            try{
                Thread.sleep(100);
                modelRepository.deleteAll();
                
                // Clean up test files
                Path modelsDir = Paths.get("..","Test", "Models").toAbsolutePath();
                if (Files.exists(modelsDir)) {
                    Files.walk(modelsDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
                break;
            } catch (Exception e){
                count++;
            }
        }
    }

}