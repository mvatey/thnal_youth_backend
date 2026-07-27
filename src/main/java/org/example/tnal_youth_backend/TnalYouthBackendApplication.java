package org.example.tnal_youth_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class TnalYouthBackendApplication {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(TnalYouthBackendApplication.class, args);
    }

    @PostConstruct
    public void generateTestPassword() {
        System.out.println("==================================");
        System.out.println("Password: 12345");
        System.out.println("BCrypt : " + passwordEncoder.encode("12345"));
        System.out.println("==================================");
    }
}