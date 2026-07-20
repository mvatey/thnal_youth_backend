package org.example.tnal_youth_backend.member.credential.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_credentials",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_credentials_credential_no",
                        columnNames = "credential_no"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "member_id",
            nullable = false
    )
    private Long memberId;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "credential_kind",
            nullable = false,
            length = 100
    )
    private String credentialKind;

    @Column(
            name = "credential_no",
            unique = true,
            length = 150
    )
    private String credentialNo;

    @Column(name = "issued_on")
    private LocalDate issuedOn;

    /*
     * Database type is BIGINT.
     * This represents the ID of the user who issued the credential.
     */
    @Column(name = "issued_by")
    private Long issuedById;

    @Column(name = "file_id")
    private Long fileId;

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

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}