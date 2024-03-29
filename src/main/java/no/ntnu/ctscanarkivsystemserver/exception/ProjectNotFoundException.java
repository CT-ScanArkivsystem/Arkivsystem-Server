package no.ntnu.ctscanarkivsystemserver.exception;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(UUID projectId) {
        super("No project with the id: " + projectId + " found!");
    }

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
