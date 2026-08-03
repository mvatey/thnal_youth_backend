package org.example.tnal_youth_backend.member.ethnicity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ethnicities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ethnicity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Column(
            name = "label_km",
            nullable = false,
            length = 100
    )
    private String labelKm;

    @Column(
            name = "label_en",
            length = 100
    )
    private String labelEn;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive;
}