package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.ProjectDao;
import no.ntnu.ctscanarkivsystemserver.dao.ProjectDaoClass;
import no.ntnu.ctscanarkivsystemserver.exception.ProjectNameExistsException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This class handles the business logic related to projects
 * @author Brage
 */
@Service
public class ProjectService {

    private final ProjectDao projectDao;

    @Autowired
    public ProjectService(@Qualifier("postgreSQL1") ProjectDao projectDao) {
        this.projectDao = projectDao;
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
        Project newProject = new Project(project.getProjectName(), project.getIsPrivate());
        return projectDao.createProject(newProject);
    }

}
