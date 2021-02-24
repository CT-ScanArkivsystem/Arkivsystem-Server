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

import java.util.Date;
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
     * @param projectDto The projectDto object passed from the controller.
     * @return The the created project
     */
    public Project createProject(ProjectDTO projectDto) {
        Project newProject = parseProjectDTO(projectDto);
        //Project newProject = new Project(projectDto.getProjectName(), projectDto.getIsPrivate(), projectDto.getCreation());
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
     * @param projectDto The ProjectDTO object used to pass data
     * @return the resulting Project object after it has been modified
     * @throws NullPointerException if projectDto is null
     * @throws ProjectNotFoundException If a project with this UUID does not exist
     * @throws UserNotFoundException If a user with this UUID does not exist
     */
    public Project changeProjectOwner(ProjectDTO projectDto) throws ProjectNotFoundException, UserNotFoundException, NullPointerException {
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
        else {
            if (hasSpecialPermission(projectToEdit, newOwner)) {
                projectDao.removeSpecialPermission(projectToEdit, newOwner);
            }
            projectDao.addProjectMember(projectToEdit, oldOwner);
            return projectDao.changeProjectOwner(projectToEdit, newOwner);
        }
    }

    /**
     * This method takes the DTO from the controller and sends project and user to the DAO
     * @param projectDto The ProjectDTO object used to pass data
     * @return The resulting Project object after it has been modified
     */
    public Project addMemberToProject(ProjectDTO projectDto) {
        Project thisProject = projectDao.getProjectById(projectDto.getProjectId());
        User thisUser = userDao.getUserById(projectDto.getUserId());
        return projectDao.addProjectMember(thisProject, thisUser);
    }

    /**
     * Helper method used to turn a ProjectDTO into a Project.
     * @param projectDto The ProjectDTO object used to pass data
     * @return The created Project object
     */
    private Project parseProjectDTO(ProjectDTO projectDto) {
        Project newProject = new Project();

        String projectName = projectDto.getProjectName();
        if (!projectName.isEmpty()) {
            if (projectDao.doesNameExist(projectName)) {
                throw new ProjectNameExistsException(projectName);
            }
            else {
                newProject.setProjectName(projectName);
            }
        }
        Boolean isPrivate = projectDto.getIsPrivate();
        if (isPrivate == null) {
            newProject.setIsPrivate(false);
        } else {
            newProject.setIsPrivate(isPrivate);
        }
        Date creation = projectDto.getCreation();
        if (creation != null) {
            newProject.setCreation(creation);
        }

        MyUserDetails loggedInUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        newProject.setOwner(userDao.getUserByEmail(loggedInUserDetails.getUsername()));

        return newProject;
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
