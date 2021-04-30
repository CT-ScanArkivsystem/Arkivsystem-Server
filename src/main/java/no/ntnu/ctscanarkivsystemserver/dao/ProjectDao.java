package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.exception.ProjectNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.database.Project;
import no.ntnu.ctscanarkivsystemserver.model.database.Tag;
import no.ntnu.ctscanarkivsystemserver.model.database.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.*;

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
     * Method to delete a project from the database
     * Takes the object that is passed from the ProjectService class and removes in from the database
     * using EntityManager.
     * @param project The project you want to delete
     * @return True if it is removed successfully, false if it is not
     * @throws NullPointerException if project parameter is null
     */
    @Transactional
    public boolean deleteProject(Project project) throws NullPointerException {
        if (project != null) {
            em.remove(project);
            em.flush();
            return !doesProjectExist(project.getProjectId());
        }
        else {
            throw new NullPointerException("Project is null");
        }
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
        query.setParameter("projectName", name);
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
            return (Project) query.getResultList().get(0);
        } else {
            System.out.println("Found no project with id: " + uuid);
            throw new ProjectNotFoundException(uuid);
        }
    }

    /**
     * Return a list of all the projects the user owns or are member of.
     * This will also remove all duplicates.
     * @param userId Id of user to get projects from.
     * @return List of all projects user owns or are member of without duplicates.
     */
    public List<Project> getMyProjects(UUID userId) {
        List<Project> myProjects = em.createNamedQuery(Project.FIND_MY_PROJECTS)
                .setParameter("id", userId).getResultList();
        Set<Project> myProjectWithoutDupes = new LinkedHashSet<>(myProjects);
        myProjects.clear();
        myProjects.addAll(myProjectWithoutDupes);
        return myProjects;
    }

    /**
     * This method uses the Entity Manager to modify and save the changes in the database
     * @param project The project that will be changed
     * @param newOwner The owner you want to set instead
     * @return True if new owner is set correctly, false otherwise
     */
    @Transactional
    public boolean changeProjectOwner(Project project, User newOwner) {
        em.refresh(project);
        prepareProjectForEdit(project);
        project.setOwner(newOwner);
        saveProject(project);

        return project.getOwner().getUserId() == newOwner.getUserId();
    }

    /**
     * This method uses the entity manager to remove a user from special-permission.
     * @param inputProject The project you want to modify
     * @param user The user you want to remove
     * @return True if user does not have special-permission anymore, false otherwise
     */
    @Transactional
    public boolean revokeSpecialPermission(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getUsersWithSpecialPermission().remove(user);
        saveProject(inputProject);
        return !inputProject.getUsersWithSpecialPermission().contains(user);
    }

    /**
     * This method uses the entity manager to remove a user from special-permission.
     * @param inputProject The project you want to modify
     * @param user The user you want to add
     * @return True if user now has special-permission, false otherwise
     */
    @Transactional
    public boolean grantSpecialPermission(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getUsersWithSpecialPermission().add(user);
        saveProject(inputProject);
        return inputProject.getUsersWithSpecialPermission().contains(user);
    }

    /**
     * This method uses the entity manager to add a user to project_members.
     * @param inputProject The project you want to modify
     * @param user The user you want to add
     * @return True if member has been added
     */
    @Transactional
    public boolean addProjectMember(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getProjectMembers().add(user);
        saveProject(inputProject);
        return inputProject.getProjectMembers().contains(user);
    }

    /**
     * This method uses the entity manager to remove a user from project_members.
     * @param inputProject The project you want to modify
     * @param user The user you want to remove
     * @return True if member has been removed
     */
    @Transactional
    public boolean removeProjectMember(Project inputProject, User user) {
        em.refresh(inputProject);
        prepareProjectForEdit(inputProject);
        inputProject.getProjectMembers().remove(user);
        saveProject(inputProject);
        return !inputProject.getProjectMembers().contains(user);
    }

    /**
     * Add tags to a project.
     * @param project project for tag to be added to.
     * @param tags tags to be added to project.
     * @return project if successful. Null if something went wrong.
     */
    @Transactional
    public Project addProjectTag(Project project, List<Tag> tags) {
        em.refresh(project);
        prepareProjectForEdit(project);
        for(Tag tag:tags) {
            project.getTags().add(tag);
        }
        return saveProject(project);
    }

    /**
     * Remove tags from a project.
     * @param project project for tag to be removed from.
     * @param tags tags to be removed.
     * @return project if successful. Null if something went wrong.
     */
    @Transactional
    public Project removeProjectTag(Project project, List<Tag> tags) {
        em.refresh(project);
        prepareProjectForEdit(project);
        for(Tag tag:tags) {
            project.getTags().remove(tag);
        }
        return saveProject(project);
    }



    /**
     * Helper method for preparing the database and locking the entry that will be modified
     * @param project the project you want to change
     */
    private void prepareProjectForEdit(Project project) {
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
        if(projectToSave != null) {
            try {
                em.merge(projectToSave);
                em.lock(projectToSave, LockModeType.NONE);
                em.flush();
                return projectToSave;
            } catch (Exception e) {
                System.out.println("Exception in save project: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Set a projects privacy.
     * @param project project to set privacy of.
     * @param privacy true to set project as private else false.
     * @return true if change was successful.
     */
    @Transactional
    public boolean setPrivacy(Project project, boolean privacy) {
        em.refresh(project);
        prepareProjectForEdit(project);
        project.setIsPrivate(privacy);
        project = saveProject(project);
        return project != null;
    }

    /**
     * Set the description of a project.
     * @param project project to set description of.
     * @param description description to set on project.
     * @return description if successful else null.
     */
    @Transactional
    public String setDescription(Project project, String description) {
        em.refresh(project);
        prepareProjectForEdit(project);
        project.setDescription(description);
        project = saveProject(project);
        if(project == null) {
            return null;
        } else {
            return project.getDescription();
        }
    }
}
