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
}