package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.request.ForgotPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.ResetPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;

public interface ForgotPasswordService {

    ApiResponse forgotPassword(
            ForgotPasswordRequest request
    );


    ApiResponse resetPassword(
            ResetPasswordRequest request
    );

}