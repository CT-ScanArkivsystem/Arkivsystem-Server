package no.ntnu.ctscanarkivsystemserver.exception;

import java.util.UUID;

/**
 * This runtime exception is to be thrown when the system tries to get a user, but no user is found.
 * @author TrymV
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("No users with the id: " + email + " found!");
    }

    public UserNotFoundException(UUID id) {
        super("No users with the id: " + id + " found!");
    }
}
