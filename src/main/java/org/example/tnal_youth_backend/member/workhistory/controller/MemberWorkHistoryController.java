package org.example.tnal_youth_backend.member.workhistory.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.service.MemberWorkHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/members/{memberId}/work-history"
)
@RequiredArgsConstructor
@Tag(
        name = "3.0.3 Member Page - Work History",
        description = "View and manage work history for a selected member"
)
public class MemberWorkHistoryController {

    private final MemberWorkHistoryService
            workHistoryService;

    /*
     * GET /api/members/{memberId}/work-history
     */
    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<List<MemberWorkHistoryResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                workHistoryService
                        .getByMemberId(
                                memberId
                        )
        );
    }

    /*
     * POST /api/members/{memberId}/work-history
     */
    @PostMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberWorkHistoryResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        workHistoryService
                                .create(
                                        memberId,
                                        request
                                )
                );
    }

    /*
     * PUT /api/members/{memberId}/work-history/{workId}
     */
    @PutMapping("/{workId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberWorkHistoryResponse>
    update(
            @PathVariable Long memberId,

            @PathVariable Long workId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity.ok(
                workHistoryService
                        .update(
                                memberId,
                                workId,
                                request
                        )
        );
    }

    /*
     * DELETE /api/members/{memberId}/work-history/{workId}
     */
    @DeleteMapping("/{workId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<Void>
    delete(
            @PathVariable Long memberId,

            @PathVariable Long workId
    ) {
        workHistoryService
                .delete(
                        memberId,
                        workId
                );

        return ResponseEntity
                .noContent()
                .build();
    }
}