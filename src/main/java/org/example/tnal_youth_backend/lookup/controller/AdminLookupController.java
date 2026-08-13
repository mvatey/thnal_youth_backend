package org.example.tnal_youth_backend.lookup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.lookup.dto.variable.AdminLookupResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.CreateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.LookupCategoryResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupStatusRequest;

import org.example.tnal_youth_backend.lookup.service.AdminLookupService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lookups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "A. Variable Administration",
        description = "Manage configurable lookup variables"
)
public class AdminLookupController {

    private final AdminLookupService
            adminLookupService;


    /*
     * ==========================================================
     * GET VARIABLE CATEGORIES
     * ==========================================================
     *
     * GET /api/admin/lookups/categories
     */

    @GetMapping("/categories")
    public ResponseEntity<
            List<LookupCategoryResponse>
            >
    getCategories() {

        return ResponseEntity.ok(
                adminLookupService
                        .getCategories()
        );
    }


    /*
     * ==========================================================
     * GET VARIABLES BY CATEGORY
     * ==========================================================
     *
     * Example:
     *
     * GET /api/admin/lookups/activity-types
     *
     * GET /api/admin/lookups/activity-types
     *      ?search=internal
     *      &status=ACTIVE
     */

    @GetMapping("/{category}")
    public ResponseEntity<
            List<AdminLookupResponse>
            >
    getItems(
            @PathVariable
            String category,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    defaultValue = "ALL"
            )
            String status
    ) {

        return ResponseEntity.ok(
                adminLookupService
                        .getItems(
                                category,
                                search,
                                status
                        )
        );
    }


    /*
     * ==========================================================
     * CREATE VARIABLE
     * ==========================================================
     *
     * POST /api/admin/lookups/activity-types
     */

    @PostMapping("/{category}")
    public ResponseEntity<
            AdminLookupResponse
            >
    createItem(
            @PathVariable
            String category,

            @Valid
            @RequestBody
            CreateLookupRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        adminLookupService
                                .createItem(
                                        category,
                                        request
                                )
                );
    }


    /*
     * ==========================================================
     * UPDATE VARIABLE
     * ==========================================================
     *
     * PUT /api/admin/lookups/activity-types/1
     */

    @PutMapping(
            "/{category}/{id}"
    )
    public ResponseEntity<
            AdminLookupResponse
            >
    updateItem(
            @PathVariable
            String category,

            @PathVariable
            Short id,

            @Valid
            @RequestBody
            UpdateLookupRequest request
    ) {

        return ResponseEntity.ok(
                adminLookupService
                        .updateItem(
                                category,
                                id,
                                request
                        )
        );
    }


    /*
     * ==========================================================
     * UPDATE VARIABLE STATUS
     * ==========================================================
     *
     * PATCH /api/admin/lookups/activity-types/1/status
     */

    @PatchMapping(
            "/{category}/{id}/status"
    )
    public ResponseEntity<
            AdminLookupResponse
            >
    updateStatus(
            @PathVariable
            String category,

            @PathVariable
            Short id,

            @Valid
            @RequestBody
            UpdateLookupStatusRequest request
    ) {

        return ResponseEntity.ok(
                adminLookupService
                        .updateStatus(
                                category,
                                id,
                                request
                        )
        );
    }
}