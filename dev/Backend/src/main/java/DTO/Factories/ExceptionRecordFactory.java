package DTO.Factories;

import DTO.Records.Requests.Responses.ExceptionDTO;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ExceptionRecordFactory {
    private static final String ohNo= "An extra extra fatal occurred while handling an error";

    //TODO A singular exceptionDTO factory, made with the purpose of having a central fatal/non fatal error logging class,
    // need to implement logger. Remove informative errors messages from user side, for network/internal errors.
    public static ExceptionDTO makeDTO(Exception exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An unknown error occurred, see log for details, or contract the developer");
    }
    public static ExceptionDTO makeDTO(RuntimeException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An unexpected fatal error occurred. See log for details, or contract the developer");
    }
    public static ExceptionDTO makeDTO(BadRequestException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("Bad HTTP request. See log for details, or contract the developer");
    }
    public static ExceptionDTO makeDTO(IOException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An error occurred while trying to access the file system.\n" +
                "See log for details, or contract the developer");
    }
    public static ExceptionDTO makeDTO(ZimplCompileError exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO(String.format("An error occurred while compiling zimpl code.\nError code:%d\nError message:%s",
                exception.getErrorCode(),exception.getMessage()));
    }
    public static ExceptionDTO makeDTO(HttpRequestMethodNotSupportedException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO(String.format("An server communication error occurred, HTTP request is invalid. Current method:%s\n" +
                "Supported methods:%s",exception.getMethod(), Arrays.toString(exception.getSupportedMethods())));
    }
    public static ExceptionDTO makeDTO(HttpMediaTypeNotSupportedException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An server communication error occurred, HTTP content media type not supported.");
    }
    public static ExceptionDTO makeDTO(HttpMessageNotReadableException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An server communication error occurred, HTTP request content type is invalid");
    }
    public static ExceptionDTO makeDTO(InvalidFormatException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An server communication error occurred. HTTP request payload parsing failed, invalid format");
    }
    public static ExceptionDTO makeDTO(NestedRuntimeException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("An unhandled server communication occurred.");
    }
}
