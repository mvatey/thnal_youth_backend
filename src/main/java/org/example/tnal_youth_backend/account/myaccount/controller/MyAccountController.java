package org.example.tnal_youth_backend.account.myaccount.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-account")
@RequiredArgsConstructor
public class MyAccountController {

    private final MyAccountService myAccountService;

    @GetMapping
    public ResponseEntity<MyAccountResponse> getMyAccount() {

        return ResponseEntity.ok(
                myAccountService.getMyAccount()
        );
    }

    @PutMapping
    public ResponseEntity<MyAccountResponse> updateMyAccount(
            @Valid
            @RequestBody
            UpdateMyAccountRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService.updateMyAccount(request)
        );
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changeMyPassword(
            @Valid
            @RequestBody
            ChangeMyPasswordRequest request
    ) {
        myAccountService.changeMyPassword(request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateMyAccount() {

        myAccountService.deactivateMyAccount();

        return ResponseEntity.noContent().build();
    }
}