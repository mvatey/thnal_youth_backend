package org.example.tnal_youth_backend.member.branch.repository;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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

    @Query(
            value = """
                SELECT b.*
                FROM branches b
                INNER JOIN branch_statuses bs
                        ON bs.id = b.status_id
                WHERE UPPER(bs.code) = 'ACTIVE'
                ORDER BY b.name_km ASC
                """,
            nativeQuery = true
    )
    List<Branch> findAllActiveBranches();

    @Query(
            value = """
                SELECT b.*
                FROM branches b
                INNER JOIN branch_statuses bs
                        ON bs.id = b.status_id
                WHERE b.id IN (:branchIds)
                  AND UPPER(bs.code) = 'ACTIVE'
                ORDER BY b.name_km ASC
                """,
            nativeQuery = true
    )
    List<Branch> findActiveByIds(
            @Param("branchIds")
            Set<Long> branchIds
    );

    long countByIdIn(
            Iterable<Long> branchIds
    );

    long countByIdInAndStatusId(
            Iterable<Long> branchIds,
            Short statusId
    );

    long countByStatusId(
            Short statusId
    );

    @Query("""
        SELECT b
        FROM Branch b
        WHERE (
            :search = ''
            OR LOWER(b.branchCode)
                LIKE CONCAT(
                    '%',
                    LOWER(:search),
                    '%'
                )
            OR LOWER(b.nameKm)
                LIKE CONCAT(
                    '%',
                    LOWER(:search),
                    '%'
                )
            OR LOWER(COALESCE(b.nameEn, ''))
                LIKE CONCAT(
                    '%',
                    LOWER(:search),
                    '%'
                )
        )
        AND (
            :levelId IS NULL
            OR b.branchLevelId = :levelId
        )
        AND (
            :provinceId IS NULL
            OR b.provinceId = :provinceId
        )
        AND (
            :districtId IS NULL
            OR b.districtId = :districtId
        )
        AND (
            :statusId IS NULL
            OR b.statusId = :statusId
        )
        """)
    Page<Branch> findBranchPageForAdmin(
            @Param("search")
            String search,

            @Param("levelId")
            Short levelId,

            @Param("provinceId")
            Short provinceId,

            @Param("districtId")
            Integer districtId,

            @Param("statusId")
            Short statusId,

            Pageable pageable
    );

    @Query("""
        SELECT b
        FROM Branch b
        WHERE b.id IN :branchIds
        AND (
            :search IS NULL
            OR LOWER(b.branchCode)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(b.nameKm)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(b.nameEn, ''))
                LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (
            :levelId IS NULL
            OR b.branchLevelId = :levelId
        )
        AND (
            :provinceId IS NULL
            OR b.provinceId = :provinceId
        )
        AND (
            :districtId IS NULL
            OR b.districtId = :districtId
        )
        AND (
            :statusId IS NULL
            OR b.statusId = :statusId
        )
        """)
    Page<Branch> findBranchPageByScope(
            @Param("branchIds")
            Set<Long> branchIds,

            @Param("search")
            String search,

            @Param("levelId")
            Short levelId,

            @Param("provinceId")
            Short provinceId,

            @Param("districtId")
            Integer districtId,

            @Param("statusId")
            Short statusId,

            Pageable pageable
    );

    boolean existsByBranchLevelIdAndProvinceId(
            Short branchLevelId,
            Short provinceId
    );

    boolean existsByBranchLevelIdAndDistrictId(
            Short branchLevelId,
            Integer districtId
    );

    boolean existsByBranchLevelIdAndCommuneId(
            Short branchLevelId,
            Integer communeId
    );

    boolean existsByBranchLevelIdAndProvinceIdAndIdNot(
            Short branchLevelId,
            Short provinceId,
            Long id
    );

    boolean existsByBranchLevelIdAndDistrictIdAndIdNot(
            Short branchLevelId,
            Integer districtId,
            Long id
    );

    boolean existsByBranchLevelIdAndCommuneIdAndIdNot(
            Short branchLevelId,
            Integer communeId,
            Long id
    );

    boolean existsByBranchCode(
            String branchCode
    );
}