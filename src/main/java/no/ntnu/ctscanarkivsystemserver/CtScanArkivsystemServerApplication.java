package no.ntnu.ctscanarkivsystemserver;

import no.ntnu.ctscanarkivsystemserver.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class CtScanArkivsystemServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC+1"));
        System.out.println("Starting CT-Scan-Application!! :D");
        SpringApplication.run(CtScanArkivsystemServerApplication.class, args);
    }
}