package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param project The object I want to create. Needs to contain projectName and isPrivate
     * @return Response code OK and the project itself. Received from the service class
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
}
