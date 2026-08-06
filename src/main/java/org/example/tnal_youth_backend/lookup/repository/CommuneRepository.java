package org.example.tnal_youth_backend.lookup.repository;

import org.example.tnal_youth_backend.lookup.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommuneRepository
        extends JpaRepository<Commune, Integer> {

    List<Commune>
    findAllByDistrictIdAndIsActiveTrueOrderByNameKmAsc(
            Integer districtId
    );
}