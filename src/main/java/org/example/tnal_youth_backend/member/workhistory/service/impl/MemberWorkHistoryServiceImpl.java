package org.example.tnal_youth_backend.member.workhistory.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.entity.MemberWorkHistory;
import org.example.tnal_youth_backend.member.workhistory.mapper.MemberWorkHistoryMapper;
import org.example.tnal_youth_backend.member.workhistory.repository.MemberWorkHistoryRepository;
import org.example.tnal_youth_backend.member.workhistory.service.MemberWorkHistoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWorkHistoryServiceImpl
        implements MemberWorkHistoryService {

    private final MemberWorkHistoryRepository workHistoryRepository;
    private final MemberRepository memberRepository;
    private final MemberWorkHistoryMapper workHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberWorkHistoryResponse> getByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return workHistoryRepository
                .findAllByMemberIdOrderByStartDateDescIdDesc(
                        memberId
                )
                .stream()
                .map(workHistoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberWorkHistoryResponse create(
            Long memberId,
            MemberWorkHistoryRequest request
    ) {
        Member member = findMember(memberId);

        MemberWorkHistory workHistory =
                MemberWorkHistory.builder()
                        .member(member)
                        .organizationName(
                                normalizeRequired(
                                        request.organizationName(),
                                        "Organization name"
                                )
                        )
                        .positionTitle(
                                normalizeRequired(
                                        request.positionTitle(),
                                        "Position title"
                                )
                        )
                        .address(
                                trimToNull(request.address())
                        )
                        .employmentSectorId(
                                request.employmentSectorId()
                        )
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .build();

        try {
            MemberWorkHistory saved =
                    workHistoryRepository
                            .saveAndFlush(workHistory);

            return workHistoryMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Work history could not be saved. Check that \
                    employment_sector_id exists and the date range \
                    is valid.
                    """
            );
        }
    }

    @Override
    @Transactional
    public MemberWorkHistoryResponse update(
            Long memberId,
            Long workId,
            MemberWorkHistoryRequest request
    ) {
        MemberWorkHistory workHistory =
                findWorkHistory(memberId, workId);

        workHistory.setOrganizationName(
                normalizeRequired(
                        request.organizationName(),
                        "Organization name"
                )
        );

        workHistory.setPositionTitle(
                normalizeRequired(
                        request.positionTitle(),
                        "Position title"
                )
        );

        workHistory.setAddress(
                trimToNull(request.address())
        );

        workHistory.setEmploymentSectorId(
                request.employmentSectorId()
        );

        workHistory.setStartDate(
                request.startDate()
        );

        workHistory.setEndDate(
                request.endDate()
        );

        try {
            MemberWorkHistory updated =
                    workHistoryRepository
                            .saveAndFlush(workHistory);

            return workHistoryMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Work history could not be updated. Check that \
                    employment_sector_id exists and the date range \
                    is valid.
                    """
            );
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long workId
    ) {
        MemberWorkHistory workHistory =
                findWorkHistory(memberId, workId);

        workHistoryRepository.delete(workHistory);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private void verifyMemberExists(Long memberId) {
        findMember(memberId);
    }

    private MemberWorkHistory findWorkHistory(
            Long memberId,
            Long workId
    ) {
        if (workId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Work history ID is required"
            );
        }

        return workHistoryRepository
                .findByIdAndMemberId(
                        workId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Work history not found with ID: "
                                        + workId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}