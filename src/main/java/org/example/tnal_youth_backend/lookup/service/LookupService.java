package org.example.tnal_youth_backend.lookup.service;

import org.example.tnal_youth_backend.lookup.dto.*;

import java.util.List;

public interface LookupService {

    List<LookupOptionResponse<Long>>
    getBranchOptions();

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

    List<LookupOptionResponse<Short>> getProvinceOptions();

    List<LookupOptionResponse<Integer>> getDistrictOptions(Short provinceId);

    List<LookupOptionResponse<Integer>> getCommuneOptions(Integer districtId);

    List<LookupOptionResponse<Short>> getEducationLevelOptions();

    List<LookupOptionResponse<Short>> getEmploymentSectorOptions();

    List<LookupOptionResponse<Short>> getProficiencyLevelOptions();

    List<LookupOptionResponse<String>> getCountryOptions();

    List<LookupOptionResponse<Short>> getActivityTypeOptions();

    List<LookupOptionResponse<Short>> getActivitySectorOptions();

    List<LookupOptionResponse<Short>> getActivityStatusOptions();
}
