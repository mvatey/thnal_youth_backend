package org.example.tnal_youth_backend.document.document.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record DocumentPageResponse(
        List<DocumentListItemResponse> content,
        PaginationResponse pagination
) {

        public static DocumentPageResponse from(
                Page<DocumentListItemResponse> page
        ) {
                if (page == null) {
                        return empty(0, 10);
                }

                PaginationResponse pagination = new PaginationResponse(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()
                );

                return new DocumentPageResponse(
                        page.getContent(),
                        pagination
                );
        }

        public static DocumentPageResponse empty(
                int page,
                int size
        ) {
                return new DocumentPageResponse(
                        List.of(),
                        new PaginationResponse(
                                page,
                                size,
                                0,
                                0,
                                false,
                                false
                        )
                );
        }
}