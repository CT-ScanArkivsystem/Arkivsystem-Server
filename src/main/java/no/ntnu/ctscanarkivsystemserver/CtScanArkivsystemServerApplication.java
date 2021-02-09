package no.ntnu.ctscanarkivsystemserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CtScanArkivsystemServerApplication {

    public static void main(String[] args) {
        System.out.println("Starting CT-Scan-Application!! :O");
        SpringApplication.run(CtScanArkivsystemServerApplication.class, args);
    }

}