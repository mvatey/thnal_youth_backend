package org.example.tnal_youth_backend.account.memberdocument.dto.response;

import java.time.OffsetDateTime;

public record MyDocumentSummaryResponse(

        long totalDocuments,

        long pdfDocuments,

        long imageDocuments,

        long otherDocuments,

        long totalSizeBytes,

        OffsetDateTime latestDocumentCreatedAt
) {
}