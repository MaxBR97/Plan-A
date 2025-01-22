package groupId;

import DTO.Factories.RecordFactory;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Image.SolutionDTO;
import DTO.Records.Requests.Responses.ImageResponseDTO;
import Image.Image;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserController {
private final Map<UUID,Image> images;


    public UserController(){
        images = new HashMap<>();
    }

    public ImageResponseDTO createImageFromPath(String path) throws IOException {
        Image image=new Image(path);
        UUID id= UUID.randomUUID();
        images.put(id,image);
        // If there is a compilation error, and exception is to be caught and returned.
        return RecordFactory.makeDTO(id,image);
    }

    public ImageResponseDTO createImageFromFile(String code) throws IOException {
        UUID id= UUID.randomUUID();
        String name = id.toString();
        
        Path path= Paths.get("User/Models"+ File.separator+name+".zpl");
        Files.createDirectories(path.getParent());
        Files.writeString(path,code, StandardOpenOption.CREATE);
        Image image=new Image(path.toAbsolutePath().toString());
        images.put(id,image);
        return RecordFactory.makeDTO(id,image);
    }

    public SolutionDTO solve(SolveCommandDTO command) {
        return images.get(UUID.fromString(command.id())).solve(Integer.parseInt(command.timeout()));
    }
}
