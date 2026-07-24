package org.example.tnal_youth_backend.account.memberfamily.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberfamily.dto.request.MyFamilyRequest;
import org.example.tnal_youth_backend.account.memberfamily.dto.response.MyFamilyResponse;
import org.example.tnal_youth_backend.account.memberfamily.service.MyFamilyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/family")
@RequiredArgsConstructor
@Tag(
        name = "My Account - Family",
        description = "គ្រួសារ ( My-Account )"
)
public class MyFamilyController {

    private final MyFamilyService myFamilyService;

    @GetMapping
    public ResponseEntity<List<MyFamilyResponse>> getMyFamily() {

        return ResponseEntity.ok(
                myFamilyService.getMyFamily()
        );
    }

    @GetMapping("/{familyId}")
    public ResponseEntity<MyFamilyResponse> getMyFamilyMember(
            @PathVariable Long familyId
    ) {
        return ResponseEntity.ok(
                myFamilyService.getMyFamilyMember(
                        familyId
                )
        );
    }

    @PostMapping
    public ResponseEntity<MyFamilyResponse> createMyFamilyMember(
            @Valid
            @RequestBody
            MyFamilyRequest request
    ) {
        MyFamilyResponse response =
                myFamilyService.createMyFamilyMember(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{familyId}")
    public ResponseEntity<MyFamilyResponse> updateMyFamilyMember(
            @PathVariable Long familyId,

            @Valid
            @RequestBody
            MyFamilyRequest request
    ) {
        return ResponseEntity.ok(
                myFamilyService.updateMyFamilyMember(
                        familyId,
                        request
                )
        );
    }

    @DeleteMapping("/{familyId}")
    public ResponseEntity<Void> deleteMyFamilyMember(
            @PathVariable Long familyId
    ) {
        myFamilyService.deleteMyFamilyMember(
                familyId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}