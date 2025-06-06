package Exceptions;

import DTO.Records.Requests.Responses.ExceptionDTO;

public class ZimpleCompileException extends Exception {
    final int errorCode;
    public ZimpleCompileException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
