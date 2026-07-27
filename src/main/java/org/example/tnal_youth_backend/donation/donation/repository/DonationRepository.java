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

    boolean existsByDonationNoIgnoreCase(
            String donationNo
    );

    boolean existsByDonationNoIgnoreCaseAndIdNot(
            String donationNo,
            Long id
    );

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
     * MONTHLY MEMBER DONATIONS
     * ==========================================================
     */

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    WHERE UPPER(donation_type.code) =
                          'MONTHLY_DONATION'

                      AND donation.member_id IS NOT NULL

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation> findAllMonthlyDonations();

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    WHERE UPPER(donation_type.code) =
                          'MONTHLY_DONATION'

                      AND donation.member_id IS NOT NULL

                      AND donation.donation_period
                          BETWEEN :startDate AND :endDate

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation> findMonthlyDonationsByPeriod(
            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate
    );

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    WHERE UPPER(donation_type.code) =
                          'MONTHLY_DONATION'

                      AND donation.member_id IS NOT NULL

                      AND donation.payment_method_id =
                          :paymentMethodId

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation>
    findMonthlyDonationsByPaymentMethod(
            @Param("paymentMethodId")
            Short paymentMethodId
    );

    /*
     * Retained for compatibility with older code.
     */
    List<Donation>
    findAllByDonationPeriodBetweenOrderByPaidAtDesc(
            LocalDate startDate,
            LocalDate endDate
    );

    /*
     * ==========================================================
     * SPONSOR DONATIONS
     * ==========================================================
     */

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    WHERE UPPER(donation_type.code) =
                          'SPONSOR_DONATION'

                      AND donation.sponsor_id IS NOT NULL

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation> findAllSponsorDonations();

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    INNER JOIN sponsors sponsor
                            ON sponsor.id =
                               donation.sponsor_id

                    WHERE UPPER(donation_type.code) =
                          'SPONSOR_DONATION'

                      AND donation.sponsor_id IS NOT NULL

                      AND (
                          LOWER(sponsor.name)
                              LIKE LOWER(
                                  CONCAT(
                                      '%',
                                      :search,
                                      '%'
                                  )
                              )

                          OR LOWER(
                              COALESCE(
                                  sponsor.phone,
                                  ''
                              )
                          ) LIKE LOWER(
                              CONCAT(
                                  '%',
                                  :search,
                                  '%'
                              )
                          )

                          OR LOWER(
                              COALESCE(
                                  sponsor.email::TEXT,
                                  ''
                              )
                          ) LIKE LOWER(
                              CONCAT(
                                  '%',
                                  :search,
                                  '%'
                              )
                          )
                      )

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation> searchSponsorDonations(
            @Param("search")
            String search
    );

    @Query(
            value = """
                    SELECT donation.*
                    FROM donations donation

                    INNER JOIN donation_types donation_type
                            ON donation_type.id =
                               donation.donation_type_id

                    WHERE UPPER(donation_type.code) =
                          'SPONSOR_DONATION'

                      AND donation.sponsor_id IS NOT NULL

                      AND donation.payment_method_id =
                          :paymentMethodId

                    ORDER BY
                        donation.paid_at DESC,
                        donation.id DESC
                    """,
            nativeQuery = true
    )
    List<Donation>
    findSponsorDonationsByPaymentMethod(
            @Param("paymentMethodId")
            Short paymentMethodId
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

    long countByMemberId(
            Long memberId
    );

    long countByActivityId(
            Long activityId
    );

    long countByBranchId(
            Long branchId
    );

    boolean existsByIdAndRecordedById(
            Long id,
            Long recordedById
    );
}