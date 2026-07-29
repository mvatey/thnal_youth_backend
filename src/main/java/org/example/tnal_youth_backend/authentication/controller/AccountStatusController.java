package org.example.tnal_youth_backend.authentication.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.request.AccountStatusRequest;
import org.example.tnal_youth_backend.authentication.model.response.AccountStatusResponse;
import org.example.tnal_youth_backend.authentication.service.AccountStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountStatusController {

    private final AccountStatusService
            accountStatusService;

    @PostMapping("/account-status")
    public ResponseEntity<AccountStatusResponse>
    getAccountStatus(
            @Valid
            @RequestBody
            AccountStatusRequest request
    ) {
        return ResponseEntity.ok(
                accountStatusService
                        .getAccountStatus(
                                request
                        )
        );
    }
}