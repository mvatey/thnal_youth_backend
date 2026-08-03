package org.example.tnal_youth_backend.member.ethnicity.repository;

import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EthnicityRepository
        extends JpaRepository<Ethnicity, Short> {

    List<Ethnicity>
    findAllByIsActiveTrueOrderByLabelKmAsc();

    Optional<Ethnicity>
    findByCodeIgnoreCase(String code);
}