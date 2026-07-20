package org.example.tnal_youth_backend.member.credential.repository;

import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCredentialRepository
        extends JpaRepository<MemberCredential, Long> {

    List<MemberCredential> findAllByMemberIdOrderByCreatedAtDesc(
            Long memberId
    );

    Optional<MemberCredential> findByIdAndMemberId(
            Long id,
            Long memberId
    );

    boolean existsByCredentialNo(String credentialNo);

    boolean existsByCredentialNoAndIdNot(
            String credentialNo,
            Long id
    );
}