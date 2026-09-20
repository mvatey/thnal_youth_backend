package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.AccountStatusRequest;
import org.example.tnal_youth_backend.authentication.model.response.AccountStatusResponse;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.AccountStatusService;
import org.example.tnal_youth_backend.authentication.util.PhoneNumberUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountStatusServiceImpl
        implements AccountStatusService {

    private final UserRepository userRepository;

    @Override
    public AccountStatusResponse getAccountStatus(
            AccountStatusRequest request
    ) {
        String identifier =
                normalizeIdentifier(
                        request.phoneOrEmail()
                );

        User user =
                userRepository
                        .findByLoginUsernameOrEmailOrPhone(
                                identifier,
                                identifier,
                                identifier
                        )
                        .orElse(null);

        /*
         * Avoid returning detailed account information
         * for unknown identifiers.
         */
        if (user == null) {
            return new AccountStatusResponse(
                    false,
                    null,
                    false,
                    "ACCOUNT_NOT_FOUND"
            );
        }

        UserStatus status =
                user.getStatus();

        boolean activated =
                user.getActivatedAt() != null;

        String nextStep =
                resolveNextStep(
                        status,
                        activated
                );

        return new AccountStatusResponse(
                true,
                status != null
                        ? status.name()
                        : null,
                activated,
                nextStep
        );
    }

    private String resolveNextStep(
            UserStatus status,
            boolean activated
    ) {
        if (status == null) {
            return "CONTACT_ADMIN";
        }

        if (status == UserStatus.PENDING_ACTIVATION
                && !activated) {
            return "ACTIVATE_ACCOUNT";
        }

        if (status == UserStatus.ACTIVE
                && activated) {
            return "ENTER_PASSWORD";
        }

        if (status == UserStatus.INACTIVE) {
            return "ACCOUNT_INACTIVE";
        }

        if (status == UserStatus.LOCKED) {
            return "ACCOUNT_LOCKED";
        }

        return "CONTACT_ADMIN";
    }

    private String normalizeIdentifier(
            String identifier
    ) {
        String trimmed =
                identifier.trim();

        if (trimmed.contains("@")) {
            return trimmed.toLowerCase();
        }

        // A username (e.g. "Phan Rithy") contains letters/spaces that
        // PhoneNumberUtil.toDatabaseFormat rejects outright -- only run it
        // through phone normalization when the input actually looks like
        // one, otherwise pass it through as-is for the username match.
        if (looksLikePhoneNumber(trimmed)) {
            return PhoneNumberUtil.toDatabaseFormat(
                    trimmed
            );
        }

        return trimmed;
    }

    private boolean looksLikePhoneNumber(
            String value
    ) {
        return value.matches(
                "^[0-9+()\\- ]+$"
        );
    }
}