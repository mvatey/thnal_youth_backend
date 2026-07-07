package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.entity.MemberStatus;
import org.example.tnal_youth_backend.member.service.MemberStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-statuses")
public class MemberStatusController {
    private final MemberStatusService statusService;

    public MemberStatusController(MemberStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping
    public List<MemberStatus> getAllStatuses() {
        return statusService.getAllStatuses();
    }

    @PostMapping
    public MemberStatus createStatus(@RequestBody MemberStatus status) {
        return statusService.createStatus(status);
    }
}
