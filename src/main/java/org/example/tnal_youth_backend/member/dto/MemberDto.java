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
        private String branchName;

        //  Add these
        private String positionCode;
        private String statusCode;

        // Optional: keep names if you want to show labels in responses
        private String positionName;
        private String statusName;
}

