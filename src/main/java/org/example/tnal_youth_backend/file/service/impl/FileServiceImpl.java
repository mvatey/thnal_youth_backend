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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

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
    public FileResponse getFileById(Long id) {
        FileEntity file = findFileById(id);

        return fileMapper.toResponse(file);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public FileResponse getFileByPath(
//            String filePath
//    ) {
//        String normalizedPath =
//                normalizeRequiredText(
//                        filePath,
//                        "File path"
//                );
//
//        FileEntity file =
//                fileRepository
//                        .findByFilePath(normalizedPath)
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "File not found with path: "
//                                                + normalizedPath
//                                )
//                        );
//
//        return fileMapper.toResponse(file);
//    }

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

        if (fileRepository.existsByFilePath(filePath)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File path already exists: " + filePath
            );
        }

        FileEntity file = FileEntity.builder()
                .filePath(filePath)
                .originalName(originalName)
                .mimeType(mimeType)
                .sizeBytes(request.sizeBytes())
                .uploadedById(request.uploadedById())
                .build();

        try {
            FileEntity savedFile =
                    fileRepository.saveAndFlush(file);

            return fileMapper.toResponse(savedFile);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    createDatabaseErrorMessage(
                            request.uploadedById()
                    )
            );
        }
    }

    @Override
    @Transactional
    public FileResponse updateFile(
            Long id,
            UpdateFileRequest request
    ) {
        FileEntity file = findFileById(id);

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
                    "File path already exists: " + filePath
            );
        }

        file.setFilePath(filePath);
        file.setOriginalName(originalName);
        file.setMimeType(mimeType);
        file.setSizeBytes(request.sizeBytes());

        try {
            FileEntity updatedFile =
                    fileRepository.saveAndFlush(file);

            return fileMapper.toResponse(updatedFile);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File metadata could not be updated"
            );
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        FileEntity file = findFileById(id);

        try {
            fileRepository.delete(file);
            fileRepository.flush();

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this file because another database \
                    record is using it
                    """
            );
        }
    }

    private FileEntity findFileById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File ID is required"
            );
        }

        return fileRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "File not found with ID: " + id
                        )
                );
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
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
        ).toLowerCase(Locale.ROOT);
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