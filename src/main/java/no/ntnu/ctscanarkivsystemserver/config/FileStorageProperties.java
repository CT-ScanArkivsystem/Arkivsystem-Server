package no.ntnu.ctscanarkivsystemserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * This class helps to get the filepath from the application.properties.
 * @author Rajeev Singh, trymv
 * @source https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
 */
@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;
    private String documentDir;
    private String imageDir;
    private String logDir;
    private String dicomDir;
    private String tiffDir;
    private List<String> directories;

    private String user;
    private String pass;
    private String domain;
    private String url;
}