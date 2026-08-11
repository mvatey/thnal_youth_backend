package org.example.tnal_youth_backend.lookup.service;

import org.example.tnal_youth_backend.lookup.dto.*;

import java.util.List;

public interface LookupService {

    List<LookupOptionResponse<Long>>
    getBranchOptions();

    List<LookupOptionResponse<Long>>
    getActivityInvitableBranchOptions();

    List<LookupOptionResponse<Short>>
    getMemberStatusOptions();

    List<GenderOptionResponse>
    getGenderOptions();

    List<MemberLevelOptionResponse>
    getMemberLevelOptions();

    List<NationalityOptionResponse>
    getNationalityOptions();

    List<RoleOptionResponse>
    getUserRoleOptions();

    List<LookupOptionResponse<Short>> getBranchLevelOptions();

    List<LookupOptionResponse<Short>> getBranchStatusOptions();

    List<LookupOptionResponse<Short>> getProvinceLookupOptions();

    List<LookupOptionResponse<Integer>> getDistrictOptions(Short provinceId);

    List<LookupOptionResponse<Integer>> getCommuneOptions(Integer districtId);

    List<LookupOptionResponse<Short>> getEmploymentSectorOptions();

    List<LookupOptionResponse<String>> getCountryOptions();

    List<LookupOptionResponse<Short>> getActivitySectorOptions();

    List<LookupOptionResponse<Short>> getActivityStatusOptions();

    List<LookupOptionResponse<Short>>
    getActivityTypeOptions();

    List<LookupOptionResponse<Short>>
    getAttendanceStatusOptions();

    List<LookupOptionResponse<Short>>
    getEthnicityOptions();

    List<LookupOptionResponse<Short>>
    getReligionOptions();

    List<LookupOptionResponse<String>>
    getTshirtSizeOptions();

    List<LookupOptionResponse<Short>>
    getEducationLevelOptions();

    List<LookupOptionResponse<Short>>
    getLanguageOptions();

    List<LookupOptionResponse<Short>>
    getSkillOptions();

    List<LookupOptionResponse<Short>>
    getProficiencyLevelOptions();

    List<LookupOptionResponse<Short>>
    getPoliticalPartyOptions();

    List<ProvinceOptionResponse>
    getProvinceOptions();

    List<LocationOptionResponse>
    getProvinces();

    List<LocationOptionResponse>
    getDistricts(
            Short provinceId
    );

    List<LocationOptionResponse>
    getCommunes(
            Integer districtId
    );

    List<BranchStatusOptionResponse>
    getBranchStatuses();
}
