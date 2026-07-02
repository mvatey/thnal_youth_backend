package org.example.tnal_youth_backend.service;



import org.example.tnal_youth_backend.model.request.LoginRequest;
import org.example.tnal_youth_backend.model.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}