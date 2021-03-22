package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Data To Object class used for projects
 * @author Brage
 */

@Data
public class ProjectDTO {

    private UUID projectId;
    private String projectName;
    private Boolean isPrivate;
    private Date creation;
    private String description;
    private List<Tag> tags;
    private List<User> usersWithSpecialPermission;
    private List<User> projectMembers;
    private UUID userId;

}
