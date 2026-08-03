package org.example.tnal_youth_backend.member.branch.repository;

import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BranchRepository
        extends JpaRepository<Branch, Long> {

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM branches b
                        WHERE LOWER(BTRIM(b.name_km))
                              = LOWER(BTRIM(:nameKm))
                          AND b.province_id = :provinceId
                          AND COALESCE(b.district_id, 0)
                              = COALESCE(:districtId, 0)
                          AND COALESCE(b.commune_id, 0)
                              = COALESCE(:communeId, 0)
                    )
                    """,
            nativeQuery = true
    )
    boolean existsDuplicateBranch(
            @Param("nameKm") String nameKm,
            @Param("provinceId") Short provinceId,
            @Param("districtId") Integer districtId,
            @Param("communeId") Integer communeId
    );

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM branches b
                        WHERE LOWER(BTRIM(b.name_km))
                              = LOWER(BTRIM(:nameKm))
                          AND b.province_id = :provinceId
                          AND COALESCE(b.district_id, 0)
                              = COALESCE(:districtId, 0)
                          AND COALESCE(b.commune_id, 0)
                              = COALESCE(:communeId, 0)
                          AND b.id <> :id
                    )
                    """,
            nativeQuery = true
    )
    boolean existsDuplicateBranchExceptId(
            @Param("nameKm") String nameKm,
            @Param("provinceId") Short provinceId,
            @Param("districtId") Integer districtId,
            @Param("communeId") Integer communeId,
            @Param("id") Long id
    );

    boolean existsByParentBranchId(Long parentBranchId);
}