package org.example.tnal_youth_backend.activity.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "activity_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_activity_participant",
                        columnNames = {
                                "activity_id",
                                "member_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "activity_id",
            nullable = false
    )
    private Activity activity;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(name = "attendance_status_id")
    private Short attendanceStatusId;

    @Column(
            name = "registered_at",
            nullable = false
    )
    private OffsetDateTime registeredAt;


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


    @Column(name = "checked_in_at")
    private OffsetDateTime checkedInAt;

    @Column(name = "checked_out_at")
    private OffsetDateTime checkedOutAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_branch_id")
    private ActivityInvitedBranch invitedBranch;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "registration_source",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private ParticipantRegistrationSource registrationSource =
            ParticipantRegistrationSource.MANUAL;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now =
                OffsetDateTime.now();

        if (registeredAt == null) {
            registeredAt = now;
        }

        if (registrationSource == null) {
            registrationSource =
                    ParticipantRegistrationSource.MANUAL;
        }

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