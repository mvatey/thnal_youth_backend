package org.example.tnal_youth_backend.lookup.repository;

import org.example.tnal_youth_backend.lookup.entity.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchStatusRepository
        extends JpaRepository<BranchStatus, Short> {

    List<BranchStatus>
    findAllByIsActiveTrueOrderByIdAsc();
}