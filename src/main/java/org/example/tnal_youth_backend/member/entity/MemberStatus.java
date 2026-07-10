package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_statuses")
@Data
public class MemberStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String labelKh;
    private String labelEn;
    private Boolean isActive;

    // ✅ Add sortOrder
    private Integer sortOrder;
}


