package org.example.tnal_youth_backend.lookup.service;

import org.example.tnal_youth_backend.lookup.dto.variable.AdminLookupResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.CreateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.LookupCategoryResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupStatusRequest;

import java.util.List;

public interface AdminLookupService {

    List<LookupCategoryResponse>
    getCategories();

    List<AdminLookupResponse>
    getItems(
            String category,
            String search,
            String status
    );

    AdminLookupResponse
    createItem(
            String category,
            CreateLookupRequest request
    );

    AdminLookupResponse
    updateItem(
            String category,
            Short id,
            UpdateLookupRequest request
    );

    AdminLookupResponse
    updateStatus(
            String category,
            Short id,
            UpdateLookupStatusRequest request
    );
}