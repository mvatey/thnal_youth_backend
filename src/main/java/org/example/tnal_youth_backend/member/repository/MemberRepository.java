package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByMemberNo(String memberNo);
}
