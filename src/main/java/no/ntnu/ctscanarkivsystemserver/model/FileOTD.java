package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * File object to data.
 * @author TrymV
 */
@Data
@NoArgsConstructor
public class FileOTD {

    private String fineName;

    private List<Tag> fileTags;

    public FileOTD(String fineName, List<Tag> fileTags) {
        this.fileTags = fileTags;
        this.fineName = fineName;
    }
}
