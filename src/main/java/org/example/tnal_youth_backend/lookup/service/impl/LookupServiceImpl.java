package org.example.tnal_youth_backend.lookup.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.lookup.dto.*;
import org.example.tnal_youth_backend.lookup.service.LookupService;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.level.service.MemberLevelService;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.nationality.service.NationalityService;
import org.example.tnal_youth_backend.member.status.service.MemberStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final JdbcTemplate jdbcTemplate;

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
    public List<LookupOptionResponse<Long>>
    getActivityInvitableBranchOptions() {
        return branchService
                .getAllActiveBranchOptions()
                .stream()
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
    public List<LookupOptionResponse<Short>> getBranchLevelOptions() {
        return jdbcTemplate.query(
                """
                SELECT id, code, label_km, label_en
                FROM branch_levels
                WHERE is_active = TRUE
                ORDER BY sort_order, hierarchy_order, label_km
                """,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getShort("id"), rs.getString("code"),
                        rs.getString("label_km"), rs.getString("label_en")
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getBranchStatusOptions() {
        return jdbcTemplate.query(
                """
                SELECT id, code, label_km, label_en
                FROM branch_statuses
                WHERE is_active = TRUE
                ORDER BY sort_order, label_km
                """,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getShort("id"), rs.getString("code"),
                        rs.getString("label_km"), rs.getString("label_en")
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getProvinceOptions() {
        return jdbcTemplate.query(
                """
                SELECT id, code, name_km, name_en
                FROM provinces
                WHERE is_active = TRUE
                ORDER BY name_km
                """,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getShort("id"), rs.getString("code"),
                        rs.getString("name_km"), rs.getString("name_en")
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Integer>> getDistrictOptions(Short provinceId) {
        return jdbcTemplate.query(
                """
                SELECT id, code, name_km, name_en
                FROM districts
                WHERE is_active = TRUE AND province_id = ?
                ORDER BY name_km
                """,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getInt("id"), rs.getString("code"),
                        rs.getString("name_km"), rs.getString("name_en")
                ),
                provinceId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Integer>> getCommuneOptions(Integer districtId) {
        return jdbcTemplate.query(
                """
                SELECT id, code, name_km, name_en
                FROM communes
                WHERE is_active = TRUE AND district_id = ?
                ORDER BY name_km
                """,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getInt("id"), rs.getString("code"),
                        rs.getString("name_km"), rs.getString("name_en")
                ),
                districtId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getEducationLevelOptions() {
        return getSimpleActiveLookup("education_levels");
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getEmploymentSectorOptions() {
        return getSimpleActiveLookup("employment_sectors");
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getProficiencyLevelOptions() {
        return getSimpleActiveLookup("proficiency_levels");
    }

    private List<LookupOptionResponse<Short>> getSimpleActiveLookup(String tableName) {
        String sql = "SELECT id, code, label_km, label_en FROM "
                + tableName
                + " WHERE is_active = TRUE ORDER BY sort_order, label_km";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LookupOptionResponse<>(
                        rs.getShort("id"),
                        rs.getString("code"),
                        rs.getString("label_km"),
                        rs.getString("label_en")
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<String>> getCountryOptions() {
        return List.of(
                new LookupOptionResponse<>("KH", "KH", "កម្ពុជា", "Cambodia"),
                new LookupOptionResponse<>("VN", "VN", "វៀតណាម", "Vietnam"),
                new LookupOptionResponse<>("TH", "TH", "ថៃ", "Thailand"),
                new LookupOptionResponse<>("LA", "LA", "ឡាវ", "Laos"),
                new LookupOptionResponse<>("MY", "MY", "ម៉ាឡេស៊ី", "Malaysia"),
                new LookupOptionResponse<>("SG", "SG", "សិង្ហបុរី", "Singapore"),
                new LookupOptionResponse<>("ID", "ID", "ឥណ្ឌូនេស៊ី", "Indonesia"),
                new LookupOptionResponse<>("PH", "PH", "ហ្វីលីពីន", "Philippines"),
                new LookupOptionResponse<>("MM", "MM", "មីយ៉ាន់ម៉ា", "Myanmar"),
                new LookupOptionResponse<>("BN", "BN", "ប្រ៊ុយណេ", "Brunei"),
                new LookupOptionResponse<>("CN", "CN", "ចិន", "China"),
                new LookupOptionResponse<>("JP", "JP", "ជប៉ុន", "Japan"),
                new LookupOptionResponse<>("KR", "KR", "កូរ៉េខាងត្បូង", "South Korea"),
                new LookupOptionResponse<>("IN", "IN", "ឥណ្ឌា", "India"),
                new LookupOptionResponse<>("AU", "AU", "អូស្ត្រាលី", "Australia"),
                new LookupOptionResponse<>("FR", "FR", "បារាំង", "France"),
                new LookupOptionResponse<>("DE", "DE", "អាល្លឺម៉ង់", "Germany"),
                new LookupOptionResponse<>("CA", "CA", "កាណាដា", "Canada"),
                new LookupOptionResponse<>("US", "US", "សហរដ្ឋអាមេរិក", "United States"),
                new LookupOptionResponse<>("GB", "GB", "ចក្រភពអង់គ្លេស", "United Kingdom")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getActivityTypeOptions() {
        return getSimpleActiveLookup("activity_types");
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getActivitySectorOptions() {
        return getSimpleActiveLookup("activity_sectors");
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupOptionResponse<Short>> getActivityStatusOptions() {
        return getSimpleActiveLookup("activity_statuses");
    }
}
