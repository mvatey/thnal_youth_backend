package org.example.tnal_youth_backend.lookup.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.attendance.repository.AttendanceStatusRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityTypeRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.lookup.dto.*;
import org.example.tnal_youth_backend.lookup.repository.BranchStatusRepository;
import org.example.tnal_youth_backend.lookup.repository.CommuneRepository;
import org.example.tnal_youth_backend.lookup.repository.DistrictRepository;
import org.example.tnal_youth_backend.lookup.repository.ProvinceRepository;
import org.example.tnal_youth_backend.lookup.service.LookupService;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.education.repository.EducationLevelRepository;
import org.example.tnal_youth_backend.member.language.repository.LanguageRepository;
import org.example.tnal_youth_backend.member.language.repository.MemberLanguageRepository;
import org.example.tnal_youth_backend.member.level.service.MemberLevelService;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;
import org.example.tnal_youth_backend.member.nationality.service.NationalityService;
import org.example.tnal_youth_backend.member.politicalaffiliation.repository.PoliticalPartyRepository;
import org.example.tnal_youth_backend.member.proficiency.repository.ProficiencyLevelRepository;
import org.example.tnal_youth_backend.member.skill.repository.MemberSkillRepository;
import org.example.tnal_youth_backend.member.skill.repository.SkillRepository;
import org.example.tnal_youth_backend.member.status.service.MemberStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;

import java.util.Arrays;
import java.util.List;

