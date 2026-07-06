package org.example.tnal_youth_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.model.request.LoginRequest;
import org.example.tnal_youth_backend.model.response.LoginResponse;
import org.example.tnal_youth_backend.service.AuthService;
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