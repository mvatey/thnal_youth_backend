package org.example.tnal_youth_backend.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.notification.dto.NotificationCountDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateResultDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationPageDTO;
import org.example.tnal_youth_backend.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationCreateResultDTO>> create(
            @Valid @RequestBody NotificationCreateDTO req) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(req)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<NotificationPageDTO>> listMine(
            @RequestParam(defaultValue = "false") boolean onlyUnread,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(service.listMine(onlyUnread, page, size)));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResponse<NotificationCountDTO>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(new NotificationCountDTO(service.unreadCount())));
    }

    @PostMapping("/me/{id}/read")
    public ResponseEntity<ApiResponse<Boolean>> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.markRead(id)));
    }

    @PostMapping("/me/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllRead() {
        return ResponseEntity.ok(ApiResponse.ok(service.markAllRead()));
    }
}