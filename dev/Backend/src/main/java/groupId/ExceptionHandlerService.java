package groupId;
import DTO.Factories.ExceptionRecordFactory;
import DTO.Factories.RecordFactory;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import DTO.Records.Requests.Responses.ExceptionDTO;

/**
 * Usage instructions: use some Record to make the DTO of the exception and return it.
 * Should have a method for each exception needing a custom or logging.
 * the message/logging itself should be in the factory, No logic is to be implemented here.
 */
@RestControllerAdvice
public class ExceptionHandlerService {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(500).body(errorResponse);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(RuntimeException ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(500).body(errorResponse);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(BadRequestException ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(400).body(errorResponse);
    }
    @ExceptionHandler(ZimplCompileError.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(ZimplCompileError ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(400).body(errorResponse);
    }
}