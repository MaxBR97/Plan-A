package Exceptions;

import DTO.Records.Requests.Responses.ExceptionDTO;

public class ZimplCompileException extends Exception {
    final int errorCode;
        public ZimplCompileException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
