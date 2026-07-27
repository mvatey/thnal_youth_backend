//package org.example.tnal_youth_backend.donation.sponsordonation.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public record SponsorDonationResponse(
//
//        Long id,
//
//        String sponsorDonationNo,
//
//        SponsorInfo sponsor,
//
//        BranchInfo branch,
//
//        BigDecimal amountKhr,
//
//        BigDecimal amountUsd,
//
//        BigDecimal totalAmountUsd,
//
//        PaymentMethodInfo paymentMethod,
//
//        OffsetDateTime paidAt,
//
//        String paymentReference,
//
//        RecordedByInfo recordedBy,
//
//        ReceiptInfo receipt,
//
//        String note
//) {
//
//        public record SponsorInfo(
//                Long id,
//                Short sponsorTypeId,
//                String sponsorTypeCode,
//                String sponsorTypeLabelKm,
//                String sponsorTypeLabelEn,
//                String name,
//                String phone,
//                String email
//        ) {
//        }
//
//        public record BranchInfo(
//                Long id,
//                String nameKm,
//                String nameEn
//        ) {
//        }
//
//        public record PaymentMethodInfo(
//                Short id,
//                String code,
//                String labelKm,
//                String labelEn
//        ) {
//        }
//
//        public record RecordedByInfo(
//                Long id,
//                Long memberId,
//                String fullNameKm,
//                String fullNameEn
//        ) {
//        }
//
//        public record ReceiptInfo(
//                Long id,
//                String url,
//                String originalName,
//                String mimeType,
//                Long sizeBytes
//        ) {
//        }
//}
