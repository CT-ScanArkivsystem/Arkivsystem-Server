package no.ntnu.ctscanarkivsystemserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "prop")
public class Properties {
    private String jwtKey;
    private String domain;
    private long jwtLifetimeInMin;
    private String port;
}
