package Acceptance;
import DTO.Factories.RecordFactory;
import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.PreferenceModuleDTO;
import DTO.Records.Image.VariableModuleDTO;
import DTO.Records.Model.ModelDefinition.*;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import groupId.Service;
import groupId.UserController;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;
public class ServiceRequestsTests {
    static String SimpleCodeExample = """
                param x := 10;
                set mySet := {1,2,3};

                var myVar[mySet];

                subto sampleConstraint:
                    myVar[x] == mySet[1];

                maximize myObjective:
                    1;
            """;
    static Path tmpDirPath;
    static String sourcePath = "src/test/Utilities/Stubs/ExampleZimplProgram.zpl";
    UserController userController;
    Service service;
    @BeforeAll
    public static void setup(){
        try {
            //System default tmp folder, for now I delete it at end of run, not 100% sure if should
            tmpDirPath= Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        }
        catch (IOException e){
            fail(e.getMessage());
        }
    }
    @BeforeEach
    public void setUp() {
        userController = new UserController();
        service=new Service(userController);
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
                    "some id", new ModelDTO(
                    List.of(new ConstraintDTO("sampleConstraint", new DependenciesDTO(List.of("mySet"), List.of("x")))),
                    List.of(new PreferenceDTO("myObjective", new DependenciesDTO(List.of(), List.of()))),
                    List.of(new VariableDTO("myVar", new DependenciesDTO(List.of("mySet"), List.of()))),
                    Map.of(
                            "mySet", "INT",
                            "x", "INT"
                    )
            ));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().imageId());
            assertEquals(response.getBody().model().constraints(), expected.model().constraints());
            assertEquals(response.getBody().model().preferences(), expected.model().preferences());
            assertEquals(response.getBody().model().variables(), expected.model().variables());
            assertEquals(response.getBody().model().types(), expected.model().types());
            /**
             * TEST
             */
            List<ConstraintModuleDTO> constraintModuleDTOs = List.of(
                    new ConstraintModuleDTO("Test module", "PeanutButter",
                            List.of("sampleConstraint"), List.of("mySet"), List.of("x")));
            List<PreferenceModuleDTO> preferenceModuleDTOs = List.of(
                    new PreferenceModuleDTO("Test module", "PeanutButter",
                            List.of("myObjective"), List.of(), List.of()));
            VariableModuleDTO variableModuleDTO = new VariableModuleDTO(List.of("myVar"), List.of("mySet"), List.of());
            ImageDTO imageDTO = new ImageDTO(variableModuleDTO, constraintModuleDTOs, preferenceModuleDTOs);
            ImageConfigDTO configDTO= new ImageConfigDTO(response.getBody().imageId(),imageDTO);
            ResponseEntity<Void> response2= service.configureImage(configDTO);
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            Image image= userController.getImage(response.getBody().imageId());
            assertNotNull(image);
            ImageDTO actual= RecordFactory.makeDTO(image);
            assertEquals(imageDTO, actual);

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
