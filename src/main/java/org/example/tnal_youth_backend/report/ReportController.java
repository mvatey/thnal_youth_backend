package org.example.tnal_youth_backend.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/*
 * Downloadable reports filled into the association's own Excel/Word
 * templates. Who may ask for what is decided in each service (a branch the
 * caller cannot access is refused there).
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final MemberRegisterReportService memberRegisterReportService;

    @GetMapping("/member-register")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> memberRegister(
            @RequestParam(required = false) Long branchId
    ) {
        MemberRegisterReportService.RegisterFile file =
                memberRegisterReportService.build(branchId);

        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.title() + ".xlsx", StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(file.content());
    }
}
