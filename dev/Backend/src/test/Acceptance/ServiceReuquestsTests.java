package Acceptance;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.CreateImageFromPathDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Model.Model;
import Model.ModelInterface;
import Utilities.Stubs.ModelStub;
import groupId.Service;
import groupId.UserController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import Model.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
public class ServiceReuquestsTests {
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
            /*
            Path emptyZimple = tmpDirPath.resolve("badZimpl.zimpl");
            Files.copy(Path.of(sourcePath), emptyZimple, StandardCopyOption.REPLACE_EXISTING);
            emptyZimple.toFile().deleteOnExit();
            FileWriter writer = new FileWriter(emptyZimple.toString(), false);
            assertNotNull(response.getBody().image());
*/
            String name= "emptyZimplFIle";
            String data="";
            ResponseEntity<ImageResponseDTO> response= service.createImage(new CreateImageFromFileDTO(/*name,*/data));

            ImageDTO image= response.getBody().image();
            assertEquals(0, image.constraints().size());
            assertEquals(0, image.preferences().size());
            assertEquals(0, image.variables().size());
        }
        catch (Exception e){
            fail(e.getMessage());
        }
    }
}
