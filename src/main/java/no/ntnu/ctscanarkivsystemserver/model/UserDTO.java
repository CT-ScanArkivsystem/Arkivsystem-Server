package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;

import java.util.UUID;

/**
 * This is a Data To Object class. The job is to be the middleman between the api-parameter and User class for security reasons.
 * @author TrymV
 */
@Data
public class UserDTO {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String role;
}
