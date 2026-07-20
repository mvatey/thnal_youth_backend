package org.example.tnal_youth_backend.member.status.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.status.dto.MemberStatusRequest;
import org.example.tnal_youth_backend.member.status.dto.MemberStatusResponse;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.example.tnal_youth_backend.member.status.repository.MemberStatusRepository;
import org.example.tnal_youth_backend.member.status.service.MemberStatusService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberStatusServiceImpl implements MemberStatusService {

    private final MemberStatusRepository memberStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MemberStatusResponse> getAllMemberStatuses(
            Boolean activeOnly
    ) {
        List<MemberStatus> statuses;

        if (Boolean.TRUE.equals(activeOnly)) {
            statuses =
                    memberStatusRepository
                            .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
        } else {
            statuses =
                    memberStatusRepository
                            .findAllByOrderBySortOrderAscIdAsc();
        }

        return statuses.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberStatusResponse getMemberStatusById(
            Short id
    ) {
        MemberStatus memberStatus = findMemberStatusById(id);

        return toResponse(memberStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberStatusResponse getMemberStatusByCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member status code is required"
            );
        }

        MemberStatus memberStatus =
                memberStatusRepository
                        .findByCodeIgnoreCase(code.trim())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member status not found with code: " + code
                        ));

        return toResponse(memberStatus);
    }

    @Override
    @Transactional
    public MemberStatusResponse createMemberStatus(
            MemberStatusRequest request
    ) {
        String normalizedCode = normalizeCode(request.code());

        if (memberStatusRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member status code already exists: " + normalizedCode
            );
        }

        MemberStatus memberStatus = MemberStatus.builder()
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

        MemberStatus savedStatus =
                memberStatusRepository.save(memberStatus);

        return toResponse(savedStatus);
    }

    @Override
    @Transactional
    public MemberStatusResponse updateMemberStatus(
            Short id,
            MemberStatusRequest request
    ) {
        MemberStatus memberStatus = findMemberStatusById(id);

        String normalizedCode = normalizeCode(request.code());

        boolean duplicateCode =
                memberStatusRepository
                        .existsByCodeIgnoreCaseAndIdNot(
                                normalizedCode,
                                id
                        );

        if (duplicateCode) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member status code already exists: " + normalizedCode
            );
        }

        memberStatus.setCode(normalizedCode);
        memberStatus.setLabelKm(request.labelKm().trim());
        memberStatus.setLabelEn(trimToNull(request.labelEn()));
        memberStatus.setDescription(
                trimToNull(request.description())
        );

        if (request.isActive() != null) {
            memberStatus.setIsActive(request.isActive());
        }

        if (request.sortOrder() != null) {
            memberStatus.setSortOrder(request.sortOrder());
        }

        MemberStatus updatedStatus =
                memberStatusRepository.save(memberStatus);

        return toResponse(updatedStatus);
    }

    @Override
    @Transactional
    public void deleteMemberStatus(
            Short id
    ) {
        MemberStatus memberStatus = findMemberStatusById(id);

        try {
            memberStatusRepository.delete(memberStatus);
            memberStatusRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this member status because it is already used by members"
            );
        }
    }

    private MemberStatus findMemberStatusById(
            Short id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member status ID is required"
            );
        }

        return memberStatusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member status not found with ID: " + id
                ));
    }

    private MemberStatusResponse toResponse(
            MemberStatus memberStatus
    ) {
        return new MemberStatusResponse(
                memberStatus.getId(),
                memberStatus.getCode(),
                memberStatus.getLabelKm(),
                memberStatus.getLabelEn(),
                memberStatus.getDescription(),
                memberStatus.getIsActive(),
                memberStatus.getSortOrder(),
                memberStatus.getCreatedAt(),
                memberStatus.getUpdatedAt()
        );
    }

    private String normalizeCode(
            String code
    ) {
        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");
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