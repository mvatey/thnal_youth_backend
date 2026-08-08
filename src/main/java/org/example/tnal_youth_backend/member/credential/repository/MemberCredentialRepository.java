package org.example.tnal_youth_backend.member.credential.repository;

import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCredentialRepository
        extends JpaRepository<MemberCredential, Long> {

    @EntityGraph(attributePaths = {
            "activity",
            "issuedBy",
            "file"
    })
    List<MemberCredential>
    findAllByMemberIdOrderByCreatedAtDesc(
            Long memberId
    );

    @EntityGraph(attributePaths = {
            "activity",
            "issuedBy",
            "file"
    })
    Optional<MemberCredential>
    findByIdAndMemberId(
            Long id,
            Long memberId
    );

    boolean existsByCredentialNo(
            String credentialNo
    );

    boolean existsByCredentialNoAndIdNot(
            String credentialNo,
            Long id
    );

    boolean existsByMemberIdAndCredentialKindIgnoreCase(
            Long memberId,
            String credentialKind
    );

    boolean existsByMemberIdAndCredentialKindIgnoreCaseAndIdNot(
            Long memberId,
            String credentialKind,
            Long id
    );


    boolean existsByMemberIdAndActivityIdAndCredentialKindIgnoreCase(
            Long memberId,
            Long activityId,
            String credentialKind
    );

    boolean existsByMemberIdAndActivityIdAndCredentialKindIgnoreCaseAndIdNot(
            Long memberId,
            Long activityId,
            String credentialKind,
            Long id
    );
}