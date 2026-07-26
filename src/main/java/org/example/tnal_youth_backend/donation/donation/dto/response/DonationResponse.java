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

        Long memberId,

        Long branchId,

        LocalDate donationPeriod,

        BigDecimal amountKhr,

        BigDecimal amountUsd,

        PaymentMethodInfo paymentMethod,

        OffsetDateTime paidAt,

        ReceiptInfo receipt,

        String note

) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record DonationTypeInfo(

                Short id,

                String labelKm

        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PaymentMethodInfo(

                Short id,

                String labelKm

        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ReceiptInfo(

                Long id

        ) {
        }
}