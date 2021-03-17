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
public class File {

    @Id
    @Column(name="file_id")
    private UUID fileId;

    @Column(name="file_name")
    private String fileName;

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
}
