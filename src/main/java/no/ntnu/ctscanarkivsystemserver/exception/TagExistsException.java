package no.ntnu.ctscanarkivsystemserver.exception;

public class TagExistsException extends RuntimeException {
    public TagExistsException(String tagName) {
        super("A tag with the name: " + tagName + " already exist!");
    }
}
