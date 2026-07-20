package org.example.tnal_youth_backend.member.participation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(
            name = "activity_id",
            nullable = false
    )
    private Long activityId;

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

    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;

    @Column(name = "checked_in_at")
    private OffsetDateTime checkedInAt;

    @Column(name = "invited_by")
    private Long invitedById;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
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