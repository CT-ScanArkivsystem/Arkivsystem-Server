package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is the model class for a file
 * @author TrymV
 */
@Data
@NoArgsConstructor
@Entity(name = "files")
@NamedQuery(name = File.FIND_FILE_BY_NAME_AND_PROJECT, query =
        "SELECT f FROM files f WHERE f.fileName LIKE: fileName AND f.inProject.projectId =: projectId AND f.subFolder LIKE: subFolder")
public class File {
    public static final String FIND_FILE_BY_NAME_AND_PROJECT = "File.findFileByNameAndProject";

    @Id
    @Column(name="file_id")
    private UUID fileId;

    @Column(name="file_name")
    private String fileName;

    @Column(name="sub_folder")
    private String subFolder;

    @ManyToOne
    @JoinColumn(
            name="in_project",
            referencedColumnName = "project_id"
    )
    private Project inProject;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "file_tags",
            joinColumns = @JoinColumn(
                    name = "file_id",
                    referencedColumnName = "file_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "tag_name",
                    referencedColumnName = "tag_name"))
    private List<Tag> tags = new ArrayList<>();

    /**
     *
     * @param fileName
     * @param project
     */
    public File(String fileName, String subFolder, Project project) {
        this.fileId = UUID.randomUUID();
        this.fileName = fileName;
        this.subFolder = subFolder;
        this.inProject = project;
    }
}
