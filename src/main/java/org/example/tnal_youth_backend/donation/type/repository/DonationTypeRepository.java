package org.example.tnal_youth_backend.donation.type.repository;

import org.example.tnal_youth_backend.donation.type.model.entity.DonationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationTypeRepository
        extends JpaRepository<DonationType, Short> {

    List<DonationType> findAllByOrderBySortOrderAscIdAsc();

    List<DonationType>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();

    Optional<DonationType> findByCodeIgnoreCase(
            String code
    );
}