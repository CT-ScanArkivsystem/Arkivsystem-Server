package no.ntnu.ctscanarkivsystemserver.model.database;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * This is the model class for projects.
 * @author Brage
 */
@Entity(name = "projects")
@Data
@NamedQuery(name = Project.FIND_ALL_PROJECTS, query = "SELECT p FROM projects p ORDER BY p.projectName")
@NamedQuery(name = Project.FIND_PROJECTS_BY_NAME, query = "SELECT p FROM projects p WHERE p.projectName LIKE: projectName")
@NamedQuery(name = Project.FIND_PROJECTS_BY_UUID, query = "SELECT p FROM projects p WHERE p.projectId =: projectId")
@NamedQuery(name = Project.FIND_MY_PROJECTS, query = "SELECT p FROM projects p left JOIN p.projectMembers pm WHERE p.owner.userId =: id OR pm.userId =:id")
public class Project {

    public static final String FIND_ALL_PROJECTS = "Project.findAllNames";
    public static final String FIND_PROJECTS_BY_NAME = "Project.findProjectsByName";
    public static final String FIND_PROJECTS_BY_UUID = "Project.findProjectsByUUID";
    public static final String FIND_MY_PROJECTS = "Project.findMyProjects";

    @Id
    @Column(name="project_id")
    private UUID projectId;

    @Column(name="project_name")
    private String projectName;

    @Column(name="private")
    private Boolean isPrivate;

    @Temporal(TemporalType.DATE)
    @Column(name="creation")
    private Date creation;

    @Column(name="description")
    private String description;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "project_tags",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "tag_name",
                    referencedColumnName = "tag_name"))
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name="project_special_permission",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"))
    private List<User> usersWithSpecialPermission = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name="project_members",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"))
    private List<User> projectMembers = new ArrayList<>();

    @ManyToOne
    @JoinColumn(
            name="owner",
            referencedColumnName = "user_id"
    )
    private User owner;

    public Project() {
        this.projectId =  UUID.randomUUID();
    }

    /**
     * Constructor for the Project class.
     * @param projectName The name of this project
     * @param isPrivate If this project is private or not
     * @param date The creation date of the project
     */
    public Project(String projectName, Boolean isPrivate, Date date) {
        this.projectId = UUID.randomUUID();
        this.projectName = projectName;
        this.isPrivate = isPrivate;
        this.creation = date;
    }

}
