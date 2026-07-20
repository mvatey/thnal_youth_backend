package org.example.tnal_youth_backend.member.workhistory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.service.MemberWorkHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/members/{memberId}/work-history"
)
@RequiredArgsConstructor
public class MemberWorkHistoryController {

    private final MemberWorkHistoryService workHistoryService;

    @GetMapping
    public ResponseEntity<List<MemberWorkHistoryResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                workHistoryService.getByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberWorkHistoryResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        workHistoryService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{workId}")
    public ResponseEntity<MemberWorkHistoryResponse>
    update(
            @PathVariable Long memberId,
            @PathVariable Long workId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity.ok(
                workHistoryService.update(
                        memberId,
                        workId,
                        request
                )
        );
    }

    @DeleteMapping("/{workId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long workId
    ) {
        workHistoryService.delete(
                memberId,
                workId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}