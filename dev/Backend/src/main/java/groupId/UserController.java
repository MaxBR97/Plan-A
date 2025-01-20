package groupId;

import DTO.Records.Commands.CreateImageDTO;
import DTO.Records.Commands.ImageConfigDTO;
import DTO.Records.Commands.ImageInputDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import org.springframework.stereotype.Service;

@Service
public class UserController {
    //Image image; // commented out for succesful compilation

    public UserController(){
        
    }

    public ImageDTO createImage(CreateImageDTO zplFile){
        return null;
    }

    public void configureImage(ImageConfigDTO config){
    }

    public SolutionDTO solve(ImageInputDTO inputs) {
        return null;
    }

    
}
