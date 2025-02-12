package groupId;
import DTO.Factories.ExceptionRecordFactory;
import DTO.Factories.RecordFactory;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;

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
    
    // // Most specific exceptions first
    // @ExceptionHandler(BadRequestException.class)
    // public ResponseEntity<ExceptionDTO> handleBadRequest(BadRequestException ex) {
    //     ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    // }

    // @ExceptionHandler(ZimplCompileError.class)
    // public ResponseEntity<ExceptionDTO> handleZimplCompile(ZimplCompileError ex) {
    //     ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    // }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<ExceptionDTO> handleValidation(MethodArgumentNotValidException ex) {
    //     ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    // }

    // @ExceptionHandler({
    //     HttpRequestMethodNotSupportedException.class,
    //     HttpMediaTypeNotSupportedException.class,
    //     HttpMessageNotReadableException.class
    // })
    // public ResponseEntity<ExceptionDTO> handleHttpErrors(Exception ex) {
    //     ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    // }

    // // More general exceptions last
    // @ExceptionHandler(RuntimeException.class)
    // public ResponseEntity<ExceptionDTO> handleRuntime(RuntimeException ex) {
    //     ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    // }

    // Most generic catch-all handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleAll(Exception ex) {
        ExceptionDTO errorResponse = ExceptionRecordFactory.makeDTO(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}