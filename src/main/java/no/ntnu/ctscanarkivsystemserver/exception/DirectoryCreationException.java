package no.ntnu.ctscanarkivsystemserver.exception;

/**
 * This exception is for when creation of directories failed.
 * @author TrymV
 */
public class DirectoryCreationException extends RuntimeException{
    public DirectoryCreationException(String message) {
        super("DirectoryCreationException: " + message);
    }
}
