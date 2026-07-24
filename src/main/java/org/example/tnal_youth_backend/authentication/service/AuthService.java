package org.example.tnal_youth_backend.authentication.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.tnal_youth_backend.authentication.model.request.*;
import org.example.tnal_youth_backend.authentication.model.response.*;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    ApiResponse logout(RefreshTokenRequest request);

    UserProfileResponse getCurrentUser();

    ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    );

    VerifyOtpResponse verifyOtp(
            VerifyOtpRequest request
    );

    ApiResponse resetPassword(
            ResetPasswordRequest request
    );
}