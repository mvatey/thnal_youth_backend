package org.example.tnal_youth_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberPaymentDto {
    private Long memberId;
    private String paymentType;
    private Double amount;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String paymentMethod;
}
