package org.example.tnal_youth_backend.member.politicalaffiliation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "political_parties")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliticalParty {

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
            length = 255
    )
    private String labelKm;

    @Column(
            name = "label_en",
            nullable = false,
            length = 255
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