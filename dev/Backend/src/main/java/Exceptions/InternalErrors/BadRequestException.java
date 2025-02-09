package Exceptions.InternalErrors;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public static void requireNotNull(Object object, String message) {
        if(object==null){
            throw new BadRequestException(message);
        }
    }
}
