package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberWorkHistoryDto;
import org.example.tnal_youth_backend.member.entity.MemberWorkHistory;
import org.example.tnal_youth_backend.member.service.MemberWorkHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-work-history")
public class MemberWorkHistoryController {

    private final MemberWorkHistoryService service;

    public MemberWorkHistoryController(MemberWorkHistoryService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberWorkHistory> getWorkHistoryByMember(@PathVariable Long memberId) {
        return service.getWorkHistoryByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberWorkHistory getWorkHistoryById(@PathVariable Long id) {
        return service.getWorkHistoryById(id);
    }

    @PostMapping
    public MemberWorkHistory createWorkHistory(@RequestBody MemberWorkHistoryDto dto) {
        return service.createWorkHistory(dto);
    }

    @PutMapping("/{id}")
    public MemberWorkHistory updateWorkHistory(@PathVariable Long id, @RequestBody MemberWorkHistoryDto dto) {
        return service.updateWorkHistory(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkHistory(@PathVariable Long id) {
        service.deleteWorkHistory(id);
        return ResponseEntity.noContent().build();
    }
}
