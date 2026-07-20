package org.example.tnal_youth_backend.member.proficiency.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.proficiency.dto.response.ProficiencyLevelResponse;
import org.example.tnal_youth_backend.member.proficiency.service.ProficiencyLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proficiency-levels")
@RequiredArgsConstructor
public class ProficiencyLevelController {

    private final ProficiencyLevelService
            proficiencyLevelService;

    @GetMapping
    public ResponseEntity<List<ProficiencyLevelResponse>>
    getProficiencyLevels() {

        return ResponseEntity.ok(
                proficiencyLevelService
                        .getActiveProficiencyLevels()
        );
    }
}