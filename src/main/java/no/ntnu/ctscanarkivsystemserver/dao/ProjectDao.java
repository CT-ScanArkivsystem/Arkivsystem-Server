package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
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
@Repository("projectDaoRepository")
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

    public boolean doesProjectExist(UUID uuid) {
        Query query = em.createNamedQuery(Project.FIND_PROJECTS_BY_UUID);
        query.setParameter("projectId", uuid);
        List<Project> queryResult = query.getResultList();
        return !queryResult.isEmpty();
    }

    public Project getProjectById(UUID uuid) {
        Query query = em.createNamedQuery(Project.FIND_PROJECTS_BY_UUID);
        query.setParameter("projectId", uuid);
        List<Project> queryResult = query.getResultList();
        if (queryResult.size()==1) {
            return (Project) query.getResultList().get(0);
        } else {
            System.out.println("ERROR: This is not possible");
            return null;
        }
    }

    public Project changeProjectOwner(Project inputProject, User newOwner) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);

        inputProject.setOwner(newOwner);

        return saveProject(inputProject);
    }


    private void prepareProjectForEdit(Project project) {
        System.out.println("user getting ready for edit.");
        if(project != null) {
            try {
                em.lock(project, LockModeType.PESSIMISTIC_WRITE);
            } catch (Exception e) {
                System.out.println("Exception in prepare project for edit: " + e.getMessage());
            }
        }
    }

    private Project saveProject(Project projectToSave) {
        System.out.println("Trying to save user.");
        if(projectToSave != null) {
            try {
                em.merge(projectToSave);
                em.lock(projectToSave, LockModeType.NONE);
                em.flush();
                return projectToSave;
            } catch (Exception e) {
                System.out.println("Exception in save user: " + e.getMessage());
            }
        }
        return null;
    }






}
