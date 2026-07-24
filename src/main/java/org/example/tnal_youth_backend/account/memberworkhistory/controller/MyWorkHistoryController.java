package org.example.tnal_youth_backend.account.memberworkhistory.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberworkhistory.service.MyWorkHistoryService;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/work-history")
@RequiredArgsConstructor
@Tag(
        name = "My Account - Work History",
        description = "ប្រវិត្តការងារ ( My-Account )"
)
public class MyWorkHistoryController {

    private final MyWorkHistoryService myWorkHistoryService;

    /* GET ALL
     * GET /api/my-account/work-history
     */

    @GetMapping
    public ResponseEntity<List<MemberWorkHistoryResponse>>
    getMyWorkHistory() {

        return ResponseEntity.ok(
                myWorkHistoryService.getMyWorkHistory()
        );
    }

    /*   GET ONE
     *   GET /api/my-account/work-history/{workId}
     */

    @GetMapping("/{workId}")
    public ResponseEntity<MemberWorkHistoryResponse>
    getMyWorkHistoryById(
            @PathVariable Long workId
    ) {
        return ResponseEntity.ok(
                myWorkHistoryService.getMyWorkHistoryById(
                        workId
                )
        );
    }

    /* CREATE
     * POST /api/my-account/work-history
     */

    @PostMapping
    public ResponseEntity<MemberWorkHistoryResponse>
    createMyWorkHistory(
            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        MemberWorkHistoryResponse response =
                myWorkHistoryService
                        .createMyWorkHistory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * UPDATE
     * PUT /api/my-account/work-history/{workId}
     */

    @PutMapping("/{workId}")
    public ResponseEntity<MemberWorkHistoryResponse>
    updateMyWorkHistory(
            @PathVariable Long workId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity.ok(
                myWorkHistoryService
                        .updateMyWorkHistory(
                                workId,
                                request
                        )
        );
    }

    /*
     * DELETE
     * DELETE /api/my-account/work-history/{workId}
     */

    @DeleteMapping("/{workId}")
    public ResponseEntity<Void>
    deleteMyWorkHistory(
            @PathVariable Long workId
    ) {
        myWorkHistoryService.deleteMyWorkHistory(
                workId
        );
        return ResponseEntity
                .noContent()
                .build();
    }
}