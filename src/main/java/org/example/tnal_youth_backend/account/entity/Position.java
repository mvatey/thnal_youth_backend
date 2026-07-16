package org.example.tnal_youth_backend.account.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "positions")
@Data
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    private String description;
}
