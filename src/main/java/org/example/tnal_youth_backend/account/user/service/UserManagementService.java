package org.example.tnal_youth_backend.account.user.service;

import org.example.tnal_youth_backend.account.user.dto.request.CreateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.request.UpdateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.response.UserResponse;

import java.util.List;

public interface UserManagementService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse createUser(
            CreateUserRequest request
    );

    UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    );

    void deleteUser(Long id);
}