package no.ntnu.ctscanarkivsystemserver.exception;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String tagName) {
        super("No tag with the name: " + tagName + " exists in the database!");
    }
}
