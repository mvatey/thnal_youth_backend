package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.InviteParticipantsRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantResponse;
import org.example.tnal_youth_backend.activity.service.ActivityParticipantService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(
        "/api/activities/{activityId}/participants"
)
@RequiredArgsConstructor
public class ActivityParticipantController {

    private final ActivityParticipantService
            participantService;

    @PostMapping("/invite")
    public ResponseEntity<
            List<ActivityParticipantResponse>
            >
    inviteParticipants(
            @PathVariable Long activityId,
            @Valid
            @RequestBody
            InviteParticipantsRequest request,
            Authentication authentication
    ) {
        Long currentUserId =
                getCurrentUserId(authentication);

        List<ActivityParticipantResponse> response =
                participantService
                        .inviteParticipants(
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
            List<ActivityParticipantResponse>
            >
    getParticipants(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                participantService
                        .getParticipants(
                                activityId
                        )
        );
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long activityId,
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        Long currentUserId =
                getCurrentUserId(authentication);

        participantService.removeParticipant(
                activityId,
                memberId,
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