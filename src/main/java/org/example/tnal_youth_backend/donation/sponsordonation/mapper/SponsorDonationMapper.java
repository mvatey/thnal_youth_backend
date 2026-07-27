//package org.example.tnal_youth_backend.donation.sponsordonation.mapper;
//
//import lombok.RequiredArgsConstructor;
//import org.example.tnal_youth_backend.donation.sponsordonation.dto.response.SponsorDonationResponse;
//import org.example.tnal_youth_backend.donation.sponsordonation.entity.SponsorDonation;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class SponsorDonationMapper {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public SponsorDonationResponse toResponse(
//            SponsorDonation donation
//    ) {
//        if (donation == null) {
//            return null;
//        }
//
//        return new SponsorDonationResponse(
//                donation.getId(),
//                donation.getSponsorDonationNo(),
//                getSponsorInfo(
//                        donation.getSponsorId()
//                ),
//                getBranchInfo(
//                        donation.getBranchId()
//                ),
//                donation.getAmountKhr(),
//                donation.getAmountUsd(),
//                donation.getTotalAmountUsd(),
//                getPaymentMethodInfo(
//                        donation.getPaymentMethodId()
//                ),
//                donation.getPaidAt(),
//                donation.getPaymentReference(),
//                getRecordedByInfo(
//                        donation.getRecordedById()
//                ),
//                getReceiptInfo(
//                        donation.getReceiptFileId()
//                ),
//                donation.getNote()
//        );
//    }
//
//    private SponsorDonationResponse.SponsorInfo
//    getSponsorInfo(
//            Long sponsorId
//    ) {
//        if (sponsorId == null) {
//            return null;
//        }
//
//        List<SponsorDonationResponse.SponsorInfo> results =
//                jdbcTemplate.query(
//                        """
//                        SELECT
//                            sponsor.id,
//                            sponsor.sponsor_type_id,
//                            sponsor_type.code
//                                AS sponsor_type_code,
//                            sponsor_type.label_km
//                                AS sponsor_type_label_km,
//                            sponsor_type.label_en
//                                AS sponsor_type_label_en,
//                            sponsor.name,
//                            sponsor.phone,
//                            sponsor.email
//                        FROM sponsors sponsor
//
//                        LEFT JOIN sponsor_types sponsor_type
//                               ON sponsor_type.id =
//                                  sponsor.sponsor_type_id
//
//                        WHERE sponsor.id = ?
//                        """,
//                        (
//                                resultSet,
//                                rowNumber
//                        ) -> new SponsorDonationResponse.SponsorInfo(
//                                getNullableLong(
//                                        resultSet.getObject("id")
//                                ),
//                                getNullableShort(
//                                        resultSet.getObject(
//                                                "sponsor_type_id"
//                                        )
//                                ),
//                                resultSet.getString(
//                                        "sponsor_type_code"
//                                ),
//                                resultSet.getString(
//                                        "sponsor_type_label_km"
//                                ),
//                                resultSet.getString(
//                                        "sponsor_type_label_en"
//                                ),
//                                resultSet.getString("name"),
//                                resultSet.getString("phone"),
//                                resultSet.getString("email")
//                        ),
//                        sponsorId
//                );
//
//        return firstOrNull(results);
//    }
//
//    private SponsorDonationResponse.BranchInfo
//    getBranchInfo(
//            Long branchId
//    ) {
//        if (branchId == null) {
//            return null;
//        }
//
//        List<SponsorDonationResponse.BranchInfo> results =
//                jdbcTemplate.query(
//                        """
//                        SELECT
//                            id,
//                            name_km,
//                            name_en
//                        FROM branches
//                        WHERE id = ?
//                        """,
//                        (
//                                resultSet,
//                                rowNumber
//                        ) -> new SponsorDonationResponse.BranchInfo(
//                                getNullableLong(
//                                        resultSet.getObject("id")
//                                ),
//                                resultSet.getString("name_km"),
//                                resultSet.getString("name_en")
//                        ),
//                        branchId
//                );
//
//        return firstOrNull(results);
//    }
//
//    private SponsorDonationResponse.PaymentMethodInfo
//    getPaymentMethodInfo(
//            Short paymentMethodId
//    ) {
//        if (paymentMethodId == null) {
//            return null;
//        }
//
//        List<SponsorDonationResponse.PaymentMethodInfo> results =
//                jdbcTemplate.query(
//                        """
//                        SELECT
//                            id,
//                            code,
//                            label_km,
//                            label_en
//                        FROM payment_methods
//                        WHERE id = ?
//                        """,
//                        (
//                                resultSet,
//                                rowNumber
//                        ) -> new SponsorDonationResponse.PaymentMethodInfo(
//                                getNullableShort(
//                                        resultSet.getObject("id")
//                                ),
//                                resultSet.getString("code"),
//                                resultSet.getString("label_km"),
//                                resultSet.getString("label_en")
//                        ),
//                        paymentMethodId
//                );
//
//        return firstOrNull(results);
//    }
//
//    private SponsorDonationResponse.RecordedByInfo
//    getRecordedByInfo(
//            Long recordedById
//    ) {
//        if (recordedById == null) {
//            return null;
//        }
//
//        List<SponsorDonationResponse.RecordedByInfo> results =
//                jdbcTemplate.query(
//                        """
//                        SELECT
//                            user_account.id,
//                            user_account.member_id,
//
//                            COALESCE(
//                                member.full_name_km,
//                                user_account.full_name_km
//                            ) AS full_name_km,
//
//                            COALESCE(
//                                member.full_name_en,
//                                user_account.full_name_en
//                            ) AS full_name_en
//
//                        FROM users user_account
//
//                        LEFT JOIN members member
//                               ON member.id =
//                                  user_account.member_id
//
//                        WHERE user_account.id = ?
//                        """,
//                        (
//                                resultSet,
//                                rowNumber
//                        ) -> new SponsorDonationResponse.RecordedByInfo(
//                                getNullableLong(
//                                        resultSet.getObject("id")
//                                ),
//                                getNullableLong(
//                                        resultSet.getObject("member_id")
//                                ),
//                                resultSet.getString("full_name_km"),
//                                resultSet.getString("full_name_en")
//                        ),
//                        recordedById
//                );
//
//        return firstOrNull(results);
//    }
//
//    private SponsorDonationResponse.ReceiptInfo
//    getReceiptInfo(
//            Long receiptFileId
//    ) {
//        if (receiptFileId == null) {
//            return null;
//        }
//
//        List<SponsorDonationResponse.ReceiptInfo> results =
//                jdbcTemplate.query(
//                        """
//                        SELECT
//                            id,
//                            file_path,
//                            original_name,
//                            mime_type,
//                            size_bytes
//                        FROM files
//                        WHERE id = ?
//                        """,
//                        (
//                                resultSet,
//                                rowNumber
//                        ) -> new SponsorDonationResponse.ReceiptInfo(
//                                getNullableLong(
//                                        resultSet.getObject("id")
//                                ),
//                                resultSet.getString("file_path"),
//                                resultSet.getString("original_name"),
//                                resultSet.getString("mime_type"),
//                                getNullableLong(
//                                        resultSet.getObject("size_bytes")
//                                )
//                        ),
//                        receiptFileId
//                );
//
//        return firstOrNull(results);
//    }
//
//    private Long getNullableLong(
//            Object value
//    ) {
//        return value == null
//                ? null
//                : ((Number) value).longValue();
//    }
//
//    private Short getNullableShort(
//            Object value
//    ) {
//        return value == null
//                ? null
//                : ((Number) value).shortValue();
//    }
//
//    private <T> T firstOrNull(
//            List<T> results
//    ) {
//        return results == null
//                || results.isEmpty()
//                ? null
//                : results.get(0);
//    }
//}
