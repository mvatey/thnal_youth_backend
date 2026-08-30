package org.example.tnal_youth_backend.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Enriched read row for a donation: raw columns plus resolved labels from the
 * lookup / reference tables (type, payment method, branch, donor, recorder,
 * last editor).
 *
 * <p>{@code donorDisplay} is a convenience the DB computes as
 * {@code COALESCE(member.full_name_km, sponsor.name, donor_name)} so the UI has a
 * single name to render regardless of which donor source was used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;
    private String donationNo;

    private Short donationTypeId;
    private String typeCode;
    private String typeLabelKm;
    private String typeLabelEn;

    // donor source (only one of the ids/name is populated)
    private Long memberId;
    private String memberName;
    private Long sponsorId;
    private String sponsorName;
    private String donorName;
    /** COALESCE(memberName, sponsorName, donorName) — always present. */
    private String donorDisplay;

    private Long activityId;
    private String activityTitle;
    private String activityTitleEn;

    private Long branchId;
    private String branchName;
    private String branchNameEn;

    private LocalDate donationPeriod;

    private BigDecimal amountKhr;
    private BigDecimal amountUsd;
    private BigDecimal exchangeRateKhrPerUsd;
    private BigDecimal totalAmountUsd;

    private Short paymentMethodId;
    private String paymentMethodCode;
    private String paymentMethodLabelKm;
    private String paymentMethodLabelEn;

    private OffsetDateTime paidAt;

    private String paymentReference;
    private Long receiptFileId;

    private Long recordedBy;
    private String recordedByName;

    /** Who last edited this donation (V24). Null when it has never been updated. */
    private Long updatedBy;
    private String updatedByName;

    private String note;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
