package org.example.tnal_youth_backend.file.repository;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository
        extends JpaRepository<FileEntity, Long> {

    boolean existsByFilePath(String filePath);

    boolean existsByFilePathAndIdNot(
            String filePath,
            Long id
    );

    Optional<FileEntity> findByFilePath(
            String filePath
    );

    Page<FileEntity> findAllByMimeTypeContainingIgnoreCase(
            String mimeType,
            Pageable pageable
    );

    Page<FileEntity> findAllByUploadedById(
            Long uploadedById,
            Pageable pageable
    );

    Page<FileEntity>
    findAllByMimeTypeContainingIgnoreCaseAndUploadedById(
            String mimeType,
            Long uploadedById,
            Pageable pageable
    );
}