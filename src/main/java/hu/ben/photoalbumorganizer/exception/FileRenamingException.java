package hu.ben.photoalbumorganizer.exception;

public class FileRenamingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileRenamingException(Exception exception) {
        super(exception);
    }

    public FileRenamingException(String message) {
        super(message);
    }

}
