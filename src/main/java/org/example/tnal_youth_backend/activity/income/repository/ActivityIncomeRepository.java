package org.example.tnal_youth_backend.activity.income.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeActivityResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeListItemResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeMemberRowResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeSummaryResponse;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface ActivityIncomeRepository {

    @Select("""
        SELECT branch_id
        FROM users
        WHERE id = #{userId}
        """)
    Long findBranchIdByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT",
            "    a.id AS activityId,",
            "    a.title_km AS activityTitleKm,",
            "    a.title_en AS activityTitleEn,",
            "    a.branch_id AS branchId,",
            "    b.name_km AS branchNameKm,",
            "    b.name_en AS branchNameEn,",
            "    a.starts_at AS startsAt,",
            "    a.ends_at AS endsAt,",
            "    COUNT(DISTINCT n.member_id) AS donorCount,",
            "    COALESCE(SUM(n.amount_khr), 0) AS totalKhr,",
            "    COALESCE(SUM(n.amount_usd), 0) AS totalUsd,",
            "    COALESCE(SUM(n.total_amount_usd), 0) AS overallTotalUsd,",
            "    MAX(n.paid_at) AS latestReceivedAt",
            "FROM donations n",
            "JOIN donation_types dt ON dt.id = n.donation_type_id",
            "JOIN activities a ON a.id = n.activity_id",
            "JOIN branches b ON b.id = a.branch_id",
            "WHERE dt.code = 'ACTIVITY_DONATION'",
            "  AND n.activity_id IS NOT NULL",
            "  <if test='branchId != null'> AND n.branch_id = #{branchId} </if>",
            "  <if test='paidFrom != null'> AND n.paid_at &gt;= #{paidFrom} </if>",
            "  <if test='paidTo != null'> AND n.paid_at &lt;= #{paidTo} </if>",
            "  <if test='search != null and search != \"\"'>",
            "    AND (",
            "      a.title_km ILIKE ('%' || #{search} || '%')",
            "      OR a.title_en ILIKE ('%' || #{search} || '%')",
            "      OR b.name_km ILIKE ('%' || #{search} || '%')",
            "      OR b.name_en ILIKE ('%' || #{search} || '%')",
            "    )",
            "  </if>",
            "GROUP BY a.id, a.title_km, a.title_en, a.branch_id,",
            "         b.name_km, b.name_en, a.starts_at, a.ends_at",
            "ORDER BY MAX(n.paid_at) DESC, a.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ActivityIncomeListItemResponse> listGrouped(
            @Param("branchId") Long branchId,
            @Param("search") String search,
            @Param("paidFrom") OffsetDateTime paidFrom,
            @Param("paidTo") OffsetDateTime paidTo,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT a.id",
            "  FROM donations n",
            "  JOIN donation_types dt ON dt.id = n.donation_type_id",
            "  JOIN activities a ON a.id = n.activity_id",
            "  JOIN branches b ON b.id = a.branch_id",
            "  WHERE dt.code = 'ACTIVITY_DONATION'",
            "    AND n.activity_id IS NOT NULL",
            "    <if test='branchId != null'> AND n.branch_id = #{branchId} </if>",
            "    <if test='paidFrom != null'> AND n.paid_at &gt;= #{paidFrom} </if>",
            "    <if test='paidTo != null'> AND n.paid_at &lt;= #{paidTo} </if>",
            "    <if test='search != null and search != \"\"'>",
            "      AND (",
            "        a.title_km ILIKE ('%' || #{search} || '%')",
            "        OR a.title_en ILIKE ('%' || #{search} || '%')",
            "        OR b.name_km ILIKE ('%' || #{search} || '%')",
            "        OR b.name_en ILIKE ('%' || #{search} || '%')",
            "      )",
            "    </if>",
            "  GROUP BY a.id",
            ") grouped_activities",
            "</script>"
    })
    long countGrouped(
            @Param("branchId") Long branchId,
            @Param("search") String search,
            @Param("paidFrom") OffsetDateTime paidFrom,
            @Param("paidTo") OffsetDateTime paidTo
    );

    @Select("""
        SELECT
            a.id AS id,
            a.title_km AS titleKm,
            a.title_en AS titleEn,
            a.branch_id AS branchId,
            b.name_km AS branchNameKm,
            b.name_en AS branchNameEn,
            a.starts_at AS startsAt,
            a.ends_at AS endsAt
        FROM activities a
        JOIN branches b ON b.id = a.branch_id
        WHERE a.id = #{activityId}
        """)
    ActivityIncomeActivityResponse findActivity(@Param("activityId") Long activityId);

    @Select("""
        SELECT
            COUNT(DISTINCT n.member_id) AS memberCount,
            COALESCE(SUM(n.amount_khr), 0) AS totalKhr,
            COALESCE(SUM(n.amount_usd), 0) AS totalUsd,
            COALESCE(SUM(n.total_amount_usd), 0) AS overallTotalUsd
        FROM donations n
        JOIN donation_types dt ON dt.id = n.donation_type_id
        WHERE dt.code = 'ACTIVITY_DONATION'
          AND n.activity_id = #{activityId}
        """)
    ActivityIncomeSummaryResponse summarize(@Param("activityId") Long activityId);

    @Select("""
        SELECT
            n.id AS donationId,
            n.donation_no AS donationNo,
            n.member_id AS memberId,
            m.member_no AS memberNo,
            m.full_name_km AS memberNameKm,
            m.full_name_en AS memberNameEn,
            m.profile_photo_id AS profilePhotoId,
            m.gender AS gender,
            m.date_of_birth AS dateOfBirth,
            n.amount_khr AS amountKhr,
            n.amount_usd AS amountUsd,
            n.exchange_rate_khr_per_usd AS exchangeRateKhrPerUsd,
            n.total_amount_usd AS totalAmountUsd,
            n.payment_method_id AS paymentMethodId,
            pm.code AS paymentMethodCode,
            pm.label_km AS paymentMethodLabelKm,
            pm.label_en AS paymentMethodLabelEn,
            n.payment_reference AS paymentReference,
            n.receipt_file_id AS receiptFileId,
            n.note AS description,
            n.paid_at AS receivedAt
        FROM donations n
        JOIN donation_types dt ON dt.id = n.donation_type_id
        JOIN payment_methods pm ON pm.id = n.payment_method_id
        LEFT JOIN members m ON m.id = n.member_id
        WHERE dt.code = 'ACTIVITY_DONATION'
          AND n.activity_id = #{activityId}
        ORDER BY n.paid_at DESC, n.id DESC
        """)
    List<ActivityIncomeMemberRowResponse> findRows(
            @Param("activityId") Long activityId
    );

    @Select("""
        SELECT n.branch_id
        FROM donations n
        JOIN donation_types dt ON dt.id = n.donation_type_id
        WHERE n.id = #{donationId}
          AND n.activity_id = #{activityId}
          AND dt.code = 'ACTIVITY_DONATION'
        """)
    Long findIncomeBranchId(
            @Param("activityId") Long activityId,
            @Param("donationId") Long donationId
    );

    @Delete("""
        DELETE FROM donations
        WHERE id = #{donationId}
          AND activity_id = #{activityId}
          AND donation_type_id = (
              SELECT id
              FROM donation_types
              WHERE code = 'ACTIVITY_DONATION'
              LIMIT 1
          )
        """)
    int deleteIncome(
            @Param("activityId") Long activityId,
            @Param("donationId") Long donationId
    );
}
