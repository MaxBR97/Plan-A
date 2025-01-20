package groupId;

import org.springframework.http.ResponseEntity;

import DTO.Records.Commands.CreateImageDTO;
import DTO.Records.Commands.ImageConfigDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Commands.ImageInputDTO;
import DTO.Records.Image.SolutionDTO;

public interface ServiceInterface {
    public ResponseEntity<ImageDTO> createImage(CreateImageDTO sourcePath) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(ImageInputDTO input) throws Exception;
}
