package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberPaymentRepository extends JpaRepository<MemberPayment, Long> {
    List<MemberPayment> findByMemberId(Long memberId);
}
