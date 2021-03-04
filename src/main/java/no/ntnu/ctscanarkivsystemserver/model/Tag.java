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
@NamedQuery(name = Tag.FIND_ALL_TAGS, query = "SELECT t FROM tags t ORDER BY t.tagName")
public class Tag {
    public static final String FIND_TAG_BY_NAME = "Tag.findTagByName";
    public static final String FIND_ALL_TAGS = "Tag.findAllTags";

    @Id
    @Column(name="tag_name")
    private String tagName;

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
