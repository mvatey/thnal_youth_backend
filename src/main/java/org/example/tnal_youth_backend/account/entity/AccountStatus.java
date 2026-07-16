package org.example.tnal_youth_backend.account.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "account_statuses")
@Data
public class AccountStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="status_name", nullable=false, unique=true)
    private String statusName;

    private String description;
}

