package org.example.tnal_youth_backend.notification.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean-validation constraint for user-supplied deep links (notification action URLs).
 *
 * <p>Accepts app-relative paths ({@code /programs/12}) and absolute {@code http(s)}
 * URLs whose host is on {@code app.notification.allowed-link-hosts}. Rejects
 * protocol-relative URLs, {@code javascript:}/{@code data:} schemes, and any
 * off-site host. See {@link LinkUrlSafety} for the exact rules.
 */
@Documented
@Constraint(validatedBy = SafeLinkValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeLink {

    String message() default
            "actionUrl must be an app-relative path (starting with /) "
                    + "or an http(s) URL on an allowed host";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
