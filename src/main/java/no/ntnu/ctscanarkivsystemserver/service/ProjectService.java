package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.ProjectDao;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;
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
    public ProjectService(@Qualifier("projectDaoRepository") ProjectDao projectDao,
                          @Qualifier("postgreSQL") UserDao userDao) {
        this.projectDao = projectDao;
        this.userDao = userDao;
    }


    /**
     * Method for creating a new project.
     * Checks if another project has this name and throws and exception if necessary
     * @param projectDto The projectDto object passed from the controller.
     * @return The the created project
     */
    public Project createProject(ProjectDTO projectDto, User user) throws NullPointerException {
        if (user != null) {
            Project newProject = parseProjectDTO(projectDto);
            newProject.setOwner(user);
            return projectDao.createProject(newProject);
        } else {
            throw new NullPointerException("'user' cannot be null");
        }

    }

    /**
     * THis methos removes a project from the database using UUID from projectDto.
     * @param projectDto The projectDto object passed from the controller.
     * @return True if project has been removes, false if not
     * @throws NullPointerException If projectDto is null
     * @throws ProjectNotFoundException
     */
    public boolean deleteProject(ProjectDTO projectDto) throws NullPointerException, ProjectNotFoundException{
        if (projectDto == null) {
            throw new NullPointerException("ERROR: projectDto is null");
        }
        Project projectToDelete = projectDao.getProjectById(projectDto.getProjectId());
        if (!projectDao.doesProjectExist(projectDto.getProjectId())) {
            System.out.println("WHYYYYYYYYYYYYYYYYY????????");
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
     * This method is used to change the owner of a product.
     * It also moves the old owner to project_members.
     * If the new owner is already in special_permissions, he is removed from there
     * @param projectDto The ProjectDTO object used to pass data
     * @return the resulting Project object after it has been modified
     * @throws NullPointerException if projectDto is null
     * @throws ProjectNotFoundException If a project with this UUID does not exist
     * @throws UserNotFoundException If a user with this UUID does not exist
     */
    public Project changeProjectOwner(ProjectDTO projectDto, User user) throws ProjectNotFoundException, UserNotFoundException,
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
        else if (!canUserModify(projectToEdit, user)) {
            throw new ForbiddenException("The logged on user is not allowed to change owner of this project");
        }
        else {
            if (hasSpecialPermission(projectToEdit, newOwner)) {
                projectDao.removeSpecialPermission(projectToEdit, newOwner);
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
     * @param projectDto The ProjectDTO object used to pass data
     * @param user The logged in user
     * @return The resulting Project object after it has been modified
     * @throws ForbiddenException If user is not allowed to add members
     */
    public Project addMemberToProject(ProjectDTO projectDto, User user) throws ForbiddenException {
        Project thisProject = projectDao.getProjectById(projectDto.getProjectId());
        User thisUser = userDao.getUserById(projectDto.getUserId());
        if (canUserModify(thisProject, user)) {
            return projectDao.addProjectMember(thisProject, thisUser);
        } else {
            throw new ForbiddenException("The logged on user is not allowed to add members to this project");
        }


    }

    /**
     * This method takes the DTO from the controller and sends project and user to the DAO
     * @param projectDto The ProjectDTO object used to pass data
     * @param user The logged in user
     * @return The resulting Project object after it has been modified
     * @throws ForbiddenException If user is not allowed to add members
     */
    public Project removeMemberFromProject(ProjectDTO projectDto, User user) throws ForbiddenException {
        Project thisProject = projectDao.getProjectById(projectDto.getProjectId());
        User thisUser = userDao.getUserById(projectDto.getUserId());
        if (canUserModify(thisProject, user)) {
            return projectDao.removeProjectMember(thisProject, thisUser);
        } else {
            throw new ForbiddenException("The logged on user is not allowed to remove members from this project");
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
        Date creation = projectDto.getCreation();
        if (creation != null) {
            newProject.setCreation(creation);
        }

        return newProject;
    }

    /**
     * Checks if the new owner already has special permissions.
     * @param project The project
     * @param newOwner The new owner
     * @return True if he has special permissions, false otherwise
     */
    private boolean hasSpecialPermission(Project project, User newOwner) {
        return project.getUsersWithSpecialPermission().contains(newOwner);
    }

    private boolean canUserModify(Project project, User user) {
        return project.getOwner().equals(user) || user.getRoles().get(0).getRoleName().equals("ROLE_" + Role.ADMIN);
    }
}
