package org.example.tnal_youth_backend.member.level.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.level.dto.MemberLevelRequest;
import org.example.tnal_youth_backend.member.level.dto.MemberLevelResponse;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;
import org.example.tnal_youth_backend.member.level.service.MemberLevelService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl implements MemberLevelService {

    private final MemberLevelRepository memberLevelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MemberLevelResponse> getAllMemberLevels(
            Boolean activeOnly
    ) {
        List<MemberLevel> memberLevels;

        if (Boolean.TRUE.equals(activeOnly)) {
            memberLevels =
                    memberLevelRepository
                            .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
        } else {
            memberLevels =
                    memberLevelRepository
                            .findAllByOrderBySortOrderAscIdAsc();
        }

        return memberLevels.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberLevelResponse getMemberLevelById(
            Short id
    ) {
        MemberLevel memberLevel = findMemberLevelById(id);

        return toResponse(memberLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberLevelResponse getMemberLevelByCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member level code is required"
            );
        }

        String normalizedCode = normalizeCode(code);

        MemberLevel memberLevel =
                memberLevelRepository
                        .findByCodeIgnoreCase(normalizedCode)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member level not found with code: "
                                        + normalizedCode
                        ));

        return toResponse(memberLevel);
    }

    @Override
    @Transactional
    public MemberLevelResponse createMemberLevel(
            MemberLevelRequest request
    ) {
        String normalizedCode = normalizeCode(request.code());

        if (memberLevelRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member level code already exists: "
                            + normalizedCode
            );
        }

        MemberLevel memberLevel = MemberLevel.builder()
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

        MemberLevel savedMemberLevel =
                memberLevelRepository.save(memberLevel);

        return toResponse(savedMemberLevel);
    }

    @Override
    @Transactional
    public MemberLevelResponse updateMemberLevel(
            Short id,
            MemberLevelRequest request
    ) {
        MemberLevel memberLevel = findMemberLevelById(id);

        String normalizedCode = normalizeCode(request.code());

        boolean codeAlreadyExists =
                memberLevelRepository
                        .existsByCodeIgnoreCaseAndIdNot(
                                normalizedCode,
                                id
                        );

        if (codeAlreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member level code already exists: "
                            + normalizedCode
            );
        }

        memberLevel.setCode(normalizedCode);
        memberLevel.setLabelKm(request.labelKm().trim());
        memberLevel.setLabelEn(trimToNull(request.labelEn()));
        memberLevel.setDescription(
                trimToNull(request.description())
        );

        if (request.isActive() != null) {
            memberLevel.setIsActive(request.isActive());
        }

        if (request.sortOrder() != null) {
            memberLevel.setSortOrder(request.sortOrder());
        }

        MemberLevel updatedMemberLevel =
                memberLevelRepository.save(memberLevel);

        return toResponse(updatedMemberLevel);
    }

    @Override
    @Transactional
    public void deleteMemberLevel(
            Short id
    ) {
        MemberLevel memberLevel = findMemberLevelById(id);

        try {
            memberLevelRepository.delete(memberLevel);
            memberLevelRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this member level because it is used by members"
            );
        }
    }

    private MemberLevel findMemberLevelById(
            Short id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member level ID is required"
            );
        }

        return memberLevelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member level not found with ID: " + id
                ));
    }

    private MemberLevelResponse toResponse(
            MemberLevel memberLevel
    ) {
        return new MemberLevelResponse(
                memberLevel.getId(),
                memberLevel.getCode(),
                memberLevel.getLabelKm(),
                memberLevel.getLabelEn(),
                memberLevel.getDescription(),
                memberLevel.getIsActive(),
                memberLevel.getSortOrder(),
                memberLevel.getCreatedAt(),
                memberLevel.getUpdatedAt()
        );
    }

    private String normalizeCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member level code is required"
            );
        }

        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "-");
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