package org.example.tnal_youth_backend.account.memberfamily.service;

import org.example.tnal_youth_backend.account.memberfamily.dto.request.MyFamilyRequest;
import org.example.tnal_youth_backend.account.memberfamily.dto.response.MyFamilyResponse;

import java.util.List;

public interface MyFamilyService {

    List<MyFamilyResponse> getMyFamily();

    MyFamilyResponse getMyFamilyMember(
            Long familyId
    );

    MyFamilyResponse createMyFamilyMember(
            MyFamilyRequest request
    );

    MyFamilyResponse updateMyFamilyMember(
            Long familyId,
            MyFamilyRequest request
    );

    void deleteMyFamilyMember(
            Long familyId
    );
}