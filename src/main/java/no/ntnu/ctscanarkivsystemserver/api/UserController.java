package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import no.ntnu.ctscanarkivsystemserver.service.TagService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TagService tagService;

    @Autowired
    public UserController(UserService userService, ProjectService projectService, TagService tagService) {
        this.userService = userService;
        this.projectService = projectService;
        this.tagService = tagService;
    }

    @GetMapping(path = "/allUsers")
    public ResponseEntity<?> getAllUsers() {
        System.out.println("Getting all users!");
        List<User> allUsers = userService.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allUsers);
        }
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
     * This API request will return a list of all existing projects
     * @return Response code 200 OK and the list of projects.
     */
    @GetMapping(path = "/getAllProjects")
    public ResponseEntity<List<Project>> getAllProject() {
        System.out.println("Getting all projects!");
        List<Project> allProjects = projectService.getAllProjects();
        if(allProjects == null || allProjects.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allProjects);
        }
    }

    /**
     * This API request will return a project
     * @param projectId UUID of the project
     * @return If successful: Project & response 200 OK
     *         If projectId is null: 400 Bad request
     *         If project is not found: 404 Not found
     *         If something else goes wrong: 500 Internal server error
     */
    @GetMapping(path = "/getProject")
    public ResponseEntity<Project> getProject(@RequestParam UUID projectId) {
        Project project;
        if (projectId == null) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                project = projectService.getProject(projectId);
            } catch (ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.notFound().build();
            }
            if (project != null) {
                return ResponseEntity.ok(project);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    /**
     * Retrieves all tags from the database.
     * @return If Successful: 200-OK and List with Tags
     *         If there are no tags: 404-Not Found.
     */
    @GetMapping(path = "/getAllTags")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> allTags = tagService.getAllTags();
        if(allTags == null || allTags.isEmpty()) {
            //No tags in the system.
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allTags);
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
