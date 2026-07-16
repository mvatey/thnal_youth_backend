package org.example.tnal_youth_backend.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_work_history")
@Data
public class AccountWorkHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="account_id", nullable=false)
    private Account account;

    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibilities;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
