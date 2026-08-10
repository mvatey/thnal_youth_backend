package org.example.tnal_youth_backend.document.option.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.option.dto.DocumentOptionResponse;
import org.example.tnal_youth_backend.document.option.entity.DocumentOption;
import org.example.tnal_youth_backend.document.option.repository.DocumentOptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentOptionService {

    private static final Set<String> CATEGORIES = Set.of("FONT", "LANGUAGE", "CARD_SIZE");
    private final DocumentOptionRepository repository;

    @Transactional(readOnly = true)
    public List<DocumentOptionResponse> getActiveOptions(String category) {
        String normalizedCategory = normalizeCategory(category);
        List<DocumentOption> options = normalizedCategory == null
                ? repository.findAllByIsActiveTrueOrderByCategoryAscSortOrderAscIdAsc()
                : repository.findAllByCategoryAndIsActiveTrueOrderBySortOrderAscIdAsc(normalizedCategory);

        return options.stream().map(this::toResponse).toList();
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String normalized = category.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if (!CATEGORIES.contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category must be FONT, LANGUAGE, or CARD_SIZE"
            );
        }
        return normalized;
    }

    private DocumentOptionResponse toResponse(DocumentOption option) {
        return new DocumentOptionResponse(
                option.getId(), option.getCategory(), option.getCode(), option.getValue(),
                option.getLabelKm(), option.getLabelEn(), option.getDescription(), option.getSortOrder()
        );
    }
}
