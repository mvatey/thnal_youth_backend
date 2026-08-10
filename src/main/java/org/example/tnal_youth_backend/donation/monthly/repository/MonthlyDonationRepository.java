package org.example.tnal_youth_backend.donation.monthly.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBranchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationListItemResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationRowResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MemberMonthlyDonationResponse;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MonthlyDonationRepository {

    @Select("SELECT branch_id FROM members WHERE id = #{memberId}")
    Long findMemberBranchId(@Param("memberId") Long memberId);

    @Select("""
        SELECT d.id,
               d.member_id AS memberId,
               d.donation_period AS donationPeriod,
               d.amount_khr AS amountKhr,
               d.amount_usd AS amountUsd,
               d.paid_at AS paidAt,
               d.recorded_by AS recordedById,
               ru.full_name_km AS recordedByName,
               d.payment_method_id AS paymentMethodId,
               pm.code AS paymentMethodCode,
               pm.label_km AS paymentMethodLabelKm,
               pm.label_en AS paymentMethodLabelEn,
               d.receipt_file_id AS receiptFileId,
               d.note
        FROM donations d
        JOIN donation_types dt ON dt.id = d.donation_type_id
        LEFT JOIN payment_methods pm ON pm.id = d.payment_method_id
        LEFT JOIN members ru ON ru.id = d.recorded_by
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.member_id = #{memberId}
        ORDER BY d.donation_period DESC, d.paid_at DESC, d.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<MemberMonthlyDonationResponse> findMemberHistory(
            @Param("memberId") Long memberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select("""
        SELECT COUNT(*)
        FROM donations d
        JOIN donation_types dt ON dt.id = d.donation_type_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.member_id = #{memberId}
        """)
    long countMemberHistory(@Param("memberId") Long memberId);

    @Select("""
        SELECT COUNT(DISTINCT d.member_id) AS memberCount,
               COALESCE(SUM(d.amount_khr), 0) AS totalKhr,
               COALESCE(SUM(d.amount_usd), 0) AS totalUsd,
               COALESCE(SUM(d.total_amount_usd), 0) AS overallTotalUsd
        FROM donations d
        JOIN donation_types dt ON dt.id = d.donation_type_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.member_id = #{memberId}
        """)
    MonthlyDonationSummaryResponse summarizeMemberHistory(
            @Param("memberId") Long memberId
    );

    @Select("""
        SELECT COUNT(*)
        FROM donations d
        JOIN donation_types dt ON dt.id = d.donation_type_id
        JOIN payment_methods pm ON pm.id = d.payment_method_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.member_id = #{memberId}
          AND UPPER(pm.code) = #{methodCode}
        """)
    long countMemberHistoryByMethod(
            @Param("memberId") Long memberId,
            @Param("methodCode") String methodCode
    );

    @Select("""
        SELECT id
        FROM donation_types
        WHERE UPPER(code) = 'MONTHLY_DONATION'
          AND is_active = TRUE
        LIMIT 1
        """)
    Short findMonthlyDonationTypeId();

    @Select("""
        SELECT COUNT(*)
        FROM donations d
        JOIN donation_types dt
          ON dt.id = d.donation_type_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.member_id = #{memberId}
          AND d.branch_id = #{branchId}
          AND d.donation_period = #{donationPeriod}
        """)
    int countExistingMonthlyDonation(
            @Param("memberId") Long memberId,
            @Param("branchId") Long branchId,
            @Param("donationPeriod") LocalDate donationPeriod
    );

    @Select({
            "<script>",
            "SELECT",
            "  m.id AS memberId,",
            "  m.member_no AS memberNo,",
            "  m.full_name_km AS fullNameKm,",
            "  m.full_name_en AS fullNameEn,",
            "  m.profile_photo_id AS profilePhotoId,",
            "  m.gender AS gender,",
            "  m.date_of_birth AS dateOfBirth,",
            "  m.branch_id AS branchId,",
            "  b.name_km AS branchNameKm,",
            "  d.id AS existingDonationId,",
            "  d.amount_khr AS amountKhr,",
            "  d.amount_usd AS amountUsd,",
            "  d.payment_method_id AS paymentMethodId,",
            "  pm.code AS paymentMethodCode,",
            "  d.receipt_file_id AS receiptFileId,",
            "  (d.id IS NOT NULL) AS alreadyPaid",
            "FROM members m",
            "JOIN branches b ON b.id = m.branch_id",
            "JOIN member_statuses ms ON ms.id = m.status_id",
            "LEFT JOIN donations d ON d.id = (",
            "  SELECT d2.id",
            "  FROM donations d2",
            "  JOIN donation_types dt2",
            "    ON dt2.id = d2.donation_type_id",
            "  WHERE dt2.code = 'MONTHLY_DONATION'",
            "    AND d2.member_id = m.id",
            "    AND d2.branch_id = #{branchId}",
            "    AND d2.donation_period = #{donationPeriod}",
            "  ORDER BY d2.id DESC",
            "  LIMIT 1",
            ")",
            "LEFT JOIN payment_methods pm",
            "  ON pm.id = d.payment_method_id",
            "WHERE m.branch_id = #{branchId}",
            "  AND ms.code = 'ACTIVE'",
            "  <if test='search != null and search != \"\"'>",
            "    AND (",
            "      m.member_no ILIKE ('%' || #{search} || '%')",
            "      OR m.full_name_km ILIKE ('%' || #{search} || '%')",
            "      OR m.full_name_en ILIKE ('%' || #{search} || '%')",
            "    )",
            "  </if>",
            "ORDER BY m.full_name_km ASC, m.id ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<MonthlyDonationMemberResponse> listMembers(
            @Param("branchId") Long branchId,
            @Param("donationPeriod") LocalDate donationPeriod,
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM members m",
            "JOIN member_statuses ms",
            "  ON ms.id = m.status_id",
            "WHERE m.branch_id = #{branchId}",
            "  AND ms.code = 'ACTIVE'",
            "  <if test='search != null and search != \"\"'>",
            "    AND (",
            "      m.member_no ILIKE ('%' || #{search} || '%')",
            "      OR m.full_name_km ILIKE ('%' || #{search} || '%')",
            "      OR m.full_name_en ILIKE ('%' || #{search} || '%')",
            "    )",
            "  </if>",
            "</script>"
    })
    long countMembers(
            @Param("branchId") Long branchId,
            @Param("search") String search
    );

    @Select("""
        SELECT branch_id
        FROM users
        WHERE id = #{userId}
        """)
    Long findBranchIdByUserId(
            @Param("userId") Long userId
    );

    @Select({
            "<script>",
            "SELECT",
            "  d.branch_id AS branchId,",
            "  b.branch_code AS branchCode,",
            "  b.name_km AS branchNameKm,",
            "  b.name_en AS branchNameEn,",
            "  d.donation_period AS donationPeriod,",
            "  COUNT(DISTINCT d.member_id) AS donorCount,",
            "  COALESCE(SUM(d.amount_khr), 0) AS totalKhr,",
            "  COALESCE(SUM(d.amount_usd), 0) AS totalUsd,",
            "  COALESCE(SUM(d.total_amount_usd), 0) AS overallTotalUsd,",
            "  MAX(d.paid_at) AS latestPaidAt",
            "FROM donations d",
            "JOIN donation_types dt",
            "  ON dt.id = d.donation_type_id",
            "JOIN branches b",
            "  ON b.id = d.branch_id",
            "WHERE dt.code = 'MONTHLY_DONATION'",

            "  <if test='branchId != null'>",
            "    AND d.branch_id = #{branchId}",
            "  </if>",

            "  <if test='month != null'>",
            "    AND EXTRACT(MONTH FROM d.donation_period) = #{month}",
            "  </if>",

            "  <if test='year != null'>",
            "    AND EXTRACT(YEAR FROM d.donation_period) = #{year}",
            "  </if>",

            "  <if test='search != null and search != \"\"'>",
            "    AND (",
            "      b.branch_code ILIKE ('%' || #{search} || '%')",
            "      OR b.name_km ILIKE ('%' || #{search} || '%')",
            "      OR b.name_en ILIKE ('%' || #{search} || '%')",
            "    )",
            "  </if>",

            "GROUP BY",
            "  d.branch_id,",
            "  b.branch_code,",
            "  b.name_km,",
            "  b.name_en,",
            "  d.donation_period",

            "ORDER BY",
            "  d.donation_period DESC,",
            "  MAX(d.paid_at) DESC,",
            "  d.branch_id ASC",

            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<MonthlyDonationListItemResponse> listMonthlyDonationGroups(
            @Param("branchId") Long branchId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM (",
            "  SELECT",
            "    d.branch_id,",
            "    d.donation_period",
            "  FROM donations d",
            "  JOIN donation_types dt",
            "    ON dt.id = d.donation_type_id",
            "  JOIN branches b",
            "    ON b.id = d.branch_id",
            "  WHERE dt.code = 'MONTHLY_DONATION'",

            "    <if test='branchId != null'>",
            "      AND d.branch_id = #{branchId}",
            "    </if>",

            "    <if test='month != null'>",
            "      AND EXTRACT(MONTH FROM d.donation_period) = #{month}",
            "    </if>",

            "    <if test='year != null'>",
            "      AND EXTRACT(YEAR FROM d.donation_period) = #{year}",
            "    </if>",

            "    <if test='search != null and search != \"\"'>",
            "      AND (",
            "        b.branch_code ILIKE ('%' || #{search} || '%')",
            "        OR b.name_km ILIKE ('%' || #{search} || '%')",
            "        OR b.name_en ILIKE ('%' || #{search} || '%')",
            "      )",
            "    </if>",

            "  GROUP BY",
            "    d.branch_id,",
            "    d.donation_period",
            ") grouped_monthly_donations",
            "</script>"
    })
    long countMonthlyDonationGroups(
            @Param("branchId") Long branchId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("search") String search
    );

    @Select("""
        SELECT
            id,
            branch_code AS branchCode,
            name_km AS nameKm,
            name_en AS nameEn
        FROM branches
        WHERE id = #{branchId}
        """)
    MonthlyDonationBranchResponse findBranch(
            @Param("branchId") Long branchId
    );

    @Select("""
        SELECT
            COUNT(DISTINCT d.member_id) AS memberCount,
            COALESCE(SUM(d.amount_khr), 0) AS totalKhr,
            COALESCE(SUM(d.amount_usd), 0) AS totalUsd,
            COALESCE(SUM(d.total_amount_usd), 0) AS overallTotalUsd
        FROM donations d
        JOIN donation_types dt
          ON dt.id = d.donation_type_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.branch_id = #{branchId}
          AND d.donation_period = #{donationPeriod}
        """)
    MonthlyDonationSummaryResponse summarizeMonthlyDonations(
            @Param("branchId") Long branchId,
            @Param("donationPeriod") LocalDate donationPeriod
    );

    @Select("""
        SELECT
            d.id AS donationId,
            d.donation_no AS donationNo,
            d.member_id AS memberId,
            m.member_no AS memberNo,
            m.full_name_km AS memberNameKm,
            m.full_name_en AS memberNameEn,
            d.amount_khr AS amountKhr,
            d.amount_usd AS amountUsd,
            d.exchange_rate_khr_per_usd AS exchangeRateKhrPerUsd,
            d.total_amount_usd AS totalAmountUsd,
            d.payment_method_id AS paymentMethodId,
            pm.code AS paymentMethodCode,
            pm.label_km AS paymentMethodLabelKm,
            pm.label_en AS paymentMethodLabelEn,
            d.payment_reference AS paymentReference,
            d.receipt_file_id AS receiptFileId,
            d.note AS description,
            d.paid_at AS paidAt
        FROM donations d
        JOIN donation_types dt
          ON dt.id = d.donation_type_id
        JOIN payment_methods pm
          ON pm.id = d.payment_method_id
        LEFT JOIN members m
          ON m.id = d.member_id
        WHERE dt.code = 'MONTHLY_DONATION'
          AND d.branch_id = #{branchId}
          AND d.donation_period = #{donationPeriod}
        ORDER BY
          m.full_name_km ASC NULLS LAST,
          d.id ASC
        """)
    List<MonthlyDonationRowResponse> findMonthlyDonationRows(
            @Param("branchId") Long branchId,
            @Param("donationPeriod") LocalDate donationPeriod
    );

    @Select("""
        SELECT d.branch_id
        FROM donations d
        JOIN donation_types dt
          ON dt.id = d.donation_type_id
        WHERE d.id = #{donationId}
          AND dt.code = 'MONTHLY_DONATION'
        """)
    Long findMonthlyDonationBranchId(
            @Param("donationId") Long donationId
    );

    @Delete("""
        DELETE FROM donations
        WHERE id = #{donationId}
          AND donation_type_id = (
              SELECT id
              FROM donation_types
              WHERE code = 'MONTHLY_DONATION'
              LIMIT 1
          )
        """)
    int deleteMonthlyDonation(
            @Param("donationId") Long donationId
    );
}
