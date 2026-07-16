package org.example.tnal_youth_backend.account.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String accountCode;
    private String roleName;
    private Boolean isActive;
}

