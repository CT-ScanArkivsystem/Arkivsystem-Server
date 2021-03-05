package no.ntnu.ctscanarkivsystemserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing project tags
 * @author Brage
 */
@Entity(name = "tags")
@Data
@NoArgsConstructor
@NamedQuery(name = Tag.FIND_TAG_BY_NAME, query = "SELECT t FROM tags t WHERE t.tagName LIKE: tagName")
@NamedQuery(name = Tag.FIND_ALL_TAGS, query = "SELECT t FROM tags t ORDER BY t.tagName")
public class Tag {
    public static final String FIND_TAG_BY_NAME = "Tag.findTagByName";
    public static final String FIND_ALL_TAGS = "Tag.findAllTags";

    @Id
    @Column(name="tag_name")
    private String tagName;

    //This variable has to be set and will be 0 as default.
    @Transient
    private int numberOfProjects;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Project> projects = new ArrayList<>();

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
