package groupId;

import org.springframework.http.ResponseEntity;

import DTO.CreateImageDTO;
import DTO.ImageConfigDTO;
import DTO.ImageDTO;
import DTO.ImageInputDTO;
import DTO.SolutionDTO;

public interface ServiceInterface {
    public ResponseEntity<ImageDTO> createImage(CreateImageDTO sourcePath) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(ImageInputDTO input) throws Exception;
}
