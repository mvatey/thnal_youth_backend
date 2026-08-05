package org.example.tnal_youth_backend.donation.monthly.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberMonthlyDonationResponse {

    private Long id;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("donation_period")
    private LocalDate donationPeriod;

    @JsonProperty("amount_khr")
    private BigDecimal amountKhr;

    @JsonProperty("amount_usd")
    private BigDecimal amountUsd;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @JsonProperty("recorded_by_id")
    private Long recordedById;

    @JsonProperty("recorded_by_name")
    private String recordedByName;

    @JsonProperty("payment_method_id")
    private Short paymentMethodId;

    @JsonProperty("payment_method_code")
    private String paymentMethodCode;

    @JsonProperty("payment_method_label_km")
    private String paymentMethodLabelKm;

    @JsonProperty("payment_method_label_en")
    private String paymentMethodLabelEn;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;

    private String note;
}