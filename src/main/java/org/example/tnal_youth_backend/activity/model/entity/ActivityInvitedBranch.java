package org.example.tnal_youth_backend.activity.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.member.branch.entity.Branch;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "activity_invited_branches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_activity_invited_branch",
                        columnNames = {
                                "activity_id",
                                "branch_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityInvitedBranch {

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
            name = "branch_id",
            nullable = false
    )
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "invitation_status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private ActivityInvitationStatus invitationStatus =
            ActivityInvitationStatus.PENDING;

    @Column(
            name = "can_manage_attendance",
            nullable = false
    )
    @Builder.Default
    private Boolean canManageAttendance = false;

    @Column(
            name = "can_record_donation",
            nullable = false
    )
    @Builder.Default
    private Boolean canRecordDonation = false;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "invited_by",
            nullable = false
    )
    private User invitedBy;

    @Column(
            name = "invited_at",
            nullable = false
    )
    private OffsetDateTime invitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

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

        if (invitationStatus == null) {
            invitationStatus =
                    ActivityInvitationStatus.PENDING;
        }

        if (canManageAttendance == null) {
            canManageAttendance = false;
        }

        if (canRecordDonation == null) {
            canRecordDonation = false;
        }

        if (invitedAt == null) {
            invitedAt = now;
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