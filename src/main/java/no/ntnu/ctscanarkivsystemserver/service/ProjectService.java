package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.ProjectDao;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.exception.*;
import no.ntnu.ctscanarkivsystemserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class handles the business logic related to projects
 * @author Brage, trymv
 */
@Service
public class ProjectService {

    private final ProjectDao projectDao;
    private final UserDao userDao;
    private final FileService fileService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProjectService(@Qualifier("projectDaoRepository") ProjectDao projectDao,
                          @Qualifier("postgreSQL") UserDao userDao,
                          FileService fileService, FileStorageService fileStorageService) {
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.fileService = fileService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Method for creating a new project.
     * Checks if another project has this name and throws and exception if necessary
     * @param projectDto The projectDto object passed from the controller.
     * @return The the created project
     */
    public Project createProject(ProjectDTO projectDto, User user) throws ProjectNameExistsException {
        if (user == null) {
            throw new NullPointerException("User cannot be null");
        } else {
            Project newProject = parseProjectDTO(projectDto);
            newProject.setOwner(user);
            return projectDao.createProject(newProject);
        }
    }

    /**
     * This method removes a project from the database using UUID from projectDto.
     * @param projectDto The projectDto object passed from the controller.
     * @param user The logged in user
     * @return True if project has been removes, false if not
     * @throws NullPointerException If projectDto is null
     * @throws ProjectNotFoundException If a project with this UUID does not exist
     */
    public boolean deleteProject(ProjectDTO projectDto, User user) throws NullPointerException, ProjectNotFoundException,
    ForbiddenException {
        if (projectDto == null) {
            throw new NullPointerException("ERROR: projectDto is null");
        }
        Project projectToDelete = projectDao.getProjectById(projectDto.getProjectId());
        if (!userIsOwnerOrAdmin(projectToDelete, user)) {
            throw new ForbiddenException("The logged on user is not allowed to delete projects");
        }
        if (!projectDao.doesProjectExist(projectDto.getProjectId())) {
            throw new ProjectNotFoundException(projectDto.getProjectId());
        }

        return projectDao.deleteProject(projectToDelete);
    }

    /**
     * Method for returning a list of projects
     * @return The list of projects
     */
    public List<Project> getAllProjects() {
        return projectDao.getAllProjects();
    }

    /**
     * Method for returning a specific project
     * @param projectId UUID of the project
     * @return The project
     * @throws ProjectNotFoundException If no project with this UUID exists
     */
    public Project getProject(UUID projectId) throws ProjectNotFoundException {
        if (projectDao.doesProjectExist(projectId)) {
            return projectDao.getProjectById(projectId);
        } else {
            throw new ProjectNotFoundException(projectId);
        }
    }

    /**
     * This method is used to change the owner of a product.
     * It also moves the old owner to project_members.
     * If the new owner is already in special_permissions, he is removed from there
     * @param projectDto The ProjectDTO object used to pass data
     * @param user The logged in user
     * @return True if new owner is set correctly, false otherwise
     * @throws NullPointerException if projectDto is null
     * @throws ProjectNotFoundException If a project with this UUID does not exist
     * @throws UserNotFoundException If a user with this UUID does not exist
     */
    public boolean changeProjectOwner(ProjectDTO projectDto, User user) throws ProjectNotFoundException, UserNotFoundException,
            NullPointerException, ForbiddenException {
        if (projectDto == null) {
            throw new NullPointerException("ERROR: projectDto is null");
        }
        UUID projectId = projectDto.getProjectId();
        UUID newOwnerId = projectDto.getUserId();
        User newOwner = userDao.getUserById(newOwnerId);
        Project projectToEdit = projectDao.getProjectById(projectId);
        User oldOwner = projectToEdit.getOwner();

        if (!projectDao.doesProjectExist(projectId)) {
            System.out.println("ERROR: This project does not exist.");
            throw new ProjectNotFoundException(projectId);
        }
        else if (newOwner == null) {
            System.out.println("ERROR: User does not exist");
            throw new UserNotFoundException(newOwnerId);
        }
        else if (!userIsOwnerOrAdmin(projectToEdit, user)) {
            throw new ForbiddenException("The logged on user is not allowed to change owner of this project");
        }
        else {
            if (hasSpecialPermission(projectToEdit, newOwner)) {
                projectDao.revokeSpecialPermission(projectToEdit, newOwner);
            }
            projectDao.addProjectMember(projectToEdit, oldOwner);
            if (projectToEdit.getProjectMembers().contains(newOwner)) {
                projectDao.removeProjectMember(projectToEdit, newOwner);
            }
            return projectDao.changeProjectOwner(projectToEdit, newOwner);
        }
    }

    /**
     * This method takes the DTO from the controller and sends project and user to the DAO
     * @param projectDto with userEmail (email of user to be added) and projectId.
     * @param user The logged in user
     * @return True if user has been successfully added, false otherwise
     * @throws ForbiddenException If user is not allowed to add members
     * @throws IllegalArgumentException If user is already a member of the project.
     * @throws UserNotFoundException If no user with email was found.
     */
    public boolean addMemberToProject(ProjectDTO projectDto, User user) throws ForbiddenException, IllegalArgumentException, UserNotFoundException {
        Project thisProject = projectDao.getProjectById(projectDto.getProjectId());
        User userToBeAdded = userDao.getUserByEmail(projectDto.getUserEmail());
        if(userToBeAdded != null) {
            if (!userToBeAdded.getRoles().get(0).getRoleName().equals("ROLE_" + Role.USER)) {
                if (userIsOwnerOrAdmin(thisProject, user)) {
                    if (!thisProject.getProjectMembers().contains(userToBeAdded)) {
                        return projectDao.addProjectMember(thisProject, userToBeAdded);
                    } else {
                        throw new IllegalArgumentException("User is already member of project.");
                    }
                } else {
                    throw new ForbiddenException("The logged on user is not allowed to add members to this project");
                }
            } else {
                throw new ForbiddenException("User with email " + userToBeAdded.getEmail() + " don't have high enough role to be added as a member.");
            }
        } else {
            throw new UserNotFoundException("Cannot find any user with email: " + projectDto.getUserEmail());
        }
    }

    /**
     * This method takes the DTO from the controller and sends project and user to the DAO
     * @param projectDto with userEmail (email of user to be removed) and projectId.
     * @param user The logged in user
     * @return The resulting Project object after it has been modified
     * @throws ForbiddenException If user is not allowed to add members
     */
    public boolean removeMemberFromProject(ProjectDTO projectDto, User user) throws ForbiddenException {
        Project thisProject = projectDao.getProjectById(projectDto.getProjectId());
        User userToBeRemoved = userDao.getUserByEmail(projectDto.getUserEmail());
        if (userIsOwnerOrAdmin(thisProject, user)) {
            return projectDao.removeProjectMember(thisProject, userToBeRemoved);
        } else {
            throw new ForbiddenException("The logged on user is not allowed to remove members from this project");
        }
    }

    /**
     * This method adds a user to the special permissions of a project.
     * @param projectId UUID of the project
     * @param userEmail Email of the user
     * @param loggedInUser The current logged in user
     * @return True if user has been added to special permissions, false otherwise
     * @throws ProjectNotFoundException If no project with this UUID exists
     * @throws UserNotFoundException If no user with this email exists
     * @throws ForbiddenException If logged on user is not allowed to grant special permission
     * @throws IllegalArgumentException If user already has special permission
     */
    public boolean grantSpecialPermission(UUID projectId, String userEmail, User loggedInUser) throws ProjectNotFoundException,
            UserNotFoundException, ForbiddenException, IllegalArgumentException {
        if (!projectDao.doesProjectExist(projectId)) {
            throw new ProjectNotFoundException(projectId);
        } else if (userDao.getUserByEmail(userEmail) == null) {
            throw new UserNotFoundException(userEmail);
        } else if (projectDao.getProjectById(projectId).getUsersWithSpecialPermission().contains(userDao.getUserByEmail(userEmail))) {
            throw new IllegalArgumentException("User " + userEmail + " already has special permission for this project");
        }
        else {
            if (!isUserPermittedToChangeProject(projectDao.getProjectById(projectId), loggedInUser)) {
                throw new ForbiddenException("User is not allowed to grant special permissions");
            }
            return projectDao.grantSpecialPermission(projectDao.getProjectById(projectId), userDao.getUserByEmail(userEmail));
        }
    }

    /**
     * This method removes a user from the special permissions of a project.
     * @param projectId UUID of the project
     * @param userEmail Email of the user
     * @param loggedInUser The current logged in user
     * @return True if user has been removed from special permissions, false otherwise
     * @throws ProjectNotFoundException If no project with this UUID exists
     * @throws UserNotFoundException If no user with this email exists
     * @throws ForbiddenException If logged on user is not allowed to grant special permission
     * @throws IllegalArgumentException If user does not have special permission to begin with
     */
    public boolean revokeSpecialPermission(UUID projectId, String userEmail, User loggedInUser) throws ProjectNotFoundException,
            UserNotFoundException, ForbiddenException, IllegalArgumentException {
        if (!projectDao.doesProjectExist(projectId)) {
            throw new ProjectNotFoundException(projectId);
        } else if (userDao.getUserByEmail(userEmail) == null) {
            throw new UserNotFoundException(userEmail);
        } else if (!projectDao.getProjectById(projectId).getUsersWithSpecialPermission().contains(userDao.getUserByEmail(userEmail))) {
            throw new IllegalArgumentException("User " + userEmail + " does not have special permission for this project");
        } else {
            if (!isUserPermittedToChangeProject(projectDao.getProjectById(projectId), loggedInUser)) {
                throw new ForbiddenException("User is not allowed to grant special permissions");
            }
            return projectDao.revokeSpecialPermission(projectDao.getProjectById(projectId), userDao.getUserByEmail(userEmail));
        }
    }

    /**
     * Helper method used to turn a ProjectDTO into a Project.
     * @param projectDto The ProjectDTO object used to pass data
     * @return The created Project object
     */
    private Project parseProjectDTO(ProjectDTO projectDto) throws ProjectNameExistsException {
        Project newProject = new Project();

        String projectName = projectDto.getProjectName();
        if (!projectName.trim().isEmpty()) {
            if (projectDao.doesNameExist(projectName)) {
                throw new ProjectNameExistsException(projectName);
            }
            else {
                newProject.setProjectName(projectName);
            }
        } else {
            return null;
        }
        Boolean isPrivate = projectDto.getIsPrivate();
        if (isPrivate == null) {
            newProject.setIsPrivate(false);
        } else {
            newProject.setIsPrivate(isPrivate);
        }
        if (projectDto.getCreation() != null) {
            newProject.setCreation(projectDto.getCreation());
        }
        if(projectDto.getDescription() != null) {
            newProject.setDescription(projectDto.getDescription());
        }

        return newProject;
    }

    /**
     * Helper method that checks if the new owner already has special permissions.
     * @param project The project
     * @param newOwner The new owner
     * @return True if he has special permissions, false otherwise
     */
    public boolean hasSpecialPermission(Project project, User newOwner) {
        return project.getUsersWithSpecialPermission().contains(newOwner);
    }

    /**
     * Helper method to check if a user can modify a project
     * @param project The project to check
     * @param user The user you want to check permissions for
     * @return True is user is allowed to modify project, false otherwise
     */
    private boolean userIsOwnerOrAdmin(Project project, User user) {
        return project.getOwner().equals(user) || user.getRoles().get(0).getRoleName().equals("ROLE_" + Role.ADMIN);
    }

    /**
     * Adds a new tag to a project.
     * @param projectId Id of project for tag to be added to.
     * @param tagsToBeAdded tags to be added to project.
     * @param adder user which is adding tag to project.
     * @return Project if adding was successful. Null if something went wrong.
     * @throws ProjectNotFoundException if no project with projectId was found.
     * @throws ForbiddenException if user is forbidden to do changes on this project.
     * @throws TagExistsException if tag already exist in the project.
     */
    public Project addTag(UUID projectId, List<Tag> tagsToBeAdded, User adder) throws ProjectNotFoundException, ForbiddenException, TagExistsException {
        Project project = projectDao.getProjectById(projectId);
        if(!isUserPermittedToChangeProject(project, adder)) {
            throw new ForbiddenException("User is forbidden to do changes on this project!");
        } else {
            for(Tag tagToBeAdded:tagsToBeAdded) {
                if(doesTagExistInProject(tagToBeAdded, project)) {
                    throw new TagExistsException(tagToBeAdded.getTagName());
                }
            }
            return projectDao.addProjectTag(project, tagsToBeAdded);
        }
    }

    /**
     * Removes a tag from a project.
     * @param projectId Id of project to be removed from.
     * @param tagsToBeRemoved tags to be removed from project.
     * @param remover user which is removing tag from project.
     * @return Project if removing was successful.
     * @throws ProjectNotFoundException if no project with projectId was found.
     * @throws ForbiddenException if user is forbidden to do changes on this project.
     * @throws TagNotFoundException if tag does not exist in the project.
     */
    public Project removeTag(UUID projectId, List<Tag> tagsToBeRemoved, User remover) throws ProjectNotFoundException, ForbiddenException, TagNotFoundException{
        Project project = projectDao.getProjectById(projectId);
        if(!isUserPermittedToChangeProject(project, remover)) {
            throw new ForbiddenException("User is forbidden to do changes on this project!");
        }  else {
            for(Tag tagToBeRemoved:tagsToBeRemoved) {
                if(!doesTagExistInProject(tagToBeRemoved, project)) {
                    throw new TagNotFoundException(tagToBeRemoved.getTagName());
                }
            }
            return projectDao.removeProjectTag(project, tagsToBeRemoved);
        }
    }

    /**
     * Checks if a user is permitted to do changes on a project.
     * This does not include adding members.
     * @param project project to check.
     * @param user user to check.
     * @return true if user is permitted to do changes.
     */
    public boolean isUserPermittedToChangeProject(Project project, User user) {
        return project.getOwner().equals(user) ||
               isUserMember(project, user) ||
               user.getRoles().get(0).getRoleName().equals("ROLE_" + Role.ADMIN);
    }

    /**
     * Returns true if user is a member of project.
     * @param project project to check.
     * @param user user to check.
     * @return true if user is a member of project.
     */
    private boolean isUserMember(Project project, User user) {
        for(User userInProject:project.getProjectMembers()) {
            if(userInProject.equals(user)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if tag already exist in project.
     * @param tag tag to see if exist.
     * @param project project to see if tag exist in.
     * @return true if tag exist in project.
     */
    private boolean doesTagExistInProject(Tag tag, Project project) {
        for(Tag projectTag:project.getTags()) {
            if(projectTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set a projects privacy.
     * @param project project to set privacy of.
     * @param privacy true to set project as private else false.
     * @return true if change was successful.
     */
    public boolean setProjectPrivacy(Project project, boolean privacy) {
        return projectDao.setPrivacy(project, privacy);
    }

    /**
     * Set the description of a project.
     * @param project project to set description of.
     * @param description description to set on project.
     * @return description if successful else null.
     * @throws IllegalArgumentException if project or description is null.
     */
    public String setProjectDescription(Project project, String description) throws IllegalArgumentException {
        if(project == null || description == null) {
            throw new IllegalArgumentException("Project and description cannot be null!");
        } else {
            return projectDao.setDescription(project, description);
        }
    }

    /**
     * Search thought all projects for name, description, owner and members (first name, last name, email),
     * project tags, file tags and file names if they contain the search word.
     * @param searchWord word to use to search for in projects.
     * @return map of all projects where at least one search result was ture in, and a String with information
     * about where each place in the project search result was found in.
     * @throws IllegalArgumentException if search word is empty.
     * @throws ProjectNotFoundException if no project was found in the database.
     */
    public Map<UUID, List<String>> searchForProject(String searchWord, List<Tag> tagFilter) throws IllegalArgumentException, ProjectNotFoundException {
        List<Project> allProjects = projectDao.getAllProjects();
        if(tagFilter != null && !tagFilter.isEmpty()) {
            allProjects.removeIf(project -> !doesProjectContainTags(project, tagFilter));
        }
        HashMap<UUID, List<String>> result = new HashMap<>();
        if(searchWord == null) {
            throw new IllegalArgumentException("Search word cannot be null or empty!");
        } else if(allProjects.isEmpty()) {
            throw new ProjectNotFoundException("No projects found in the database!");
        } else {
            for(Project project:allProjects) {
                String resultInfo = searchInProject(project, searchWord);
                if(resultInfo != null) {
                    List<String> resultWithProjectName = new ArrayList<>();
                    resultInfo = resultInfo.substring(0, resultInfo.length() -2);
                    resultWithProjectName.add(project.getProjectName());
                    resultWithProjectName.add(resultInfo);
                    result.put(project.getProjectId(), resultWithProjectName);
                }
            }
        }
        return result;
    }

    /**
     * Checks if a project contains all tags in filter
     * @param project project to see if contain tags.
     * @param tagFilter tags to see if project contains.
     * @return true if project contain all tags in tagFilter.
     */
    private boolean doesProjectContainTags(Project project, List<Tag> tagFilter) {
        return project.getTags().containsAll(tagFilter);
    }

    /**
     * Search in a project for search word in project name, description, owner and members (first name, last name, email),
     * project tags, file tags and file names if they contain the search word.
     * @param project project to search thought.
     * @param searchWord word to search with thought the project.
     * @return string with information about where search word was found in project. Null if search word did not exist.
     */
    private String searchInProject(Project project, String searchWord) {
        StringBuilder resultInfo = new StringBuilder();
        searchWord = searchWord.toLowerCase();
        if(project.getProjectName().toLowerCase().contains(searchWord)) {
            resultInfo.append("name, ");
        }
        if(project.getDescription().toLowerCase().contains(searchWord)) {
            resultInfo.append("description, ");
        }
        List<User> owner = new ArrayList<>();
        owner.add(project.getOwner());
        if(doesAtLeastOneUserContainWord(searchWord, owner)) {
            resultInfo.append("owner, ");
        }
        if(doesAtLeastOneUserContainWord(searchWord, project.getProjectMembers())) {
            resultInfo.append("member, ");
        }
        if(doesAtLeastOneStringContainWord(searchWord, project.getTags().stream().map(Tag::getTagName).collect(Collectors.toList()))) {
            resultInfo.append("project_Tag, ");
        }
        if(doesAtLeastOneStringContainWord(searchWord, fileService.getAllTagNamesAssociatedWithProject(project))) {
            resultInfo.append("file_tag, ");
        }
        String fileResultInfo = searchInProjectFiles(project, searchWord, true);
        if(fileResultInfo != null) {
            resultInfo.append(fileResultInfo);
        }
        if(resultInfo.length() == 0) {
            return null;
        }
        return resultInfo.toString();
    }

    /**
     * Search though all file names in the file server under a project if they contain the search word.
     * @param project project to search.
     * @param searchWord word to search for.
     * @param ignore if true this function will be ignored.
     * @return string with information if at least one file contains the search word.
     * If subFolder or project was not found this will return null.
     */
    private String searchInProjectFiles(Project project, String searchWord, boolean ignore) {
        List<String> allFiles = new ArrayList<>();
        String result = "";
        if(ignore) {
            return null;
        } else {
            try {
                for (String subFolder : fileStorageService.getAllProjectSubFolders(project)) {
                    allFiles.addAll(fileStorageService.getAllFileNames("all", project, subFolder));
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                return null;
            }
            if (doesAtLeastOneStringContainWord(searchWord, allFiles)) {
                result = "file_name, ";
            }
        }
        return result;
    }

    /**
     * Checks if at least one user in the list contains the search word in first name,
     * last name or email.
     * @param searchWord word to see if exist in a user.
     * @param users list of user to check.
     * @return true if at least one user contains the search word.
     */
    private boolean doesAtLeastOneUserContainWord(String searchWord, List<User> users) {
        if(!users.isEmpty()) {
            for(User user:users) {
                if(user.getFirstName().toLowerCase().contains(searchWord) ||
                        user.getLastName().toLowerCase().contains(searchWord) ||
                        user.getEmail().contains(searchWord)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if at least one string contain the search word.
     * @param searchWord word to see if at least one string contains.
     * @param strings list of strings to check.
     * @return true if at least one string contains the search word.
     */
    private boolean doesAtLeastOneStringContainWord(String searchWord, List<String> strings) {
        if(!strings.isEmpty()) {
          for(String stringsInList:strings) {
              if(stringsInList.toLowerCase().contains(searchWord)) {
                  return true;
              }
          }
        }
        return false;
    }
}
