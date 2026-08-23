package org.example.tnal_youth_backend.file.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.dto.request.CreateFileRequest;
import org.example.tnal_youth_backend.file.dto.request.UpdateFileRequest;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Files"
)
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<FileResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        FileEntity saved = fileService.uploadImage(file, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.getFileById(saved.getId()));
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')")
    public ResponseEntity<FileResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        FileEntity saved = fileService.uploadAttachment(file, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.getFileById(saved.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','VIEWER')")
    public ResponseEntity<List<FileResponse>> getAllFiles() {

        return ResponseEntity.ok(
                fileService.getAllFiles()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fileAccess.canRead(#id)")
    public ResponseEntity<FileResponse> getFileById(
            @PathVariable Long id
    ) {
        FileResponse response =
                fileService.getFileById(id);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/by-path")
//    public ResponseEntity<FileResponse> getFileByPath(
//            @RequestParam String filePath
//    ) {
//        FileResponse response =
//                fileService.getFileByPath(filePath);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<FileResponse> createFile(
            @Valid
            @RequestBody
            CreateFileRequest request
    ) {
        FileResponse response =
                fileService.createFile(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY')")
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateFileRequest request
    ) {
        FileResponse response =
                fileService.updateFile(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY')")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id
    ) {
        fileService.deleteFile(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/content")
    @PreAuthorize("@fileAccess.canRead(#id)")
    public ResponseEntity<Resource> getFileContent(
            @PathVariable Long id
    ) {
        FileEntity file =
                fileService.getFileEntity(id);

        Resource resource =
                fileService.loadFileResource(id);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
                    file.getMimeType()
            );

        } catch (InvalidMediaTypeException exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition =
                ContentDisposition.inline()
                        .filename(
                                file.getOriginalName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.getSizeBytes())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(resource);
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestPart("file")
            MultipartFile file
    ) {
        FileResponse response =
                fileService.uploadFile(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(
            value = "/document-attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')"
    )
    public ResponseEntity<FileResponse>
    uploadDocumentAttachment(
            @RequestParam("file")
            MultipartFile file,
            Authentication authentication
    ) {
        FileEntity saved =
                fileService.uploadDocumentAttachment(
                        file,
                        currentUserId(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        fileService.getFileById(
                                saved.getId()
                        )
                );
    }
}
