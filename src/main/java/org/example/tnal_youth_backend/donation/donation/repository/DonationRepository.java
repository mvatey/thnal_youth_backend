package org.example.tnal_youth_backend.donation.donation.repository;

import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
     * SEARCH BY MONTHLY DONATION PERIOD
     * ==========================================================
     *
     * The service converts yyyy-MM into:
     *
     * startDate = first day of the month
     * endDate   = last day of the month
     *
     * Example:
     *
     * 2026-07
     *
     * becomes:
     *
     * 2026-07-01 through 2026-07-31
     */
    List<Donation>
    findAllByDonationPeriodBetweenOrderByPaidAtDesc(
            LocalDate startDate,
            LocalDate endDate
    );

    /*
     * ==========================================================
     * GENERAL OPTIONAL FILTERS
     * ==========================================================
     */

    @Query("""
            SELECT donation
            FROM Donation donation

            WHERE (
                :memberId IS NULL
                OR donation.memberId = :memberId
            )

            AND (
                :activityId IS NULL
                OR donation.activityId = :activityId
            )

            AND (
                :branchId IS NULL
                OR donation.branchId = :branchId
            )

            AND (
                :donationTypeId IS NULL
                OR donation.donationTypeId =
                   :donationTypeId
            )

            AND (
                :paymentMethodId IS NULL
                OR donation.paymentMethodId =
                   :paymentMethodId
            )

            AND (
                :paidFrom IS NULL
                OR donation.paidAt >= :paidFrom
            )

            AND (
                :paidTo IS NULL
                OR donation.paidAt <= :paidTo
            )

            ORDER BY
                donation.paidAt DESC,
                donation.id DESC
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
     */

    @Query(
            value = """
                    SELECT donation_no
                    FROM donations
                    WHERE donation_no
                          LIKE CONCAT(:prefix, '%')
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
     * DELETE / OWNERSHIP CHECKS
     * ==========================================================
     */

    boolean existsByIdAndRecordedById(
            Long id,
            Long recordedById
    );
}