package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.*;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.ProjectDTO;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import no.ntnu.ctscanarkivsystemserver.service.TagService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.UUID;

/**
 * Class for the project APIs.
 * @author Brage, trymv
 */
@RequestMapping("/professor")
@RestController
public class ProfessorController {

    private final ProjectService projectService;
    private final TagService tagService;
    private final UserService userService;

    @Autowired
    public ProfessorController(ProjectService projectService, TagService tagService, UserService userService) {
        this.projectService = projectService;
        this.tagService = tagService;
        this.userService = userService;
    }

    /**
     * This API request will create a new project. Calls the createProject method of the ProjectService class.
     * If the project object is null, it will return Bad Request response.
     * If the name of the new project already exists, will catch an exception and return a CONFLICT status.
     * Otherwise returns status OK.
     * @param projectDto The object I want to create.
     * @return Response code 200 OK and the project itself. Received from the service class
     */
    @PostMapping(path = "/createProject")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDto) {
        Project result = null;
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.createProject(projectDto);
        } catch (ProjectNameExistsException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * This API request will return a list of all existing projects
     * @return Response code 200 OK and the list of projects.
     */
    @GetMapping(path = "/allProjects")
    public ResponseEntity<?> getAllUsers() {
        System.out.println("Getting all projects!");
        List<Project> allProjects = projectService.getAllProjects();
        if(allProjects == null || allProjects.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allProjects);
        }
    }

    /**
     * This API request is used to change the owner of a project.
     * @param projectDto The ProjectDTO object used to pass data
     * @return If successful: Response code 200 OK and the Project object
     *         If ProjectDTO is null: Response Bad Request
     */
    @PostMapping(path = "/changeProjectOwner")
    public ResponseEntity<?> changeProjectOwner(@RequestBody ProjectDTO projectDto) {
        Project result = null;

        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            System.out.println("Controller: projectDto is not null, attempting to use projectservice");
            result = projectService.changeProjectOwner(projectDto);
        } catch (ProjectNotFoundException | UserNotFoundException e) {
            System.out.println(e.getMessage());
            ResponseEntity.notFound().build();
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * This API request is used to add an existing user to the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK and the modified project
     *         If ProjectDTO is null: Response Bad Request
     */
    @PostMapping(path = "/addMemberToProject")
    public ResponseEntity<?> addMemberToProject(@RequestBody ProjectDTO projectDto) {
        Project result = null;

        if (projectDto  == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.addMemberToProject(projectDto);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * This API request is used to remove an existing user from the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK and the modified project
     *         If ProjectDTO is null: Response Bad Request
     */
    @PostMapping(path = "/removeMemberFromProject")
    public ResponseEntity<?> removeMemberFromProject(@RequestBody ProjectDTO projectDto) {
        Project result = null;

        if (projectDto  == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.removeMemberFromProject(projectDto);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * This request will create a new tag and add it into the system.
     * @param tagName name of tag to be added.
     * @return If Successful: 200-OK and new Tag.
     *         If Tag name is null or empty: 400-Bad Request.
     *         If Tag already exist: 409-Conflict.
     */
    @PostMapping(path = "/createTag")
    public ResponseEntity<Tag> createTag(@RequestParam String tagName) {
        Tag tagToBeAdded;
        if(tagName == null || tagName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                tagToBeAdded = tagService.createTag(tagName);
            } catch (TagExistsException e) {
                //Tag already exist!
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (IllegalArgumentException e) {
                //Tag name is null or empty!
                System.out.println(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(tagToBeAdded);
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
     * Add a tag to a project.
     * @param tagName name of tag to be added.
     * @param projectId id of project to which tag is getting added to.
     * @return If Successful: 200-Ok with Project.
     *         If tagName or projectId is null; 400-Bad Request.
     *         If tag, project or adder user is not found: 404-Not Found.
     *         If user is not allowed to do changes on project: 403-Forbidden.
     *         If database failed to add tag: 500-Internal Server Error.
     *         If tag already exist in project: 409-Conflict.
     */
    @PutMapping(path = "/addTag")
    public ResponseEntity<Project> addTag(@RequestParam String tagName, @RequestParam UUID projectId) {
        Project addedProject;
        if(tagName == null || tagName.trim().isEmpty() || projectId == null) {
            //Tag name cannot be empty and project id cannot be null!
            return ResponseEntity.badRequest().build();
        } else {
            try {
                addedProject = projectService.addTag(projectId, tagService.getTag(tagName), userService.getCurrentLoggedUser());
            } catch (UserNotFoundException | TagNotFoundException | ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                //Tag, project or user not found.
                return ResponseEntity.notFound().build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                //User is forbidden to do changes on this project.
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (TagExistsException e) {
                System.out.println(e.getMessage());
                //Tag already exist in project.
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        if(addedProject == null) {
            //Something went wrong when trying to add tag!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            return ResponseEntity.ok(addedProject);
        }
    }
}