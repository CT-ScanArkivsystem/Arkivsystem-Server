package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/allUsers")
    public ResponseEntity<?> getAllUsers() {
        System.out.println("Getting all users!");
        List<User> allUsers = userService.getAllUsers();
        if(allUsers == null || allUsers.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allUsers);
        }
    }
}
