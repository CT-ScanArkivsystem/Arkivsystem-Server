package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * The job of this class is to be the endpoint for all requests limited
 * to user with the role admin.
 * @author TrymV
 */
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * This request will add a new user into the system.
     * If user is null or role is not valid this will return a badRequest.
     * If email already exist this will return CONFLICT.
     * @param user to be added to the system. Require firstName, lastName, email and password.
     * @param role to be giving to the new user.
     * @return the created user.
     */
    @PostMapping(path = "/newUser")
    public ResponseEntity<?> addUser(@RequestBody User user, @RequestParam("role") String role) {
        if(user == null) {
            //User cannot be null!
            System.out.println("User is null!");
            return ResponseEntity.badRequest().build();
        }
        role = role.toUpperCase();
        if(userService.isRoleValid(role)) {
            try {
                user = userService.addUser(user, role);
            } catch (EmailExistsException e) {
                System.out.println(e.toString());
                //Email already exists in the database! (409 = Conflict)
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            //role is not a valid role in the system.
            System.out.println(role + " is not a valid role!");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * This request will change an existing user in the system.
     * If user id is empty email will be used instead to find the user.
     * If a variable in user is empty no change will happen to that variable.
     * @param user user object with the changes to be done to the user.
     *             Id or email will be used to find the user.
     * @param role to give user. If empty role wont change.
     * @return changed user.
     */
    @PutMapping(path = "/editUser")
    public ResponseEntity<?> editUserDetails(@RequestBody User user, @RequestParam("role") String role) {
        User userAfterChange;
        if(user == null || user.getUserId() == null && user.getEmail() == null) {
            System.out.println("User, id and email cannot be null!");
            return ResponseEntity.badRequest().build();
        }
        role = role.toUpperCase(Locale.ROOT);
        if(userService.isRoleValid(role) || role.isEmpty()) {
            try {
                userAfterChange = userService.editUser(user, role);
                if(userAfterChange != null) {
                    return ResponseEntity.ok(userAfterChange);
                } else {
                    System.out.println("Something went wrong when trying to save user into database.");
                    //Something went wrong when trying to save user into database.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (UserNotFoundException e) {
                System.out.println(e.toString());
                return ResponseEntity.notFound().build();
            } catch (EmailExistsException e) {
                System.out.println(e.toString());
                //Email already exists.
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        //Role is not valid or is not empty.
        return ResponseEntity.badRequest().build();
    }
}
