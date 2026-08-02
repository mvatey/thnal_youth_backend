package org.example.tnal_youth_backend.member.credential.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialTabResponse;
import org.example.tnal_youth_backend.member.credential.entity.CredentialStatus;
import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.example.tnal_youth_backend.member.credential.mapper.MemberCredentialMapper;
import org.example.tnal_youth_backend.member.credential.repository.MemberCredentialRepository;
import org.example.tnal_youth_backend.member.credential.service.MemberCredentialService;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCredentialServiceImpl
        implements MemberCredentialService {

    private static final String MEMBERSHIP_CARD_KIND =
            "MEMBERSHIP_CARD";

    private static final String ACTIVITY_CERTIFICATE_KIND =
            "ACTIVITY_CERTIFICATE";

    private static final String APPOINTMENT_LETTER_KIND =
            "APPOINTMENT_LETTER";

    private final MemberCredentialRepository
            memberCredentialRepository;

    private final MemberRepository
            memberRepository;

    private final ActivityRepository
            activityRepository;

    private final FileRepository
            fileRepository;

    private final MemberCredentialMapper
            memberCredentialMapper;

    /*
     * Replace this dependency with the authenticated-user
     * helper already used elsewhere in your project.
     *
     * Example:
     *
     * private final CurrentUserService currentUserService;
     */

    @Override
    @Transactional(readOnly = true)
    public List<MemberCredentialResponse> getAllByMemberId(
            Long memberId
    ) {
        validateMemberExists(memberId);

        return memberCredentialRepository
                .findAllByMemberIdOrderByCreatedAtDesc(
                        memberId
                )
                .stream()
                .map(memberCredentialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberCredentialResponse getById(
            Long memberId,
            Long credentialId
    ) {
        validateMemberExists(memberId);

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        return memberCredentialMapper
                .toResponse(credential);
    }

    @Override
    public MemberCredentialResponse create(
            Long memberId,
            MemberCredentialRequest request
    ) {
        validateMemberExists(memberId);

        String credentialKind =
                normalizeCredentialKind(
                        request.getCredentialKind()
                );

        request.setCredentialKind(
                credentialKind
        );

        validateCredentialActivity(
                credentialKind,
                request.getActivityId()
        );

        validateFileExists(
                request.getFileId()
        );

        validateSingleMembershipCardOnCreate(
                memberId,
                credentialKind
        );

        String credentialNo =
                normalizeCredentialNo(
                        request.getCredentialNo()
                );

        request.setCredentialNo(
                credentialNo
        );

        validateUniqueCredentialNoForCreate(
                credentialNo
        );

        Long issuedById =
                resolveCurrentUserId();

        MemberCredential credential =
                memberCredentialMapper.toEntity(
                        memberId,
                        issuedById,
                        request
                );

        credential.setStatus(
                CredentialStatus.ACTIVE
        );

        MemberCredential savedCredential =
                memberCredentialRepository.save(
                        credential
                );

        return memberCredentialMapper.toResponse(
                savedCredential
        );
    }

    @Override
    public MemberCredentialResponse update(
            Long memberId,
            Long credentialId,
            MemberCredentialRequest request
    ) {
        validateMemberExists(memberId);

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        String credentialKind =
                normalizeCredentialKind(
                        request.getCredentialKind()
                );

        request.setCredentialKind(
                credentialKind
        );

        validateCredentialActivity(
                credentialKind,
                request.getActivityId()
        );

        validateFileExists(
                request.getFileId()
        );

        validateSingleMembershipCardOnUpdate(
                memberId,
                credentialId,
                credentialKind
        );

        String credentialNo =
                normalizeCredentialNo(
                        request.getCredentialNo()
                );

        request.setCredentialNo(
                credentialNo
        );

        validateUniqueCredentialNoForUpdate(
                credentialId,
                credentialNo
        );

        /*
         * Do not overwrite:
         *
         * issuedById
         * status
         * memberId
         *
         * during a normal update.
         */
        memberCredentialMapper.updateEntity(
                credential,
                request
        );

        MemberCredential savedCredential =
                memberCredentialRepository.save(
                        credential
                );

        return memberCredentialMapper.toResponse(
                savedCredential
        );
    }

    @Override
    public void delete(
            Long memberId,
            Long credentialId
    ) {
        validateMemberExists(memberId);

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        /*
         * Membership cards are created automatically and should
         * normally not be deleted through the standard endpoint.
         */
        if (MEMBERSHIP_CARD_KIND.equals(
                credential.getCredentialKind()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Membership cards cannot be deleted"
            );
        }

        memberCredentialRepository.delete(
                credential
        );
    }

    /*
     * Used automatically after creating a member.
     */
    @Override
    public MemberCredentialResponse createDefaultMembershipCard(
            Long memberId,
            Long issuedById
    ) {
        validateMemberExists(memberId);

        boolean alreadyExists =
                memberCredentialRepository
                        .existsByMemberIdAndCredentialKindIgnoreCase(
                                memberId,
                                MEMBERSHIP_CARD_KIND
                        );

        if (alreadyExists) {
            return memberCredentialRepository
                    .findAllByMemberIdOrderByCreatedAtDesc(
                            memberId
                    )
                    .stream()
                    .filter(credential ->
                            MEMBERSHIP_CARD_KIND.equalsIgnoreCase(
                                    credential.getCredentialKind()
                            )
                    )
                    .findFirst()
                    .map(memberCredentialMapper::toResponse)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Membership card already exists"
                            )
                    );
        }

        MemberCredential credential =
                MemberCredential.builder()
                        .memberId(memberId)
                        .issuedById(issuedById)
                        .credentialKind(
                                MEMBERSHIP_CARD_KIND
                        )
                        .credentialNo(
                                generateMembershipCardNumber(
                                        memberId
                                )
                        )
                        .title(
                                "ប័ណ្ណសម្គាល់សមាជិក"
                        )
                        .issuedOn(
                                java.time.LocalDate.now()
                        )
                        .status(
                                CredentialStatus.ACTIVE
                        )
                        .build();

        MemberCredential savedCredential =
                memberCredentialRepository.save(
                        credential
                );

        return memberCredentialMapper.toResponse(
                savedCredential
        );
    }

    private MemberCredential findCredential(
            Long memberId,
            Long credentialId
    ) {
        if (credentialId == null
                || credentialId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential ID must be greater than zero"
            );
        }

        return memberCredentialRepository
                .findByIdAndMemberId(
                        credentialId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member credential not found. "
                                        + "memberId="
                                        + memberId
                                        + ", credentialId="
                                        + credentialId
                        )
                );
    }

    private void validateMemberExists(
            Long memberId
    ) {
        if (memberId == null
                || memberId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        if (!memberRepository.existsById(
                memberId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: "
                            + memberId
            );
        }
    }

    private void validateFileExists(
            Long fileId
    ) {
        if (fileId == null) {
            return;
        }

        if (fileId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File ID must be greater than zero"
            );
        }

        if (!fileRepository.existsById(
                fileId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File not found with ID: "
                            + fileId
            );
        }
    }

    private void validateCredentialActivity(
            String credentialKind,
            Long activityId
    ) {
        if (ACTIVITY_CERTIFICATE_KIND.equals(
                credentialKind
        )) {
            if (activityId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Activity ID is required for an activity certificate"
                );
            }

            if (activityId <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Activity ID must be greater than zero"
                );
            }

            if (!activityRepository.existsById(
                    activityId
            )) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with ID: "
                                + activityId
                );
            }

            return;
        }

        if (activityId != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is not allowed for credential kind: "
                            + credentialKind
            );
        }
    }

    private void validateSingleMembershipCardOnCreate(
            Long memberId,
            String credentialKind
    ) {
        if (!MEMBERSHIP_CARD_KIND.equals(
                credentialKind
        )) {
            return;
        }

        boolean alreadyHasMembershipCard =
                memberCredentialRepository
                        .existsByMemberIdAndCredentialKindIgnoreCase(
                                memberId,
                                MEMBERSHIP_CARD_KIND
                        );

        if (alreadyHasMembershipCard) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has a membership card"
            );
        }
    }

    private void validateSingleMembershipCardOnUpdate(
            Long memberId,
            Long credentialId,
            String credentialKind
    ) {
        if (!MEMBERSHIP_CARD_KIND.equals(
                credentialKind
        )) {
            return;
        }

        boolean alreadyHasAnotherMembershipCard =
                memberCredentialRepository
                        .existsByMemberIdAndCredentialKindIgnoreCaseAndIdNot(
                                memberId,
                                MEMBERSHIP_CARD_KIND,
                                credentialId
                        );

        if (alreadyHasAnotherMembershipCard) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has another membership card"
            );
        }
    }

    private void validateUniqueCredentialNoForCreate(
            String credentialNo
    ) {
        if (credentialNo == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential number is required"
            );
        }

        if (memberCredentialRepository
                .existsByCredentialNo(
                        credentialNo
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Credential number already exists: "
                            + credentialNo
            );
        }
    }

    private void validateUniqueCredentialNoForUpdate(
            Long credentialId,
            String credentialNo
    ) {
        if (credentialNo == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential number is required"
            );
        }

        if (memberCredentialRepository
                .existsByCredentialNoAndIdNot(
                        credentialNo,
                        credentialId
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Credential number already exists: "
                            + credentialNo
            );
        }
    }

    private String normalizeCredentialKind(
            String credentialKind
    ) {
        if (credentialKind == null
                || credentialKind.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential kind is required"
            );
        }

        String normalizedKind =
                credentialKind
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalizedKind) {
            case MEMBERSHIP_CARD_KIND,
                 ACTIVITY_CERTIFICATE_KIND,
                 APPOINTMENT_LETTER_KIND ->
                    normalizedKind;

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unsupported credential kind: "
                                    + credentialKind
                    );
        };
    }

    private String normalizeCredentialNo(
            String credentialNo
    ) {
        if (credentialNo == null
                || credentialNo.isBlank()) {
            return null;
        }

        return credentialNo.trim();
    }

    private String generateMembershipCardNumber(
            Long memberId
    ) {
        return String.format(
                "TNAL-CARD-%05d",
                memberId
        );
    }

    private Long resolveCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        return userDetails.getUserId();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberCredentialTabResponse getCredentialTab(
            Long memberId
    ) {
        validateMemberExists(
                memberId
        );

        List<MemberCredentialResponse> credentials =
                memberCredentialRepository
                        .findAllByMemberIdOrderByCreatedAtDesc(
                                memberId
                        )
                        .stream()
                        .map(memberCredentialMapper::toResponse)
                        .toList();

        MemberCredentialResponse membershipCard =
                credentials
                        .stream()
                        .filter(credential ->
                                credential.getCredentialKind() != null
                                        && MEMBERSHIP_CARD_KIND.equals(
                                        credential
                                                .getCredentialKind()
                                                .getCode()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        List<MemberCredentialResponse> certificates =
                credentials
                        .stream()
                        .filter(credential ->
                                credential.getCredentialKind() != null
                                        && ACTIVITY_CERTIFICATE_KIND.equals(
                                        credential
                                                .getCredentialKind()
                                                .getCode()
                                )
                        )
                        .toList();

        List<MemberCredentialResponse> appointmentLetters =
                credentials
                        .stream()
                        .filter(credential ->
                                credential.getCredentialKind() != null
                                        && APPOINTMENT_LETTER_KIND.equals(
                                        credential
                                                .getCredentialKind()
                                                .getCode()
                                )
                        )
                        .toList();

        return new MemberCredentialTabResponse(
                membershipCard,
                certificates,
                appointmentLetters
        );
    }
}