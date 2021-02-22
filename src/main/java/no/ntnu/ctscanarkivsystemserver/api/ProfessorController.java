package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.ProjectDTO;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.model.UserDTO;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class for the project APIs.
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
     * @param project The object I want to create. Needs to contain projectName, isPrivate and creation date
     * @return Response code 200 OK and the project itself. Received from the service class
     */
    @PostMapping(path = "/createProject")
    public ResponseEntity<?> createProject(@RequestBody Project project) {
        if (project == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            project = projectService.createProject(project);
        } catch (ProjectNameExistsException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(project);
    }

    /**
     * This API request will return a list of all existing projects
     * @return Response code 200 OK and the list of projects.
     */
    @GetMapping(path = "/allProjects")
    public ResponseEntity<?> getAllUsers() {
        //System.out.println("Getting all projects!");
        List<Project> allProjects = projectService.getAllProjects();
        if(allProjects == null || allProjects.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(allProjects);
        }
    }

    @PutMapping(path = "/changeProjectOwner")
    public ResponseEntity<?> changeProjectOwner(@RequestBody ProjectDTO project, @RequestBody UserDTO newOwner) {
        Project result = null;

        if (project == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            result = projectService.changeProjectOwner(project, newOwner);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        assert result != null;
        return ResponseEntity.ok(result);
    }

}
