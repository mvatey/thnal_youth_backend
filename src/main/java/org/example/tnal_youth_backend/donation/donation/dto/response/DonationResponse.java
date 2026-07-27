package org.example.tnal_youth_backend.donation.donation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DonationResponse(

        Long id,

        String donationNo,

        DonationTypeInfo donationType,

        MemberInfo member,

        SponsorInfo sponsor,

        String donorName,

        ActivityInfo activity,

        BranchInfo branch,

        LocalDate donationPeriod,

        BigDecimal amountKhr,

        BigDecimal amountUsd,

        BigDecimal totalAmountUsd,

        PaymentMethodInfo paymentMethod,

        OffsetDateTime paidAt,

        String paymentReference,

        RecordedByInfo recordedBy,

        ReceiptInfo receipt,

        String note

) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record DonationTypeInfo(

                Short id,

                String code,

                String labelKm,

                String labelEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record MemberInfo(

                Long id,

                String memberNo,

                String fullNameKm,

                String fullNameEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record SponsorInfo(

                Long id,

                String name
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ActivityInfo(

                Long id,

                String titleKm,

                String titleEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record BranchInfo(

                Long id,

                String nameKm,

                String nameEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PaymentMethodInfo(

                Short id,

                String code,

                String labelKm,

                String labelEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record RecordedByInfo(

                Long id,

                Long memberId,

                String fullNameKm,

                String fullNameEn
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ReceiptInfo(

                Long id,

                String url,

                String originalName,

                String mimeType,

                Long sizeBytes
        ) {
        }
}