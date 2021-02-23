package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.ProjectDao;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * This class handles the business logic related to projects
 * @author Brage
 */
@Service
public class ProjectService {

    private final ProjectDao projectDao;
    private final UserDao userDao;

    @Autowired
    public ProjectService(@Qualifier("projectDaoRepository") ProjectDao projectDao, @Qualifier("postgreSQL") UserDao userDao) {
        this.projectDao = projectDao;
        this.userDao = userDao;
    }


    /**
     * Method for creating a new project.
     * Checks if another project has this name and throws and exception if necessary
     * @param project The project passed from the controller.
     * @return The the created project
     */
    public Project createProject(Project project) {
        if (projectDao.doesNameExist(project.getProjectName())) {
            throw new ProjectNameExistsException(project.getProjectName());
        }
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Project newProject = new Project(project.getProjectName(), project.getIsPrivate(), userDao.getUserByEmail(userDetails.getUsername()), project.getCreation());
        return projectDao.createProject(newProject);
    }

    /**
     * Method for returning a list of projects
     * @return The list of projects
     */
    public List<Project> getAllProjects() {
        List<Project> projectList = projectDao.getAllProjects();
        return projectList;
    }

    /**
     * This method is used to change the owner of a product.
     * It also moves the old owner to project_members.
     * If the new owner is already in special_permissions, he is removed from there
     * @param project The ProjectDTO object used to pass data
     * @param inputUser The UserDTO object used to pass data
     * @return the resulting Project object after i has been modified
     * @throws ProjectNotFoundException If a project with this UUID does not exist
     * @throws UserNotFoundException If a user with this UUID does not exist
     */
    public Project changeProjectOwner(ProjectDTO project, UserDTO inputUser) throws ProjectNotFoundException, UserNotFoundException {
        UUID projectId = project.getProjectId();
        UUID ownerId = inputUser.getUserId();
        User newOwner = userDao.getUserById(ownerId);
        Project projectToEdit = projectDao.getProjectById(projectId);
        User oldOwner = projectToEdit.getOwner();

        if (!projectDao.doesProjectExist(projectId)) {
            System.out.println("ERROR: This project does not exist.");
            throw new ProjectNotFoundException(projectId);
        }
        else if (newOwner == null) {
            System.out.println("ERROR: User does not exist");
            throw new UserNotFoundException(ownerId);
        }
        else {
            if (hasSpecialPermission(projectToEdit, newOwner)) {
                projectDao.removeSpecialPermission(projectToEdit, newOwner);
            }
            projectDao.addProjectMember(projectToEdit, oldOwner);
            return projectDao.changeProjectOwner(projectToEdit, newOwner);
        }
    }

    /**
     * Checks if the new owner already has special permissions.
     * @param project The project
     * @param newOwner The new owner
     * @return True if he has special permissions, false otherwise
     */
    private boolean hasSpecialPermission(Project project, User newOwner) {
        if (project.getUsersWithSpecialPermission().contains(newOwner)) {
            return true;
        } else {
            return false;
        }
    }
}
