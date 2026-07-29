package org.example.tnal_youth_backend.authentication.model.response;

public record AccountStatusResponse(

        boolean accountExists,

        String status,

        boolean activated,

        String nextStep
) {
}