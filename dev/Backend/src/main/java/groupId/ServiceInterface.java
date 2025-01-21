package groupId;

import DTO.Records.Requests.Responses.ImageResponseDTO;
import org.springframework.http.ResponseEntity;

import DTO.Records.Requests.Commands.CreateImageFromPathDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Image.SolutionDTO;

public interface ServiceInterface {
    public ResponseEntity<ImageResponseDTO> createImage(CreateImageFromPathDTO sourcePath) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(SolveCommandDTO input) throws Exception;
}