import static org.example.tnal_youth_backend.authentication.security.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
public class LookupServiceImpl
        implements LookupService {

    private final BranchService branchService;
    private final MemberStatusService memberStatusService;
    private final MemberLevelService memberLevelService;
    private final NationalityService nationalityService;
    private final UserRepository userRepository;
    private final ActivityTypeRepository
            activityTypeRepository;

    private final AttendanceStatusRepository
            attendanceStatusRepository;

    private final EthnicityRepository
            ethnicityRepository;

    private final ReligionRepository
            religionRepository;

    private final EducationLevelRepository
            educationLevelRepository;

    private final LanguageRepository languageRepository;
    private final SkillRepository skillRepository;
    private final ProficiencyLevelRepository proficiencyLevelRepository;
    private final PoliticalPartyRepository
            politicalPartyRepository;

    private final ProvinceRepository
            provinceRepository;

    private final DistrictRepository
            districtRepository;

    private final CommuneRepository
            communeRepository;

    private final BranchStatusRepository
            branchStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Long>>
    getBranchOptions() {

        List<BranchOptionResponse> branches =
                branchService
                        .getAccessibleBranchOptions();

        return branches.stream()
                .map(branch ->
                        new LookupOptionResponse<>(
                                branch.id(),
                                branch.branchCode(),
                                branch.nameKm(),
                                branch.nameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceOptionResponse>
    getProvinceOptions() {
        return provinceRepository
                .findProvinceOptionsForAdmin()
                .stream()
                .map(province ->
                        new ProvinceOptionResponse(
                                province.getId(),
                                province.getNameKm(),
                                province.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getMemberStatusOptions() {
        return memberStatusService
                .getMemberStatusOptions()
                .stream()
                .map(status ->
                        new LookupOptionResponse<>(
                                status.id(),
                                status.code(),
                                status.labelKm(),
                                status.labelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenderOptionResponse>
    getGenderOptions() {
        return Arrays.stream(Gender.values())
                .map(gender ->
                        new GenderOptionResponse(
                                gender.name(),
                                gender.getLabelKm(),
                                gender.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberLevelOptionResponse>
    getMemberLevelOptions() {
        return memberLevelService
                .getAllMemberLevels(true)
                .stream()
                .map(level ->
                        new MemberLevelOptionResponse(
                                level.id(),
                                level.code(),
                                level.labelKm(),
                                level.labelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NationalityOptionResponse>
    getNationalityOptions() {
        return nationalityService
                .getActiveNationalities()
                .stream()
                .map(nationality ->
                        new NationalityOptionResponse(
                                nationality.id(),
                                nationality.code(),
                                nationality.labelKm(),
                                nationality.labelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getActivityTypeOptions() {

        return activityTypeRepository
                .findAll()
                .stream()
                .map(type ->
                        new LookupOptionResponse<>(
                                type.getId(),
                                type.getCode(),
                                type.getLabelKm(),
                                type.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getAttendanceStatusOptions() {

        return attendanceStatusRepository
                .findAll()
                .stream()
                .map(status ->
                        new LookupOptionResponse<>(
                                status.getId(),
                                status.getCode(),
                                status.getLabelKm(),
                                status.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleOptionResponse>
    getUserRoleOptions() {
        User currentUser =
                getCurrentUser();

        return switch (currentUser.getRole()) {
            case SECRETARY ->
                    List.of(
                            toRoleOption(
                                    UserRole.MEMBER
                            )
                    );

            case BRANCH_LEADER ->
                    List.of(
                            toRoleOption(
                                    UserRole.MEMBER
                            ),
                            toRoleOption(
                                    UserRole.SECRETARY
                            )
                    );

            case ADMIN ->
                    List.of(
                            toRoleOption(
                                    UserRole.MEMBER
                            ),
                            toRoleOption(
                                    UserRole.SECRETARY
                            ),
                            toRoleOption(
                                    UserRole.BRANCH_LEADER
                            )
                    );

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You are not allowed to assign user roles"
                    );
        };
    }

    private RoleOptionResponse toRoleOption(
            UserRole role
    ) {
        return switch (role) {
            case MEMBER ->
                    new RoleOptionResponse(
                            "MEMBER",
                            "សមាជិក",
                            "Member"
                    );

            case SECRETARY ->
                    new RoleOptionResponse(
                            "SECRETARY",
                            "លេខាធិការ",
                            "Secretary"
                    );

            case BRANCH_LEADER ->
                    new RoleOptionResponse(
                            "BRANCH_LEADER",
                            "ប្រធានសាខា",
                            "Branch Leader"
                    );

            case ADMIN ->
                    new RoleOptionResponse(
                            "ADMIN",
                            "អ្នកគ្រប់គ្រង",
                            "Administrator"
                    );
        };
    }

    private User getCurrentUser() {
        User principalUser =
                SecurityUtil.getCurrentUser();

        if (principalUser == null
                || principalUser.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        principalUser.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }
    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getEthnicityOptions() {

        return ethnicityRepository
                .findAllByIsActiveTrueOrderByLabelKmAsc()
                .stream()
                .map(ethnicity ->
                        new LookupOptionResponse<>(
                                ethnicity.getId(),
                                ethnicity.getCode(),
                                ethnicity.getLabelKm(),
                                ethnicity.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getReligionOptions() {

        return religionRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(religion ->
                        new LookupOptionResponse<>(
                                religion.getId(),
                                religion.getCode(),
                                religion.getLabelKm(),
                                religion.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<String>>
    getTshirtSizeOptions() {

        return Arrays.stream(
                        TshirtSize.values()
                )
                .map(size ->
                        new LookupOptionResponse<>(
                                size.getValue(),
                                size.name(),
                                size.getValue(),
                                size.getValue()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getEducationLevelOptions() {

        return educationLevelRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(level ->
                        new LookupOptionResponse<>(
                                level.getId(),
                                level.getCode(),
                                level.getLabelKm(),
                                level.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getLanguageOptions() {

        return languageRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(language ->
                        new LookupOptionResponse<>(
                                language.getId(),
                                language.getCode(),
                                language.getLabelKm(),
                                language.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getSkillOptions() {

        return skillRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(skill ->
                        new LookupOptionResponse<>(
                                skill.getId(),
                                skill.getCode(),
                                skill.getLabelKm(),
                                skill.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getProficiencyLevelOptions() {

        return proficiencyLevelRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(level ->
                        new LookupOptionResponse<>(
                                level.getId(),
                                level.getCode(),
                                level.getLabelKm(),
                                level.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>>
    getPoliticalPartyOptions() {

        return politicalPartyRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(party ->
                        new LookupOptionResponse<>(
                                party.getId(),
                                party.getCode(),
                                party.getLabelKm(),
                                party.getLabelEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationOptionResponse>
    getProvinces() {
        return provinceRepository
                .findAllByIsActiveTrueOrderByNameKmAsc()
                .stream()
                .map(province ->
                        new LocationOptionResponse(
                                province.getId(),
                                province.getCode(),
                                province.getNameKm(),
                                province.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationOptionResponse>
    getDistricts(
            Short provinceId
    ) {
        if (provinceId == null || provinceId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Province ID must be greater than zero"
            );
        }

        if (!provinceRepository.existsById(provinceId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Province not found with ID: "
                            + provinceId
            );
        }

        return districtRepository
                .findAllByProvinceIdAndIsActiveTrueOrderByNameKmAsc(
                        provinceId
                )
                .stream()
                .map(district ->
                        new LocationOptionResponse(
                                district.getId(),
                                district.getCode(),
                                district.getNameKm(),
                                district.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationOptionResponse>
    getCommunes(
            Integer districtId
    ) {
        if (districtId == null || districtId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "District ID must be greater than zero"
            );
        }

        if (!districtRepository.existsById(districtId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "District not found with ID: "
                            + districtId
            );
        }

        return communeRepository
                .findAllByDistrictIdAndIsActiveTrueOrderByNameKmAsc(
                        districtId
                )
                .stream()
                .map(commune ->
                        new LocationOptionResponse(
                                commune.getId(),
                                commune.getCode(),
                                commune.getNameKm(),
                                commune.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchStatusOptionResponse>
    getBranchStatuses() {
        return branchStatusRepository
                .findAllByIsActiveTrueOrderByIdAsc()
                .stream()
                .map(status ->
                        new BranchStatusOptionResponse(
                                status.getId(),
                                status.getCode(),
                                status.getNameKm(),
                                status.getNameEn()
                        )
                )
                .toList();
    }

}