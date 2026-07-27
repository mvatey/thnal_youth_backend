package org.example.tnal_youth_backend.member.religion.service;

import org.example.tnal_youth_backend.member.religion.dto.ReligionRequest;
import org.example.tnal_youth_backend.member.religion.dto.ReligionResponse;

import java.util.List;

public interface ReligionService {

    List<ReligionResponse> getAllReligions(
            Boolean activeOnly
    );

    ReligionResponse getReligionById(
            Short id
    );

    ReligionResponse getReligionByCode(
            String code
    );

    ReligionResponse createReligion(
            ReligionRequest request
    );

    ReligionResponse updateReligion(
            Short id,
            ReligionRequest request
    );

    void deleteReligion(
            Short id
    );
}