package org.example.tnal_youth_backend.member.member.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tnal_youth_backend.member.member.dto.response.MemberMonthlyDonationSummaryResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberMonthlyDonationTotalResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberActivityDonationSummaryResponse;

@Mapper
public interface MemberDetailSummaryRepository {

    /*
     * Excludes activity donations whose activity has since been cancelled
     * -- same guard used by the donation list/summary and dashboard
     * totals, so a cancelled activity's money doesn't linger in a
     * member's overall total either. Non-activity donations (member_id
     * set, activity_id null) are untouched by the join/filter.
     */
    @Select("""
        SELECT
            COALESCE(SUM(d.amount_khr), 0) AS totalKhr,
            COALESCE(SUM(d.amount_usd), 0) AS totalUsd
        FROM donations d
        LEFT JOIN activities a ON a.id = d.activity_id
        LEFT JOIN activity_statuses ast ON ast.id = a.status_id
        WHERE d.member_id = #{memberId}
          AND (d.activity_id IS NULL OR UPPER(COALESCE(ast.code, '')) != 'CANCELLED')
        """)
    MemberMonthlyDonationTotalResponse
    summarizeTotalDonationByMemberId(
            @Param("memberId")
            Long memberId
    );

    @Select("""
    SELECT
        COUNT(d.id) AS donationCount,

        COALESCE(
            SUM(d.amount_khr),
            0
        ) AS totalDonationKhr,

        COALESCE(
            SUM(d.amount_usd),
            0
        ) AS totalDonationUsd,

        COUNT(d.id) FILTER (
            WHERE UPPER(COALESCE(pm.category, '')) = 'CASH'
        ) AS cashPaymentCount,

        COUNT(d.id) FILTER (
            WHERE UPPER(COALESCE(pm.category, '')) = 'BANK'
        ) AS bankPaymentCount

    FROM donations d

    JOIN donation_types dt
      ON dt.id = d.donation_type_id

    LEFT JOIN payment_methods pm
      ON pm.id = d.payment_method_id

    WHERE UPPER(dt.code) = 'MONTHLY_DONATION'
      AND d.member_id = #{memberId}
    """)
    MemberMonthlyDonationSummaryResponse
    summarizeMemberMonthlyDonations(
            @Param("memberId")
            Long memberId
    );

    /*
     * Excludes donations whose activity has since been cancelled -- same
     * guard as summarizeTotalDonationByMemberId above and the donation
     * list/summary queries in DonationRepository.
     */
    @Select("""
    SELECT
        COUNT(d.id) AS donationCount,
        COALESCE(SUM(d.amount_khr), 0) AS totalDonationKhr,
        COALESCE(SUM(d.amount_usd), 0) AS totalDonationUsd,
        COUNT(d.id) FILTER (
            WHERE UPPER(COALESCE(pm.category, '')) = 'MATERIAL'
        ) AS materialDonationCount,
        COUNT(d.id) FILTER (
            WHERE UPPER(COALESCE(pm.category, '')) = 'BANK'
        ) AS bankPaymentCount
    FROM donations d
    JOIN donation_types dt
      ON dt.id = d.donation_type_id
    LEFT JOIN payment_methods pm
      ON pm.id = d.payment_method_id
    LEFT JOIN activities a
      ON a.id = d.activity_id
    LEFT JOIN activity_statuses ast
      ON ast.id = a.status_id
    WHERE UPPER(dt.code) = 'ACTIVITY_DONATION'
      AND d.member_id = #{memberId}
      AND (d.activity_id IS NULL OR UPPER(COALESCE(ast.code, '')) != 'CANCELLED')
    """)
    MemberActivityDonationSummaryResponse
    summarizeMemberActivityDonations(
            @Param("memberId")
            Long memberId
    );
}
