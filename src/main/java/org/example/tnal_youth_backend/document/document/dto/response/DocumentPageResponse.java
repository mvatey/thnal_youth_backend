package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DocumentPageResponse(

        List<DocumentResponse> content,

        int page,

        int size,

        @JsonProperty("total_elements")
        long totalElements,

        @JsonProperty("total_pages")
        int totalPages,

        boolean first,

        boolean last
) {
}