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

        // Extra fields
        private String profilePhoto;
        private String cvFile;
        private LocalDate membershipExpiry;

        // New field
        private String level; // values: ក, ខ, គ, ឃ, ង, ច
}
