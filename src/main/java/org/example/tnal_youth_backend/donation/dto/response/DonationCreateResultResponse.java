package org.example.tnal_youth_backend.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Lightweight result returned by create. Echoes the minted donation number and
 * the server-computed USD total so the client does not have to re-fetch just to
 * show a confirmation / receipt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationCreateResultResponse {
    private Long id;
    private String donationNo;
    private BigDecimal totalAmountUsd;
    private OffsetDateTime createdAt;
}
