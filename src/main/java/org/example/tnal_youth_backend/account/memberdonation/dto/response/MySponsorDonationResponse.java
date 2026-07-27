package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MySponsorDonationResponse(

        Long id,

        String donationNo,

        DonationTypeInfo donationType,

        SponsorInfo sponsor,

        String donorName,

        BranchInfo branch,

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
    public record SponsorInfo(
            Long id,
            String name
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