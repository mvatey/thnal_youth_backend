package org.example.tnal_youth_backend.account.memberdocument.service;

import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentResponse;
import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentSummaryResponse;

import java.util.List;

public interface MyDocumentService {

    /*
     * Get all documents directly assigned to
     * the currently logged-in member.
     */
    List<MyDocumentResponse> getMyDocuments();

    /*
     * Get one document only when it belongs to
     * the currently logged-in member.
     */
    MyDocumentResponse getMyDocumentById(
            Long documentId
    );

    /*
     * Return document totals for the
     * currently logged-in member.
     */
    MyDocumentSummaryResponse getMyDocumentSummary();
}