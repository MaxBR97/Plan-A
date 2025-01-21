package DTO.Factories;

import DTO.Records.Requests.Responses.ExceptionDTO;
import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;

import java.io.IOException;
import java.util.Objects;

public class ExceptionRecordFactory {
    private static final String ohNo= "An extra extra fatal occurred while handling an error";


    //TODO A singular exceptionDTO factory, made with the purpose of having a central fatal/non fatal error logging class, need to implement logger
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
    public static ExceptionDTO makeException(BadRequestException exception) {
        Objects.requireNonNull(exception,ohNo);
        //TODO: LOG
        return new ExceptionDTO("Bad HTTP request. See log for details, or contract the developer");
    }
    public static ExceptionDTO makeException(IOException exception) {
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


}
