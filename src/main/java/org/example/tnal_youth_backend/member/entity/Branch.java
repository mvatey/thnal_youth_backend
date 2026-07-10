package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "branches")
@Data
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="branch_code", unique = true, nullable = false)
    private String branchCode;

    @Column(name="name_en")
    private String nameEn;

    @Column(name="name_kh")
    private String nameKh;
}
