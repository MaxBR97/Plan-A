package groupId;
import DTO.RecordFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import DTO.Records.Commands.ExceptionDTO;

@RestControllerAdvice
public class ExceptionHandlerService {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception ex) {
        ExceptionDTO errorResponse = RecordFactory.makeDTO(ex);
        return ResponseEntity.status(500).body(errorResponse);
    }
    
}
