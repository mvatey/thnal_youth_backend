package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberNo(String memberNo);
}
