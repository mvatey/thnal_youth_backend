package org.example.tnal_youth_backend.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bound from {@code app.notification.*} in application.properties.
 *
 * <p>{@code allowedLinkHosts} is the allow-list an absolute notification
 * {@code actionUrl} may target. App-relative links ({@code /programs/12}) are
 * always allowed regardless of this list. Defaults are provided so the module is
 * safe out of the box even if the property is omitted.
 */
@Component
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

    /** Hosts an absolute http(s) actionUrl may point at. Compared case-insensitively. */
    private Set<String> allowedLinkHosts = new LinkedHashSet<>(Set.of("cyna.org", "www.cyna.org"));

    public Set<String> getAllowedLinkHosts() {
        return allowedLinkHosts;
    }

    public void setAllowedLinkHosts(Set<String> allowedLinkHosts) {
        this.allowedLinkHosts = allowedLinkHosts;
    }
}
