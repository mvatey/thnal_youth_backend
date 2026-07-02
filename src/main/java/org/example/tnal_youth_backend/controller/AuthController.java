package org.example.tnal_youth_backend.controller;



import org.example.tnal_youth_backend.model.request.LoginRequest;
import org.example.tnal_youth_backend.model.response.LoginResponse;
import org.example.tnal_youth_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody LoginRequest request){

        return ResponseEntity.ok(
                authService.login(request));
    }

}