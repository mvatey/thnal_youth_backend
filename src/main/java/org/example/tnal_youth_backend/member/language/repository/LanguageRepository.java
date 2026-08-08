package org.example.tnal_youth_backend.member.language.repository;

import org.example.tnal_youth_backend.member.language.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository
        extends JpaRepository<Language, Short> {

    List<Language>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}