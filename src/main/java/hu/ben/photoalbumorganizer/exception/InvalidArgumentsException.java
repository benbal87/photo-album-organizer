package hu.ben.photoalbumorganizer.exception;

public class InvalidArgumentsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidArgumentsException(Exception exception) {
        super(exception);
    }

    public InvalidArgumentsException(String message) {
        super(message);
    }

}
