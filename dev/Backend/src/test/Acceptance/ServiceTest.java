package Acceptance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
@ActiveProfiles({"H2mem", "securityAndGateway"})
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
    public void initilize(){
        WebClient webClient = webClientBuilder
        .baseUrl("http://localhost:" + port)
        .build();
        requestsManager = new RequestsManager(port, webClient);
    }
    
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
                    Set.of(new VariableDTO("myVar",List.of("INT"),List.of("INT"), new DependenciesDTO(Set.of("mySet"),Set.of()),null)),
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
        
        @Test
        public void GivenImageDTO_WhenConfigImage_ImageIsCorrect() {
            CreateImageFromFileDTO createImage = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
            ResponseEntity<?> response = requestsManager.sendCreateImageRequest(createImage);
            CreateImageResponseDTO result = expectSuccess(response, CreateImageResponseDTO.class);

            ImageConfigDTO configImage = new ConfigureImageRequestBuilder(imageName, result)
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
                .addConstraintsModule("Test module const", "PeanutButter", Set.of("sampleConstraint"), Set.of("mySet"), Set.of("x"))
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
            assertEquals(Set.of("mySet"), constModule.inputSets().stream()
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
                    Set.of(new VariableDTO("Shibutsim",List.of("TEXT","TEXT","INT"),List.of("TEXT","TEXT","INT") , new DependenciesDTO(Set.of("People","Emdot"),Set.of("shiftTime")),null),
                            new VariableDTO("TotalMishmarot", List.of("TEXT"),List.of("TEXT"),new DependenciesDTO(Set.of("People"),Set.of()),null)),
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

        // @ParameterizedTest
        // @ValueSource(strings = {"..\\..\\ZimplExamplePrograms\\SoldiersExampleProgram2.zpl",
        //                         "..\\..\\ZimplExamplePrograms\\LearningParity2.zpl",
        //                         "..\\..\\ZimplExamplePrograms\\ComplexSoldiersExampleProgram.zpl"})
        // public void testSuccessfulCreationOfDifferentImages(String pathStringToFile){
        //     CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription,Paths.get(pathStringToFile)).build();
        //     ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
        //     assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
        // }

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
    
        //TODO:Migrate this test to webflux instead of resttemplate
        // @Test
        // public void testLoadImageInput() {
        //     // create Image
        // CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
        // ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
        
        // ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
        //         .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
        //         .addConstraintsModule("MyConst", "", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
        //         .addPreferencesModule("MyPref", "desc", Set.of("myVar[3]"),Set.of(),Set.of(),Set.of())
        //         .build();

        // ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);
    
        // InputDTO expected = new InputDTO(
        // Map.of(
        //     "mySet", List.of(List.of("7"), List.of("6"),List.of("4"))
        // ),
        // Map.of(
        //     "x", List.of("10")
        // ),
        // List.of(), 
        // List.of()  
        // );
    
        //     HttpHeaders headers = new HttpHeaders();
        //     headers.setContentType(MediaType.APPLICATION_JSON);
            
        //     // Create http request with body and headers
        //     HttpEntity<Void> request = new HttpEntity<>( headers);
    
        //     // Send POST request with body
        //     String url = "http://localhost:" + port + "/images/"+responseCreateImage.getBody().imageId()+"/inputs";
        //     ResponseEntity<InputDTO> response = restTemplate.exchange(
        //         url,
        //         HttpMethod.GET,
        //         request,
        //         InputDTO.class
        //     );
    
        //     assertEquals(HttpStatus.OK, response.getStatusCode());
        //     assertNotNull(response.getBody());
        //     assertEquals(response.getBody().setsToValues(),expected.setsToValues());
        //     assertEquals(response.getBody().paramsToValues(),expected.paramsToValues());
        //     assertEquals(response.getBody().constraintModulesToggledOff(),expected.constraintModulesToggledOff());
        //     assertEquals(response.getBody().preferenceModulesToggledOff(),expected.preferenceModulesToggledOff());
        // }
        
        @Test
        public void getImagesTest() {
            CreateImageFromFileDTO createImage1 = new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
            expectSuccess(requestsManager.sendCreateImageRequest(createImage1), CreateImageResponseDTO.class);

            CreateImageFromFileDTO createImage2 = new CreateImageRequestBuilder(imageName,imageDescription,"none", false,"""
                                                                set S := {<1,"a">, <2,"bs">};
                                                                minimize this:
                                                                    300;
                                                                """).build();
            expectSuccess(requestsManager.sendCreateImageRequest(createImage2), CreateImageResponseDTO.class);

            List<ImageDTO> images = expectSuccess(requestsManager.sendGetAllImagesRequest(), List.class);
            assertEquals(2, images.size());
        }

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

            // Delete the image
            ResponseEntity<?> deleteResponse = requestsManager.sendDeleteImageRequest(imageId);
            expectSuccess(deleteResponse, Void.class);

            // Try to get the deleted image - should fail
            ResponseEntity<?> getDeletedResponse = requestsManager.sendGetImageRequest(imageId);
            expectError(getDeletedResponse, HttpStatus.INTERNAL_SERVER_ERROR);

            // Try to delete an already deleted image - should also fail
            ResponseEntity<?> secondDeleteResponse = requestsManager.sendDeleteImageRequest(imageId);
            expectError(secondDeleteResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

        
        // @Test
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
        public void cleanUp() throws Exception {
            System.gc();
            int count = 0;
            while(count<20){
                try{
                    modelRepository.deleteAll();
                    Thread.sleep(100);
                    break;
                } catch (Exception e){
                    count++;
                }
            }
        }

}