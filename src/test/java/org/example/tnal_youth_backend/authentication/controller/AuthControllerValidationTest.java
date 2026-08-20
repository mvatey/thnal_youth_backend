package org.example.tnal_youth_backend.authentication.controller;

import org.example.tnal_youth_backend.authentication.service.AuthService;
import org.example.tnal_youth_backend.authentication.service.ForgotPasswordService;
import org.example.tnal_youth_backend.config.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(AuthControllerValidationTest.PermitAllSecurityConfig.class)
class AuthControllerValidationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ForgotPasswordService forgotPasswordService;

    @Test
    void login_withoutIdentifier_returns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneOrEmail\":\"\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));

        verify(authService, never()).login(any(), any());
    }

    @Test
    void login_withoutPassword_returns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneOrEmail\":\"member@example.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(), any());
    }

    @Test
    void refresh_withoutToken_returns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refresh(any());
    }

    @Test
    void refresh_withOversizedToken_returns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + "x".repeat(37) + "\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refresh(any());
    }

    @Test
    void logout_withBlankToken_returns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(any());
    }

    @Test
    void forgotPassword_withoutDeliveryChannel_returns400WithoutCallingService()
            throws Exception {
        mvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneOrEmail\":\"member@example.com\"}"))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).forgotPassword(any());
    }

    @TestConfiguration
    static class PermitAllSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
                throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}
