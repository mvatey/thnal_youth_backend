package org.example.tnal_youth_backend.activity.model.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityDailyScheduleResponse {
    private LocalDate scheduleDate;
    private LocalTime startsAt;
    private LocalTime endsAt;
}
