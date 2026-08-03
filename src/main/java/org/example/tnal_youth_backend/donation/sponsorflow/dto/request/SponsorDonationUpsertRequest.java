package org.example.tnal_youth_backend.donation.sponsorflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SponsorDonationUpsertRequest {

    /**
     * Supported UI sponsor types:
     * INDIVIDUAL, INSTITUTION, MEMBER.
     */
    @NotBlank(message = "donorKind is required")
    @Pattern(
            regexp = "INDIVIDUAL|INSTITUTION|MEMBER",
            message = "donorKind must be INDIVIDUAL, INSTITUTION, or MEMBER"
    )
    private String donorKind;

    /**
     * Existing sponsor selected from sponsor lookup.
     * Used only for INDIVIDUAL or INSTITUTION.
     */
    private Long sponsorId;

    /**
     * Existing member selected from member lookup.
     * Required when donorKind is MEMBER.
     */
    private Long memberId;

    /**
     * Required when creating a new INDIVIDUAL or INSTITUTION sponsor.
     */
    @Size(
            max = 255,
            message = "name must be 255 characters or fewer"
    )
    private String name;

    @Size(
            max = 30,
            message = "phone must be 30 characters or fewer"
    )
    private String phone;

    @Email(message = "email must be valid")
    @Size(
            max = 255,
            message = "email must be 255 characters or fewer"
    )
    private String email;

    @Size(
            max = 2000,
            message = "address must be 2000 characters or fewer"
    )
    private String address;

    @NotNull(message = "branchId is required")
    private Long branchId;

    /**
     * Optional activity receiving the sponsor donation.
     */
    private Long activityId;

    @DecimalMin(
            value = "0.00",
            message = "amountKhr must be zero or positive"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "amountKhr must fit NUMERIC(14,2)"
    )
    private BigDecimal amountKhr;

    @DecimalMin(
            value = "0.00",
            message = "amountUsd must be zero or positive"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "amountUsd must fit NUMERIC(14,2)"
    )
    private BigDecimal amountUsd;

    @NotNull(message = "paymentMethodId is required")
    private Short paymentMethodId;

    @NotNull(message = "paidAt is required")
    private OffsetDateTime paidAt;

    @Size(
            max = 100,
            message = "paymentReference must be 100 characters or fewer"
    )
    private String paymentReference;

    private Long receiptFileId;

    /**
     * Required when payment method code is MATERIAL.
     */
    @Size(
            max = 100,
            message = "materialCategory must be 100 characters or fewer"
    )
    private String materialCategory;

    /**
     * Required and greater than zero when payment method is MATERIAL.
     */
    @Min(
            value = 1,
            message = "materialQuantity must be greater than zero"
    )
    private Integer materialQuantity;

    @Size(
            max = 255,
            message = "purpose must be 255 characters or fewer"
    )
    private String purpose;

    @Size(
            max = 4000,
            message = "note must be 4000 characters or fewer"
    )
    private String note;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "clientRequestId must be a UUID"
    )
    private String clientRequestId;

    /**
     * Optimistic-lock value returned by the detail endpoint.
     */
    private OffsetDateTime expectedUpdatedAt;
}