package org.example.tnal_youth_backend.authentication.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneOrEmail)
            throws UsernameNotFoundException {

        if (phoneOrEmail == null || phoneOrEmail.isBlank()) {
            throw new UsernameNotFoundException(
                    "Phone number or email is required"
            );
        }

        String identifier = phoneOrEmail.trim();

        // See JwtAuthenticationFilter's identical fix -- findByEmailOrPhone
        // alone misses a "None" contact-method account (loginUsername is
        // the only identifier it has), same gap, same fix.
        User user = userRepository
                .findByLoginUsernameOrEmailOrPhone(identifier, identifier, identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        return new CustomUserDetails(user);
    }
}