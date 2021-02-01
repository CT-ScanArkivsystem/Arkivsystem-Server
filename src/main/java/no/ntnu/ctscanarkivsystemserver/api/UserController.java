package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/newUser")
    public void addUser(@RequestBody User user) {
        userService.addUser(user);
    }

    @GetMapping(path = "/allUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
