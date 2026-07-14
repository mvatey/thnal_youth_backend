package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_payments")
@Data
public class MemberPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "bank_type")       // NEW: matches UI
    private String bankType;

    @Column(name = "transaction_id")  // NEW: useful for tracking
    private String transactionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
