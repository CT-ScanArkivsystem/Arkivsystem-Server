package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.database.Project;
import no.ntnu.ctscanarkivsystemserver.model.database.Tag;
import no.ntnu.ctscanarkivsystemserver.model.database.User;

import java.util.List;
import java.util.UUID;

/**
 * @author TrymV
 */
public interface ProjectDao {
    Project createProject(Project newProject);

    boolean deleteProject(Project project);

    List<Project> getAllProjects();

    boolean doesNameExist(String name);

    boolean doesProjectExist(UUID uuid);

    Project getProjectById(UUID uuid);

    List<Project> getMyProjects(UUID userId);

    boolean changeProjectOwner(Project project, User newOwner);

    boolean revokeSpecialPermission(Project inputProject, User user);

    boolean grantSpecialPermission(Project inputProject, User user);

    boolean addProjectMember(Project inputProject, User user);

    boolean removeProjectMember(Project inputProject, User user);

    Project addProjectTag(Project project, List<Tag> tags);

    Project removeProjectTag(Project project, List<Tag> tags);

    boolean setPrivacy(Project project, boolean privacy);

    String setDescription(Project project, String description);
}
