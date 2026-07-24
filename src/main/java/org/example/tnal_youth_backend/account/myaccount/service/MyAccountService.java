package org.example.tnal_youth_backend.account.myaccount.service;

import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;

public interface MyAccountService {

    MyAccountResponse getMyAccount();

    MyAccountResponse updateMyAccount(
            UpdateMyAccountRequest request
    );

    void changeMyPassword(
            ChangeMyPasswordRequest request
    );
}