package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberPaymentDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberPayment;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberPaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberPaymentService {
    private final MemberPaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    public MemberPaymentService(MemberPaymentRepository paymentRepository, MemberRepository memberRepository) {
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberPayment> getPaymentsByMember(Long memberId) {
        return paymentRepository.findByMemberId(memberId);
    }

    public MemberPayment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public MemberPayment createPayment(MemberPaymentDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberPayment payment = new MemberPayment();
        payment.setMember(member);
        payment.setPaymentType(dto.getPaymentType());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setBankType(dto.getBankType());          // NEW
        payment.setTransactionId(dto.getTransactionId()); // NEW
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public MemberPayment updatePayment(Long id, MemberPaymentDto dto) {
        MemberPayment payment = getPaymentById(id);
        payment.setPaymentType(dto.getPaymentType());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setBankType(dto.getBankType());          // NEW
        payment.setTransactionId(dto.getTransactionId()); // NEW
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
