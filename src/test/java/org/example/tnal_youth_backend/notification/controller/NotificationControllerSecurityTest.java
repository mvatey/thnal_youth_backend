package org.example.tnal_youth_backend.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tnal_youth_backend.authentication.config.JwtAuthenticationFilter;
import org.example.tnal_youth_backend.common.exception.GlobalExceptionHandler;
import org.example.tnal_youth_backend.notification.config.NotificationProperties;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateResultDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationPageDTO;
import org.example.tnal_youth_backend.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization-contract test for the notification endpoints.
 *
 * <p>Whereas {@link NotificationControllerTest} uses a permit-all chain (so it can
 * focus on JSON/validation), this test mounts a chain that mirrors the RELEVANT
 * production rule from {@code SecurityConfig}: everything under {@code /api/**}
 * requires authentication ({@code anyRequest().authenticated()}), plus
 * {@code @EnableMethodSecurity} for the {@code @PreAuthorize("hasRole('ADMIN')")}
 * on create. The JWT filter itself is excluded — principals are simulated with
 * {@code @WithMockUser}/{@code @WithAnonymousUser} — so this asserts the
 * ACCESS-CONTROL wiring for the controller independently of token mechanics.
 *
 * <p>What this proves:
 * <ul>
 *   <li>create: ADMIN 200, MEMBER 403, anonymous 4xx, service never reached when denied</li>
 *   <li>inbox (/me, /me/unread-count): authenticated 200, anonymous 4xx</li>
 * </ul>
 */
@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        NotificationControllerSecurityTest.AuthenticatedSecurityConfig.class,
        GlobalExceptionHandler.class,
        NotificationProperties.class
})
class NotificationControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private NotificationService service;

    // ------------------------------------------------------------- create rule

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_admin_isAllowed() throws Exception {
        when(service.create(any(NotificationCreateDTO.class)))
                .thenReturn(new NotificationCreateResultDTO(1L, 1, OffsetDateTime.parse("2026-07-23T06:00:00Z")));

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_member_isForbidden() throws Exception {
        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isForbidden());

        verify(service, never()).create(any());
    }

    @Test
    @WithAnonymousUser
    void create_anonymous_isRejected() throws Exception {
        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().is4xxClientError());

        verify(service, never()).create(any());
    }

    // -------------------------------------------------------------- inbox rule

    @Test
    @WithMockUser(roles = "MEMBER")
    void listMine_authenticated_isAllowed() throws Exception {
        when(service.listMine(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new NotificationPageDTO(List.of(), 0L, 0, 20));

        mvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void listMine_anonymous_isRejected() throws Exception {
        mvc.perform(get("/api/notifications/me"))
                .andExpect(status().is4xxClientError());

        verify(service, never()).listMine(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    @WithAnonymousUser
    void unreadCount_anonymous_isRejected() throws Exception {
        mvc.perform(get("/api/notifications/me/unread-count"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithAnonymousUser
    void markAllRead_anonymous_isRejected() throws Exception {
        mvc.perform(post("/api/notifications/me/read-all").with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    // ---------------------------------------------------------------- helpers

    private NotificationCreateDTO validCreateBody() {
        var dto = new NotificationCreateDTO();
        dto.setTypeId((short) 1);
        dto.setTitle("System maintenance");
        dto.setBody("The system will be down tonight.");
        dto.setActionUrl("/announcements/1");
        dto.setTarget(NotificationCreateDTO.TargetMode.ALL);
        return dto;
    }

    /**
     * Mirrors the production access rule for these routes: authenticated by
     * default, ADMIN-gated create enforced by method security. CSRF is disabled
     * exactly as in the real SecurityConfig.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class AuthenticatedSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }
}
