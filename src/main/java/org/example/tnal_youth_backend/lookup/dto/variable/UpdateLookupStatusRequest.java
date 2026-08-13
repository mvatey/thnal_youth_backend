package org.example.tnal_youth_backend.lookup.dto.variable;

import jakarta.validation.constraints.NotNull;

public record UpdateLookupStatusRequest(

        @NotNull
        Boolean active

) {
}