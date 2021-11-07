package no.ntnu.ctscanarkivsystemserver;

import com.sun.research.ws.wadl.Application;
import no.ntnu.ctscanarkivsystemserver.api.AuthController;
import no.ntnu.ctscanarkivsystemserver.config.FileStorageProperties;
import no.ntnu.ctscanarkivsystemserver.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class,
        Properties.class
})
public class CtScanArkivsystemServerApplication {

    private static ConfigurableApplicationContext context;
    private static Logger logger = LoggerFactory.getLogger(CtScanArkivsystemServerApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC+1"));
        System.out.println("Starting CT-Scan-Application!! :D");
        logger.info("Starting CT-Scan-Application!! :D");
        SpringApplication.run(CtScanArkivsystemServerApplication.class, args);
    }

    /**
     * Restarts the server.
     */
    public static void restart() {
        System.out.println("Server is restarting!");
        logger.info("Server is restarting!");
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(Application.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }
}
