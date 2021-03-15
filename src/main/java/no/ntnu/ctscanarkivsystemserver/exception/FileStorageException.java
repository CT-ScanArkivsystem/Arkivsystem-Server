package no.ntnu.ctscanarkivsystemserver.exception;

/**
 * @author Rajeev Singh
 * @source https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}