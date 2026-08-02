package org.example.tnal_youth_backend.member.nationality.mapper;

import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.springframework.stereotype.Component;

@Component
public class NationalityMapper {

    public NationalityResponse toResponse(
            Nationality nationality
    ) {
        if (nationality == null) {
            return null;
        }

        return new NationalityResponse(
                nationality.getId(),
                nationality.getCode(),
                nationality.getLabelKm(),
                nationality.getLabelEn()
        );
    }
}