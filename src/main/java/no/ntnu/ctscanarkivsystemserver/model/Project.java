package no.ntnu.ctscanarkivsystemserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@NamedQuery(name = Project.FIND_ALL_PROJECTS, query = "SELECT p FROM projects p ORDER BY p.projectName")
@NamedQuery(name = Project.FIND_PROJECTS_BY_NAME, query = "SELECT p FROM projects p WHERE p.projectName LIKE: projectName")
@NamedQuery(name = Project.FIND_PROJECTS_BY_UUID, query = "SELECT p FROM projects p WHERE p.projectId =: projectId")

public class Project {

    public static final String FIND_ALL_PROJECTS = "Project.findAllNames";
    public static final String FIND_PROJECTS_BY_NAME = "Project.findProjectsByName";
    public static final String FIND_PROJECTS_BY_UUID = "Project.findProjectsByUUID";

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

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "project_tags",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "tag_name",
                    referencedColumnName = "tag_name"))
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name="project_special_permission",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"))
    private List<User> usersWithSpecialPermission = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name="project_members",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"))
    private List<User> projectMembers = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(
            name="owner",
            referencedColumnName = "user_id"
    )
    private User owner;


    /**
     * Constructor for the Project class.
     * @param projectName The name of this project
     * @param isPrivate If this project is private or not
     * @param user The user which is the owner of the project
     * @param date The creation date of the project
     */
    public Project(String projectName, Boolean isPrivate, User user, Date date) {
        this.projectId = UUID.randomUUID();
        this.projectName = projectName;
        this.isPrivate = isPrivate;
        this.owner = user;
        this.creation = date;
    }
}
