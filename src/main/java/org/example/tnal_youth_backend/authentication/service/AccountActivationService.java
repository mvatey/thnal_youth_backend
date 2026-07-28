package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.request.SendActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.request.SetActivationPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.VerifyActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;

public interface AccountActivationService {

    ApiResponse sendActivationOtp(
            SendActivationOtpRequest request
    );

    ApiResponse verifyActivationOtp(
            VerifyActivationOtpRequest request
    );

    ApiResponse setInitialPassword(
            SetActivationPasswordRequest request
    );
}