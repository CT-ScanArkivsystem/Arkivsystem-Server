package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@RequestBody User user) {
        return Response.ok(userService.addUser(user)).build();
    }

    @GetMapping(path = "/allUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        if(allUsers == null || allUsers.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(allUsers).build();
        }
    }
}
