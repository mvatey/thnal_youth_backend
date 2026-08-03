package org.example.tnal_youth_backend.notification.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.tnal_youth_backend.notification.config.NotificationProperties;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wires {@link SafeLink} to the pure {@link LinkUrlSafety} logic, sourcing the
 * host allow-list from {@link NotificationProperties}.
 *
 * <p>Spring Boot's {@code LocalValidatorFactoryBean} is Spring-aware, so this
 * validator is created via constructor injection (the {@code NotificationProperties}
 * bean is supplied by the context). The allow-list is lowercased once at
 * construction to match {@link LinkUrlSafety}, which lowercases the parsed host.
 */
public class SafeLinkValidator implements ConstraintValidator<SafeLink, String> {

    private final Set<String> allowedHosts;

    public SafeLinkValidator(NotificationProperties properties) {
        this.allowedHosts = properties.getAllowedLinkHosts().stream()
                .filter(h -> h != null && !h.isBlank())
                .map(h -> h.strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return LinkUrlSafety.isSafe(value, allowedHosts);
    }
}
