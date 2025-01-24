package Unit.Image;
import Image.*;
import Model.Model;
import Model.ModelInterface;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

public class ImageTests {

    @Mock
    ModelInterface model;
    static String sourcePath = "src/test/Utilities/ZimplExamples/ExampleZimplProgram.zpl";
    static Path tmpDirPath;
    Image image;
    @BeforeAll
    public static void setup(){
        try {
            //System default tmp folder, for now I delete it at end of run, not 100% sure if should
            tmpDirPath= Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        }
        catch (IOException e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    @BeforeEach
    public void beforeEach() {
        try {
            model = new Model(sourcePath);
            image = new Image(model);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }


}
