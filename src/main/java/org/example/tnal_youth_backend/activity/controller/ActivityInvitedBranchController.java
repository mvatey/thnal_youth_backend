package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.InviteBranchRequest;
import org.example.tnal_youth_backend.activity.model.request.RespondBranchInvitationRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityInvitedBranchResponse;
import org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(
        "/api/activities/{activityId}/invited-branches"
)
@RequiredArgsConstructor
public class ActivityInvitedBranchController {

    private final ActivityInvitedBranchService
            invitedBranchService;

    @PostMapping
    public ResponseEntity<ActivityInvitedBranchResponse>
    inviteBranch(
            @PathVariable Long activityId,
            @Valid
            @RequestBody InviteBranchRequest request,
            Authentication authentication
    ) {
        Long currentUserId =
                getCurrentUserId(authentication);

        ActivityInvitedBranchResponse response =
                invitedBranchService.inviteBranch(
                        activityId,
                        request,
                        currentUserId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<
            List<ActivityInvitedBranchResponse>
            >
    getInvitedBranches(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                invitedBranchService
                        .getInvitedBranches(
                                activityId
                        )
        );
    }

    @PatchMapping("/{invitationId}/respond")
    public ResponseEntity<ActivityInvitedBranchResponse>
    respondToInvitation(
            @PathVariable Long activityId,
            @PathVariable Long invitationId,
            @Valid
            @RequestBody
            RespondBranchInvitationRequest request,
            Authentication authentication
    ) {
        Long currentUserId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                invitedBranchService
                        .respondToInvitation(
                                activityId,
                                invitationId,
                                request,
                                currentUserId
                        )
        );
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long activityId,
            @PathVariable Long invitationId,
            Authentication authentication
    ) {
        Long currentUserId =
                getCurrentUserId(authentication);

        invitedBranchService.cancelInvitation(
                activityId,
                invitationId,
                currentUserId
        );

        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof CustomUserDetails userDetails) {

            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}