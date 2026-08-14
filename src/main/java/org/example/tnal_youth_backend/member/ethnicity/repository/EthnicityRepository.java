package org.example.tnal_youth_backend.member.ethnicity.repository;

import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EthnicityRepository
        extends JpaRepository<Ethnicity, Short> {

    List<Ethnicity>
    findAllByIsActiveTrueOrderByLabelKmAsc();

    /*
     * Admin management needs every row (active and inactive), unlike
     * the public-facing endpoint above which only returns active ones.
     */
    List<Ethnicity>
    findAllByOrderByLabelKmAsc();

    Optional<Ethnicity>
    findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}