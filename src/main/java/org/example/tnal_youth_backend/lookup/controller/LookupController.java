package org.example.tnal_youth_backend.lookup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.lookup.dto.*;
import org.example.tnal_youth_backend.lookup.service.LookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/branch-levels")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getBranchLevelOptions() {
        return ResponseEntity.ok(lookupService.getBranchLevelOptions());
    }

    @GetMapping("/branch-statuses")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getBranchStatusOptions() {
        return ResponseEntity.ok(lookupService.getBranchStatusOptions());
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getProvinceOptions() {
        return ResponseEntity.ok(lookupService.getProvinceOptions());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<LookupOptionResponse<Integer>>> getDistrictOptions(
            @RequestParam Short provinceId
    ) {
        return ResponseEntity.ok(lookupService.getDistrictOptions(provinceId));
    }

    @GetMapping("/communes")
    public ResponseEntity<List<LookupOptionResponse<Integer>>> getCommuneOptions(
            @RequestParam Integer districtId
    ) {
        return ResponseEntity.ok(lookupService.getCommuneOptions(districtId));
    }

    @GetMapping("/education-levels")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getEducationLevelOptions() {
        return ResponseEntity.ok(lookupService.getEducationLevelOptions());
    }

    @GetMapping("/employment-sectors")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getEmploymentSectorOptions() {
        return ResponseEntity.ok(lookupService.getEmploymentSectorOptions());
    }

    @GetMapping("/proficiency-levels")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getProficiencyLevelOptions() {
        return ResponseEntity.ok(lookupService.getProficiencyLevelOptions());
    }

    @GetMapping("/countries")
    public ResponseEntity<List<LookupOptionResponse<String>>> getCountryOptions() {
        return ResponseEntity.ok(lookupService.getCountryOptions());
    }

    @GetMapping("/activity-types")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getActivityTypeOptions() {
        return ResponseEntity.ok(lookupService.getActivityTypeOptions());
    }

    @GetMapping("/activity-sectors")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getActivitySectorOptions() {
        return ResponseEntity.ok(lookupService.getActivitySectorOptions());
    }

    @GetMapping("/activity-statuses")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getActivityStatusOptions() {
        return ResponseEntity.ok(lookupService.getActivityStatusOptions());
    }
}
