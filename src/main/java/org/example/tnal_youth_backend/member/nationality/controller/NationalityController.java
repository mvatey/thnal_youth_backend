package org.example.tnal_youth_backend.member.nationality.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.service.NationalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nationalities")
@RequiredArgsConstructor
public class NationalityController {

    private final NationalityService nationalityService;

    @GetMapping
    public ResponseEntity<List<NationalityResponse>>
    getNationalities() {

        return ResponseEntity.ok(
                nationalityService.getActiveNationalities()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NationalityResponse>
    getNationalityById(
            @PathVariable Short id
    ) {
        return ResponseEntity.ok(
                nationalityService.getNationalityById(id)
        );
    }
}