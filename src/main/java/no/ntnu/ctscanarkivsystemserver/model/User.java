package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import java.util.UUID;

@Entity(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @Column(name="user_id")
    private UUID userID;

    @NotEmpty
    @Column(name="fist_name")
    private String firstName;

    @NotEmpty
    @Column(name="last_name")
    private String lastName;

    @Email
    @NotEmpty
    private String email;

    //@Min(6)
    @NotEmpty
    private String password;

    //private final List<> roles;
    /**
     * Constructor.
     * @param firstName The users first name, cannot be empty.
     * @param lastName The users last name, cannot be empty.
     * @param email The users e-mail, cannot be empty, must be in e-mail format.
     * @param password The users password, cannot be empty, minimum 6 characters.
     */
    public User(String firstName, String lastName, String email, String password) {
        this.userID = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
