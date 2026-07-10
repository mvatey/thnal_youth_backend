package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member_family")
@Data
public class MemberFamily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "relation", nullable = false)
    private String relation; // e.g. Father, Mother, Sibling, Spouse, Child

    @Column(name = "full_name_en")
    private String fullNameEn;

    @Column(name = "full_name_kh")
    private String fullNameKh;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "occupation")
    private String occupation;
}
