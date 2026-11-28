package org.example.tnal_youth_backend.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

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
    private final NationalSummaryReportService nationalSummaryReportService;
    private final BranchReportService branchReportService;

    // The whole-country summary: administrators, and the central branch on
    // "all branches" (the service refuses anyone who does not read every
    // branch).
    @GetMapping("/national-summary")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> nationalSummary() {
        NationalSummaryReportService.SummaryFile file =
                nationalSummaryReportService.build();

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

    // The same summary for just the chosen branches (the branch list's
    // filter); a POST only because the list of ids can be long.
    @PostMapping("/national-summary")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> nationalSummaryForBranches(
            @RequestBody BranchesRequest request
    ) {
        NationalSummaryReportService.SummaryFile file =
                nationalSummaryReportService.build(
                        request.branchIds() == null
                                ? null
                                : new HashSet<>(request.branchIds())
                );

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

    public record BranchesRequest(List<Long> branchIds) {
    }

    // The Word branch report (see BranchReportService): the branch's own
    // staff and anyone who can open the branch; the service decides what the
    // caller's file contains.
    @GetMapping("/branch-report")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> branchReport(
            @RequestParam Long branchId,
            @RequestParam(required = false) Integer year
    ) {
        BranchReportService.ReportFile file =
                branchReportService.build(branchId, year);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.title() + ".docx", StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(file.content());
    }

    @GetMapping("/member-register")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> memberRegister(
            @RequestParam(required = false) Long branchId
    ) {
        return registerResponse(
                memberRegisterReportService.build(branchId, null)
        );
    }

    /**
     * The register for a filtered member list: the same file, but only the
     * members whose ids are sent (the rows still have to be inside the
     * caller's branch scope, so an id from elsewhere adds nothing).
     */
    @PostMapping("/member-register")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')")
    public ResponseEntity<byte[]> memberRegisterForMembers(
            @RequestBody MemberRegisterRequest request
    ) {
        return registerResponse(
                memberRegisterReportService.build(
                        request.branchId(),
                        request.memberIds() == null
                                ? null
                                : new HashSet<>(request.memberIds())
                )
        );
    }

    public record MemberRegisterRequest(Long branchId, List<Long> memberIds) {
    }

    private ResponseEntity<byte[]> registerResponse(
            MemberRegisterReportService.RegisterFile file
    ) {
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
