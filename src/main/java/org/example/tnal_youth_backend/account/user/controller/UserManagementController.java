package org.example.tnal_youth_backend.account.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.user.dto.request.CreateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.request.UpdateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.response.UserResponse;
import org.example.tnal_youth_backend.account.user.service.UserManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService
            userManagementService;

    @GetMapping
    public ResponseEntity<List<UserResponse>>
    getAllUsers() {

        return ResponseEntity.ok(
                userManagementService.getAllUsers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse>
    getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userManagementService.getUserById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse>
    createUser(
            @Valid
            @RequestBody
            CreateUserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userManagementService
                                .createUser(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse>
    updateUser(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateUserRequest request
    ) {
        return ResponseEntity.ok(
                userManagementService.updateUser(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteUser(
            @PathVariable Long id
    ) {
        userManagementService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}