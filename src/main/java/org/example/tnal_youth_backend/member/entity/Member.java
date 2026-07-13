package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="member_no", unique = true, nullable = false)
    private String memberNo;

    @ManyToOne @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne @JoinColumn(name = "position_id")
    private MemberPosition position;

    @ManyToOne @JoinColumn(name = "status_id")
    private MemberStatus status;

    private String fullNameEn;
    private String fullNameKh;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private String bio;

    private String profilePhoto;
    private String cvFile;

    private LocalDate membershipExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
