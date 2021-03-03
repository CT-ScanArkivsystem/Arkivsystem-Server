package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.ProjectDTO;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.ForbiddenException;
import java.util.List;

/**
 * Class for the professor APIs.
 * @author Brage
 */
@RequestMapping("/professor")
@RestController
public class ProfessorController {

    private final ProjectService projectService;
    private final UserService userService;

    @Autowired
    public ProfessorController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
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
    public ResponseEntity<Project> createProject(@RequestBody ProjectDTO projectDto) {
        Project result;
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.createProject(projectDto, userService.getCurrentLoggedUser());
        } catch (ProjectNameExistsException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
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
        }
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
        }
        return ResponseEntity.ok().build();
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
     * This API request is used to change the owner of a project.
     * @param projectDto The ProjectDTO object used to pass data
     * @return If successful: Response code 200 OK and the Project object
     *         If ProjectDto is null: Response Bad Request
     *         If user is not allowed to delete projects: Http response FORBIDDEN
     */
    @PostMapping(path = "/changeProjectOwner")
    public ResponseEntity<?> changeProjectOwner(@RequestBody ProjectDTO projectDto) {
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            System.out.println("Controller: projectDto is not null, attempting to use projectService");
            if (!projectService.changeProjectOwner(projectDto, userService.getCurrentLoggedUser())) {
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

    /**
     * This API request is used to add an existing user to the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK and the modified project
     *         If ProjectDto is null: Response Bad Request
     */
    @PostMapping(path = "/addMemberToProject")
    public ResponseEntity<Project> addMemberToProject(@RequestBody ProjectDTO projectDto) {
        Project result = null;

        if (projectDto  == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.addMemberToProject(projectDto, userService.getCurrentLoggedUser());

        } catch (ForbiddenException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            return ResponseEntity.ok(result);
        }
    }

    /**
     * This API request is used to remove an existing user from the members of a project
     * @param projectDto The 'Data To Object' used to carry the required id's. Must contain userId and projectId
     * @return If successful: Response code 200 OK and the modified project
     *         If ProjectDto is null: Response Bad Request
     */
    @PostMapping(path = "/removeMemberFromProject")
    public ResponseEntity<Project> removeMemberFromProject(@RequestBody ProjectDTO projectDto) {
        Project result = null;

        if (projectDto  == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.removeMemberFromProject(projectDto, userService.getCurrentLoggedUser());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


}
