package org.example.tnal_youth_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                /*
                 * Disable Spring's default login page and HTTP Basic login.
                 * This project authenticates through JWT endpoints.
                 */
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Swagger and error endpoint.
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error",
                                "/uploads/**"
                        )
                        .permitAll()

                        /*
                         * Public authentication endpoints.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/activation/**",
                                "/api/auth/account-status"
                        )
                        .permitAll()

                        /*
                         * Current authenticated user.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me",
                                "/api/users/me"
                        )
                        .authenticated()

                        /*
                         * Dashboard.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/dashboard/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * Create activity.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/activities",
                                "/api/activities/"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * View activity lists and details.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/activities",
                                "/api/activities/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER",
                                "MEMBER"
                        )

                        /*
                         * Update activities.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/activities/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/activities/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * Delete activities.
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/activities/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY"
                        )

                        /*
                         * Feature-level permissions.
                         */
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/branch/**")
                        .hasAnyRole(
                                "ADMIN",
                                "BRANCH_LEADER"
                        )

                        .requestMatchers("/api/secretary/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY"
                        )

                        .requestMatchers("/api/member/**")
                        .hasAnyRole(
                                "ADMIN",
                                "BRANCH_LEADER",
                                "SECRETARY",
                                "MEMBER"
                        )

                        /*
                         * Everything else requires a valid JWT.
                         */
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}