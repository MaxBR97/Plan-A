package groupId;

import DTO.RecordFactory;
import DTO.Records.Commands.CreateImageDTO;
import DTO.Records.Commands.ImageConfigDTO;
import DTO.Records.Commands.ImageInputDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import Image.Image;
import Image.Modules.ConstraintModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Service
public class UserController {
    Image image; // commented out for succesful compilation
private final Map<UUID,Image> images
    public UserController(){
    }

    public ImageDTO createImage(String path) throws IOException {
        image=new Image(path);
        UUID id = UUID.randomUUID();
        images.put(id,image);
    }

    public void configureImage(ImageConfigDTO config){
    }

    public SolutionDTO solve(ImageInputDTO inputs) {
        return null;
    }

    
}
