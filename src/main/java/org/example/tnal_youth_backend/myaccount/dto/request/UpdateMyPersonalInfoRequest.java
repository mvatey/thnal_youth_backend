package org.example.tnal_youth_backend.myaccount.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.LifeStatus;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;

import java.time.LocalDate;

public record UpdateMyPersonalInfoRequest(

        @JsonProperty("full_name_km")
        @NotBlank(
                message = "Khmer full name is required"
        )
        @Size(
                max = 255,
                message = "Khmer full name must not exceed 255 characters"
        )
        String fullNameKm,

        @JsonProperty("full_name_en")
        @Size(
                max = 255,
                message = "English full name must not exceed 255 characters"
        )
        String fullNameEn,

        Gender gender,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @Size(
                max = 255,
                message = "Username must not exceed 255 characters"
        )
        String username,

        @Email(
                message = "Email format is invalid"
        )
        String email,

        @Size(
                max = 30,
                message = "Phone must not exceed 30 characters"
        )
        String phone,

        @JsonProperty("religion_id")
        Short religionId,

        @JsonProperty("ethnicity_id")
        Short ethnicityId,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("member_level_id")
        Short memberLevelId,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("tshirt_size")
        TshirtSize tshirtSize,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress,

        /** Optional; null leaves the stored value unchanged. */
        @JsonProperty("registered_voter")
        Boolean registeredVoter,

        /** Optional; null leaves it unchanged, blank clears it. */
        @JsonProperty("national_id_number")
        @Size(max = 50, message = "National ID number must not exceed 50 characters")
        String nationalIdNumber,

        /** Optional; null leaves it unchanged, blank clears it. Cleared when not a registered voter. */
        @JsonProperty("voter_card_number")
        @Size(max = 50, message = "Voter card number must not exceed 50 characters")
        String voterCardNumber,

        /** Optional; null leaves the stored value unchanged. */
        @JsonProperty("from_other_party")
        Boolean fromOtherParty,

        /** Optional; null leaves it unchanged, blank clears it. Cleared when not from another party. */
        @JsonProperty("former_party")
        @Size(max = 255, message = "Former party must not exceed 255 characters")
        String formerParty,

        /**
         * Ignored: life status is recorded by staff from the member's
         * personal-info page, not by the account owner.
         */
        @JsonProperty("life_status")
        LifeStatus lifeStatus
) {
}
