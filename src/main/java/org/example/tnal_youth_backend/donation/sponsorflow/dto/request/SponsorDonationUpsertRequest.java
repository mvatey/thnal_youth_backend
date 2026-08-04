package org.example.tnal_youth_backend.donation.sponsorflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SponsorDonationUpsertRequest {

    @NotBlank(message = "donorKind is required")
    @Pattern(
            regexp = "INDIVIDUAL|INSTITUTION|MEMBER",
            message = "donorKind must be INDIVIDUAL, INSTITUTION, or MEMBER"
    )
    private String donorKind;

    private Long sponsorId;
    private Long memberId;

    @Size(max = 255, message = "name must be 255 characters or fewer")
    private String name;

    @Size(max = 30, message = "phone must be 30 characters or fewer")
    private String phone;

    @Email(message = "email must be valid")
    @Size(max = 255, message = "email must be 255 characters or fewer")
    private String email;

    @Size(max = 2000, message = "address must be 2000 characters or fewer")
    private String address;

    @NotNull(message = "branchId is required")
    private Long branchId;

    private Long activityId;

    @DecimalMin(value = "0.00", message = "amountKhr must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amountKhr must fit NUMERIC(14,2)")
    private BigDecimal amountKhr;

    @DecimalMin(value = "0.00", message = "amountUsd must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amountUsd must fit NUMERIC(14,2)")
    private BigDecimal amountUsd;

    @NotNull(message = "paymentMethodId is required")
    private Short paymentMethodId;

    @NotNull(message = "paidAt is required")
    private OffsetDateTime paidAt;

    @Size(max = 100, message = "paymentReference must be 100 characters or fewer")
    private String paymentReference;

    private Long receiptFileId;

    /** Free-text material name/category, e.g. សៀវភៅ, អង្ករ. */
    @Size(max = 150, message = "materialCategory must be 150 characters or fewer")
    private String materialCategory;

    /** Decimal quantity is supported, e.g. 1.5 kg. */
    @DecimalMin(value = "0.01", message = "materialQuantity must be greater than zero")
    @Digits(integer = 12, fraction = 3, message = "materialQuantity must fit NUMERIC(15,3)")
    private BigDecimal materialQuantity;

    /** Free-text unit/type, e.g. ក្បាល, គីឡូក្រាម, ប្រអប់. */
    @Size(max = 100, message = "materialQuantityType must be 100 characters or fewer")
    private String materialQuantityType;

    @Size(max = 255, message = "purpose must be 255 characters or fewer")
    private String purpose;

    @Size(max = 4000, message = "note must be 4000 characters or fewer")
    private String note;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "clientRequestId must be a UUID"
    )
    private String clientRequestId;

    private OffsetDateTime expectedUpdatedAt;
}
