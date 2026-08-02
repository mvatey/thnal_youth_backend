package org.example.tnal_youth_backend.member.nationality.service;

import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;

import java.util.List;

public interface NationalityService {

    List<NationalityResponse> getActiveNationalities();

    NationalityResponse getNationalityById(
            Short id
    );

    Nationality getActiveNationalityEntityById(
            Short id
    );
}