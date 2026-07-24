package org.example.tnal_youth_backend.member.status.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.status.dto.MemberStatusRequest;
import org.example.tnal_youth_backend.member.status.dto.MemberStatusResponse;
import org.example.tnal_youth_backend.member.status.service.MemberStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-statuses")
@RequiredArgsConstructor
@Tag(
        name = "C. Member Page - Member-Status"
)
public class MemberStatusController {

    private final MemberStatusService memberStatusService;

    @GetMapping
    public ResponseEntity<List<MemberStatusResponse>>
    getAllMemberStatuses(
            @RequestParam(
                    defaultValue = "false"
            ) Boolean activeOnly
    ) {
        List<MemberStatusResponse> response =
                memberStatusService
                        .getAllMemberStatuses(activeOnly);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberStatusResponse>
    getMemberStatusById(
            @PathVariable Short id
    ) {
        MemberStatusResponse response =
                memberStatusService
                        .getMemberStatusById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MemberStatusResponse>
    getMemberStatusByCode(
            @PathVariable String code
    ) {
        MemberStatusResponse response =
                memberStatusService
                        .getMemberStatusByCode(code);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MemberStatusResponse>
    createMemberStatus(
            @Valid
            @RequestBody
            MemberStatusRequest request
    ) {
        MemberStatusResponse response =
                memberStatusService
                        .createMemberStatus(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberStatusResponse>
    updateMemberStatus(
            @PathVariable Short id,

            @Valid
            @RequestBody
            MemberStatusRequest request
    ) {
        MemberStatusResponse response =
                memberStatusService
                        .updateMemberStatus(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteMemberStatus(
            @PathVariable Short id
    ) {
        memberStatusService.deleteMemberStatus(id);

        return ResponseEntity.noContent().build();
    }
}