package org.example.tnal_youth_backend.file.service.impl;

import org.example.tnal_youth_backend.file.dto.request.CreateFileRequest;
import org.example.tnal_youth_backend.file.dto.request.UpdateFileRequest;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.mapper.FileMapper;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.file.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_IMAGE_SIZE =
            5L * 1024 * 1024;

    private static final long MAX_ATTACHMENT_SIZE =
            20L * 1024 * 1024;

    private static final int MAX_FILES_PER_REQUEST = 10;

    private static final Set<String> IMAGE_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private static final Set<String> ATTACHMENT_TYPES =
            Set.of(
                    "application/pdf",

                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",

                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",

                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",

                    "application/zip",
                    "application/x-zip-compressed",

                    "text/plain",
                    "text/csv"
            );

    private final FileRepository fileRepository;

    private final FileMapper fileMapper;
    private final Path uploadRoot;

    public FileServiceImpl(
            FileRepository fileRepository,
            FileMapper fileMapper,
            @Value("${app.file.upload-dir}") String uploadDirectory
    ) {
        this.fileRepository = fileRepository;
        this.fileMapper = fileMapper;

        this.uploadRoot = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadRoot);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create the file upload directory",
                    exception
            );
        }
    }

    // ============================================================
    // EXISTING FILE METADATA OPERATIONS
    // ============================================================

    /*
     * ==========================================================
     * READ
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getAllFiles() {
        return fileRepository.findAll()
                .stream()
                .map(fileMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileById(
            Long id
    ) {
        FileEntity file = findFileById(id);

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
        return uploadImage(multipartFile, null);
    }

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
        FileEntity file = findFileById(id);

        Path physicalPath =
                resolveStoredPath(file);

        try {
            /*
             * Flush first so database constraints are checked before
             * deleting the physical file.
             */
            fileRepository.delete(file);
            fileRepository.flush();

            Files.deleteIfExists(physicalPath);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this file because another database \
                    record is using it
                    """,
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "File metadata was removed, but the physical file could not be deleted"
            );
        }
    }

    // ============================================================
    // REAL MULTIPART UPLOAD OPERATIONS
    // ============================================================

    @Override
    @Transactional
    public FileEntity uploadImage(
            MultipartFile file,
            Long uploadedById
    ) {
        validateMultipartFile(
                file,
                MAX_IMAGE_SIZE,
                IMAGE_TYPES,
                "image"
        );

        return storeMultipartFile(
                file,
                uploadedById,
                "images"
        );
    }

    @Override
    @Transactional
    public FileEntity uploadAttachment(
            MultipartFile file,
            Long uploadedById
    ) {
        validateMultipartFile(
                file,
                MAX_ATTACHMENT_SIZE,
                ATTACHMENT_TYPES,
                "attachment"
        );

        return storeMultipartFile(
                file,
                uploadedById,
                "attachments"
        );
    }

    @Override
    @Transactional
    public List<FileEntity> uploadImages(
            List<MultipartFile> files,
            Long uploadedById
    ) {
        validateFileList(
                files,
                MAX_FILES_PER_REQUEST
        );

        /*
         * Validate every file before saving any of them.
         * This prevents a partially completed upload when a later
         * image is invalid.
         */
        for (MultipartFile file : files) {
            validateMultipartFile(
                    file,
                    MAX_IMAGE_SIZE,
                    IMAGE_TYPES,
                    "image"
            );
        }

        return files.stream()
                .map(file ->
                        storeMultipartFile(
                                file,
                                uploadedById,
                                "images"
                        )
                )
                .toList();
    }

    @Override
    @Transactional
    public List<FileEntity> uploadAttachments(
            List<MultipartFile> files,
            Long uploadedById
    ) {
        validateFileList(
                files,
                MAX_FILES_PER_REQUEST
        );

        for (MultipartFile file : files) {
            validateMultipartFile(
                    file,
                    MAX_ATTACHMENT_SIZE,
                    ATTACHMENT_TYPES,
                    "attachment"
            );
        }

        return files.stream()
                .map(file ->
                        storeMultipartFile(
                                file,
                                uploadedById,
                                "attachments"
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadFileResource(
            Long fileId
    ) {
        FileEntity file = findFileById(fileId);

        Path physicalPath =
                resolveStoredPath(file);

        try {
            Resource resource =
                    new UrlResource(
                            physicalPath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stored file could not be found"
                );
            }

            return resource;

        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Stored file path is invalid"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFileEntity(
            Long fileId
    ) {
        return findFileById(fileId);
    }

    // ============================================================
    // STORAGE HELPERS
    // ============================================================

    private FileEntity storeMultipartFile(
            MultipartFile multipartFile,
            Long uploadedById,
            String folder
    ) {
        String originalName =
                sanitizeOriginalName(
                        multipartFile.getOriginalFilename()
                );

        String extension =
                extractExtension(originalName);

        String storedName =
                UUID.randomUUID()
                        + extension;

        Path targetDirectory =
                uploadRoot
                        .resolve(folder)
                        .normalize();

        Path targetPath =
                targetDirectory
                        .resolve(storedName)
                        .normalize();

        validatePathInsideUploadRoot(targetPath);

        String contentType =
                normalizeMimeType(
                        multipartFile.getContentType()
                );

        try {
            Files.createDirectories(targetDirectory);

            Files.copy(
                    multipartFile.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String relativePath =
                    uploadRoot
                            .relativize(targetPath)
                            .toString()
                            .replace("\\", "/");

            FileEntity entity =
                    FileEntity.builder()
                            .filePath(relativePath)
                            .originalName(originalName)
                            .mimeType(contentType)
                            .sizeBytes(
                                    multipartFile.getSize()
                            )
                            .uploadedById(uploadedById)
                            .build();

            try {
                return fileRepository
                        .saveAndFlush(entity);

            } catch (RuntimeException exception) {
                /*
                 * Database save failed, so remove the physical file.
                 */
                deletePhysicalFileQuietly(targetPath);
                throw exception;
            }

        } catch (IOException exception) {
            deletePhysicalFileQuietly(targetPath);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store the uploaded file"
            );
        }
    }

    private Path resolveStoredPath(
            FileEntity file
    ) {
        Path physicalPath =
                uploadRoot
                        .resolve(file.getFilePath())
                        .normalize();

        validatePathInsideUploadRoot(physicalPath);

        return physicalPath;
    }

    private void validatePathInsideUploadRoot(
            Path path
    ) {
        if (!path.startsWith(uploadRoot)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }
    }

    private void validateMultipartFile(
            MultipartFile file,
            long maximumSize,
            Set<String> allowedMimeTypes,
            String fileType
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded " + fileType + " is empty"
            );
        }

        if (file.getSize() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded " + fileType
                            + " must contain data"
            );
        }

        if (file.getSize() > maximumSize) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Uploaded " + fileType
                            + " exceeds the allowed size"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || contentType.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Uploaded " + fileType
                            + " does not have a valid MIME type"
            );
        }

        String normalizedContentType =
                contentType
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (!allowedMimeTypes.contains(
                normalizedContentType
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported " + fileType
                            + " type: "
                            + normalizedContentType
            );
        }
    }

    private void validateFileList(
            List<MultipartFile> files,
            int maximumFiles
    ) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one file is required"
            );
        }

        if (files.size() > maximumFiles) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A maximum of "
                            + maximumFiles
                            + " files may be uploaded at once"
            );
        }
    }

    private String sanitizeOriginalName(
            String originalName
    ) {
        if (originalName == null
                || originalName.isBlank()) {

            return "unnamed-file";
        }

        String cleaned =
                Paths.get(originalName)
                        .getFileName()
                        .toString()
                        .trim();

        if (cleaned.isBlank()) {
            cleaned = "unnamed-file";
        }

        if (cleaned.length() > 255) {
            String extension =
                    extractExtension(cleaned);

            int availableLength =
                    255 - extension.length();

            cleaned =
                    cleaned.substring(
                            0,
                            Math.max(1, availableLength)
                    ) + extension;
        }

        return cleaned;
    }

    private String extractExtension(
            String filename
    ) {
        int dotIndex =
                filename.lastIndexOf('.');

        if (dotIndex < 0
                || dotIndex == filename.length() - 1) {

            return "";
        }

        return filename
                .substring(dotIndex)
                .toLowerCase(Locale.ROOT);
    }

    private void deletePhysicalFileQuietly(
            Path path
    ) {
        try {
            Files.deleteIfExists(path);

        } catch (IOException ignored) {
            /*
             * Cleanup failure must not hide the original exception.
             */
        }
    }

    // ============================================================
    // DATABASE HELPERS
    // ============================================================

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

}
