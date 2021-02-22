package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class ProjectDTO {

    private UUID projectId;
    private String projectName;
    private Boolean isPrivate;
    private Date creation;
    private User owner;

}
