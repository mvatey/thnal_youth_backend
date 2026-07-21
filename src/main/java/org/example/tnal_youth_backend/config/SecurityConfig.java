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
                /*
                 * REST API using JWT, so CSRF sessions are not used.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * Uses your CorsConfig configuration.
                 */
                .cors(Customizer.withDefaults())

                /*
                 * Do not create an HTTP session.
                 * Every secured request must contain a valid JWT.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Swagger and Spring error endpoint.
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
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
                                "/api/auth/reset-password"
                        )
                        .permitAll()

                        /*
                         * Current authenticated user's profile.
                         *
                         * Keep both temporarily if your project has used
                         * both route names. Later, remove the unused route.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me",
                                "/api/users/me"
                        )
                        .authenticated()

                        /*
                         * ==================================================
                         * ACTIVITY PERMISSIONS
                         * ==================================================
                         */

                        /*
                         * Create an activity:
                         * ADMIN, SECRETARY and BRANCH_LEADER only.
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
                         * Any authenticated system user can view them.
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
                         * Edit an activity.
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
                         * Delete an activity.
                         *
                         * For now ADMIN and SECRETARY can delete.
                         * You can allow BRANCH_LEADER later with branch-scope
                         * validation inside the service.
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
                         * ==================================================
                         * FEATURE-LEVEL PERMISSIONS
                         * ==================================================
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
                         * Every other endpoint requires authentication.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * Read JWT before Spring's username/password filter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}