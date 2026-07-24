package org.example.tnal_youth_backend.authentication.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Swagger
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        )
                        .permitAll()

                        /*
                         * Authentication
                         */
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        )
                        .permitAll()

                        .requestMatchers("/api/auth/me")
                        .authenticated()

                        /*
                         * Admin
                         */
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        /*
                         * Branch
                         */
                        .requestMatchers("/api/branch/**")
                        .hasAnyRole(
                                "ADMIN",
                                "BRANCH_LEADER"
                        )

                        /*
                         * Secretary
                         */
                        .requestMatchers("/api/secretary/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY"
                        )

                        /*
                         * Member Management
                         *
                         * Admin
                         * Secretary
                         * Branch Leader
                         */
                        .requestMatchers("/api/members/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * Member APIs
                         *
                         * All logged-in users
                         */
                        .requestMatchers("/api/member/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER",
                                "MEMBER"
                        )

                        /*
                         * Donation Management
                         *
                         * Members can view their own donations
                         * through /api/my-account/donations.
                         *
                         * They cannot create, update, or delete
                         * donations from /api/donations.
                         */

                                /*
                                 * Sponsor Management
                                 */
                                .requestMatchers("/api/sponsors/**")
                                .hasAnyRole(
                                        "ADMIN",
                                        "SECRETARY",
                                        "BRANCH_LEADER"
                                )

                        //  Donation Managemen
                        .requestMatchers("/api/donations/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * My Account
                         *
                         * Every authenticated user can manage
                         * their own account and view their own
                         * donation history.
                         */
                        .requestMatchers("/api/my-account/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER",
                                "MEMBER"
                        )

                        /*
                         * Everything else
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