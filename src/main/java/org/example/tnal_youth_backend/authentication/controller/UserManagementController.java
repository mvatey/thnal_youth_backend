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
 * Admin-only user account management.
 *
 * Lets an ADMIN create branch-less accounts (ADMIN or VIEWER role,
 * users.member_id stays NULL) and view the full user list/summary.
 *
 * Gated both by the URL-level "/api/admin/**" -> hasRole('ADMIN')
 * rule in SecurityConfig and, defensively, by the class-level
 * @PreAuthorize below.
 */
/*
 * NOTE ON THE BEAN NAME:
 * There is a pre-existing, unrelated class also named
 * "UserManagementController" in
 * org.example.tnal_youth_backend.account.user.controller
 * (a generic, currently ungated /api/users CRUD endpoint).
 * Spring registers beans under their default (decapitalized
 * simple class) name regardless of package, so two classes
 * named UserManagementController in different packages collide
 * at startup unless one of them is given an explicit bean name.
 * This controller is intentionally given one below so both
 * classes can coexist unchanged.
 */
@RestController("adminUserManagementController")
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "A. User Administration",
        description = "Manage non-branch-linked user accounts (ADMIN, VIEWER)"
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
