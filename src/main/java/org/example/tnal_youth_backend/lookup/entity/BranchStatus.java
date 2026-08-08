package org.example.tnal_youth_backend.lookup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String code;

    @Column(
            name = "label_km",
            nullable = false,
            length = 100
    )
    private String nameKm;

    @Column(
            name = "label_en",
            length = 100
    )
    private String nameEn;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive;
}