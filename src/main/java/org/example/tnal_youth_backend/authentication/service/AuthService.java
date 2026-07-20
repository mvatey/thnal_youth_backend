package org.example.tnal_youth_backend.authentication.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.model.request.RefreshTokenRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.model.response.LoginResponse;
import org.example.tnal_youth_backend.authentication.model.response.RefreshTokenResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserProfileResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    ApiResponse logout(RefreshTokenRequest request);

    UserProfileResponse getCurrentUser();
}