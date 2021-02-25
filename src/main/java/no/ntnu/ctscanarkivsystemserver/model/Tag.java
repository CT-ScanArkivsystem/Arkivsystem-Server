package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * Class for representing project tags
 * @author Brage
 */
@Entity(name = "tags")
@Data
@NoArgsConstructor
@NamedQuery(name = Tag.FIND_TAG_BY_NAME, query = "SELECT t FROM tags t WHERE t.tagName LIKE: tagName")
public class Tag {
    public static final String FIND_TAG_BY_NAME = "Tag.findTagByName";

    @Id
    @Column(name="tag_name")
    private String tagName;

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
