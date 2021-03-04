package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
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

    /**
     * This method checks if a project with this uuid already exists.
     * @param uuid The uuid you want to look for
     * @return True if if does exist, false if not
     */
    public boolean doesProjectExist(UUID uuid) {
        Query query = em.createNamedQuery(Project.FIND_PROJECTS_BY_UUID);
        query.setParameter("projectId", uuid);
        List<Project> queryResult = query.getResultList();
        return !queryResult.isEmpty();
    }

    /**
     * Gets a project from the database with a UUID.
     * @param uuid UUID of project to find.
     * @return project with UUID.
     * @throws ProjectNotFoundException if no project was found.
     */
    public Project getProjectById(UUID uuid) throws ProjectNotFoundException {
        Query query = em.createNamedQuery(Project.FIND_PROJECTS_BY_UUID);
        if(uuid == null) {
            return null;
        }
        query.setParameter("projectId", uuid);
        List<Project> queryResult = query.getResultList();
        if(queryResult.size() == 1) {
            System.out.println("Found a project with id: " + uuid);
            return (Project) query.getResultList().get(0);
        } else {
            System.out.println("Found no project with id: " + uuid);
            throw new ProjectNotFoundException(uuid);
        }
    }

    /**
     * This method uses the Entity Manager to modify and save the changes in the database
     * @param inputProject The project that will be changed
     * @param newOwner The owner you want to set instead
     * @return The modified Project
     */
    @Transactional
    public Project changeProjectOwner(Project inputProject, User newOwner) {
        System.out.println("ProjectDao: attempting to set newOwner as owner");
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.setOwner(newOwner);
        return saveProject(inputProject);
    }

    /**
     * This method uses the entity manager to remove a user from special-permission.
     * @param inputProject The project you want to modify
     * @param newOwner The user you want to remove
     * @return The modified project
     */
    @Transactional
    public Project removeSpecialPermission(Project inputProject, User newOwner) {
        System.out.println("ProjectDao: removing newOwner from special permissions");
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getUsersWithSpecialPermission().remove(newOwner);
        return saveProject(inputProject);
    }

    /**
     * This method uses the entity manager to add a user to project_members.
     * @param inputProject The project you want to modify
     * @param user The user you want to add
     * @return The modified project
     */
    @Transactional
    public Project addProjectMember(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getProjectMembers().add(user);
        return saveProject(inputProject);
    }

    /**
     * This method uses the entity manager to remove a user from project_members.
     * @param inputProject The project you want to modify
     * @param user The user you want to remove
     * @return The modified project
     */
    @Transactional
    public Project removeProjectMember(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getProjectMembers().remove(user);
        return saveProject(inputProject);
    }

    /**
     * Adds a tag to a project.
     * @param project project for tag to be added to.
     * @param tag tag to be added to project.
     * @return project if successful. Null if something went wrong.
     */
    @Transactional
    public Project addProjectTag(Project project, Tag tag) {
        em.refresh(project);
        prepareProjectForEdit(project);
        project.getTags().add(tag);
        return saveProject(project);
    }

    /**
     * Removes a tag from a project.
     * @param project project for tag to be removed from.
     * @param tag tag to be removed.
     * @return project if successful. Null if something went wrong.
     */
    @Transactional
    public Project removeProjectTag(Project project, Tag tag) {
        em.refresh(project);
        prepareProjectForEdit(project);
        project.getTags().remove(tag);
        return saveProject(project);
    }



    /**
     * Helper method for preparing the database and locking the entry that will be modified
     * @param project the project you want to change
     */
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

    /**
     * Helper method for saving the database
     * @param projectToSave The project that you want to change
     * @return
     */
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
