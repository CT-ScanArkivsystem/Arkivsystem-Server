package no.ntnu.ctscanarkivsystemserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import java.util.*;

@Entity(name = "users")
@Data
@NoArgsConstructor
@NamedQuery(name = User.FIND_ALL_USERS, query = "SELECT u FROM users u ORDER BY u.email")
@NamedQuery(name = User.FIND_USER_BY_EMAIL, query = "SELECT u FROM users u WHERE u.email LIKE: email")
@NamedQuery(name = User.FIND_USER_BY_ID, query = "SELECT u FROM users u WHERE u.userId =: userId")
public class User {
    public static final String FIND_ALL_USERS = "User.findAllUsers";
    public static final String FIND_USER_BY_EMAIL = "User.findUserByEmail";
    public static final String FIND_USER_BY_ID = "User.findUserById";

    @Id
    @Column(name="user_id")
    private UUID userId;

    @NotEmpty
    @Column(name="first_name")
    private String firstName;

    @NotEmpty
    @Column(name="last_name")
    private String lastName;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_name",
                    referencedColumnName = "role_name"))
    private List<Role> roles = new ArrayList<>();

    /**
     * Constructor.
     * @param firstName The users first name, cannot be empty.
     * @param lastName The users last name, cannot be empty.
     * @param email The users e-mail, cannot be empty, must be in e-mail format.
     * @param password The users password, cannot be empty, minimum 6 characters.
     */
    public User(String firstName, String lastName, String email, String password) {
        this.userId = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email.toLowerCase();
        this.password = password;
    }
}
