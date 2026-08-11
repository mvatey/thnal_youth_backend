package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyDonationResponse(

        Long id,

        String donationNo,

        DonationTypeInfo donationType,

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

    /*
     * ==========================================================
     * DONATION TYPE
     * ==========================================================
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DonationTypeInfo(

            Short id,

            String code,

            String labelKm,

            String labelEn

    ) {
    }

    /*
     * ==========================================================
     * SPONSOR
     * ==========================================================
     *
     * This object appears only for Sponsor Donations.
     * It remains null for Monthly Donations.
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SponsorInfo(

            Long id,

            Short sponsorTypeId,

            String sponsorTypeCode,

            String sponsorTypeLabelKm,

            String sponsorTypeLabelEn,

            String name,

            String phone,

            String email

    ) {
    }

    /*
     * ==========================================================
     * ACTIVITY
     * ==========================================================
     *
     * This object appears only when a donation is related
     * to an activity.
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActivityInfo(

            Long id,

            String titleKm,

            String titleEn

    ) {
    }

    /*
     * ==========================================================
     * BRANCH
     * ==========================================================
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BranchInfo(

            Long id,

            String nameKm,

            String nameEn

    ) {
    }

    /*
     * ==========================================================
     * PAYMENT METHOD
     * ==========================================================
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentMethodInfo(

            Short id,

            String code,

            String labelKm,

            String labelEn

    ) {
    }

    /*
     * ==========================================================
     * RECORDED BY
     * ==========================================================
     *
     * This is the user who entered the transaction.
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecordedByInfo(

            Long id,

            String fullNameKm,

            String fullNameEn

    ) {
    }

    /*
     * ==========================================================
     * RECEIPT
     * ==========================================================
     */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReceiptInfo(

            Long id,

            String url,

            String originalName,

            String mimeType,

            Long sizeBytes,

            Double sizeKb,

            Double sizeMb

    ) {
    }
}