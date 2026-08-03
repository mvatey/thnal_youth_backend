package org.example.tnal_youth_backend.member.religion.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.religion.dto.ReligionRequest;
import org.example.tnal_youth_backend.member.religion.dto.ReligionResponse;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;
import org.example.tnal_youth_backend.member.religion.service.ReligionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReligionServiceImpl implements ReligionService {

    private final ReligionRepository religionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReligionResponse> getAllReligions(
            Boolean activeOnly
    ) {
        List<Religion> religions;

        if (Boolean.TRUE.equals(activeOnly)) {
            religions =
                    religionRepository
                            .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
        } else {
            religions =
                    religionRepository
                            .findAllByOrderBySortOrderAscIdAsc();
        }

        return religions.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReligionResponse getReligionById(
            Short id
    ) {
        Religion religion = findReligionById(id);

        return toResponse(religion);
    }

    @Override
    @Transactional(readOnly = true)
    public ReligionResponse getReligionByCode(
            String code
    ) {
        String normalizedCode = normalizeCode(code);

        Religion religion =
                religionRepository
                        .findByCodeIgnoreCase(normalizedCode)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Religion not found with code: "
                                        + normalizedCode
                        ));

        return toResponse(religion);
    }

    @Override
    @Transactional
    public ReligionResponse createReligion(
            ReligionRequest request
    ) {
        String normalizedCode =
                normalizeCode(request.code());

        if (religionRepository
                .existsByCodeIgnoreCase(normalizedCode)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Religion code already exists: "
                            + normalizedCode
            );
        }

        Religion religion = Religion.builder()
                .code(normalizedCode)
                .labelKm(request.labelKm().trim())
                .labelEn(trimToNull(request.labelEn()))
                .description(trimToNull(request.description()))
                .isActive(
                        request.isActive() == null
                                ? true
                                : request.isActive()
                )
                .sortOrder(
                        request.sortOrder() == null
                                ? 0
                                : request.sortOrder()
                )
                .build();

        Religion savedReligion =
                religionRepository.save(religion);

        return toResponse(savedReligion);
    }

    @Override
    @Transactional
    public ReligionResponse updateReligion(
            Short id,
            ReligionRequest request
    ) {
        Religion religion = findReligionById(id);

        String normalizedCode =
                normalizeCode(request.code());

        boolean codeAlreadyExists =
                religionRepository
                        .existsByCodeIgnoreCaseAndIdNot(
                                normalizedCode,
                                id
                        );

        if (codeAlreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Religion code already exists: "
                            + normalizedCode
            );
        }

        religion.setCode(normalizedCode);
        religion.setLabelKm(request.labelKm().trim());
        religion.setLabelEn(
                trimToNull(request.labelEn())
        );
        religion.setDescription(
                trimToNull(request.description())
        );

        if (request.isActive() != null) {
            religion.setIsActive(request.isActive());
        }

        if (request.sortOrder() != null) {
            religion.setSortOrder(request.sortOrder());
        }

        Religion updatedReligion =
                religionRepository.save(religion);

        return toResponse(updatedReligion);
    }

    @Override
    @Transactional
    public void deleteReligion(
            Short id
    ) {
        Religion religion = findReligionById(id);

        try {
            religionRepository.delete(religion);
            religionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this religion because it is used by members"
            );
        }
    }

    private Religion findReligionById(
            Short id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Religion ID is required"
            );
        }

        return religionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Religion not found with ID: " + id
                ));
    }

    private ReligionResponse toResponse(
            Religion religion
    ) {
        return new ReligionResponse(
                religion.getId(),
                religion.getCode(),
                religion.getLabelKm(),
                religion.getLabelEn(),
                religion.getDescription(),
                religion.getIsActive(),
                religion.getSortOrder(),
                religion.getCreatedAt(),
                religion.getUpdatedAt()
        );
    }

    private String normalizeCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Religion code is required"
            );
        }

        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }
}