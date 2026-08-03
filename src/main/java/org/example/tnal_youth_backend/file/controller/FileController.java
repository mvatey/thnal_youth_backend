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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {

        return ResponseEntity.ok(
                fileService.getAllFiles()
        );
    }

        @GetMapping("/{id}")
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
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id
    ) {
        fileService.deleteFile(id);

        return ResponseEntity
                .noContent()
                .build();
    }


    @GetMapping("/{id}/content")
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

        } catch (Exception exception) {
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
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(resource);
    }
}