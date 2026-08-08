package org.example.tnal_youth_backend.lookup.repository;

import org.example.tnal_youth_backend.lookup.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProvinceRepository
        extends JpaRepository<Province, Short> {

    @Query("""
            SELECT p
            FROM Province p
            WHERE p.id IN (
                SELECT DISTINCT b.provinceId
                FROM Branch b
            )
            ORDER BY p.nameKm ASC
            """)
    List<Province> findProvinceOptionsForAdmin();

    @Query("""
            SELECT p
            FROM Province p
            WHERE p.id IN (
                SELECT DISTINCT b.provinceId
                FROM Branch b
                WHERE b.id IN :branchIds
            )
            ORDER BY p.nameKm ASC
            """)
    List<Province> findProvinceOptionsByBranchIds(
            @Param("branchIds")
            Set<Long> branchIds
    );

    List<Province>
    findAllByIsActiveTrueOrderByNameKmAsc();
}