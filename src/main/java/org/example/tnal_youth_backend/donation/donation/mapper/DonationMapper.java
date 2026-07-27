package org.example.tnal_youth_backend.donation.donation.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DonationMapper {

    private final JdbcTemplate jdbcTemplate;

    public DonationResponse toResponse(
            Donation donation
    ) {
        if (donation == null) {
            return null;
        }

        DonationResponse.DonationTypeInfo donationType =
                getDonationTypeInfo(
                        donation.getDonationTypeId()
                );

        DonationResponse.MemberInfo member =
                getMemberInfo(
                        donation.getMemberId()
                );

        DonationResponse.SponsorInfo sponsor =
                getSponsorInfo(
                        donation.getSponsorId()
                );

        DonationResponse.ActivityInfo activity =
                getActivityInfo(
                        donation.getActivityId()
                );

        DonationResponse.BranchInfo branch =
                getBranchInfo(
                        donation.getBranchId()
                );

        DonationResponse.PaymentMethodInfo paymentMethod =
                getPaymentMethodInfo(
                        donation.getPaymentMethodId()
                );

        DonationResponse.RecordedByInfo recordedBy =
                getRecordedByInfo(
                        donation.getRecordedById()
                );

        DonationResponse.ReceiptInfo receipt =
                getReceiptInfo(
                        donation.getReceiptFileId()
                );

        return new DonationResponse(
                donation.getId(),
                donation.getDonationNo(),
                donationType,
                member,
                sponsor,
                donation.getDonorName(),
                activity,
                branch,
                donation.getDonationPeriod(),
                donation.getAmountKhr(),
                donation.getAmountUsd(),
                donation.getTotalAmountUsd(),
                paymentMethod,
                donation.getPaidAt(),
                donation.getPaymentReference(),
                recordedBy,
                receipt,
                donation.getNote()
        );
    }

    /*
     * ==========================================================
     * DONATION TYPE
     * ==========================================================
     */

    private DonationResponse.DonationTypeInfo
    getDonationTypeInfo(
            Short donationTypeId
    ) {
        if (donationTypeId == null) {
            return null;
        }

        List<DonationResponse.DonationTypeInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            code,
                            label_km,
                            label_en
                        FROM donation_types
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.DonationTypeInfo(
                                getNullableShort(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "code"
                                ),
                                resultSet.getString(
                                        "label_km"
                                ),
                                resultSet.getString(
                                        "label_en"
                                )
                        ),
                        donationTypeId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * MEMBER
     * ==========================================================
     */

    private DonationResponse.MemberInfo
    getMemberInfo(
            Long memberId
    ) {
        if (memberId == null) {
            return null;
        }

        List<DonationResponse.MemberInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            member_no,
                            full_name_km,
                            full_name_en
                        FROM members
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.MemberInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "member_no"
                                ),
                                resultSet.getString(
                                        "full_name_km"
                                ),
                                resultSet.getString(
                                        "full_name_en"
                                )
                        ),
                        memberId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * SPONSOR
     * ==========================================================
     *
     * This assumes the sponsors table uses a column named "name".
     *
     * If your actual column is sponsor_name, name_km, or another
     * name, change only the SELECT alias below.
     */

    private DonationResponse.SponsorInfo
    getSponsorInfo(
            Long sponsorId
    ) {
        if (sponsorId == null) {
            return null;
        }

        List<DonationResponse.SponsorInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            name
                        FROM sponsors
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.SponsorInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "name"
                                )
                        ),
                        sponsorId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * ACTIVITY
     * ==========================================================
     */

    private DonationResponse.ActivityInfo
    getActivityInfo(
            Long activityId
    ) {
        if (activityId == null) {
            return null;
        }

        List<DonationResponse.ActivityInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            title_km,
                            title_en
                        FROM activities
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.ActivityInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "title_km"
                                ),
                                resultSet.getString(
                                        "title_en"
                                )
                        ),
                        activityId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * BRANCH
     * ==========================================================
     */

    private DonationResponse.BranchInfo
    getBranchInfo(
            Long branchId
    ) {
        if (branchId == null) {
            return null;
        }

        List<DonationResponse.BranchInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            name_km,
                            name_en
                        FROM branches
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.BranchInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "name_km"
                                ),
                                resultSet.getString(
                                        "name_en"
                                )
                        ),
                        branchId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * PAYMENT METHOD
     * ==========================================================
     */

    private DonationResponse.PaymentMethodInfo
    getPaymentMethodInfo(
            Short paymentMethodId
    ) {
        if (paymentMethodId == null) {
            return null;
        }

        List<DonationResponse.PaymentMethodInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            code,
                            label_km,
                            label_en
                        FROM payment_methods
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.PaymentMethodInfo(
                                getNullableShort(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "code"
                                ),
                                resultSet.getString(
                                        "label_km"
                                ),
                                resultSet.getString(
                                        "label_en"
                                )
                        ),
                        paymentMethodId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * RECORDED BY
     * ==========================================================
     *
     * recorded_by points to users.id.
     *
     * A user may be linked to a member through users.member_id.
     * COALESCE keeps the user's name available even when there is
     * no linked member profile.
     */

    private DonationResponse.RecordedByInfo
    getRecordedByInfo(
            Long recordedById
    ) {
        if (recordedById == null) {
            return null;
        }

        List<DonationResponse.RecordedByInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            u.id,
                            u.member_id,

                            COALESCE(
                                m.full_name_km,
                                u.full_name_km
                            ) AS full_name_km,

                            COALESCE(
                                m.full_name_en,
                                u.full_name_en
                            ) AS full_name_en

                        FROM users u

                        LEFT JOIN members m
                               ON m.id = u.member_id

                        WHERE u.id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.RecordedByInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                getNullableLong(
                                        resultSet.getObject(
                                                "member_id"
                                        )
                                ),
                                resultSet.getString(
                                        "full_name_km"
                                ),
                                resultSet.getString(
                                        "full_name_en"
                                )
                        ),
                        recordedById
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * RECEIPT
     * ==========================================================
     */

    private DonationResponse.ReceiptInfo
    getReceiptInfo(
            Long receiptFileId
    ) {
        if (receiptFileId == null) {
            return null;
        }

        List<DonationResponse.ReceiptInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            file_path,
                            original_name,
                            mime_type,
                            size_bytes
                        FROM files
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.ReceiptInfo(
                                getNullableLong(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "file_path"
                                ),
                                resultSet.getString(
                                        "original_name"
                                ),
                                resultSet.getString(
                                        "mime_type"
                                ),
                                getNullableLong(
                                        resultSet.getObject(
                                                "size_bytes"
                                        )
                                )
                        ),
                        receiptFileId
                );

        return firstOrNull(results);
    }

    /*
     * ==========================================================
     * CONVERSION HELPERS
     * ==========================================================
     */

    private Long getNullableLong(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value).longValue();
    }

    private Short getNullableShort(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value).shortValue();
    }

    private <T> T firstOrNull(
            List<T> results
    ) {
        return results == null
                || results.isEmpty()
                ? null
                : results.get(0);
    }
}