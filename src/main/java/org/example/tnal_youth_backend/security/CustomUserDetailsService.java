package org.example.tnal_youth_backend.security;



import org.example.tnal_youth_backend.model.entity.User;
import org.example.tnal_youth_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));



        return new CustomUserDetails(user);

    }

}