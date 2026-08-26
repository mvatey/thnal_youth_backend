package org.example.tnal_youth_backend.activity.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activity_daily_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityDailySchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;
    @Column(name = "starts_at", nullable = false)
    private LocalTime startsAt;
    @Column(name = "ends_at", nullable = false)
    private LocalTime endsAt;
}
