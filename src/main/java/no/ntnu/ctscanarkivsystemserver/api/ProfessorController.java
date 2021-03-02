package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.ProjectDTO;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class for the professor APIs.
 * @author Brage
 */
@RequestMapping("/professor")
@RestController
public class ProfessorController {

    private final ProjectService projectService;

    @Autowired
    public ProfessorController(ProjectService projectService) {
        this.projectService = projectService;
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
            result = projectService.createProject(projectDto);
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
     * @return If successful: Response code 200 OK and the boolean true
     *         If ProjectDto is null: Response Bad Request
     */
    @DeleteMapping(path = "/deleteProject")
    public ResponseEntity<Boolean> deleteProject(@RequestBody ProjectDTO projectDto) {
        boolean result;
        if (projectDto == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.deleteProject(projectDto);
        } catch (ProjectNameExistsException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok(result);
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
     */
    @PostMapping(path = "/changeProjectOwner")
    public ResponseEntity<Project> changeProjectOwner(@RequestBody ProjectDTO projectDto) {
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
     *         If ProjectDto is null: Response Bad Request
     */
    @PostMapping(path = "/addMemberToProject")
    public ResponseEntity<Project> addMemberToProject(@RequestBody ProjectDTO projectDto) {
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
     *         If ProjectDto is null: Response Bad Request
     */
    @PostMapping(path = "/removeMemberFromProject")
    public ResponseEntity<Project> removeMemberFromProject(@RequestBody ProjectDTO projectDto) {
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


}
