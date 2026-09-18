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

    /*
     * "Deletes" a login account by setting users.status to INACTIVE --
     * never an actual row delete (users.id is referenced as an audit
     * trail -- created_by/recorded_by/uploaded_by/etc. -- by roughly
     * twenty other tables with no ON DELETE CASCADE, so a real delete
     * would fail for almost any account that has ever done anything).
     * Works for BOTH standalone and member-linked accounts, unlike
     * updateUser -- this only ever touches the User row's own status,
     * never the linked Member record, which stays completely untouched.
     * listUsers/getSummary exclude INACTIVE accounts entirely, so this
     * makes the account disappear from the Users page for good while
     * every donation/activity/document they're attached to stays intact.
     */
    void deleteUser(Long id);
}
