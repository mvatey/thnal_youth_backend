package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    List<Activity> findAllByStatus_CodeIgnoreCaseAndStartsAtLessThanEqual(
            String statusCode,
            OffsetDateTime currentTime
    );

    long countByBranchId(Long branchId);

    @Query("""
            SELECT DISTINCT activity FROM Activity activity
            WHERE
            (
              :branchId IS NULL OR activity.branchId = :branchId
              OR EXISTS (
                SELECT invitation.id FROM ActivityInvitedBranch invitation
                WHERE invitation.activity = activity
                  AND invitation.branch.id = :branchId
                  AND invitation.invitationStatus <> org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus.CANCELLED
              )
            )
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(activity.titleKm) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(activity.titleEn, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(activity.locationName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:sectorId IS NULL OR activity.sector.id = :sectorId)
            AND (:typeId IS NULL OR activity.type.id = :typeId)
            AND (:filterDate = FALSE OR (activity.startsAt >= :dateStart AND activity.startsAt < :dateEnd))
            """)
    Page<Activity> findStaffVisibleActivities(
            @Param("branchId") Long branchId,
            @Param("search") String search,
            @Param("sectorId") Short sectorId,
            @Param("typeId") Short typeId,
            @Param("filterDate") boolean filterDate,
            @Param("dateStart") OffsetDateTime dateStart,
            @Param("dateEnd") OffsetDateTime dateEnd,
            Pageable pageable
    );

    @Query("""
            SELECT activity FROM Activity activity
            WHERE (:branchId IS NULL OR activity.branchId = :branchId)
              AND (:search IS NULL OR :search = '' OR LOWER(activity.titleKm) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:sectorId IS NULL OR activity.sector.id = :sectorId)
              AND (:typeId IS NULL OR activity.type.id = :typeId)
              AND (:filterDate = FALSE OR (activity.startsAt >= :dateStart AND activity.startsAt < :dateEnd))
            """)
    Page<Activity> findAdminActivities(
            @Param("branchId") Long branchId,
            @Param("search") String search,
            @Param("sectorId") Short sectorId,
            @Param("typeId") Short typeId,
            @Param("filterDate") boolean filterDate,
            @Param("dateStart") OffsetDateTime dateStart,
            @Param("dateEnd") OffsetDateTime dateEnd,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT activity FROM Activity activity
            JOIN ActivityParticipant participant ON participant.activity = activity
            WHERE participant.member.id = :memberId
              AND (:search IS NULL OR :search = '' OR LOWER(activity.titleKm) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:sectorId IS NULL OR activity.sector.id = :sectorId)
              AND (:typeId IS NULL OR activity.type.id = :typeId)
              AND (:filterDate = FALSE OR (activity.startsAt >= :dateStart AND activity.startsAt < :dateEnd))
            """)
    Page<Activity> findMemberInvitedActivities(
            @Param("memberId") Long memberId,
            @Param("search") String search,
            @Param("sectorId") Short sectorId,
            @Param("typeId") Short typeId,
            @Param("filterDate") boolean filterDate,
            @Param("dateStart") OffsetDateTime dateStart,
            @Param("dateEnd") OffsetDateTime dateEnd,
            Pageable pageable
    );

}
