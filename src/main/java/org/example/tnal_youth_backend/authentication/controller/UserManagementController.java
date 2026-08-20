package org.example.tnal_youth_backend.authentication.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.authentication.model.request.CreateUserRequest;
import org.example.tnal_youth_backend.authentication.model.response.UserListItemResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserSummaryResponse;

import org.example.tnal_youth_backend.authentication.service.UserManagementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Official admin-only user account management API.
 *
 * /api/admin/users is the single endpoint family for managing
 * login accounts. It lets ADMIN list every login account and
 * create standalone accounts for any application role while member-linked
 * accounts continue to be provisioned by the Member flow.
 *
 * Security is enforced both by SecurityConfig (/api/admin/**)
 * and by the class-level @PreAuthorize below.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "A. User Administration",
        description = "List all login accounts and create standalone accounts for any application role"
)
public class UserManagementController {

    private final UserManagementService userManagementService;


    /*
     * ==========================================================
     * SUMMARY CARDS
     * ==========================================================
     *
     * GET /api/admin/users/summary
     */

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
    public ResponseEntity<UserSummaryResponse> getSummary() {

        return ResponseEntity.ok(
                userManagementService.getSummary()
        );
    }


    /*
     * ==========================================================
     * LIST USERS
     * ==========================================================
     *
     * GET /api/admin/users
     * GET /api/admin/users?search=jay&role=VIEWER&status=ACTIVE
     */

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
    public ResponseEntity<List<UserListItemResponse>> listUsers(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String role,

            @RequestParam(required = false)
            String status
    ) {

        return ResponseEntity.ok(
                userManagementService.listUsers(
                        search,
                        role,
                        status
                )
        );
    }


    /*
     * ==========================================================
     * CREATE USER
     * ==========================================================
     *
     * POST /api/admin/users
     */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserListItemResponse> createUser(
            @Valid
            @RequestBody
            CreateUserRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userManagementService.createUser(request)
                );
    }
}
