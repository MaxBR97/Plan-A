package Acceptance;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Model.ModelDefinition.ModelDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import groupId.Service;
import groupId.UserController;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
public class ServiceRequestsTests {
    static Path tmpDirPath;
    static String sourcePath = "src/test/Utilities/Stubs/ExampleZimplProgram.zpl";
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
        service=new Service(new UserController());
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
}
