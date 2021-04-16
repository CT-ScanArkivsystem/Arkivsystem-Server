package no.ntnu.ctscanarkivsystemserver.api;


import no.ntnu.ctscanarkivsystemserver.exception.FileStorageException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.FileOTD;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TagService tagService;
    //For getting files from file-server.
    private final FileStorageService fileStorageService;
    //For files in database.
    private final FileService fileService;

    @Autowired
    public UserController(UserService userService, ProjectService projectService, TagService tagService,
                          FileStorageService fileStorageService, FileService fileService) {
        this.userService = userService;
        this.projectService = projectService;
        this.tagService = tagService;
        this.fileStorageService = fileStorageService;
        this.fileService = fileService;
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

    /**
     * Download a file from the file server.
     * @param fileNames name of file to download including file type.
     * @param projectId Id of project file is associated with.
     * @param subFolder Folder name of the sub-project.
     * @return If successful: 200-OK with the content of the file.
     *         If fileName does not include file type: 400-Bad request
     *         If user or project does not exist: 404-Not Found.
     *         If logged in user is not allowed to see project files: 403-Forbidden.
     *         If file was not found: 410-Gone.
     */
    @PostMapping(path = "/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") List<String> fileNames, @RequestParam("projectId") UUID projectId,
                                                 @RequestParam("subFolder") String subFolder) {
        byte[] fileBytes;
        if(!fileStorageService.doesAllFileNamesContainType(fileNames)) {
            //File name does not include file type.
            System.out.println("One or more files does not contain file type!");
            return ResponseEntity.badRequest().build();
        }
        try {
            Project projectToDownloadFilesFrom = projectService.getProject(projectId);
            if(!projectToDownloadFilesFrom.getIsPrivate() || projectService.hasSpecialPermission(projectToDownloadFilesFrom, userService.getCurrentLoggedUser())
                    || projectService.isUserPermittedToChangeProject(projectToDownloadFilesFrom, userService.getCurrentLoggedUser())) {
                if(fileNames.size() == 1) {
                    fileBytes = fileStorageService.loadFileAsBytes(fileNames.get(0), projectToDownloadFilesFrom, subFolder);
                } else if(fileNames.size() > 1) {
                    fileBytes = fileStorageService.getFilesAsZip(fileNames, projectToDownloadFilesFrom, subFolder);
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                //User is not permitted to see files on this project.
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (ProjectNotFoundException | UserNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.GONE).build();
        } catch (FileStorageException | IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok()
                .contentLength(fileBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new ByteArrayInputStream(fileBytes)));
    }

    /**
     * Gets a list with all file names in a directory.
     * This will also return a list of all tags which are associated with the files.
     * Valid directory arguments: documents, images, logs, dicom, tiff and all.
     * @param directory directory to get files from (Folders inside sub folder).
     * @param projectId id of project directory is associated with.
     * @param subFolder Folder name of the sub-project.
     * @return If successful: 200-OK with a map of all files in a directory and tags which are associated with each file.
     *         If directory is not a valid directory: 400-Bad Request
     *         If subFolder variable is null or empty: 400-Bad Request
     *         If user or project does not exist: 404-Not Found.
     *         If logged in user is not allowed to see project files: 403-Forbidden.
     *         If directory was not found: 410-Gone.
     */
    @GetMapping(path = "/getAllFileNames")
    public ResponseEntity<List<FileOTD>> getAllFileNames(@RequestParam("directory") String directory, @RequestParam("projectId") UUID projectId,
                                                        @RequestParam("subFolder") String subFolder) {
        List<FileOTD> files;
        if(subFolder == null || subFolder.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Project projectToGetFileNamesFrom = projectService.getProject(projectId);
            if (!projectToGetFileNamesFrom.getIsPrivate() || projectService.hasSpecialPermission(projectToGetFileNamesFrom, userService.getCurrentLoggedUser())
                    || projectService.isUserPermittedToChangeProject(projectToGetFileNamesFrom, userService.getCurrentLoggedUser())) {
                List<String> allFileNamesInDir = fileStorageService.getAllFileNames(directory, projectToGetFileNamesFrom, subFolder);
                files = fileService.getTagsOnFiles(projectId, subFolder, allFileNamesInDir);
            } else {
                //User is not permitted to see files on this project.
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (FileStorageException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ProjectNotFoundException | UserNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.GONE).build();
        } catch (IllegalArgumentException | BadRequestException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(files);
    }

    /**
     * Return a list of all sub-projects of a project.
     * @param projectId Id of project to get all sub-projects of.
     * @return If successful: 200-OK with a list of all sub-project folder names.
     *         If project folder was not found: 204-No Content
     *         If user or project does not exist: 404-Not Found.
     *         If logged in user is not allowed to see project: 403-Forbidden.
     */
    @GetMapping(path = "/getAllProjectSubFolders")
    public ResponseEntity<List<String>> getAllProjectSubFolders(@RequestParam("projectId") UUID projectId) {
        List<String> allSubFolders;
        try {
            Project projectToGetFoldersNamesFrom = projectService.getProject(projectId);
            if (!projectToGetFoldersNamesFrom.getIsPrivate() || projectService.hasSpecialPermission(projectToGetFoldersNamesFrom, userService.getCurrentLoggedUser())
                    || projectService.isUserPermittedToChangeProject(projectToGetFoldersNamesFrom, userService.getCurrentLoggedUser())) {
                allSubFolders = fileStorageService.getAllProjectSubFolders(projectToGetFoldersNamesFrom);
            } else {
                //User is not permitted to see files on this project.
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.noContent().build();
        } catch (FileStorageException | IllegalArgumentException | BadRequestException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ProjectNotFoundException | UserNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(allSubFolders);
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
     * Search for project name, description,
     * @param searchWord word to use to search for a project.
     * @return If Successful: 200-OK with list of all found projects.
     *         If searchWord is empty: 400-Bad request.
     *         If no projects was found in the database: 204-No Content.
     */
    @GetMapping(path = "/search")
    public ResponseEntity<Map<UUID, List<String>>> searchForProject(@RequestParam("search") String searchWord, @RequestParam("tagFilter") List<String> filters) {
        Map<UUID, List<String>> searchResult;
        List<Tag> filterList = new ArrayList<>();
        if(searchWord.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            if(filters != null && !filters.isEmpty()) {
                for (String filter : filters) {
                    Tag tag = tagService.getTag(filter);
                    if (tag != null) {
                        filterList.add(tag);
                    }
                }
            }
            try {
                searchResult = projectService.searchForProject(searchWord, filterList);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.badRequest().build();
            } catch (ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.ok(searchResult);
    }
}
