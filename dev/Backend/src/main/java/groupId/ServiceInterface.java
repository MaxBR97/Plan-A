package groupId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import DTO.Records.Image.SolutionDTO;
import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.ImageConfigDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;

public interface ServiceInterface {
    
    public ResponseEntity<CreateImageResponseDTO> createImage(@RequestBody CreateImageFromFileDTO data) throws Exception;

    public ResponseEntity<Void> configureImage(ImageConfigDTO config) throws Exception;

    public ResponseEntity<SolutionDTO> solve(SolveCommandDTO input) throws Exception;

    public ResponseEntity<InputDTO> loadImageInput(@PathVariable("id") String imageId) throws Exception ;
}
