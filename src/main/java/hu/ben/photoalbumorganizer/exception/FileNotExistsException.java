package hu.ben.photoalbumorganizer.exception;

public class FileNotExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileNotExistsException(Exception exception) {
        super(exception);
    }

    public FileNotExistsException(String message) {
        super(message);
    }

}
