package org.example.tnal_youth_backend.member.level.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.level.dto.MemberLevelRequest;
import org.example.tnal_youth_backend.member.level.dto.MemberLevelResponse;
import org.example.tnal_youth_backend.member.level.service.MemberLevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-levels")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    @GetMapping
    public ResponseEntity<List<MemberLevelResponse>>
    getAllMemberLevels(
            @RequestParam(
                    defaultValue = "false"
            )
            Boolean activeOnly
    ) {
        List<MemberLevelResponse> response =
                memberLevelService
                        .getAllMemberLevels(activeOnly);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberLevelResponse>
    getMemberLevelById(
            @PathVariable Short id
    ) {
        MemberLevelResponse response =
                memberLevelService
                        .getMemberLevelById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MemberLevelResponse>
    getMemberLevelByCode(
            @PathVariable String code
    ) {
        MemberLevelResponse response =
                memberLevelService
                        .getMemberLevelByCode(code);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MemberLevelResponse>
    createMemberLevel(
            @Valid
            @RequestBody
            MemberLevelRequest request
    ) {
        MemberLevelResponse response =
                memberLevelService
                        .createMemberLevel(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberLevelResponse>
    updateMemberLevel(
            @PathVariable Short id,

            @Valid
            @RequestBody
            MemberLevelRequest request
    ) {
        MemberLevelResponse response =
                memberLevelService
                        .updateMemberLevel(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteMemberLevel(
            @PathVariable Short id
    ) {
        memberLevelService.deleteMemberLevel(id);

        return ResponseEntity.noContent().build();
    }
}