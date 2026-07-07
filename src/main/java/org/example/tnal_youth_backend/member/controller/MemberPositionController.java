package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.entity.MemberPosition;
import org.example.tnal_youth_backend.member.service.MemberPositionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-positions")
public class MemberPositionController {
    private final MemberPositionService positionService;

    public MemberPositionController(MemberPositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public List<MemberPosition> getAllPositions() { return positionService.getAllPositions(); }

    @PostMapping
    public MemberPosition createPosition(@RequestBody MemberPosition position) {
        return positionService.createPosition(position);
    }
}
