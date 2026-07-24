package org.example.tnal_youth_backend.member.skill.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.service.MemberSkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@Tag(name = "Member Page")
@RequestMapping("/api/members/{memberId}/skills")
@RequiredArgsConstructor
public class MemberSkillController {

    private final MemberSkillService skillService;

    @GetMapping
    public ResponseEntity<List<MemberSkillResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                skillService.getByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberSkillResponse> create(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberSkillRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        skillService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<MemberSkillResponse> update(
            @PathVariable Long memberId,
            @PathVariable Long skillId,
            @Valid @RequestBody MemberSkillRequest request
    ) {
        return ResponseEntity.ok(
                skillService.update(
                        memberId,
                        skillId,
                        request
                )
        );
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long skillId
    ) {
        skillService.delete(
                memberId,
                skillId
        );

        return ResponseEntity.noContent().build();
    }
}