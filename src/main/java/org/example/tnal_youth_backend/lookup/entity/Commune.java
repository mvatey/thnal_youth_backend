package org.example.tnal_youth_backend.lookup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "communes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(name = "name_km", nullable = false, length = 100)
    private String nameKm;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}