package no.ntnu.ctscanarkivsystemserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectSearchResult {
    private String projectName;
    private UUID projectId;
    private boolean isPrivate;
    private Date creation;
    private String ownerName;
    private List<String> resultInfo;
}
