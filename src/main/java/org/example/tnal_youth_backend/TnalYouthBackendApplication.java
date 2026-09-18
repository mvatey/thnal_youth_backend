package org.example.tnal_youth_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class TnalYouthBackendApplication {

    public static void main(String[] args) {
        // The JVM's default zone (used by every OffsetDateTime.now()/LocalDateTime.now()
        // call across the app, e.g. Document#onCreate) otherwise falls back to whatever
        // the host OS defaults to -- UTC on both the AWS instance and a fresh Windows box.
        // Set explicitly, and before Spring starts, so it's correct regardless of host.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
        SpringApplication.run(TnalYouthBackendApplication.class, args);
    }
}
