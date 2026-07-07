package org.example.tnal_youth_backend.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {



        return ResponseEntity.ok(authService.login(request));
    }
}