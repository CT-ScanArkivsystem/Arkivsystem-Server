package no.ntnu.ctscanarkivsystemserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CtScanArkivsystemServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC+1"));
        System.out.println("Starting CT-Scan-Application!! :D");
        SpringApplication.run(CtScanArkivsystemServerApplication.class, args);
    }
}