package org.example.tnal_youth_backend.authentication.service;



import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.model.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}