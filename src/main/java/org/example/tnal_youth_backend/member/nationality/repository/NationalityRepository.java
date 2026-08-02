package org.example.tnal_youth_backend.member.nationality.repository;

import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NationalityRepository
        extends JpaRepository<Nationality, Short> {

    List<Nationality>
    findAllByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<Nationality>
    findByIdAndIsActiveTrue(Short id);

    Optional<Nationality>
    findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}