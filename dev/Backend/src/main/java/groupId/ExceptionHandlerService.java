package groupId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import DTO.ExceptionDTO;

@RestControllerAdvice
public class ExceptionHandlerService {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(ex);
        return ResponseEntity.status(500).body(errorResponse);
    }
    
}
