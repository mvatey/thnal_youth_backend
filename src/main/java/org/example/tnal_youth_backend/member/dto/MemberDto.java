package org.example.tnal_youth_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberDto {
        private String memberNo;
        private String fullNameKh;
        private String fullNameEn;
        private String gender;
        private LocalDate dateOfBirth;
        private String phone;
        private String email;
        private String address;
        private String bio;

        private String branchCode;
        private String positionCode;
        private String statusCode;

        private String profilePhoto;
        private String cvFile;
        private LocalDate membershipExpiry;

        private String level;       // values: ក, ខ, គ, ឃ, ង, ច
        private String groupCode;   // NEW: matches UI
        private LocalDate joinDate; // NEW: matches UI
}
