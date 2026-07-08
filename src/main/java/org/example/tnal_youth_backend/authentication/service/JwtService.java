package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.entity.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, User user);

    boolean isTokenExpired(String token);

}