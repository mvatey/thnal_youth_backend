package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.request.CreateUserRequest;
import org.example.tnal_youth_backend.authentication.model.request.UpdateUserRequest;
import org.example.tnal_youth_backend.authentication.model.response.UserListItemResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserSummaryResponse;

import java.util.List;

public interface UserManagementService {

    UserSummaryResponse getSummary();

    List<UserListItemResponse> listUsers(
            String search,
            String role,
            String status
    );

    UserListItemResponse createUser(
            CreateUserRequest request
    );

    UserListItemResponse updateUser(
            Long id,
            UpdateUserRequest request
    );
}
