package groupId;

import org.springframework.http.ResponseEntity;

import DTO.Records.CreateImageDTO;
import DTO.Records.ImageConfigDTO;
import DTO.Records.ImageDTO;
import DTO.Records.ImageInputDTO;
import DTO.Records.SolutionDTO;

public interface ServiceInterface {
    public ResponseEntity<ImageDTO> createImage(CreateImageDTO sourcePath) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(ImageInputDTO input) throws Exception;
}
