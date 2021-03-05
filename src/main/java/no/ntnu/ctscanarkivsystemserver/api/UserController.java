package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.TagService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;
    private final TagService tagService;

    @Autowired
    public UserController(UserService userService, TagService tagService) {
        this.userService = userService;
        this.tagService = tagService;
    }

    /**
     * Return the current logged in user.
     * @return current logged in user.
     */
    @GetMapping(path = "/currentUser")
    public ResponseEntity<?> currentUser() {
        try {
            return ResponseEntity.ok(userService.getCurrentLoggedUser());
        } catch (UserNotFoundException e) {
            System.out.println("No user found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Gets all projects a tag is used in.
     * @param tagName name of tag to get all projects from.
     * @return If Successful: 200-Ok with a list of all projects a tag is used in.
     *         If tagName is null or empty: 400-Bad Request.
     *         If no tag with tagName was found: 404-Not Found.
     *         If tagName has less than 2 characters: 400-Bad Request.
     */
    @GetMapping(path = "/getAllProjectsTagIsUsedIn")
    public ResponseEntity<List<Project>> getAllProjectsTagIsUsedIn(@RequestParam String tagName) {
        if(tagName == null || tagName.trim().isEmpty()) {
            //tagName cannot be null or empty!
            return ResponseEntity.badRequest().build();
        } else {
            try {
                return ResponseEntity.ok(tagService.getAllProjectsTagIsUsedIn(tagName));
            } catch (TagNotFoundException e) {
                System.out.println(e.getMessage());
                //No tag with tagName was found.
                return ResponseEntity.notFound().build();
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
                //tagName has less than 2 characters!
                return ResponseEntity.badRequest().build();
            }
        }
    }
}
