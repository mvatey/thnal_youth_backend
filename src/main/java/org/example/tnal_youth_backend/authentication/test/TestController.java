package org.example.tnal_youth_backend.authentication.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/auth")
    public String authenticated() {
        return "AUTHENTICATED OK";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "ADMIN OK";
    }

    @GetMapping("/branch")
    @PreAuthorize("hasRole('BRANCH_LEADER')")
    public String branch() {
        return "BRANCH OK";
    }

    @GetMapping("/secretary")
    @PreAuthorize("hasRole('SECRETARY')")
    public String secretary() {
        return "SECRETARY OK";
    }

    @GetMapping("/member")
    @PreAuthorize("hasRole('MEMBER')")
    public String member() {
        return "MEMBER OK";
    }
}