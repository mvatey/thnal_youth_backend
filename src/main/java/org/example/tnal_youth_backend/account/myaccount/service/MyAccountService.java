package org.example.tnal_youth_backend.account.myaccount.service;

import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MyAccountService {

    MyAccountResponse getMyAccount();

    MyAccountSummaryResponse getMyAccountSummary();

    MyAccountResponse updateMyAccount(
            UpdateMyAccountRequest request
    );

    MyAccountResponse updateProfilePhoto(
            MultipartFile file
    );

    void changeMyPassword(
            ChangeMyPasswordRequest request
    );
}
