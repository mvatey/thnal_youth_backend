package org.example.tnal_youth_backend.donation.monthly.repo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberResponse;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MonthlyDonationRepo {

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
        JOIN donation_types dt ON dt.id = d.donation_type_id
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
            "  m.id                    AS memberId,",
            "  m.member_no             AS memberNo,",
            "  m.full_name_km          AS fullNameKm,",
            "  m.full_name_en          AS fullNameEn,",
            "  m.branch_id             AS branchId,",
            "  b.name_km               AS branchNameKm,",
            "  d.id                    AS existingDonationId,",
            "  d.amount_khr            AS amountKhr,",
            "  d.amount_usd            AS amountUsd,",
            "  d.payment_method_id     AS paymentMethodId,",
            "  pm.code                 AS paymentMethodCode,",
            "  d.receipt_file_id       AS receiptFileId,",
            "  (d.id IS NOT NULL)      AS alreadyPaid",
            "FROM members m",
            "JOIN branches b ON b.id = m.branch_id",
            "JOIN member_statuses ms ON ms.id = m.status_id",
            "LEFT JOIN donations d ON d.id = (",
            "    SELECT d2.id",
            "    FROM donations d2",
            "    JOIN donation_types dt2 ON dt2.id = d2.donation_type_id",
            "    WHERE dt2.code = 'MONTHLY_DONATION'",
            "      AND d2.member_id = m.id",
            "      AND d2.branch_id = #{branchId}",
            "      AND d2.donation_period = #{donationPeriod}",
            "    ORDER BY d2.id DESC",
            "    LIMIT 1",
            ")",
            "LEFT JOIN payment_methods pm ON pm.id = d.payment_method_id",
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
            "JOIN member_statuses ms ON ms.id = m.status_id",
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
}
