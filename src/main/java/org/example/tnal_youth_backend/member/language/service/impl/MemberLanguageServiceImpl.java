package org.example.tnal_youth_backend.member.language.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.entity.MemberLanguage;
import org.example.tnal_youth_backend.member.language.mapper.MemberLanguageMapper;
import org.example.tnal_youth_backend.member.language.repository.MemberLanguageRepository;
import org.example.tnal_youth_backend.member.language.service.MemberLanguageService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLanguageServiceImpl
        implements MemberLanguageService {

    private final MemberLanguageRepository languageRepository;
    private final MemberRepository memberRepository;
    private final MemberLanguageMapper languageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberLanguageResponse> getByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return languageRepository
                .findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(languageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberLanguageResponse create(
            Long memberId,
            MemberLanguageRequest request
    ) {
        Member member = findMember(memberId);

        String languageName =
                normalizeRequired(request.languageName());

        boolean duplicate =
                languageRepository
                        .existsByMemberIdAndLanguageNameIgnoreCase(
                                memberId,
                                languageName
                        );

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has the language: "
                            + languageName
            );
        }

        MemberLanguage language =
                MemberLanguage.builder()
                        .member(member)
                        .languageName(languageName)
                        .listeningLevelId(
                                request.listeningLevelId()
                        )
                        .speakingLevelId(
                                request.speakingLevelId()
                        )
                        .readingLevelId(
                                request.readingLevelId()
                        )
                        .writingLevelId(
                                request.writingLevelId()
                        )
                        .build();

        try {
            MemberLanguage saved =
                    languageRepository.saveAndFlush(language);

            return languageMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw proficiencyConstraintException();
        }
    }

    @Override
    @Transactional
    public MemberLanguageResponse update(
            Long memberId,
            Long languageId,
            MemberLanguageRequest request
    ) {
        MemberLanguage language =
                findLanguage(memberId, languageId);

        String languageName =
                normalizeRequired(request.languageName());

        boolean duplicate =
                languageRepository
                        .existsByMemberIdAndLanguageNameIgnoreCaseAndIdNot(
                                memberId,
                                languageName,
                                languageId
                        );

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has the language: "
                            + languageName
            );
        }

        language.setLanguageName(languageName);
        language.setListeningLevelId(
                request.listeningLevelId()
        );
        language.setSpeakingLevelId(
                request.speakingLevelId()
        );
        language.setReadingLevelId(
                request.readingLevelId()
        );
        language.setWritingLevelId(
                request.writingLevelId()
        );

        try {
            MemberLanguage updated =
                    languageRepository.saveAndFlush(language);

            return languageMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw proficiencyConstraintException();
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long languageId
    ) {
        MemberLanguage language =
                findLanguage(memberId, languageId);

        languageRepository.delete(language);
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

    private MemberLanguage findLanguage(
            Long memberId,
            Long languageId
    ) {
        if (languageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language ID is required"
            );
        }

        return languageRepository
                .findByIdAndMemberId(
                        languageId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Language not found with ID: "
                                        + languageId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language name is required"
            );
        }

        return value.trim();
    }

    private ResponseStatusException
    proficiencyConstraintException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Language could not be saved. Check that listening, \
                speaking, reading, and writing level IDs reference \
                existing proficiency level records.
                """
        );
    }
}