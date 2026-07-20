package org.example.tnal_youth_backend.document.type.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.mapper.DocumentTypeMapper;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeServiceImpl
        implements DocumentTypeService {

    private final DocumentTypeRepository
            documentTypeRepository;

    private final DocumentTypeMapper
            documentTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeResponse>
    getActiveDocumentTypes() {

        return documentTypeRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(documentTypeMapper::toResponse)
                .toList();
    }
}