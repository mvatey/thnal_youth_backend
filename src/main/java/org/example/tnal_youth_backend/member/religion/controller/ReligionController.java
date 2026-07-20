package org.example.tnal_youth_backend.member.religion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.religion.dto.ReligionRequest;
import org.example.tnal_youth_backend.member.religion.dto.ReligionResponse;
import org.example.tnal_youth_backend.member.religion.service.ReligionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/religions")
@RequiredArgsConstructor
public class ReligionController {

    private final ReligionService religionService;

    @GetMapping
    public ResponseEntity<List<ReligionResponse>>
    getAllReligions(
            @RequestParam(
                    defaultValue = "false"
            )
            Boolean activeOnly
    ) {
        List<ReligionResponse> response =
                religionService
                        .getAllReligions(activeOnly);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReligionResponse>
    getReligionById(
            @PathVariable Short id
    ) {
        ReligionResponse response =
                religionService
                        .getReligionById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ReligionResponse>
    getReligionByCode(
            @PathVariable String code
    ) {
        ReligionResponse response =
                religionService
                        .getReligionByCode(code);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReligionResponse>
    createReligion(
            @Valid
            @RequestBody
            ReligionRequest request
    ) {
        ReligionResponse response =
                religionService
                        .createReligion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReligionResponse>
    updateReligion(
            @PathVariable Short id,

            @Valid
            @RequestBody
            ReligionRequest request
    ) {
        ReligionResponse response =
                religionService
                        .updateReligion(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteReligion(
            @PathVariable Short id
    ) {
        religionService.deleteReligion(id);

        return ResponseEntity.noContent().build();
    }
}