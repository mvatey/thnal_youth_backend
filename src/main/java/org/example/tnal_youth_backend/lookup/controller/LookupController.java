package org.example.tnal_youth_backend.lookup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.lookup.dto.*;
import org.example.tnal_youth_backend.lookup.service.LookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/branches/province-options")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<List<ProvinceOptionResponse>>
    getProvinceOptions() {
        return ResponseEntity.ok(
                lookupService
                        .getProvinceOptions()
        );
    }

    @GetMapping("/activity-invitable-branches")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')")
    public ResponseEntity<List<LookupOptionResponse<Long>>>
    getActivityInvitableBranchOptions() {
        return ResponseEntity.ok(
                lookupService.getActivityInvitableBranchOptions()
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

    @GetMapping("/branch-status-options")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getBranchStatusLookupOptions() {
        return ResponseEntity.ok(lookupService.getBranchStatusOptions());
    }

    @GetMapping("/province-options")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getProvinceLookupOptions() {
        return ResponseEntity.ok(lookupService.getProvinceLookupOptions());
    }

    @GetMapping("/district-options")
    public ResponseEntity<List<LookupOptionResponse<Integer>>> getDistrictOptions(
            @RequestParam Short provinceId
    ) {
        return ResponseEntity.ok(lookupService.getDistrictOptions(provinceId));
    }

    @GetMapping("/commune-options")
    public ResponseEntity<List<LookupOptionResponse<Integer>>> getCommuneOptions(
            @RequestParam Integer districtId
    ) {
        return ResponseEntity.ok(lookupService.getCommuneOptions(districtId));
    }

    @GetMapping("/education-level-options")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getEducationLevelLookupOptions() {
        return ResponseEntity.ok(lookupService.getEducationLevelOptions());
    }

    @GetMapping("/employment-sectors")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getEmploymentSectorOptions() {
        return ResponseEntity.ok(lookupService.getEmploymentSectorOptions());
    }

    @GetMapping("/proficiency-level-options")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getProficiencyLevelLookupOptions() {
        return ResponseEntity.ok(lookupService.getProficiencyLevelOptions());
    }

    @GetMapping("/countries")
    public ResponseEntity<List<LookupOptionResponse<String>>> getCountryOptions() {
        return ResponseEntity.ok(lookupService.getCountryOptions());
    }

    @GetMapping("/activity-type-options")
    public ResponseEntity<List<LookupOptionResponse<Short>>> getActivityTypeLookupOptions() {
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

    @GetMapping("/ethnicities")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getEthnicityOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getEthnicityOptions()
        );
    }

    @GetMapping("/religions")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getReligionOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getReligionOptions()
        );
    }

    @GetMapping("/tshirt-sizes")
    public ResponseEntity<
            List<LookupOptionResponse<String>>
            >
    getTshirtSizeOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getTshirtSizeOptions()
        );
    }

    @GetMapping("/education-levels")
    public ResponseEntity<
            List<LookupOptionResponse<Short>>
            >
    getEducationLevelOptions() {

        return ResponseEntity.ok(
                lookupService
                        .getEducationLevelOptions()
        );
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getLanguages() {
        return ResponseEntity.ok(
                lookupService.getLanguageOptions()
        );
    }

    @GetMapping("/skills")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getSkills() {
        return ResponseEntity.ok(
                lookupService.getSkillOptions()
        );
    }

    @GetMapping("/proficiency-levels")
    public ResponseEntity<List<LookupOptionResponse<Short>>>
    getProficiencyLevels() {
        return ResponseEntity.ok(
                lookupService.getProficiencyLevelOptions()
        );
    }

    @GetMapping("/political-parties")
    public ResponseEntity<
            List<LookupOptionResponse<Short>>
            >
    getPoliticalPartyOptions() {
        return ResponseEntity.ok(
                lookupService
                        .getPoliticalPartyOptions()
        );
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<LocationOptionResponse>>
    getProvinces() {
        return ResponseEntity.ok(
                lookupService.getProvinces()
        );
    }

    @GetMapping("/districts")
    public ResponseEntity<List<LocationOptionResponse>>
    getDistricts(
            @RequestParam
            Short provinceId
    ) {
        return ResponseEntity.ok(
                lookupService.getDistricts(
                        provinceId
                )
        );
    }

    @GetMapping("/communes")
    public ResponseEntity<List<LocationOptionResponse>>
    getCommunes(
            @RequestParam
            Integer districtId
    ) {
        return ResponseEntity.ok(
                lookupService.getCommunes(
                        districtId
                )
        );
    }

    @GetMapping("/branch-statuses")
    public ResponseEntity<List<BranchStatusOptionResponse>>
    getBranchStatuses() {
        return ResponseEntity.ok(
                lookupService
                        .getBranchStatuses()
        );
    }
}
