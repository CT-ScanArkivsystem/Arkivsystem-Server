package no.ntnu.ctscanarkivsystemserver.model;

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
@NamedQuery(name = Project.FIND_ALL_NAMES, query = "SELECT p FROM projects p ORDER BY p.projectName")
public class Project {
    public static final String FIND_ALL_NAMES = "Project.findAllNames";

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

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "project_tags",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "tag_name",
                    referencedColumnName = "tag_name"))
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name="project_special_permission",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"))
    private List<User> usersWithSpecialPermission = new ArrayList<>();


    /**
     * Constructor for the Project class.
     * @param projectName The name of this project
     * @param isPrivate If this project is private
     */
    public Project(String projectName, Boolean isPrivate) {
        this.projectId = UUID.randomUUID();
        this.projectName = projectName;
        this.isPrivate = isPrivate;
    }
}
