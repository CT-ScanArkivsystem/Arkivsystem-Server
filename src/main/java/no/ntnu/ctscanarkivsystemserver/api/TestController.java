package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.Exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.model.Role;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class TestController {

    private final UserService userService;

    @Autowired
    public TestController(UserService userService) {
        this.userService = userService;
    }

    //TODO Remove this method! Only admin should be able to make a new user.
    @PostMapping(path = "/newUser")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        if(user == null) {
            //User cannot be null!
            return ResponseEntity.badRequest().build();
        }
        try {
            user = userService.addUser(user, Role.USER);
        } catch (EmailExistsException e) {
            System.out.println(e.toString());
            //Email already exists in the database! (409 = Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(user);
    }
}
