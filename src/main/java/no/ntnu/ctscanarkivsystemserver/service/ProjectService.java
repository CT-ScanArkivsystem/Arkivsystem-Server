package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.ProjectDao;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.model.MyUserDetails;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class handles the business logic related to projects
 * @author Brage
 */
@Service
public class ProjectService {

    private final ProjectDao projectDao;
    private final UserDao userDao;

    @Autowired
    public ProjectService(@Qualifier("postgreSQL1") ProjectDao projectDao, @Qualifier("postgreSQL") UserDao userDao) {
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
     * @return The lust of projects
     */
    public List<Project> getAllProjects() {
        List<Project> projectList = projectDao.getAllProjects();
        return projectList;
    }
}
