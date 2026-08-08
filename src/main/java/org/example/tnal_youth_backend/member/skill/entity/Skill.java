package org.example.tnal_youth_backend.member.skill.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "skills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Short id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;

    @Column(
            name = "label_km",
            nullable = false,
            length = 150
    )
    private String labelKm;

    @Column(
            name = "label_en",
            nullable = false,
            length = 150
    )
    private String labelEn;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime updatedAt;
}