package org.example.tnal_youth_backend.donation.controller;

import org.example.tnal_youth_backend.authentication.config.JwtAuthenticationFilter;
import org.example.tnal_youth_backend.common.exception.GlobalExceptionHandler;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.dto.DonationDTO;
import org.example.tnal_youth_backend.donation.dto.DonationPageDTO;
import org.example.tnal_youth_backend.donation.dto.DonationSummaryDTO;
import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization-contract test for the donation endpoints — the donation twin of
 * {@code NotificationControllerSecurityTest}.
 *
 * <p>Mounts a chain that mirrors the RELEVANT production rule from
 * {@code SecurityConfig}: {@code anyRequest().authenticated()} plus
 * {@code @EnableMethodSecurity} for the {@code @PreAuthorize} annotations on the
 * controller. The JWT filter is excluded; principals are simulated with
 * {@code @WithMockUser}/{@code @WithAnonymousUser}, so this asserts the
 * ACCESS-CONTROL wiring independently of token mechanics.
 *
 * <p>Rules under test:
 * <ul>
 *   <li>create / get / list / summary / update: STAFF
 *       (ADMIN, SECRETARY, BRANCH_LEADER) allowed; MEMBER 403; anonymous 4xx.</li>
 *   <li>delete: ADMIN only — SECRETARY (a STAFF role) is 403, proving delete is
 *       strictly narrower than the rest of the module.</li>
 *   <li>service is never reached when access is denied.</li>
 * </ul>
 */
@WebMvcTest(
        controllers = DonationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        DonationControllerSecurityTest.AuthenticatedSecurityConfig.class,
        GlobalExceptionHandler.class
})
class DonationControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private DonationService service;

    // ============================================================ create rule

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_admin_isAllowed() throws Exception {
        stubCreate();
        mvc.perform(post("/api/donations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    void create_secretary_isAllowed() throws Exception {
        stubCreate();
        mvc.perform(post("/api/donations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BRANCH_LEADER")
    void create_branchLeader_isAllowed() throws Exception {
        stubCreate();
        mvc.perform(post("/api/donations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_member_isForbidden() throws Exception {
        mvc.perform(post("/api/donations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().isForbidden());

        verify(service, never()).create(any());
    }

    @Test
    @WithAnonymousUser
    void create_anonymous_isRejected() throws Exception {
        mvc.perform(post("/api/donations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreateBody())))
                .andExpect(status().is4xxClientError());

        verify(service, never()).create(any());
    }

    // ======================================================= read rules (STAFF)

    @Test
    @WithMockUser(roles = "BRANCH_LEADER")
    void get_staff_isAllowed() throws Exception {
        when(service.get(anyLong())).thenReturn(DonationDTO.builder().id(5L).build());
        mvc.perform(get("/api/donations/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void get_member_isForbidden() throws Exception {
        mvc.perform(get("/api/donations/5"))
                .andExpect(status().isForbidden());
        verify(service, never()).get(anyLong());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    void list_staff_isAllowed() throws Exception {
        when(service.list(any(), any(), any(), any(), any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new DonationPageDTO(List.of(), 0L, 0, 20));
        mvc.perform(get("/api/donations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void list_member_isForbidden() throws Exception {
        mvc.perform(get("/api/donations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void summary_staff_isAllowed() throws Exception {
        when(service.summary(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DonationSummaryDTO(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        mvc.perform(get("/api/donations/summary"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void summary_anonymous_isRejected() throws Exception {
        mvc.perform(get("/api/donations/summary"))
                .andExpect(status().is4xxClientError());
    }

    // =========================================================== update (STAFF)

    @Test
    @WithMockUser(roles = "SECRETARY")
    void update_staff_isAllowed() throws Exception {
        when(service.update(anyLong(), any(DonationUpdateDTO.class)))
                .thenReturn(DonationDTO.builder().id(5L).build());
        mvc.perform(put("/api/donations/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validUpdateBody())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void update_member_isForbidden() throws Exception {
        mvc.perform(put("/api/donations/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validUpdateBody())))
                .andExpect(status().isForbidden());
        verify(service, never()).update(anyLong(), any());
    }

    // ==================================================== delete rule (ADMIN only)

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_admin_isAllowed() throws Exception {
        mvc.perform(delete("/api/donations/5").with(csrf()))
                .andExpect(status().isOk());
        verify(service).delete(5L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    void delete_secretary_isForbidden() throws Exception {
        // SECRETARY is STAFF (can create/read/update) but delete is ADMIN-only.
        mvc.perform(delete("/api/donations/5").with(csrf()))
                .andExpect(status().isForbidden());
        verify(service, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "BRANCH_LEADER")
    void delete_branchLeader_isForbidden() throws Exception {
        mvc.perform(delete("/api/donations/5").with(csrf()))
                .andExpect(status().isForbidden());
        verify(service, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void delete_member_isForbidden() throws Exception {
        mvc.perform(delete("/api/donations/5").with(csrf()))
                .andExpect(status().isForbidden());
        verify(service, never()).delete(anyLong());
    }

    @Test
    @WithAnonymousUser
    void delete_anonymous_isRejected() throws Exception {
        mvc.perform(delete("/api/donations/5").with(csrf()))
                .andExpect(status().is4xxClientError());
        verify(service, never()).delete(anyLong());
    }

    // ---------------------------------------------------------------- helpers

    private void stubCreate() {
        when(service.create(any(DonationCreateDTO.class)))
                .thenReturn(new DonationCreateResultDTO(
                        1L, "DON-20260727-000001", new BigDecimal("10.00"),
                        OffsetDateTime.parse("2026-07-27T08:00:00Z")));
    }

    /** Only the @NotNull-bean-validated fields are required to reach the service. */
    private DonationCreateDTO validCreateBody() {
        var dto = new DonationCreateDTO();
        dto.setDonationTypeId((short) 3);
        dto.setDonorName("Walk-in donor");
        dto.setBranchId(1L);
        dto.setAmountUsd(new BigDecimal("10.00"));
        dto.setPaymentMethodId((short) 1);
        dto.setPaidAt(OffsetDateTime.parse("2026-07-24T10:15:00Z"));
        return dto;
    }

    private DonationUpdateDTO validUpdateBody() {
        var dto = new DonationUpdateDTO();
        dto.setDonationTypeId((short) 3);
        dto.setDonorName("Walk-in donor");
        dto.setBranchId(1L);
        dto.setAmountUsd(new BigDecimal("30.00"));
        dto.setPaymentMethodId((short) 1);
        dto.setPaidAt(OffsetDateTime.parse("2026-07-24T10:15:00Z"));
        return dto;
    }

    /**
     * Mirrors the production access rule for these routes: authenticated by
     * default, method-security enforcing the per-endpoint @PreAuthorize. CSRF is
     * disabled exactly as in the real SecurityConfig.
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
