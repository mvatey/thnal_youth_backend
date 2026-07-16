package org.example.tnal_youth_backend.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tnal_youth_backend.account.dto.PositionDto;
import org.example.tnal_youth_backend.account.entity.Position;
import org.example.tnal_youth_backend.account.service.PositionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@Tag(name = "Positions API", description = "Manage account positions")
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public List<Position> getAllPositions() {
        return positionService.getAllPositions();
    }

    @PostMapping
    public Position createPosition(@RequestBody PositionDto dto) {
        return positionService.createPosition(dto.getName(), dto.getDescription());
    }
}
