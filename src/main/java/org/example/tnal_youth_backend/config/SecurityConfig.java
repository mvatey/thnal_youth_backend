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
    private final ViewerWriteBlockFilter viewerWriteBlockFilter;

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
                                "/api/auth/reset-password",
                                "/api/auth/activation/**",
                                "/api/auth/account-status"
                        )
                        .permitAll()

                        /*
                         * Bot-to-server Telegram callback. The controller
                         * validates its dedicated X-Bot-Secret and fails
                         * closed when that secret is not configured.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/telegram/link"
                        )
                        .permitAll()

                        /*
                         * Telegram's own webhook callback (production only —
                         * local dev uses TelegramPollingScheduler instead,
                         * which needs no inbound route at all). Validated by
                         * the X-Telegram-Bot-Api-Secret-Token header, not a
                         * session.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/telegram/webhook"
                        )
                        .permitAll()

                        /*
                         * Current authenticated user.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me"
                        )
                        .authenticated()

                        /*
                         * Retired legacy user-management namespace.
                         *
                         * The application now uses /api/admin/users/** for
                         * administrative user management and /api/auth/me
                         * for current-user self-service. Keep /api/users/**
                         * closed so an old CRUD controller cannot become a
                         * second, less-protected management path again.
                         */
                        .requestMatchers("/api/users/**")
                        .denyAll()

                        /*
                         * Dashboard.
                         *
                         * VIEWER has the same read access ADMIN has here —
                         * it is never added to any mutating rule below.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/dashboard/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARY",
                                "BRANCH_LEADER",
                                "VIEWER"
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
                                "SECRETARY",
                                "BRANCH_LEADER"
                        )

                        /*
                         * View activity lists and details.
                         *
                         * VIEWER has the same read access ADMIN has here —
                         * it is never added to any mutating rule below.
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
                                "MEMBER",
                                "VIEWER"
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
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/admin/**"
                        )
                        .hasAnyRole("ADMIN", "VIEWER")

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
                         *
                         * Most GET (view) endpoints in this app rely solely
                         * on this catch-all rather than a role-specific rule
                         * above, so VIEWER already reaches them for free.
                         * Write endpoints (POST/PUT/PATCH/DELETE) still fall
                         * through to here too where no rule above already
                         * restricts them — those are protected by
                         * method-level @PreAuthorize on the controller
                         * instead, none of which lists VIEWER.
                         */
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        viewerWriteBlockFilter,
                        JwtAuthenticationFilter.class
                );

        return http.build();
    }
}
