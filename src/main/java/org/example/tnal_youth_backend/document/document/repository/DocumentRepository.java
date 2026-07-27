package org.example.tnal_youth_backend.document.document.repository;

import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document>
    findAllByOrderByCreatedAtDescIdDesc();

    List<Document>
    findAllByMemberIdOrderByCreatedAtDescIdDesc(
            Long memberId
    );

    List<Document>
    findAllByBranchIdOrderByCreatedAtDescIdDesc(
            Long branchId
    );

    List<Document>
    findAllByActivityIdOrderByCreatedAtDescIdDesc(
            Long activityId
    );

    boolean existsByFileId(Long fileId);
}