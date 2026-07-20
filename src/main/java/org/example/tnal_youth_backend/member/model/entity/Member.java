package org.example.tnal_youth_backend.member.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.branch.model.entity.Branch;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "member_no",
            nullable = false,
            unique = true,
            length = 50
    )
    private String memberNo;

    @Column(
            name = "full_name_km",
            nullable = false,
            length = 255
    )
    private String fullNameKm;

    @Column(
            name = "full_name_en",
            length = 255
    )
    private String fullNameEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false
    )
    private Branch branch;

    @Column(
            name = "phone",
            length = 30
    )
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(
            name = "gender",
            nullable = false,
            length = 20
    )
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "joined_on")
    private LocalDate joinedOn;

    @Column(name = "bio")
    private String bio;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;
}