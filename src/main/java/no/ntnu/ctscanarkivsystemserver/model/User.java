package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import javax.persistence.NamedQuery;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import java.util.UUID;

@Entity(name = "users")
@Data
@NoArgsConstructor
@NamedQuery(name = User.FIND_ALL_USERS, query = "SELECT u FROM users u ORDER BY u.email")
@NamedQuery(name = User.FIND_USER_BY_EMAIL, query = "SELECT u FROM users u WHERE u.email LIKE: email")
public class User {
    public static final String FIND_ALL_USERS = "User.findAllUsers";
    public static final String FIND_USER_BY_EMAIL = "User.findUserByEmail";

    @Id
    @Column(name="user_id")
    private UUID userID;

    @NotEmpty
    @Column(name="first_name")
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
        this.email = email.toLowerCase();
        this.password = password;
    }
}
