package org.example.tnal_youth_backend.donation.donation.repository;

import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepository
        extends JpaRepository<Donation, Long> {

    /*
     * ==========================================================
     * UNIQUE VALIDATION
     * ==========================================================
     */

    boolean existsByDonationNoIgnoreCase(
            String donationNo
    );

    boolean existsByDonationNoIgnoreCaseAndIdNot(
            String donationNo,
            Long id
    );

    /*
     * ==========================================================
     * GET DONATIONS
     * ==========================================================
     */

    List<Donation> findAllByOrderByPaidAtDesc();

    Optional<Donation> findByIdAndMemberId(
            Long id,
            Long memberId
    );

    List<Donation> findAllByMemberIdOrderByPaidAtDesc(
            Long memberId
    );

    List<Donation> findAllByActivityIdOrderByPaidAtDesc(
            Long activityId
    );

    List<Donation> findAllByBranchIdOrderByPaidAtDesc(
            Long branchId
    );

    List<Donation> findAllByDonationTypeIdOrderByPaidAtDesc(
            Short donationTypeId
    );

    List<Donation> findAllByPaymentMethodIdOrderByPaidAtDesc(
            Short paymentMethodId
    );

    /*
     * ==========================================================
     * FILTER DONATIONS
     * ==========================================================
     *
     * Every filter is optional.
     *
     * Passing null means that filter is ignored.
     */

    @Query("""
            SELECT d
            FROM Donation d
            WHERE (
                :memberId IS NULL
                OR d.memberId = :memberId
            )
            AND (
                :activityId IS NULL
                OR d.activityId = :activityId
            )
            AND (
                :branchId IS NULL
                OR d.branchId = :branchId
            )
            AND (
                :donationTypeId IS NULL
                OR d.donationTypeId = :donationTypeId
            )
            AND (
                :paymentMethodId IS NULL
                OR d.paymentMethodId = :paymentMethodId
            )
            AND (
                :paidFrom IS NULL
                OR d.paidAt >= :paidFrom
            )
            AND (
                :paidTo IS NULL
                OR d.paidAt <= :paidTo
            )
            ORDER BY d.paidAt DESC
            """)
    List<Donation> findFiltered(
            @Param("memberId")
            Long memberId,

            @Param("activityId")
            Long activityId,

            @Param("branchId")
            Long branchId,

            @Param("donationTypeId")
            Short donationTypeId,

            @Param("paymentMethodId")
            Short paymentMethodId,

            @Param("paidFrom")
            OffsetDateTime paidFrom,

            @Param("paidTo")
            OffsetDateTime paidTo
    );

    /*
     * ==========================================================
     * DONATION NUMBER GENERATION
     * ==========================================================
     *
     * Finds the latest donation number with a specific prefix.
     *
     * Example prefix:
     *
     * DON-20260724-
     *
     * Existing values:
     *
     * DON-20260724-0001
     * DON-20260724-0002
     */
    @Query(
            value = """
                    SELECT donation_no
                    FROM donations
                    WHERE donation_no LIKE CONCAT(:prefix, '%')
                    ORDER BY donation_no DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<String> findLatestDonationNoByPrefix(
            @Param("prefix")
            String prefix
    );

    /*
     * ==========================================================
     * SUMMARY COUNTS
     * ==========================================================
     */

    long countByMemberId(
            Long memberId
    );

    long countByActivityId(
            Long activityId
    );

    long countByBranchId(
            Long branchId
    );

    /*
     * ==========================================================
     * DELETE CHECKS
     * ==========================================================
     */

    boolean existsByIdAndRecordedById(
            Long id,
            Long recordedById
    );
}