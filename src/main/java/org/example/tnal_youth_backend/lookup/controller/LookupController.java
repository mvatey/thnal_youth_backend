package org.example.tnal_youth_backend.lookup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.lookup.dto.*;
import org.example.tnal_youth_backend.lookup.service.LookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
@Tag(name = "A. Lookup Options")
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/branches")
    public ResponseEntity<
            List<LookupOptionResponse<Long>>
            > getBranchOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getBranchOptions()
        );
    }

    @GetMapping("/member-statuses")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getMemberStatusOptions() {
        return ResponseEntity.ok(
                lookupService.getMemberStatusOptions()
        );
    }

    @GetMapping("/genders")
    public ResponseEntity<List<GenderOptionResponse>>
    getGenderOptions() {
        return ResponseEntity.ok(
                lookupService.getGenderOptions()
        );
    }

    @GetMapping("/member-levels")
    public ResponseEntity<List<MemberLevelOptionResponse>>
    getMemberLevelOptions() {
        return ResponseEntity.ok(
                lookupService.getMemberLevelOptions()
        );
    }

    @GetMapping("/nationalities")
    public ResponseEntity<List<NationalityOptionResponse>>
    getNationalityOptions() {
        return ResponseEntity.ok(
                lookupService.getNationalityOptions()
        );
    }

    @GetMapping("/user-roles")
    public ResponseEntity<List<RoleOptionResponse>>
    getUserRoleOptions() {
        return ResponseEntity.ok(
                lookupService.getUserRoleOptions()
        );
    }

    @GetMapping("/activity-types")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getActivityTypeOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getActivityTypeOptions()
        );
    }

    @GetMapping("/attendance-statuses")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getAttendanceStatusOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getAttendanceStatusOptions()
        );
    }
}