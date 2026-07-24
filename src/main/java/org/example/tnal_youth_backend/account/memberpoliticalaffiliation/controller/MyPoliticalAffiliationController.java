package org.example.tnal_youth_backend.account.memberpoliticalaffiliation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberpoliticalaffiliation.service.MyPoliticalAffiliationService;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/my-account/political-affiliations"
)
@RequiredArgsConstructor
@Tag(
        name = "My Account - Political Affiliations",
        description = "ក្នែកនយោបាយ ( My-Account )"
)
public class MyPoliticalAffiliationController {

    private final MyPoliticalAffiliationService
            affiliationService;

    @GetMapping
    public ResponseEntity<
            List<MemberPoliticalAffiliationResponse>
            > getAll() {

        return ResponseEntity.ok(
                affiliationService
                        .getMyPoliticalAffiliations()
        );
    }

    @GetMapping("/{affiliationId}")
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > getById(
            @PathVariable Long affiliationId
    ) {
        return ResponseEntity.ok(
                affiliationService
                        .getMyPoliticalAffiliationById(
                                affiliationId
                        )
        );
    }

    @PostMapping
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > create(
            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        affiliationService
                                .createMyPoliticalAffiliation(
                                        request
                                )
                );
    }

    @PutMapping("/{affiliationId}")
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > update(
            @PathVariable Long affiliationId,

            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity.ok(
                affiliationService
                        .updateMyPoliticalAffiliation(
                                affiliationId,
                                request
                        )
        );
    }

    @DeleteMapping("/{affiliationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long affiliationId
    ) {
        affiliationService
                .deleteMyPoliticalAffiliation(
                        affiliationId
                );

        return ResponseEntity.noContent().build();
    }
}