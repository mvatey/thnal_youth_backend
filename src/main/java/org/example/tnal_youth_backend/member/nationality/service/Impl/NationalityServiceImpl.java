package org.example.tnal_youth_backend.member.nationality.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.ResourceNotFoundException;
import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.example.tnal_youth_backend.member.nationality.mapper.NationalityMapper;
import org.example.tnal_youth_backend.member.nationality.repository.NationalityRepository;
import org.example.tnal_youth_backend.member.nationality.service.NationalityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NationalityServiceImpl
        implements NationalityService {

    private final NationalityRepository nationalityRepository;
    private final NationalityMapper nationalityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NationalityResponse>
    getActiveNationalities() {

        return nationalityRepository
                .findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(nationalityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NationalityResponse getNationalityById(
            Short id
    ) {
        Nationality nationality =
                getActiveNationalityEntityById(id);

        return nationalityMapper.toResponse(
                nationality
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Nationality getActiveNationalityEntityById(
            Short id
    ) {
        return nationalityRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active nationality not found with ID: "
                                        + id
                        )
                );
    }
}