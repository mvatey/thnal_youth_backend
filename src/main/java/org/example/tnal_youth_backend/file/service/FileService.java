package org.example.tnal_youth_backend.file.service;

import org.example.tnal_youth_backend.file.dto.request.CreateFileRequest;
import org.example.tnal_youth_backend.file.dto.request.UpdateFileRequest;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    List<FileResponse> getAllFiles();

    FileResponse getFileById(
            Long id
    );

    FileResponse uploadFile(
            MultipartFile file
    );

//    FileResponse getFileByPath(
//            String filePath
//    );

    FileResponse createFile(
            CreateFileRequest request
    );

    FileResponse updateFile(
            Long id,
            UpdateFileRequest request
    );

    FileEntity uploadFileEntity(
            MultipartFile file
    );

    void deleteFile(
            Long id
    );

    /**
     * Upload one activity cover or gallery image.
     */
    FileEntity uploadImage(
            MultipartFile file,
            Long uploadedById
    );

    /**
     * Upload one activity attachment.
     */
    FileEntity uploadAttachment(
            MultipartFile file,
            Long uploadedById
    );

    /**
     * Upload multiple activity gallery images.
     */
    List<FileEntity> uploadImages(
            List<MultipartFile> files,
            Long uploadedById
    );

    /**
     * Upload multiple activity attachments.
     */
    List<FileEntity> uploadAttachments(
            List<MultipartFile> files,
            Long uploadedById
    );

    /**
     * Load the physical file for downloading or displaying.
     */
    Resource loadFileResource(
            Long fileId
    );

    /**
     * Return the JPA entity for linking it to another entity.
     */
    FileEntity getFileEntity(
            Long fileId
    );
}
