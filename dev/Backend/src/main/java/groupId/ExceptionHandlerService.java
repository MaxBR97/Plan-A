package groupId;
import DTO.Factories.ExceptionRecordFactory;
import DTO.Factories.RecordFactory;
import Exceptions.BadRequestException;
import Exceptions.ZimpleCompileException;
import Exceptions.ZimpleDataIntegrityException;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    
    @ExceptionHandler(ZimpleDataIntegrityException.class)
    public ResponseEntity<ExceptionDTO> handleZimpleDataIntegrityException(ZimpleDataIntegrityException ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ZimpleCompileException.class)
    public ResponseEntity<ExceptionDTO> handleZimpleCompileException(ZimpleCompileException ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionDTO> handleBadRequest(BadRequestException ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    // Most generic catch-all handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleAll(Exception ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}