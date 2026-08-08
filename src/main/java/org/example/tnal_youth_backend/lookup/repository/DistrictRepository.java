package org.example.tnal_youth_backend.lookup.repository;

import org.example.tnal_youth_backend.lookup.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository
        extends JpaRepository<District, Integer> {

    List<District>
    findAllByProvinceIdAndIsActiveTrueOrderByNameKmAsc(
            Short provinceId
    );
}