package no.ntnu.ctscanarkivsystemserver.config;

import no.ntnu.ctscanarkivsystemserver.api.AuthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//This class was taken from this guide: https://www.youtube.com/watch?v=PovQ6aUeRgg
@Configuration
public class CorsConfig {

    private final String domain;
    private final String port;
    Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Autowired
    public CorsConfig(Properties properties) {
        this.domain = properties.getDomain();
        this.port = properties.getPort();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        logger.info("CorsConfig.corsConfigurer() was called."
                        + "\n       domain: " + domain
                        + "\n       port: " + port);
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // TODO: When the connection becomes secure (HTTPS), change the IP to include an s!
                        .allowedOrigins("http://" + domain + ":" + port, "http://127.0.0.1:3001")
                        .exposedHeaders("Set-Cookie")
                        .allowCredentials(true);
            }
        };
    }
}
