package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberStatusDto;
import org.example.tnal_youth_backend.member.entity.MemberStatus;
import org.example.tnal_youth_backend.member.service.MemberStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-statuses")
public class MemberStatusController {

    private final MemberStatusService service;

    public MemberStatusController(MemberStatusService service) {
        this.service = service;
    }

    @GetMapping
    public List<MemberStatus> getAllStatuses() {
        return service.getAllStatuses();
    }

    @GetMapping("/{id}")
    public MemberStatus getStatusById(@PathVariable Long id) {
        return service.getStatusById(id);
    }

    @PostMapping
    public MemberStatus createStatus(@RequestBody MemberStatusDto dto) {
        return service.createStatus(dto);
    }

    @PutMapping("/{id}")
    public MemberStatus updateStatus(@PathVariable Long id, @RequestBody MemberStatusDto dto) {
        return service.updateStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable Long id) {
        service.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }
}
