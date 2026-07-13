package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, Long> {
    List<MemberCredential> findByMemberId(Long memberId);
}
