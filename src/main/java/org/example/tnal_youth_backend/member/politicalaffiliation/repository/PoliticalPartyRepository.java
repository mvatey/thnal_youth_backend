package org.example.tnal_youth_backend.member.politicalaffiliation.repository;

import org.example.tnal_youth_backend.member.politicalaffiliation.entity.PoliticalParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoliticalPartyRepository
        extends JpaRepository<PoliticalParty, Short> {

    boolean existsByIdAndIsActiveTrue(
            Short partyId
    );

    boolean existsByCodeIgnoreCase(
            String code
    );

    List<PoliticalParty>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();

    List<PoliticalParty>
    findAllByOrderBySortOrderAscIdAsc();
}