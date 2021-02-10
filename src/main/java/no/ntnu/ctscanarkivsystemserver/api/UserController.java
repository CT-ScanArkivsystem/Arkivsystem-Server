package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.Exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.List;

@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/newUser")
    public Response addUser(@RequestBody User user) {
        if(user == null) {
            //User cannot be null!
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            user = userService.addUser(user);
        } catch (EmailExistsException e) {
            System.out.println(e.toString());
            //Email already exists in the database!
            return Response.status(Response.Status.CONFLICT).build();
        }
        return Response.ok(user).build();
    }

    @GetMapping(path = "/allUsers")
    public Response getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        if(allUsers == null || allUsers.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(allUsers).build();
        }
    }
}
