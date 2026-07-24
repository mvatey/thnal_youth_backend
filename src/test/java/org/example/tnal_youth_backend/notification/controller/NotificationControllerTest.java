package org.example.tnal_youth_backend.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tnal_youth_backend.authentication.config.JwtAuthenticationFilter;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.common.exception.GlobalExceptionHandler;
import org.example.tnal_youth_backend.notification.config.NotificationProperties;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateResultDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-slice test for {@link NotificationController}.
 *
 * <p>The service layer is mocked — this verifies HTTP wiring, JSON contract,
 * request-body validation (including the new {@link org.example.tnal_youth_backend.notification.validation.SafeLink}
 * constraint), {@code @PreAuthorize} on create, and error mapping via the shared
 * common GlobalExceptionHandler. It does NOT touch the DB or the real
 * SecurityConfig (JWT filter). {@link NotificationProperties} is imported so the
 * SafeLink validator can be constructed with the default host allow-list.
 */
@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        NotificationControllerTest.TestSecurityConfig.class,
        GlobalExceptionHandler.class,
        NotificationProperties.class
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private NotificationService service;

    // ---------------------------------------------------------------- create

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns200AndResult() throws Exception {
        var result = new NotificationCreateResultDTO(42L, 7, OffsetDateTime.parse("2026-07-23T06:00:00Z"));
        when(service.create(any(NotificationCreateDTO.class))).thenReturn(result);

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value(42))
                .andExpect(jsonPath("$.data.recipientCount").value(7));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_asNonAdmin_returns403_andServiceNotCalled() throws Exception {
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
                .andExpect(status().is4xxClientError()); // 401/403 depending on entry point

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankTitle_returns400_validation() throws Exception {
        var body = validCreateBody();
        body.setTitle("   "); // fails @NotBlank

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_unsafeSchemeActionUrl_returns400_validation() throws Exception {
        var body = validCreateBody();
        body.setActionUrl("javascript:alert(1)"); // fails @SafeLink

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_offSiteActionUrl_returns400_validation() throws Exception {
        // This is the regression the old @Pattern MISSED: an absolute http(s) URL
        // on an off-site host used to pass because it started with "https://".
        var body = validCreateBody();
        body.setActionUrl("https://evil.example.com/phish");

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_protocolRelativeActionUrl_returns400_validation() throws Exception {
        var body = validCreateBody();
        body.setActionUrl("//evil.example.com/phish"); // protocol-relative, also missed before

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_allowedHostActionUrl_returns200() throws Exception {
        var result = new NotificationCreateResultDTO(43L, 1, OffsetDateTime.parse("2026-07-23T06:00:00Z"));
        when(service.create(any(NotificationCreateDTO.class))).thenReturn(result);

        var body = validCreateBody();
        body.setActionUrl("https://cyna.org/announcements/1"); // on the allow-list

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_badClientRequestId_returns400_validation() throws Exception {
        var body = validCreateBody();
        body.setClientRequestId("not-a-uuid");

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_serviceBusinessException_mapsTo400WithErrorCode() throws Exception {
        when(service.create(any(NotificationCreateDTO.class)))
                .thenThrow(new BusinessException("NOTIF_TYPE_INACTIVE",
                        "Notification type 99 does not exist or is inactive"));

        mvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOTIF_TYPE_INACTIVE"));
    }

    // ---------------------------------------------------------------- inbox

    @Test
    @WithMockUser
    void listMine_returns200AndPage_andPassesParams() throws Exception {
        var item = NotificationDTO.builder()
                .id(1L).typeCode("ANNOUNCEMENT").title("Hello").body("Body")
                .isRead(false).build();
        var page = new NotificationPageDTO(List.of(item), 1L, 0, 20);
        when(service.listMine(true, 0, 20)).thenReturn(page);

        mvc.perform(get("/api/notifications/me")
                        .param("onlyUnread", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].typeCode").value("ANNOUNCEMENT"))
                .andExpect(jsonPath("$.data.items[0].isRead").value(false));

        verify(service).listMine(true, 0, 20);
    }

    @Test
    @WithMockUser
    void listMine_usesDefaults_whenParamsOmitted() throws Exception {
        when(service.listMine(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new NotificationPageDTO(List.of(), 0L, 0, 20));

        mvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk());

        // controller defaults: onlyUnread=false, page=0, size=20
        verify(service).listMine(eq(false), eq(0), eq(20));
    }

    @Test
    @WithMockUser
    void unreadCount_returns200AndCount() throws Exception {
        when(service.unreadCount()).thenReturn(5L);

        mvc.perform(get("/api/notifications/me/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread").value(5));
    }

    // ------------------------------------------------------------ read state

    @Test
    @WithMockUser
    void markRead_returns200AndBoolean() throws Exception {
        when(service.markRead(9L)).thenReturn(true);

        mvc.perform(post("/api/notifications/me/9/read").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(service).markRead(9L);
    }

    @Test
    @WithMockUser
    void markAllRead_returns200AndAffectedCount() throws Exception {
        when(service.markAllRead()).thenReturn(3);

        mvc.perform(post("/api/notifications/me/read-all").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
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
     * Permissive chain so the slice doesn't require the JWT filter, but
     * {@code @EnableMethodSecurity} keeps {@code @PreAuthorize("hasRole('ADMIN')")}
     * enforced. csrf() tokens are still supplied by the tests to mirror production
     * (real SecurityConfig disables CSRF, so this is belt-and-braces).
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}
