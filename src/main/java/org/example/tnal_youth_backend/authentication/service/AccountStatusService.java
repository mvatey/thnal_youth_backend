package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.request.AccountStatusRequest;
import org.example.tnal_youth_backend.authentication.model.response.AccountStatusResponse;

public interface AccountStatusService {

    AccountStatusResponse getAccountStatus(
            AccountStatusRequest request
    );
}