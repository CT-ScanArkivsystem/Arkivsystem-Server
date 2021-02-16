package no.ntnu.ctscanarkivsystemserver.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("No users with the id: " + email + " found!");
    }

    public UserNotFoundException(UUID id) {
        super("No users with the id: " + id + " found!");
    }
}
