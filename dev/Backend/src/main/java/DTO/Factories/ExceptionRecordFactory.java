package DTO.Factories;

import DTO.Records.Requests.Responses.ExceptionDTO;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExceptionRecordFactory {

    //TODO A singular exceptionDTO factory, made with the purpose of having a central fatal/non fatal error logging class,
    // need to implement logger. Remove informative errors messages from user side, for network/internal errors.
    public static ExceptionDTO makeDTO(Exception exception) {
        //TODO: LOG
        //return new ExceptionDTO("An unknown error occurred, see log for details, or contract the developer");
        return new ExceptionDTO("Error: "+ exception.getMessage());
    }

    public static ExceptionDTO makeDTO(BadRequestException exception) {
        //TODO: LOG
        //return new ExceptionDTO("An unknown error occurred, see log for details, or contract the developer");
        return new ExceptionDTO("Request Error: "+ exception.getMessage());
    }
    
}
