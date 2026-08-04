package org.example.tnal_youth_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Paginated inbox response with total for UI page counts. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageDTO {
    private List<NotificationDTO> items;
    private long total;
    private int page;
    private int size;
}