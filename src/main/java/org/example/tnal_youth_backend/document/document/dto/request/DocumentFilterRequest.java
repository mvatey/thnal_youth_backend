package org.example.tnal_youth_backend.document.document.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DocumentFilterRequest(
        String search,
        Short typeId,
        Long branchId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo
) {

    public static DocumentFilterRequest empty() {
        return new DocumentFilterRequest(null, null, null, null, null, null);
    }

    public String normalizedSearch() {
        if (search == null) {
            return null;
        }
        String normalized = search.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public boolean hasTypeId() {
        return typeId != null;
    }

    public boolean hasBranchId() {
        return branchId != null;
    }

    public LocalDate effectiveDateFrom() {
        return date != null ? date : dateFrom;
    }

    public LocalDate effectiveDateTo() {
        return date != null ? date : dateTo;
    }

    public boolean isDateRangeValid() {
        LocalDate from = effectiveDateFrom();
        LocalDate to = effectiveDateTo();
        return from == null || to == null || !from.isAfter(to);
    }
}
