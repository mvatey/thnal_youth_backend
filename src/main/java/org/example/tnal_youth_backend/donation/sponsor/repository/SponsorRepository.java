package org.example.tnal_youth_backend.donation.sponsor.repository;

import org.example.tnal_youth_backend.donation.sponsor.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorRepository
        extends JpaRepository<Sponsor, Long> {

    List<Sponsor> findAllByOrderByCreatedAtDesc();

    List<Sponsor>
    findAllByIsActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );

    boolean existsByPhone(
            String phone
    );

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );
}