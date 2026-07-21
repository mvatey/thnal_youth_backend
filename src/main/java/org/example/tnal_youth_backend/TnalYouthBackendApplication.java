package org.example.tnal_youth_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TnalYouthBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TnalYouthBackendApplication.class, args);
    }

}
