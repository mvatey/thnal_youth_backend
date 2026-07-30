package org.example.tnal_youth_backend.document.document.dto.response;

public record PaginationResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}