package org.example.tnal_youth_backend.file.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.dto.request.CreateFileRequest;
import org.example.tnal_youth_backend.file.dto.request.UpdateFileRequest;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.mapper.FileMapper;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.file.service.FileService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final Path PROFILE_UPLOAD_DIRECTORY =
            Paths.get(
                            "uploads",
                            "member-profiles"
                    )
                    .toAbsolutePath()
                    .normalize();

    private static final long MAX_PROFILE_IMAGE_SIZE =
            5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final FileRepository fileRepository;

    private final FileMapper fileMapper;

    /*
     * ==========================================================
     * READ
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getAllFiles() {
        return fileRepository
                .findAll()
                .stream()
                .map(fileMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileById(
            Long id
    ) {
        FileEntity file =
                findFileById(id);

        return fileMapper.toResponse(file);
    }

    /*
     * ==========================================================
     * PHYSICAL FILE UPLOAD
     * ==========================================================
     */

    /*
     * Used by POST /api/files/upload.
     *
     * Returns the normal file response DTO.
     */
    @Override
    @Transactional
    public FileResponse uploadFile(
            MultipartFile multipartFile
    ) {
        FileEntity savedFile =
                uploadFileEntity(
                        multipartFile
                );

        return fileMapper.toResponse(
                savedFile
        );
    }

    /*
     * Internal reusable upload method.
     *
     * MemberService uses this method so it can immediately assign
     * the saved FileEntity to member.profilePhoto.
     */
    @Override
    @Transactional
    public FileEntity uploadFileEntity(
            MultipartFile multipartFile
    ) {
        validateUploadedImage(
                multipartFile
        );

        String originalName =
                sanitizeOriginalName(
                        multipartFile
                                .getOriginalFilename()
                );

        String contentType =
                multipartFile.getContentType();

        if (
                contentType == null
                        || contentType.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File MIME type is required"
            );
        }

        String mimeType =
                contentType
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        String extension =
                getSafeExtension(
                        mimeType
                );

        String storedFileName =
                UUID.randomUUID()
                        + extension;

        Path storedFilePath =
                PROFILE_UPLOAD_DIRECTORY
                        .resolve(
                                storedFileName
                        )
                        .normalize();

        if (
                !storedFilePath.startsWith(
                        PROFILE_UPLOAD_DIRECTORY
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }

        try {
            Files.createDirectories(
                    PROFILE_UPLOAD_DIRECTORY
            );

            Files.copy(
                    multipartFile.getInputStream(),
                    storedFilePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String databaseFilePath =
                    "uploads/member-profiles/"
                            + storedFileName;

            FileEntity file =
                    FileEntity.builder()
                            .filePath(
                                    databaseFilePath
                            )
                            .originalName(
                                    originalName
                            )
                            .mimeType(
                                    mimeType
                            )
                            .sizeBytes(
                                    multipartFile.getSize()
                            )
                            .uploadedById(null)
                            .build();

            return fileRepository
                    .saveAndFlush(
                            file
                    );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store uploaded file",
                    exception
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            deletePhysicalFileQuietly(
                    storedFilePath
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded file metadata could not be saved",
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * METADATA CRUD
     * ==========================================================
     */

    @Override
    @Transactional
    public FileResponse createFile(
            CreateFileRequest request
    ) {
        String filePath =
                normalizeRequiredText(
                        request.filePath(),
                        "File path"
                );

        String originalName =
                normalizeRequiredText(
                        request.originalName(),
                        "Original file name"
                );

        String mimeType =
                normalizeMimeType(
                        request.mimeType()
                );

        if (
                fileRepository
                        .existsByFilePath(
                                filePath
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File path already exists: "
                            + filePath
            );
        }

        FileEntity file =
                FileEntity.builder()
                        .filePath(
                                filePath
                        )
                        .originalName(
                                originalName
                        )
                        .mimeType(
                                mimeType
                        )
                        .sizeBytes(
                                request.sizeBytes()
                        )
                        .uploadedById(
                                request.uploadedById()
                        )
                        .build();

        try {
            FileEntity savedFile =
                    fileRepository
                            .saveAndFlush(
                                    file
                            );

            return fileMapper.toResponse(
                    savedFile
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    createDatabaseErrorMessage(
                            request.uploadedById()
                    ),
                    exception
            );
        }
    }

    @Override
    @Transactional
    public FileResponse updateFile(
            Long id,
            UpdateFileRequest request
    ) {
        FileEntity file =
                findFileById(id);

        String filePath =
                normalizeRequiredText(
                        request.filePath(),
                        "File path"
                );

        String originalName =
                normalizeRequiredText(
                        request.originalName(),
                        "Original file name"
                );

        String mimeType =
                normalizeMimeType(
                        request.mimeType()
                );

        boolean pathAlreadyExists =
                fileRepository
                        .existsByFilePathAndIdNot(
                                filePath,
                                id
                        );

        if (pathAlreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File path already exists: "
                            + filePath
            );
        }

        file.setFilePath(
                filePath
        );

        file.setOriginalName(
                originalName
        );

        file.setMimeType(
                mimeType
        );

        file.setSizeBytes(
                request.sizeBytes()
        );

        try {
            FileEntity updatedFile =
                    fileRepository
                            .saveAndFlush(
                                    file
                            );

            return fileMapper.toResponse(
                    updatedFile
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File metadata could not be updated",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void deleteFile(
            Long id
    ) {
        FileEntity file =
                findFileById(id);

        try {
            fileRepository.delete(
                    file
            );

            fileRepository.flush();

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this file because another database \
                    record is using it
                    """,
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * HELPERS
     * ==========================================================
     */

    private FileEntity findFileById(
            Long id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File ID is required"
            );
        }

        return fileRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "File not found with ID: "
                                        + id
                        )
                );
    }

    private void validateUploadedImage(
            MultipartFile multipartFile
    ) {
        if (
                multipartFile == null
                        || multipartFile.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image file is required"
            );
        }

        if (
                multipartFile.getSize()
                        > MAX_PROFILE_IMAGE_SIZE
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image size must not exceed 5 MB"
            );
        }

        String contentType =
                multipartFile.getContentType();

        if (
                contentType == null
                        || !ALLOWED_IMAGE_TYPES.contains(
                        contentType
                                .toLowerCase(
                                        Locale.ROOT
                                )
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG, and WebP images are allowed"
            );
        }
    }

    private String sanitizeOriginalName(
            String originalName
    ) {
        if (
                originalName == null
                        || originalName.isBlank()
        ) {
            return "profile-image";
        }

        String normalized =
                Paths.get(
                                originalName
                        )
                        .getFileName()
                        .toString()
                        .trim();

        if (normalized.isBlank()) {
            return "profile-image";
        }

        return normalized;
    }

    private String getSafeExtension(
            String mimeType
    ) {
        return switch (mimeType) {
            case "image/jpeg" ->
                    ".jpg";

            case "image/png" ->
                    ".png";

            case "image/webp" ->
                    ".webp";

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unsupported image type"
                    );
        };
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String normalizeMimeType(
            String mimeType
    ) {
        return normalizeRequiredText(
                mimeType,
                "MIME type"
        )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String createDatabaseErrorMessage(
            Long uploadedById
    ) {
        if (uploadedById != null) {
            return """
                    File metadata could not be saved. Make sure \
                    uploadedById references an existing user and \
                    filePath is unique.
                    """;
        }

        return """
                File metadata could not be saved. Make sure \
                filePath is unique and sizeBytes is greater than zero.
                """;
    }

    private void deletePhysicalFileQuietly(
            Path filePath
    ) {
        try {
            Files.deleteIfExists(
                    filePath
            );
        } catch (IOException ignored) {
            // Preserve the original exception.
        }
    }
}