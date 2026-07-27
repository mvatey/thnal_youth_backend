package org.example.tnal_youth_backend.document.type.service;

import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;

import java.util.List;

public interface DocumentTypeService {

    List<DocumentTypeResponse> getActiveDocumentTypes();
}