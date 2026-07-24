package org.example.tnal_youth_backend.account.membereducation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.membereducation.service.MyEducationService;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/education")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Education",
        description = "ការអប់រំ ( My-Account )"
)
public class MyEducationController {

    private final MyEducationService myEducationService;

     // GET /api/my-account/education

    @GetMapping
    public ResponseEntity<List<MemberEducationResponse>>
    getMyEducation() {

        return ResponseEntity.ok(
                myEducationService.getMyEducation()
        );
    }

    //GET /api/my-account/education/{educationId}

    @GetMapping("/{educationId}")
    public ResponseEntity<MemberEducationResponse>
    getMyEducationById(
            @PathVariable Long educationId
    ) {
        return ResponseEntity.ok(
                myEducationService.getMyEducationById(
                        educationId
                )
        );
    }

    //   POST /api/my-account/education

    @PostMapping
    public ResponseEntity<MemberEducationResponse>
    createMyEducation(
            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        MemberEducationResponse response =
                myEducationService.createMyEducation(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

//    PUT /api/my-account/education/{educationId}

    @PutMapping("/{educationId}")
    public ResponseEntity<MemberEducationResponse>
    updateMyEducation(
            @PathVariable Long educationId,

            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        return ResponseEntity.ok(
                myEducationService.updateMyEducation(
                        educationId,
                        request
                )
        );
    }

// DELETE /api/my-account/education/{educationId}

    @DeleteMapping("/{educationId}")
    public ResponseEntity<Void>
    deleteMyEducation(
            @PathVariable Long educationId
    ) {
        myEducationService.deleteMyEducation(
                educationId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}