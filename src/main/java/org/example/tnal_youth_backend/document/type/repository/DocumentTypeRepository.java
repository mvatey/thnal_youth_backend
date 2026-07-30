package org.example.tnal_youth_backend.document.type.repository;

import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository
        extends JpaRepository<DocumentType, Short> {

    Optional<DocumentType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<DocumentType> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();

    @Query("""
            SELECT dt
            FROM DocumentType dt
            WHERE dt.isActive = true
              AND dt.code IN :codes
            ORDER BY dt.sortOrder ASC, dt.id ASC
            """)
    List<DocumentType> findActiveByCodes(
            @Param("codes") Collection<String> codes
    );

    @Query("""
            SELECT dt
            FROM DocumentType dt
            WHERE dt.id = :typeId
              AND dt.isActive = true
            """)
    Optional<DocumentType> findActiveById(
            @Param("typeId") Short typeId
    );

    @Query("""
            SELECT dt
            FROM DocumentType dt
            WHERE UPPER(dt.code) IN :codes
            ORDER BY dt.sortOrder ASC, dt.id ASC
            """)
    List<DocumentType> findAllByCodes(
            @Param("codes") Collection<String> codes
    );
}