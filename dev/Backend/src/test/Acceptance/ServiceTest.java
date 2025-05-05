package Acceptance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import DataAccess.ModelRepository;
import groupId.Main;
import groupId.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = Main.class)
@ActiveProfiles({"H2mem"})
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
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ModelRepository modelRepository;

    @BeforeEach
    public void initilize(){
        requestsManager = new RequestsManager(port, restTemplate);
    }
    
        @Test
        public void testCreateImage() {
            CreateImageFromFileDTO body =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
            ResponseEntity<CreateImageResponseDTO> response = requestsManager.sendCreateImageRequest(body);
        
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
            
            assertEquals(HttpStatus.OK, response.getStatusCode(),response.getBody().toString());
            assertNotNull(response.getBody().imageId());
            assertEquals(response.getBody().model().constraints(), expected.model().constraints());
            assertEquals(response.getBody().model().preferences(), expected.model().preferences());
            assertEquals(response.getBody().model().variables(), expected.model().variables());
            assertEquals(response.getBody().model().setTypes(), expected.model().setTypes());
            assertEquals(response.getBody().model().paramTypes(), expected.model().paramTypes());
            assertEquals(response.getBody().model().varTypes(), expected.model().varTypes());
        }
        
        @Test
        public void GivenImageDTO_WhenConfigImage_ImageIsCorrect(){
            /**
             * SET UP
             */
            CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
            ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
            
            //Expected response
            CreateImageResponseDTO expected = new CreateImageResponseDTO(
                    "some imageId", new ModelDTO(
                    Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(Set.of(),Set.of("x"))),
                            new ConstraintDTO("optionalConstraint", new DependenciesDTO(Set.of(),Set.of()))),
                    Set.of(new PreferenceDTO("myVar[3]", new DependenciesDTO(Set.of(),Set.of()))),
                    Set.of(new VariableDTO("myVar",List.of("INT"),List.of("INT"), new DependenciesDTO(Set.of("mySet"),Set.of()),null)),
                    Map.of("mySet",List.of("INT")),
                    Map.of("x","INT"),
                    Map.of("myVar",List.of("INT"))
            ));
            assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
            assertNotNull(responseCreateImage.getBody());
            assertNotNull(responseCreateImage.getBody().imageId());
            assertEquals(responseCreateImage.getBody().model().constraints(), expected.model().constraints());
            assertEquals(responseCreateImage.getBody().model().preferences(), expected.model().preferences());
            assertEquals(responseCreateImage.getBody().model().variables(), expected.model().variables());
            assertEquals(responseCreateImage.getBody().model().setTypes(), expected.model().setTypes());
            assertEquals(responseCreateImage.getBody().model().paramTypes(), expected.model().paramTypes());
            assertEquals(responseCreateImage.getBody().model().varTypes(), expected.model().varTypes());
            
            ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
            .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
            .addConstraintsModule("Test module const", "PeanutButter", Set.of("sampleConstraint"), Set.of("mySet"), Set.of("x"))
            .addPreferencesModule("Test module pref", "PeanutButter", Set.of("myVar[3]"),Set.of(),Set.of(),Set.of())
            .build();
            ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);
            
            assertEquals(HttpStatus.OK, responseConfigImage.getStatusCode());
        }
    
        @Test
        public void GivenFile_WhenCreateImage_ImageIsCorrect() {
    
            CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false, Paths.get("./src/test/Acceptance/example.zpl")).build();
            ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
    
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
                                    "TotalMishmarot",List.of("TEXT")
                                )
                    )
                );
            
            assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
            assertNotNull(responseCreateImage.getBody().imageId());
            assertEquals(responseCreateImage.getBody().model().constraints(), expected.model().constraints());
            assertEquals(responseCreateImage.getBody().model().preferences(), expected.model().preferences());
            assertEquals(responseCreateImage.getBody().model().variables(), expected.model().variables());
            assertEquals(responseCreateImage.getBody().model().setTypes(), expected.model().setTypes());
            assertEquals(responseCreateImage.getBody().model().paramTypes(), expected.model().paramTypes());
            assertEquals(responseCreateImage.getBody().model().varTypes(), expected.model().varTypes());
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
                /**
                 * SET UP IMAGE, MAKE SURE ITS VALID
                 */
                CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
                ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
                
                //Expected response
                CreateImageResponseDTO expected = new CreateImageResponseDTO(
                        "some imageId", new ModelDTO(
                        Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(Set.of(),Set.of("x"))),
                                new ConstraintDTO("optionalConstraint", new DependenciesDTO(Set.of(),Set.of()))),
                        Set.of(new PreferenceDTO("myVar[3]", new DependenciesDTO(Set.of(),Set.of()))),
                        Set.of(new VariableDTO("myVar", List.of("INT"),List.of("INT"),new DependenciesDTO(Set.of("mySet"),Set.of()),null)),
                        Map.of("mySet",List.of("INT")),
                        Map.of("x","INT"),
                        Map.of("myVar",List.of("INT"))));
    
                assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
                assertNotNull(responseCreateImage.getBody().imageId());
                assertEquals(responseCreateImage.getBody().model().constraints(), expected.model().constraints());
                assertEquals(responseCreateImage.getBody().model().preferences(), expected.model().preferences());
                assertEquals(responseCreateImage.getBody().model().variables(), expected.model().variables());
                assertEquals(responseCreateImage.getBody().model().setTypes(), expected.model().setTypes());
                assertEquals(responseCreateImage.getBody().model().paramTypes(), expected.model().paramTypes());
                assertEquals(responseCreateImage.getBody().model().varTypes(), expected.model().varTypes());
                /**
                 *  CONFIG IMAGE TO DISPLAY myVar
                 */
                ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
                .setVariablesModule(Set.of("myVar"), Set.of(), Set.of())
                .build();
                ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);

                assertEquals(HttpStatus.OK, responseConfigImage.getStatusCode());
                /**
                 * CALL IMAGE TO SOLVE THE CODE, MAKE SURE SOLUTION IS CORRECT
                 */
                SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(responseCreateImage.getBody())
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .build();
                ResponseEntity<SolutionDTO> solveImageResponse = requestsManager.sendSolveRequest(solveRequest);
                
                assertEquals(HttpStatus.OK, solveImageResponse.getStatusCode());
                assertNotNull(solveImageResponse.getBody());
                assertEquals(Set.of(new SolutionValueDTO(List.of("3"),5), new SolutionValueDTO(List.of("2"),5)),solveImageResponse.getBody().solution().get("myVar").solutions());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    
        @Test
        public void testLoadImageInput() {
            // create Image
        CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
        ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
        
        ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
                .setVariablesModule(Set.of("myVar"), Set.of("mySet"), Set.of())
                .addConstraintsModule("MyConst", "", Set.of("sampleConstraint"), Set.of(), Set.of("x"))
                .addPreferencesModule("MyPref", "desc", Set.of("myVar[3]"),Set.of(),Set.of(),Set.of())
                .build();

        ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);
    
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
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create http request with body and headers
            HttpEntity<Void> request = new HttpEntity<>( headers);
    
            // Send POST request with body
            String url = "http://localhost:" + port + "/images/"+responseCreateImage.getBody().imageId()+"/inputs";
            ResponseEntity<InputDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                InputDTO.class
            );
    
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(response.getBody().setsToValues(),expected.setsToValues());
            assertEquals(response.getBody().paramsToValues(),expected.paramsToValues());
            assertEquals(response.getBody().constraintModulesToggledOff(),expected.constraintModulesToggledOff());
            assertEquals(response.getBody().preferenceModulesToggledOff(),expected.preferenceModulesToggledOff());
        }
        
        @Test
        public void getImagesTest(){
        CreateImageFromFileDTO createImage1 =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , SimpleCodeExample).build();
        ResponseEntity<CreateImageResponseDTO> responseCreateImage1 = requestsManager.sendCreateImageRequest(createImage1);

        CreateImageFromFileDTO createImage2 =  new CreateImageRequestBuilder(imageName,imageDescription,"none", false ,"""
                                                                set S := {<1,\"a\">, <2,\"bs\">};
                                                                minimize this:
                                                                    300;
                                                                """).build();
        ResponseEntity<CreateImageResponseDTO> responseCreateImage2 = requestsManager.sendCreateImageRequest(createImage2);
        
        ResponseEntity<List<ImageDTO>> responseGetAll = requestsManager.sendGetAllImagesRequest();

        //TODO: Check response more thoroughly.
        assertEquals(HttpStatus.OK, responseGetAll.getStatusCode());
        assertNotNull(responseGetAll.getBody());
        assertEquals(2, responseGetAll.getBody().size());
        }

        @Test
        public void deleteImageTest() {
        CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false, SimpleCodeExample).build();
        ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);

            assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
            String imageId = responseCreateImage.getBody().imageId();
            ResponseEntity<ImageDTO> image = requestsManager.sendGetImageRequest(imageId);
            assertEquals(HttpStatus.OK, image.getStatusCode());
            assertNotNull(image.getBody());

            ResponseEntity<Void> deleted = requestsManager.sendDeleteImageRequest(imageId);
            assertEquals(HttpStatus.OK, deleted.getStatusCode());

            image = requestsManager.sendGetImageRequest(imageId);
            assertEquals(HttpStatus.resolve(500), image.getStatusCode());
        }

        @Test
        public void testSolve_WithToggleOff() {
            try {
                /**
                 * SET UP IMAGE, MAKE SURE ITS VALID
                 */
                CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription,"none", false , SimpleCodeExample).build();
                ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
                
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
    
                assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
                assertNotNull(responseCreateImage.getBody().imageId());
                assertEquals(responseCreateImage.getBody().model().constraints(), expected.model().constraints());
                assertEquals(responseCreateImage.getBody().model().preferences(), expected.model().preferences());
                assertEquals(responseCreateImage.getBody().model().variables(), expected.model().variables());
                assertEquals(responseCreateImage.getBody().model().setTypes(), expected.model().setTypes());
                assertEquals(responseCreateImage.getBody().model().paramTypes(), expected.model().paramTypes());
                assertEquals(responseCreateImage.getBody().model().varTypes(), expected.model().varTypes());
                /**
                 *  CONFIG IMAGE TO DISPLAY myVar
                 */
                ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
                .setVariablesModule(Set.of("myVar"), Set.of(), Set.of())
                .addConstraintsModule("constraintToRemove", "", Set.of("optionalConstraint"), Set.of(), Set.of())
                .build();

                ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);

                assertEquals(HttpStatus.OK, responseConfigImage.getStatusCode());
                /**
                 * CALL IMAGE TO SOLVE THE CODE, MAKE SURE SOLUTION IS CORRECT
                 */
                SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(responseCreateImage.getBody())
                .setSetInput("mySet",List.of(List.of("1"),List.of("2"),List.of("3")))
                .setParamInput("x", List.of("10"))
                .addToggleOffConstraintModule("constraintToRemove")
                .build();
                ResponseEntity<SolutionDTO> solveImageResponse = requestsManager.sendSolveRequest(solveRequest);

                assertEquals(HttpStatus.OK, solveImageResponse.getStatusCode());
                assertNotNull(solveImageResponse.getBody());
                assertEquals(Set.of(new SolutionValueDTO(List.of("3"),10)),solveImageResponse.getBody().solution().get("myVar").solutions());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        
        // @Test
    @Test
    public void testUploadingHeavyImageAndSolveWithInputs(){
            CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , Path.of(pathToComplexSoldiersExampleProgram3))
            .build();
            ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
            assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
            
            ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
            .setDefaultVariablesModule()
            .build();
            ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);
            assertEquals(HttpStatus.OK, responseConfigImage.getStatusCode());

            ResponseEntity<InputDTO> inputDTO = requestsManager.sendGetInputsRequest(responseCreateImage.getBody().imageId());
            assertEquals(HttpStatus.OK, inputDTO.getStatusCode());

            SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(responseCreateImage.getBody())
            .setInput(inputDTO.getBody())
            .build();
            ResponseEntity<SolutionDTO> solveImageResponse = requestsManager.sendSolveRequest(solveRequest);

            assertEquals(HttpStatus.OK, solveImageResponse.getStatusCode());
        }

        @Test
        public void testUploadMultipleProgramsAndSolve(){
            List<String> uploadAll = List.of(
                                                pathToSoldiersExampleProgram2,
                                                pathToSoldiersExampleProgram3,
                                                pathToLearningParity2
                                                // pathToComplexSoldiersExampleProgram3
                                                );
            List<String> respectiveImageIds = new LinkedList<>();                                                
            for(String program : uploadAll){
                CreateImageFromFileDTO createImage =  new CreateImageRequestBuilder(imageName,imageDescription, "none", false , Path.of(program))
                .build();
                ResponseEntity<CreateImageResponseDTO> responseCreateImage = requestsManager.sendCreateImageRequest(createImage);
                assertEquals(HttpStatus.OK, responseCreateImage.getStatusCode());
                respectiveImageIds.add(responseCreateImage.getBody().imageId());
                ImageConfigDTO configImage =  new ConfigureImageRequestBuilder(imageName, responseCreateImage.getBody())
                .setDefaultVariablesModule()
                .build();
                ResponseEntity<Void> responseConfigImage = requestsManager.sendConfigImageRequest(configImage);
                assertEquals(HttpStatus.OK, responseConfigImage.getStatusCode());
            }

            for(String id : respectiveImageIds){
                ResponseEntity<InputDTO> inputDTO = requestsManager.sendGetInputsRequest(id);
                assertEquals(HttpStatus.OK, inputDTO.getStatusCode());

                SolveCommandDTO solveRequest = new SolveCommandRequestBuilder(id)
                .setInput(inputDTO.getBody())
                .build();
                ResponseEntity<SolutionDTO> solveImageResponse = requestsManager.sendSolveRequest(solveRequest);

                assertEquals(HttpStatus.OK, solveImageResponse.getStatusCode());
            }
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