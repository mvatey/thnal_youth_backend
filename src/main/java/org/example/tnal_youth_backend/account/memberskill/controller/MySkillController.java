package org.example.tnal_youth_backend.account.memberskill.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberskill.service.MySkillService;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/skills")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Skills",
        description = "ជំនាញ ( My-Account )"
)
public class MySkillController {

    private final MySkillService mySkillService;

    /*
     * GET /api/my-account/skills
     */
    @GetMapping
    public ResponseEntity<List<MemberSkillResponse>>
    getMySkills() {

        return ResponseEntity.ok(
                mySkillService.getMySkills()
        );
    }

    /*
     * GET /api/my-account/skills/{skillId}
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<MemberSkillResponse>
    getMySkillById(
            @PathVariable Long skillId
    ) {
        return ResponseEntity.ok(
                mySkillService.getMySkillById(
                        skillId
                )
        );
    }

    /*
     * POST /api/my-account/skills
     */
    @PostMapping
    public ResponseEntity<MemberSkillResponse>
    createMySkill(
            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        MemberSkillResponse response =
                mySkillService.createMySkill(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * PUT /api/my-account/skills/{skillId}
     */
    @PutMapping("/{skillId}")
    public ResponseEntity<MemberSkillResponse>
    updateMySkill(
            @PathVariable Long skillId,

            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        return ResponseEntity.ok(
                mySkillService.updateMySkill(
                        skillId,
                        request
                )
        );
    }

    /*
     * DELETE /api/my-account/skills/{skillId}
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void>
    deleteMySkill(
            @PathVariable Long skillId
    ) {
        mySkillService.deleteMySkill(
                skillId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}