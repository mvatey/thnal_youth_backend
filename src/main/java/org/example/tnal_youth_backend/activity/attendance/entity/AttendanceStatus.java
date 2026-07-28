package org.example.tnal_youth_backend.activity.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatus {

    @Id
    private Short id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "label_km", nullable = false)
    private String labelKm;

    @Column(name = "label_en")
    private String labelEn;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}