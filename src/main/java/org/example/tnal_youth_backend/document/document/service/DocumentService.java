package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;

import java.util.List;

public interface DocumentService {

    List<DocumentResponse> getAllDocuments();

    DocumentResponse getDocumentById(
            Long id
    );

    DocumentResponse createDocument(
            DocumentRequest request
    );

    DocumentResponse updateDocument(
            Long id,
            DocumentRequest request
    );

    void deleteDocument(
            Long id
    );
}