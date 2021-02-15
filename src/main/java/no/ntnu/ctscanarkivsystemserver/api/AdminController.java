package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.Exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
