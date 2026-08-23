package org.example.tnal_youth_backend.member.member.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tnal_youth_backend.member.member.dto.response.MemberMonthlyDonationSummaryResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberMonthlyDonationTotalResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberActivityDonationSummaryResponse;

@Mapper
public interface MemberDetailSummaryRepository {

    @Select("""
        SELECT
            COALESCE(SUM(d.amount_khr), 0) AS totalKhr,
            COALESCE(SUM(d.amount_usd), 0) AS totalUsd
        FROM donations d
        WHERE d.member_id = #{memberId}
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
    WHERE UPPER(dt.code) = 'ACTIVITY_DONATION'
      AND d.member_id = #{memberId}
    """)
    MemberActivityDonationSummaryResponse
    summarizeMemberActivityDonations(
            @Param("memberId")
            Long memberId
    );
}
