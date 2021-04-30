package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.ntnu.ctscanarkivsystemserver.model.database.Tag;

import java.util.List;

/**
 * File object to data.
 * @author TrymV
 */
@Data
@NoArgsConstructor
public class FileOTD {

    private String fileName;

    private List<Tag> fileTags;

    public FileOTD(String fileName, List<Tag> fileTags) {
        this.fileTags = fileTags;
        this.fileName = fileName;
    }
}
