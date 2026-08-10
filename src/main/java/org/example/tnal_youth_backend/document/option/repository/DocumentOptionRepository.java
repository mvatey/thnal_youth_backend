package org.example.tnal_youth_backend.document.option.repository;

import org.example.tnal_youth_backend.document.option.entity.DocumentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentOptionRepository extends JpaRepository<DocumentOption, Short> {

    List<DocumentOption> findAllByIsActiveTrueOrderByCategoryAscSortOrderAscIdAsc();

    List<DocumentOption> findAllByCategoryAndIsActiveTrueOrderBySortOrderAscIdAsc(String category);
}
