package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.*;
import no.ntnu.ctscanarkivsystemserver.model.*;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import no.ntnu.ctscanarkivsystemserver.service.TagService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The job of this class is to be the endpoint for all requests limited
 * to user with the role academic.
 * @author Brage, trymv
 */
@RequestMapping("/academic")
@RestController
public class AcademicController {

    private final ProjectService projectService;
    private final TagService tagService;
    private final UserService userService;

    @Autowired
    public AcademicController(ProjectService projectService, TagService tagService, UserService userService) {
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
     * @return If successful: Response code 200 OK and the project itself. Received from the service class
     *         If projectDto is null: 400 Bad request
     *         If a project with this name exists: 409 Conflict
     *         If userService can't get current user: 400 Bad request
     *
     */
    @PostMapping(path = "/createProject")
    public ResponseEntity<Project> createProject(@RequestBody ProjectDTO projectDto) {
        Project result;
        if (projectDto == null) {
            System.out.println("projectDto cannot be null");
            return ResponseEntity.badRequest().build();
        } else {
            try {
                result = projectService.createProject(projectDto, userService.getCurrentLoggedUser());
            } catch (ProjectNameExistsException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
            if (result == null) {
                System.out.println("Something went wrong while attempting to create project");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } else {
                return ResponseEntity.ok(result);
            }
        }
    }

    /**
     * This method will delete a project from the database
     * @param projectDto The object containing the id of the project to remove
     * @return If successful: Response code 200 OK
     *         If ProjectDto is null: Response Bad Request
     *         If project name exists: Http response CONFLICT
     *         If user is not allowed to delete projects: Http response FORBIDDEN
     */
    @DeleteMapping(path = "/deleteProject")
    public ResponseEntity<?> deleteProject(@RequestBody ProjectDTO projectDto) {
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                if (!projectService.deleteProject(projectDto, userService.getCurrentLoggedUser())) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (ProjectNameExistsException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.ok().build();
        }
    }

    /**
     * This API request is used to change the owner of a project.
     * @param projectDto The ProjectDTO object used to pass data
     * @return If successful: Response code 200 OK and the Project object
     *         If ProjectDto is null: Response Bad Request
     *         If user is not allowed to delete projects: Http response FORBIDDEN
     */
    @PutMapping(path = "/changeProjectOwner")
    public ResponseEntity<?> changeProjectOwner(@RequestBody ProjectDTO projectDto) {
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                System.out.println("Controller: projectDto is not null, attempting to use projectService");
                if (!projectService.changeProjectOwner(projectDto, userService.getCurrentLoggedUser())) {
                    System.out.println("Could not change project owner");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (ProjectNotFoundException | UserNotFoundException e) {
                System.out.println(e.getMessage());
                ResponseEntity.notFound().build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok().build();
        }
    }

    /**
     * This API request is used to add an existing user to the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK
     *         If ProjectDto is null: 400 Bad request
     *         If User is not allowed to add project members: 403 Forbidden
     *         If member is not added successfully or something else fails: 500 Internal server error
     */
    @PutMapping(path = "/addMemberToProject")
    public ResponseEntity<Project> addMemberToProject(@RequestBody ProjectDTO projectDto) {
        boolean success;
        if (projectDto  == null) {
            System.out.println("ProjectDto is null");
            return ResponseEntity.badRequest().build();
        } else {
            try {
                success = projectService.addMemberToProject(projectDto, userService.getCurrentLoggedUser());
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        }
    }

    /**
     * This API request is used to remove an existing user from the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK and the modified project
     *         If ProjectDto is null: Response Bad Request
     */
    @PutMapping(path = "/removeMemberFromProject")
    public ResponseEntity<Project> removeMemberFromProject(@RequestBody ProjectDTO projectDto) {
        boolean success;
        if (projectDto  == null) {
            System.out.println("ProjectDto is null");
            return ResponseEntity.badRequest().build();
        } else {
            try {
                success = projectService.removeMemberFromProject(projectDto, userService.getCurrentLoggedUser());
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
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
     * Add tags to a project.
     * @param tagNames names of tags to be added.
     * @param projectId id of project to which tags is getting added to.
     * @return If Successful: 200-Ok with Project.
     *         If tagName or projectId is null; 400-Bad Request.
     *         If tag, project or adder user is not found: 404-Not Found.
     *         If user is not allowed to do changes on project: 403-Forbidden.
     *         If database failed to add tag: 500-Internal Server Error.
     *         If tag already exist in project: 409-Conflict.
     *         If tagName has 2 or less characters: 400-Bad Request.
     */
    @PutMapping(path = "/addTag")
    public ResponseEntity<Project> addTag(@RequestParam List<String> tagNames, @RequestParam UUID projectId) {
        Project addedProject;
        if(tagNames == null || projectId == null) {
            //Tag name cannot be empty and project id cannot be null!
            return ResponseEntity.badRequest().build();
        } else {
            List<Tag> tagsToBeAdded = new ArrayList<>();
            for(String tagName: tagNames) {
                tagsToBeAdded.add(tagService.getTag(tagName));
            }
            try {
                addedProject = projectService.addTag(projectId, tagsToBeAdded, userService.getCurrentLoggedUser());
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
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
                //tagName cannot have less than 2 characters
                return ResponseEntity.badRequest().build();
            }
        }
        if(addedProject == null) {
            //Something went wrong when trying to add tag!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            return ResponseEntity.ok(addedProject);
        }
    }

    /**
     * Remove tags from a project.
     * @param tagNames names of tags to be removed.
     * @param projectId id of project to which tag is getting removed from.
     * @return If Successful: 200-Ok with Project.
     *         If tagName or projectId is null: 400-Bad Request.
     *         If tag, project or remover user is not found: 404-Not Found.
     *         If user is not allowed to do changes on project: 403-Forbidden.
     *         If database failed to remove tag: 500-Internal Server Error.
     *         If tagName has 2 or less characters: 400-Bad Request.
     */
    @PutMapping(path = ("/removeTag"))
    public ResponseEntity<Project> removeTag(@RequestParam List<String> tagNames, @RequestParam UUID projectId) {
        Project project;
        if(tagNames == null || projectId == null) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                List<Tag> tagsToBeRemoved = new ArrayList<>();
                for(String tagName:tagNames) {
                    tagsToBeRemoved.add(tagService.getTag(tagName));
                }
                project = projectService.removeTag(projectId, tagsToBeRemoved, userService.getCurrentLoggedUser());
            } catch (UserNotFoundException | TagNotFoundException | ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                //Tag, project or user not found. Tag is either does not exist or is not found in project.
                return ResponseEntity.notFound().build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                //User is forbidden to do changes on this project.
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
                //tagName cannot have less than 2 characters.
                return ResponseEntity.badRequest().build();
            }
        }
        if(project == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            return ResponseEntity.ok(project);
        }
    }

    /**
     * This method adds a user to the special permissions of a project.
     * @param projectId UUID of the project
     * @param userEmail the email of the user
     * @return If successful: 200 OK
     *         If user or project does not exist: 404 Not Found
     *         If logged in user is not allowed to grant special permissions: 403 Forbidden
     *         If user already has special permission: 409 Conflict
     *         If adding user to special permission fails some other way: 500 Internal server error
     */
    @PutMapping(path = ("/grantSpecialPermission"))
    public ResponseEntity<?> grantSpecialPermission(@RequestParam UUID projectId, @RequestParam String userEmail) {
        boolean success = false;
        if (projectId == null || userEmail == null || projectId.toString().trim().isEmpty() || userEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                success = projectService.grantSpecialPermission(projectId, userEmail, userService.getCurrentLoggedUser());
            } catch (UserNotFoundException | ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.notFound().build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    /**
     * This method removes a user from the special permissions of a project.
     * @param projectId UUID of the project
     * @param userEmail the email of the user
     * @return If successful: 200 OK
     *         If user or project does not exist: 404 Not Found
     *         If logged in user is not allowed to grant special permissions: 403 Forbidden
     *         If user already has special permission: 409 Conflict
     *         If removing user from special permission fails some other way: 500 Internal server error
     */
    @PutMapping(path = ("/revokeSpecialPermission"))
    public ResponseEntity<?> revokeSpecialPermission(@RequestParam UUID projectId, @RequestParam String userEmail) {
        boolean success = false;
        if (projectId == null || userEmail == null || projectId.toString().trim().isEmpty() || userEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            try {
                success = projectService.revokeSpecialPermission(projectId, userEmail, userService.getCurrentLoggedUser());
            } catch (UserNotFoundException | ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.notFound().build();
            } catch (ForbiddenException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}