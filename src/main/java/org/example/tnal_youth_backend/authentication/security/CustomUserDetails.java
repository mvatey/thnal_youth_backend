package org.example.tnal_youth_backend.authentication.security;

import lombok.AllArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {

        if (user.getRole() == null
                || user.getRole().name() == null
                || user.getRole().name().isBlank()) {

            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {

        if (user.getEmail() != null
                && !user.getEmail().isBlank()) {

            return user.getEmail();
        }

        return user.getPhone();
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        OffsetDateTime lockedUntil = user.getLockedUntil();

        return lockedUntil == null
                || lockedUntil.isBefore(OffsetDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        return user.getStatus() != null
                && "ACTIVE".equals(
                user.getStatus().name()
        );
    }
}