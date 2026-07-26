package org.example.tnal_youth_backend.donation.donation.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DonationMapper {

    private final JdbcTemplate jdbcTemplate;

    public DonationResponse toResponse(
            Donation donation
    ) {
        if (donation == null) {
            return null;
        }

        DonationResponse.DonationTypeInfo donationType =
                getDonationTypeInfo(
                        donation.getDonationTypeId()
                );

        DonationResponse.PaymentMethodInfo paymentMethod =
                getPaymentMethodInfo(
                        donation.getPaymentMethodId()
                );

        DonationResponse.ReceiptInfo receipt =
                donation.getReceiptFileId() == null
                        ? null
                        : new DonationResponse.ReceiptInfo(
                        donation.getReceiptFileId()
                );

        return new DonationResponse(
                donation.getId(),
                donation.getDonationNo(),
                donationType,
                donation.getMemberId(),
                donation.getBranchId(),
                donation.getDonationPeriod(),
                donation.getAmountKhr(),
                donation.getAmountUsd(),
                paymentMethod,
                donation.getPaidAt(),
                receipt,
                donation.getNote()
        );
    }

    private DonationResponse.DonationTypeInfo
    getDonationTypeInfo(
            Short donationTypeId
    ) {
        if (donationTypeId == null) {
            return null;
        }

        List<DonationResponse.DonationTypeInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            label_km
                        FROM donation_types
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.DonationTypeInfo(
                                getNullableShort(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "label_km"
                                )
                        ),
                        donationTypeId
                );

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    private DonationResponse.PaymentMethodInfo
    getPaymentMethodInfo(
            Short paymentMethodId
    ) {
        if (paymentMethodId == null) {
            return null;
        }

        List<DonationResponse.PaymentMethodInfo> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            label_km
                        FROM payment_methods
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new DonationResponse.PaymentMethodInfo(
                                getNullableShort(
                                        resultSet.getObject(
                                                "id"
                                        )
                                ),
                                resultSet.getString(
                                        "label_km"
                                )
                        ),
                        paymentMethodId
                );

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    private Short getNullableShort(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value).shortValue();
    }
}