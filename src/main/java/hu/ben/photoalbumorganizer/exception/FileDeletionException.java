package hu.ben.photoalbumorganizer.exception;

public class FileDeletionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileDeletionException(Exception exception) {
        super(exception);
    }

    public FileDeletionException(String message) {
        super(message);
    }

}
