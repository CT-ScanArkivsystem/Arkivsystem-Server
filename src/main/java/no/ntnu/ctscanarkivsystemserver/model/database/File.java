package no.ntnu.ctscanarkivsystemserver.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is the model class for a file.
 * The purpose of this class is to hold on information so the system can get tags a file in the file-server is
 * associated with. For this the system needs to know what project the file is associated with and what sub project
 * folder the file is in.
 * @author TrymV
 */
@Data
@NoArgsConstructor
@Entity(name = "files")
@NamedQuery(name = File.FIND_FILE_BY_NAME_AND_PROJECT, query =
        "SELECT f FROM files f WHERE f.fileName LIKE: fileName AND f.inProject.projectId =: projectId AND f.subFolder LIKE: subFolder")
@NamedQuery(name = File.FIND_FILE_BY_PROJECT, query = "SELECT f FROM files f WHERE f.inProject.projectId =:projectId")
public class File {
    public static final String FIND_FILE_BY_NAME_AND_PROJECT = "File.findFileByNameAndProject";
    public static final String FIND_FILE_BY_PROJECT = "File.findFileProject";

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
     * The constructor of file.
     * @param fileName name of file in the file server. Including file type. Example: MyFile.txt
     * @param subFolder sub project folder the file is in.
     * @param project project file is associated with.
     */
    public File(String fileName, String subFolder, Project project) {
        this.fileId = UUID.randomUUID();
        this.fileName = fileName;
        this.subFolder = subFolder;
        this.inProject = project;
    }
}
