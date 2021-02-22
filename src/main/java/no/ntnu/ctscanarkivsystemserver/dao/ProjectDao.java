package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * The data access service for the Project database.
 * This is the class that interacts directly with the database.
 * @author Brage
 */
@Repository("postgreSQL1")
public class ProjectDao {

    @PersistenceContext
    EntityManager em;


    /**
     * Method to create a new project.
     * Takes the object that is passed from the ProjectService class and saves it to the database
     * using EntityManager.
     * @param newProject The project that will be saved to the database
     * @return The project
     */
    @Transactional
    public Project createProject(Project newProject) {
        em.persist(newProject);
        em.flush();
        return newProject;
    }

    /**
     * This method runs the SQL query used to get all projects.
     * @return The list
     */
    public List<Project> getAllProjects() {
        Query query = em.createNamedQuery(Project.FIND_ALL_PROJECTS);
        return query.getResultList();
    }

    /**
     * Gets a project from the database with a UUID.
     * @param id UUID of project to find.
     * @return project with UUID.
     */
    public Project getProjectById(UUID id) {
        Query query = em.createNamedQuery(Project.FIND_PROJECT_BY_ID);
        if(id == null) {
            return null;
        }
        query.setParameter("id", id);
        List<Project> queryResult = query.getResultList();
        if(queryResult.size() == 1) {
            System.out.println("Found a project with id: " + id);
            return queryResult.get(0);
        } else {
            System.out.println("Found no project with id: " + id);
            return null;
        }
    }

    /**
     * Helper method to check if this another project in the database already has this name.
     * @param name The project name we want to check for.
     * @return True if another project already has this name, false otherwise.
     */
    public boolean doesNameExist(String name) {
        Query query = em.createNamedQuery(Project.FIND_PROJECTS_BY_NAME);
        System.out.println("DOESNAMEEXIST 1");
        query.setParameter("projectName", name);
        System.out.println("DOESNAMEEXIST 2");
        List<Project> queryResult = query.getResultList();
        return !queryResult.isEmpty();
    }




}
