package groupId;

import org.springframework.http.ResponseEntity;

import DTO.Records.Requests.Commands.CreateImageFromPathDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Image.ImageDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Image.SolutionDTO;

public interface ServiceInterface {
    public ResponseEntity<ImageDTO> createImage(CreateImageFromPathDTO sourcePath) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(SolveCommandDTO input) throws Exception;
}
