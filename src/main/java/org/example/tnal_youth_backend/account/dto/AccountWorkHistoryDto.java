package org.example.tnal_youth_backend.account.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AccountWorkHistoryDto {
    private Long accountId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibilities;
}
