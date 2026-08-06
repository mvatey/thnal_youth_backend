package org.example.tnal_youth_backend.member.family.repository;

import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberFamilyRepository
        extends JpaRepository<MemberFamily, Long> {

    List<MemberFamily>
    findAllByMember_IdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberFamily>
    findByIdAndMember_Id(
            Long familyId,
            Long memberId
    );

    Optional<MemberFamily>
    findByMember_IdAndRelationship(
            Long memberId,
            FamilyRelationship relationship
    );

    boolean existsByMember_IdAndRelationship(
            Long memberId,
            FamilyRelationship relationship
    );

    boolean existsByMember_IdAndRelationshipAndIdNot(
            Long memberId,
            FamilyRelationship relationship,
            Long familyId
    );
}