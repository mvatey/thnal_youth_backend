package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberTrainingDto;
import org.example.tnal_youth_backend.member.entity.MemberTraining;
import org.example.tnal_youth_backend.member.service.MemberTrainingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-training")
public class MemberTrainingController {

    private final MemberTrainingService service;

    public MemberTrainingController(MemberTrainingService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberTraining> getTrainingsByMember(@PathVariable Long memberId) {
        return service.getTrainingsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberTraining getTrainingById(@PathVariable Long id) {
        return service.getTrainingById(id);
    }

    @PostMapping
    public MemberTraining createTraining(@RequestBody MemberTrainingDto dto) {
        return service.createTraining(dto);
    }

    @PutMapping("/{id}")
    public MemberTraining updateTraining(@PathVariable Long id, @RequestBody MemberTrainingDto dto) {
        return service.updateTraining(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        service.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }
}
