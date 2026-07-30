//package org.example.tnal_youth_backend.donation.controller;
//
//import tools.jackson.databind.ObjectMapper;
//import org.example.tnal_youth_backend.authentication.config.JwtAuthenticationFilter;
//import org.example.tnal_youth_backend.common.exception.BusinessException;
//import org.example.tnal_youth_backend.common.exception.GlobalExceptionHandler;
//import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
//import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
//import org.example.tnal_youth_backend.donation.dto.DonationDTO;
//import org.example.tnal_youth_backend.donation.dto.DonationPageDTO;
//import org.example.tnal_youth_backend.donation.dto.DonationSummaryDTO;
//import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
//import org.example.tnal_youth_backend.donation.service.DonationService;
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
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Web-slice test for {@link DonationController} — the donation twin of
// * {@code NotificationControllerTest}.
// *
// * <p>The service layer is mocked; this verifies HTTP wiring, the
// * {@code ApiResponse} JSON envelope, request-body validation (@NotNull fields +
// * the UUID {@code clientRequestId} pattern), query-param binding/defaults, and
// * error mapping via the shared common GlobalExceptionHandler. It does NOT touch
// * the DB or the real SecurityConfig (JWT filter). Authorization rules are covered
// * separately in {@code DonationControllerSecurityTest}; here the chain is
// * permit-all so the focus stays on the JSON contract. {@code @WithMockUser} is
// * still applied so {@link org.example.tnal_youth_backend.security.SecurityUtils}
// * has a principal wherever a handler path needs one.
// */
//@WebMvcTest(
//        controllers = DonationController.class,
//        excludeFilters = @ComponentScan.Filter(
//                type = FilterType.ASSIGNABLE_TYPE,
//                classes = JwtAuthenticationFilter.class
//        )
//)
//@Import({
//        DonationControllerTest.TestSecurityConfig.class,
//        GlobalExceptionHandler.class
//})
//@WithMockUser(roles = "ADMIN")
//class DonationControllerTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private ObjectMapper json;
//
//    @MockitoBean
//    private DonationService service;
//
//    // ---------------------------------------------------------------- create
//
//    @Test
//    void create_returns200_andEchoesServerComputedResult() throws Exception {
//        var result = new DonationCreateResultDTO(
//                100L, "DON-20260727-000042", new BigDecimal("49.39"),
//                OffsetDateTime.parse("2026-07-27T08:00:00Z"));
//        when(service.create(any(DonationCreateDTO.class))).thenReturn(result);
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(validCreateBody())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value(100))
//                .andExpect(jsonPath("$.data.donationNo").value("DON-20260727-000042"))
//                // totalAmountUsd is server-computed; the client must see it echoed back
//                .andExpect(jsonPath("$.data.totalAmountUsd").value(49.39));
//    }
//
//    @Test
//    void create_missingDonationTypeId_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setDonationTypeId(null); // fails @NotNull
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_missingBranchId_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setBranchId(null); // fails @NotNull
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_missingPaymentMethodId_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setPaymentMethodId(null); // fails @NotNull
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_missingPaidAt_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setPaidAt(null); // fails @NotNull
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_badClientRequestId_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setClientRequestId("not-a-uuid"); // fails @Pattern
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_negativeAmount_returns400_validation() throws Exception {
//        var body = validCreateBody();
//        body.setAmountUsd(new BigDecimal("-1.00")); // fails @DecimalMin("0.00")
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).create(any());
//    }
//
//    @Test
//    void create_serviceBusinessException_mapsTo400WithErrorCode() throws Exception {
//        when(service.create(any(DonationCreateDTO.class)))
//                .thenThrow(new BusinessException("DONATION_SOURCE_INVALID",
//                        "Exactly one donor source is required: memberId, sponsorId, or donorName"));
//
//        mvc.perform(post("/api/donations")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(validCreateBody())))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.errorCode").value("DONATION_SOURCE_INVALID"));
//    }
//
//    // ------------------------------------------------------------------- get
//
//    @Test
//    void get_returns200AndDonation() throws Exception {
//        var dto = DonationDTO.builder()
//                .id(5L)
//                .donationNo("DON-20260724-000001")
//                .typeCode("SPONSOR_DONATION")
//                .donorDisplay("Acme Foundation")
//                .totalAmountUsd(new BigDecimal("49.39"))
//                .build();
//        when(service.get(5L)).thenReturn(dto);
//
//        mvc.perform(get("/api/donations/5"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value(5))
//                .andExpect(jsonPath("$.data.donationNo").value("DON-20260724-000001"))
//                .andExpect(jsonPath("$.data.donorDisplay").value("Acme Foundation"));
//
//        verify(service).get(5L);
//    }
//
//    @Test
//    void get_missing_mapsBusinessExceptionTo400() throws Exception {
//        when(service.get(77L))
//                .thenThrow(new BusinessException("DONATION_NOT_FOUND", "Donation 77 does not exist"));
//
//        mvc.perform(get("/api/donations/77"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errorCode").value("DONATION_NOT_FOUND"));
//    }
//
//    // ------------------------------------------------------------------ list
//
//    @Test
//    void list_returns200AndPage_andPassesParams() throws Exception {
//        var item = DonationDTO.builder().id(1L).donationNo("DON-20260724-000001").build();
//        var page = new DonationPageDTO(List.of(item), 1L, 0, 20);
//        when(service.list(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
//                any(), any(), eq("acme"), eq(0), eq(20)))
//                .thenReturn(page);
//
//        mvc.perform(get("/api/donations")
//                        .param("branchId", "1")
//                        .param("search", "acme")
//                        .param("paidFrom", "2026-07-01T00:00:00Z")
//                        .param("paidTo", "2026-07-31T23:59:59Z")
//                        .param("page", "0")
//                        .param("size", "20"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.total").value(1))
//                .andExpect(jsonPath("$.data.items[0].donationNo").value("DON-20260724-000001"));
//
//        verify(service).list(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
//                any(), any(), eq("acme"), eq(0), eq(20));
//    }
//
//    @Test
//    void list_usesDefaults_whenParamsOmitted() throws Exception {
//        when(service.list(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
//                isNull(), isNull(), isNull(), anyInt(), anyInt()))
//                .thenReturn(new DonationPageDTO(List.of(), 0L, 0, 20));
//
//        mvc.perform(get("/api/donations"))
//                .andExpect(status().isOk());
//
//        // controller defaults: page=0, size=20; all filters null
//        verify(service).list(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
//                isNull(), isNull(), isNull(), eq(0), eq(20));
//    }
//
//    // --------------------------------------------------------------- summary
//
//    @Test
//    void summary_returns200AndTotals() throws Exception {
//        var summary = new DonationSummaryDTO(
//                3L, new BigDecimal("149.39"), new BigDecimal("100000.00"), new BigDecimal("60.00"));
//        when(service.summary(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
//                any(), any(), isNull()))
//                .thenReturn(summary);
//
//        mvc.perform(get("/api/donations/summary")
//                        .param("branchId", "1")
//                        .param("paidFrom", "2026-07-01T00:00:00Z")
//                        .param("paidTo", "2026-07-31T23:59:59Z"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.count").value(3))
//                .andExpect(jsonPath("$.data.sumTotalUsd").value(149.39));
//    }
//
//    // ---------------------------------------------------------------- update
//
//    @Test
//    void update_returns200AndUpdatedDonation() throws Exception {
//        var dto = DonationDTO.builder()
//                .id(5L)
//                .donationNo("DON-20260724-000001")
//                .totalAmountUsd(new BigDecimal("30.00"))
//                .build();
//        when(service.update(eq(5L), any(DonationUpdateDTO.class))).thenReturn(dto);
//
//        mvc.perform(put("/api/donations/5")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(validUpdateBody())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value(5))
//                .andExpect(jsonPath("$.data.totalAmountUsd").value(30.00));
//
//        verify(service).update(eq(5L), any(DonationUpdateDTO.class));
//    }
//
//    @Test
//    void update_missingDonationTypeId_returns400_validation() throws Exception {
//        var body = validUpdateBody();
//        body.setDonationTypeId(null); // fails @NotNull
//
//        mvc.perform(put("/api/donations/5")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//
//        verify(service, never()).update(any(), any());
//    }
//
//    // ---------------------------------------------------------------- delete
//
//    @Test
//    void delete_returns200AndBoolean() throws Exception {
//        mvc.perform(delete("/api/donations/5").with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value(true));
//
//        verify(service).delete(5L);
//    }
//
//    // ---------------------------------------------------------------- helpers
//
//    /** A valid sponsor-donation create body: all @NotNull fields present, valid amount. */
//    private DonationCreateDTO validCreateBody() {
//        var dto = new DonationCreateDTO();
//        dto.setDonationTypeId((short) 3);
//        dto.setSponsorId(1L);
//        dto.setBranchId(1L);
//        dto.setAmountUsd(new BigDecimal("25.00"));
//        dto.setAmountKhr(new BigDecimal("100000.00"));
//        dto.setExchangeRateKhrPerUsd(new BigDecimal("4100.0000"));
//        dto.setPaymentMethodId((short) 2);
//        dto.setPaidAt(OffsetDateTime.parse("2026-07-24T09:00:00Z"));
//        dto.setPaymentReference("ABA-TXN-0001");
//        return dto;
//    }
//
//    private DonationUpdateDTO validUpdateBody() {
//        var dto = new DonationUpdateDTO();
//        dto.setDonationTypeId((short) 3);
//        dto.setSponsorId(1L);
//        dto.setBranchId(1L);
//        dto.setAmountUsd(new BigDecimal("30.00"));
//        dto.setPaymentMethodId((short) 2);
//        dto.setPaidAt(OffsetDateTime.parse("2026-07-24T09:00:00Z"));
//        return dto;
//    }
//
//    /**
//     * Permissive chain so the slice doesn't require the JWT filter. csrf() tokens
//     * are still supplied by the tests to mirror production (real SecurityConfig
//     * disables CSRF, so this is belt-and-braces). Authorization is asserted in
//     * {@code DonationControllerSecurityTest}, not here.
//     */
//    @TestConfiguration
//    @EnableMethodSecurity
//    static class TestSecurityConfig {
//
//        @Bean
//        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//            http
//                    .csrf(AbstractHttpConfigurer::disable)
//                    .authorizeHttpRequests(auth ->
//                            auth.anyRequest().permitAll()
//                    );
//
//            return http.build();
//        }
//    }
//}
