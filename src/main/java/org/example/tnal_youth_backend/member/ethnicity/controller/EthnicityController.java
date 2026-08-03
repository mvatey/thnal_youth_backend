package org.example.tnal_youth_backend.member.ethnicity.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.ethnicity.dto.response.EthnicityResponse;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ethnicities")
@RequiredArgsConstructor
public class EthnicityController {

    private final EthnicityRepository ethnicityRepository;

    @GetMapping
    public List<EthnicityResponse> getEthnicities() {
        return ethnicityRepository
                .findAllByIsActiveTrueOrderByLabelKmAsc()
                .stream()
                .map(value ->
                        new EthnicityResponse(
                                value.getId(),
                                value.getCode(),
                                value.getLabelKm(),
                                value.getLabelEn()
                        )
                )
                .toList();
    }
}