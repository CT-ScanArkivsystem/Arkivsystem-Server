package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.exception.FileStorageException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.FileStorageService;
import no.ntnu.ctscanarkivsystemserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Class for the project APIs.
 *
 * @author Brage
 */
@RequestMapping("/professor")
@RestController
public class ProfessorController {

    private final ProjectService projectService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProfessorController(ProjectService projectService, FileStorageService fileStorageService) {
        this.projectService = projectService;
        this.fileStorageService = fileStorageService;
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

    @PostMapping(path = "/uploadFiles")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("projectId") UUID projectId) {
        try {
            Project projectToUploadFilesTo = projectService.getProjectById(projectId);
            for(MultipartFile file:files) {
                fileStorageService.storeFile(file, projectToUploadFilesTo);
            }
        } catch (ProjectNotFoundException e) {
            //No project was found with id.
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            //TODO Maybe change to something else?
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
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

}
