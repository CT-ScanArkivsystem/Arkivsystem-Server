package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.model.UserDTO;
import no.ntnu.ctscanarkivsystemserver.service.TagService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The job of this class is to be the endpoint for all requests limited
 * to user with the role admin.
 * @author TrymV
 */
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final UserService userService;
    private final TagService tagService;

    @Autowired
    public AdminController(UserService userService, TagService tagService) {
        this.userService = userService;
        this.tagService = tagService;
    }

    /**
     * This request will add a new user into the system.
     * If user is null or role is not valid this will return a badRequest.
     * If email already exist this will return CONFLICT.
     * @param user to be added to the system. Require firstName, lastName, email, password and role.
     * @return the created user.
     */
    @PostMapping(path = "/newUser")
    public ResponseEntity<?> addUser(@RequestBody UserDTO user) {
        User addedUser;
        if(user == null) {
            //User cannot be null!
            System.out.println("User is null!");
            return ResponseEntity.badRequest().build();
        }
        user.setRole(user.getRole().toUpperCase());
        if(userService.isRoleValid(user.getRole())) {
            try {
                addedUser = userService.addUser(user);
            } catch (EmailExistsException e) {
                System.out.println(e.getMessage());
                //Email already exists in the database! (409 = Conflict)
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                //Email is null
                return ResponseEntity.badRequest().build();
            }
        } else {
            //role is not a valid role in the system.
            System.out.println(user.getRole() + " is not a valid role!");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addedUser);
    }

    /**
     * This request will change an existing user in the system.
     * If user id is empty email will be used instead to find the user.
     * If a variable in user is empty no change will happen to that variable.
     * @param user user object with the changes to be done to the user.
     *             Id or email will be used to find the user.
     * @return changed user.
     */
    @PutMapping(path = "/editUser")
    public ResponseEntity<?> editUserDetails(@RequestBody UserDTO user) {
        User userAfterChange;
        if(user == null || user.getUserId() == null && user.getEmail() == null) {
            System.out.println("User, id and email cannot be null!");
            return ResponseEntity.badRequest().build();
        }
        user.setRole(user.getRole().toUpperCase());
        if(userService.isRoleValid(user.getRole()) || user.getRole().isEmpty()) {
            try {
                userAfterChange = userService.editUser(user);
                if(userAfterChange != null) {
                    return ResponseEntity.ok(userAfterChange);
                } else {
                    System.out.println("Something went wrong when trying to save user into database.");
                    //Something went wrong when trying to save user into database.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (UserNotFoundException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.notFound().build();
            } catch (EmailExistsException e) {
                System.out.println(e.getMessage());
                //Email already exists.
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                //Email cannot be null.
                return ResponseEntity.badRequest().build();
            }
        }
        //Role is not valid or is not empty.
        return ResponseEntity.badRequest().build();
    }

    /**
     * Removes a user from the system.
     * @param user UserDTO object which must contain userId of user to be removed.
     * @return 200-ok if user was successfully removed.
     */
    @DeleteMapping(path = "/removeUser")
    public ResponseEntity<?> removeUser(@RequestBody UserDTO user) {
        if(user == null || user.getUserId() == null || user.getUserId().toString().isEmpty()) {
            //User and userId cannot be null and userId cannot be empty.
            return ResponseEntity.badRequest().build();
        }
        try {
            if(!userService.removeUser(user.getUserId())) {
                //Something went wrong when trying to remove the user.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (UserNotFoundException e) {
            //No user was found with the id.
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a tag from the system.
     * @param tagName name of tag to be deleted.
     * @return If Successful: 200-Ok.
     *         If tagName is null: 400-Bad Request.
     *         If tag was not found: 404-Not Found.
     */
    @DeleteMapping(path = "/deleteTag")
    public ResponseEntity<?> deleteTag(@RequestParam String tagName) {
        if(tagName == null || tagName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                if(!tagService.deleteTag(tagName)) {
                    //Something went wrong when trying to delete the tag.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (TagNotFoundException e) {
                System.out.println(e.getMessage());
                //Tag was not found.
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok().build();
    }
}
