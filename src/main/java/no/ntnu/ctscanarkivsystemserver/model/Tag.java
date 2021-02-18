package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Class for representing project tags
 * @author Brage
 */
@Entity(name = "tags")
@Data
@NoArgsConstructor
public class Tag {

    @Id
    @Column(name="tag_name")
    private String tagName;
}
