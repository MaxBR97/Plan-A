package Acceptance;
import DTO.Factories.RecordFactory;
import DTO.Records.Image.*;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Model.ModelDefinition.*;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import Model.Solution;
import groupId.Main;
import groupId.Service;
import groupId.UserController;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import DataAccess.ImageRepository;
import DataAccess.ModelRepository;
import jakarta.transaction.Transactional;
 
@SpringBootTest(classes = Main.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class ServiceRequestsTests {
    static String SimpleCodeExample = """
          set mySet := {7,6,4};
          param x := 10;
         
          var myVar[mySet] >= 0;
        
          subto sampleConstraint:
              myVar[1] + myVar[2] + myVar[3] == x;

          maximize myObjective:
              myVar[3];
            
            """;
    static Path tmpDirPath;
    static String sourcePath = "src/test/Utilities/ZimplExamples/ExampleZimplProgram.zpl";
    static UserController userController;
    @Autowired
    Service service;

    
    @BeforeAll
    public static void setup(){
        //try {
            //System default tmp folder, for now I delete it at end of run, not 100% sure if should
           // tmpDirPath= Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
            //changed to debug
            tmpDirPath= Paths.get("User/Solutions");
        //}
//        catch (IOException e){
//            fail(e.getMessage());
//        }
    }
    @BeforeEach
    public void setUp() {
        userController = service.getUserController();
    }

    @Test
    public void GivenEmptyZimplFIle_WhenCreatingIMageFrom_CreateEmptyImage(){
        try {
            String data="";
            ResponseEntity<CreateImageResponseDTO> response= service.createImage(new CreateImageFromFileDTO(data));

            ModelDTO model= response.getBody().model();
            assertEquals(0, model.constraints().size());
            assertEquals(0, model.preferences().size());
            assertEquals(0, model.variables().size());
        }
        catch (Exception e){
            fail(e.getMessage());
        }
    }
    @Test
    public void GivenImageDTO_WhenConfigImage_ImageIsCorrect() {
        /**
         * SET UP
         */
        CreateImageFromFileDTO body = new CreateImageFromFileDTO(SimpleCodeExample);
        try {
            ResponseEntity<CreateImageResponseDTO> response= service.createImage(body);

            CreateImageResponseDTO expected = new CreateImageResponseDTO(
                    "some imageId", new ModelDTO(
                    Set.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(Set.of(), Set.of("x")))),
                    Set.of(new PreferenceDTO("myVar[3]", new DependenciesDTO(Set.of(), Set.of()))),
                    Set.of(new VariableDTO("myVar", new DependenciesDTO(Set.of("mySet"), Set.of()))),
                    Map.of("mySet", List.of("INT")),
                    Map.of("x", "INT"),
                    Map.of()
            ));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().imageId());
            assertEquals(response.getBody().model().constraints(), expected.model().constraints());
            assertEquals(response.getBody().model().preferences(), expected.model().preferences());
            assertEquals(response.getBody().model().variables(), expected.model().variables());
            assertEquals(response.getBody().model().setTypes(), expected.model().setTypes());
            assertEquals(response.getBody().model().paramTypes(), expected.model().paramTypes());
            assertEquals(response.getBody().model().varTypes(), expected.model().varTypes());

            /**
             * TEST
             */
            Set<ConstraintModuleDTO> constraintModuleDTOs = Set.of(
                    new ConstraintModuleDTO("Test module", "PeanutButter",
                            Set.of("sampleConstraint"), Set.of(), Set.of("x")));
            Set<PreferenceModuleDTO> preferenceModuleDTOs = Set.of(
                    new PreferenceModuleDTO("Test module", "PeanutButter",
                            Set.of("myVar[3]"), Set.of(), Set.of()));
            VariableModuleDTO variableModuleDTO = new VariableModuleDTO(Set.of("myVar"), Set.of("mySet"), Set.of());
            ImageDTO imageDTO = new ImageDTO(variableModuleDTO, constraintModuleDTOs, preferenceModuleDTOs);
            ImageConfigDTO configDTO= new ImageConfigDTO(response.getBody().imageId(),imageDTO);
            ResponseEntity<Void> response2= service.configureImage(configDTO);
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            Image image= userController.getImage(response.getBody().imageId());
            assertNotNull(image);
            ImageDTO actual= RecordFactory.makeDTO(image);
            assertEquals(imageDTO, actual);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
    @Test
    public void testSolve_Simple() {
        try {
            CreateImageResponseDTO responseDTO=userController.createImageFromFile(SimpleCodeExample);
            InputDTO input=new InputDTO(Map.of("mySet",List.of(List.of("1"),List.of("2"),List.of("3"))),
                    Map.of("x",List.of("10")),
                    List.of(),List.of());
            SolveCommandDTO solveCommandDTO=new SolveCommandDTO(responseDTO.imageId(),input,600);
            ImageDTO imageDTO=new ImageDTO(new VariableModuleDTO(Set.of("myVar"),Set.of(),Set.of()),Set.of(),Set.of());
            ImageConfigDTO config= new ImageConfigDTO(responseDTO.imageId(),imageDTO);
            userController.overrideImage(config);
            SolutionDTO solution=userController.solve(solveCommandDTO);
            assertEquals(Set.of(new SolutionValueDTO(List.of("3"),10)),solution.solution().get("myVar").solutions());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
