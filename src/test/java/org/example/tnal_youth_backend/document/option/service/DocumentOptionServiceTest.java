package org.example.tnal_youth_backend.document.option.service;

import org.example.tnal_youth_backend.document.option.entity.DocumentOption;
import org.example.tnal_youth_backend.document.option.repository.DocumentOptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentOptionServiceTest {

    @Mock DocumentOptionRepository repository;
    @InjectMocks DocumentOptionService service;

    @Test
    void categoryIsNormalizedAndOnlyActiveOptionsAreRequested() {
        DocumentOption font = new DocumentOption();
        font.setId((short) 1);
        font.setCategory("FONT");
        font.setCode("NOTO_SANS");
        font.setValue("Noto Sans");
        font.setLabelKm("Noto Sans Khmer");
        font.setLabelEn("Noto Sans Khmer");
        font.setSortOrder(1);

        when(repository.findAllByCategoryAndIsActiveTrueOrderBySortOrderAscIdAsc("FONT"))
                .thenReturn(List.of(font));

        var result = service.getActiveOptions("font");

        assertEquals(1, result.size());
        assertEquals("Noto Sans", result.getFirst().value());
        verify(repository).findAllByCategoryAndIsActiveTrueOrderBySortOrderAscIdAsc("FONT");
    }

    @Test
    void unknownCategoryIsRejected() {
        assertThrows(ResponseStatusException.class, () -> service.getActiveOptions("unknown"));
    }
}
