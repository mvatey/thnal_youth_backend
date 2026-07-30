//package org.example.tnal_youth_backend.notification.controller;
//
//import org.example.tnal_youth_backend.authentication.config.JwtAuthenticationFilter;
//import org.example.tnal_youth_backend.common.exception.GlobalExceptionHandler;
//import org.example.tnal_youth_backend.notification.config.NotificationProperties;
//import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
//import org.example.tnal_youth_backend.notification.dto.NotificationCreateResultDTO;
//import org.example.tnal_youth_backend.notification.dto.NotificationPageDTO;
//import org.example.tnal_youth_backend.notification.service.NotificationService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.test.context.support.WithAnonymousUser;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Authorization tests for notification endpoints.
// *
// * <p>The JWT filter is excluded. Test users are provided through
// * {@code @WithMockUser} and {@code @WithAnonymousUser}.</p>
// */
//@WebMvcTest(
//        controllers = NotificationController.class,
//        excludeFilters = @ComponentScan.Filter(
//                type = FilterType.ASSIGNABLE_TYPE,
//                classes = JwtAuthenticationFilter.class
//        )
//)
//@Import({
//        NotificationControllerSecurityTest.AuthenticatedSecurityConfig.class,
//        GlobalExceptionHandler.class,
//        NotificationProperties.class
//})
//class NotificationControllerSecurityTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @MockitoBean
//    private NotificationService service;
//
//    // ============================================================
//    // Create notification
//    // ============================================================
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void create_admin_isAllowed() throws Exception {
//        NotificationCreateResultDTO result =
//                new NotificationCreateResultDTO(
//                        1L,
//                        1,
//                        OffsetDateTime.parse("2026-07-23T06:00:00Z")
//                );
//
//        when(service.create(any(NotificationCreateDTO.class)))
//                .thenReturn(result);
//
//        mvc.perform(
//                        post("/api/notifications")
//                                .with(csrf())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(validCreateJson())
//                )
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithMockUser(roles = "MEMBER")
//    void create_member_isForbidden() throws Exception {
//        mvc.perform(
//                        post("/api/notifications")
//                                .with(csrf())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(validCreateJson())
//                )
//                .andExpect(status().isForbidden());
//
//        verify(service, never())
//                .create(any(NotificationCreateDTO.class));
//    }
//
//    @Test
//    @WithAnonymousUser
//    void create_anonymous_isRejected() throws Exception {
//        mvc.perform(
//                        post("/api/notifications")
//                                .with(csrf())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(validCreateJson())
//                )
//                .andExpect(status().is4xxClientError());
//
//        verify(service, never())
//                .create(any(NotificationCreateDTO.class));
//    }
//
//    // ============================================================
//    // Inbox
//    // ============================================================
//
//    @Test
//    @WithMockUser(roles = "MEMBER")
//    void listMine_authenticated_isAllowed() throws Exception {
//        when(service.listMine(
//                anyBoolean(),
//                anyInt(),
//                anyInt()
//        )).thenReturn(
//                new NotificationPageDTO(
//                        List.of(),
//                        0L,
//                        0,
//                        20
//                )
//        );
//
//        mvc.perform(get("/api/notifications/me"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithAnonymousUser
//    void listMine_anonymous_isRejected() throws Exception {
//        mvc.perform(get("/api/notifications/me"))
//                .andExpect(status().is4xxClientError());
//
//        verify(service, never())
//                .listMine(
//                        anyBoolean(),
//                        anyInt(),
//                        anyInt()
//                );
//    }
//
//    @Test
//    @WithAnonymousUser
//    void unreadCount_anonymous_isRejected() throws Exception {
//        mvc.perform(get("/api/notifications/me/unread-count"))
//                .andExpect(status().is4xxClientError());
//
//        verify(service, never()).unreadCount();
//    }
//
//    @Test
//    @WithAnonymousUser
//    void markAllRead_anonymous_isRejected() throws Exception {
//        mvc.perform(
//                        post("/api/notifications/me/read-all")
//                                .with(csrf())
//                )
//                .andExpect(status().is4xxClientError());
//
//        verify(service, never()).markAllRead();
//    }
//
//    // ============================================================
//    // Helpers
//    // ============================================================
//
//    private String validCreateJson() {
//        return """
//                {
//                  "typeId": 1,
//                  "title": "System maintenance",
//                  "body": "The system will be down tonight.",
//                  "actionUrl": "/announcements/1",
//                  "target": "ALL"
//                }
//                """;
//    }
//
//    /**
//     * Test security configuration.
//     *
//     * <p>All routes require authentication. Controller method authorization,
//     * such as ADMIN-only creation, is handled by method security.</p>
//     */
//    @TestConfiguration
//    @EnableMethodSecurity
//    static class AuthenticatedSecurityConfig {
//
//        @Bean
//        SecurityFilterChain testSecurityFilterChain(
//                HttpSecurity http
//        ) throws Exception {
//
//            http
//                    .csrf(AbstractHttpConfigurer::disable)
//                    .authorizeHttpRequests(
//                            authorization ->
//                                    authorization
//                                            .anyRequest()
//                                            .authenticated()
//                    );
//
//            return http.build();
//        }
//    }
//}