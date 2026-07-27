package org.example.tnal_youth_backend.account.myaccount.service;

import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountSummaryResponse;

public interface MyAccountService {

    MyAccountResponse getMyAccount();

    MyAccountSummaryResponse getMyAccountSummary();

    MyAccountResponse updateMyAccount(
            UpdateMyAccountRequest request
    );

    void changeMyPassword(
            ChangeMyPasswordRequest request
    );
}