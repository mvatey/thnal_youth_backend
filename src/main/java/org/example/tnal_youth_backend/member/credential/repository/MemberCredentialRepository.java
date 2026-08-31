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

    /**
     * A member-owned document and its credential are linked only by
     * convention -- matching (member_id, file_id), no real foreign key
     * (see Document/MemberCredential entities) -- so deleting the
     * document has to explicitly clean up its credential too, or the
     * credential is orphaned: it still blocks a future reissue via the
     * duplicate-certificate check, and the member's own document page
     * papers over the missing document by reconstructing a "phantom"
     * card straight from it.
     */
    void deleteByMemberIdAndFileId(
            Long memberId,
            Long fileId
    );
}