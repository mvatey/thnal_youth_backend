package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.repository.BranchRepository;
import org.example.tnal_youth_backend.member.repository.MemberPositionRepository;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final MemberPositionRepository positionRepository;
    private final MemberStatusRepository statusRepository;

    public MemberService(MemberRepository memberRepository,
                         BranchRepository branchRepository,
                         MemberPositionRepository positionRepository,
                         MemberStatusRepository statusRepository) {
        this.memberRepository = memberRepository;
        this.branchRepository = branchRepository;
        this.positionRepository = positionRepository;
        this.statusRepository = statusRepository;
    }

    public List<Member> getAllMembers() { return memberRepository.findAll(); }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public Member saveMember(MemberDto dto) {
        Member member = new Member();
        member.setMemberNo(dto.getMemberNo());
        member.setFullNameEn(dto.getFullNameEn());
        member.setFullNameKh(dto.getFullNameKh());
        member.setGender(dto.getGender());
        member.setDateOfBirth(dto.getDateOfBirth());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        member.setBio(dto.getBio());

        member.setBranch(branchRepository.findByBranchCode(dto.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found")));
        member.setPosition(positionRepository.findByCode(dto.getPositionCode()).orElse(null));
        member.setStatus(statusRepository.findByCode(dto.getStatusCode()).orElse(null));

        return memberRepository.save(member);
    }

    public Member updateMember(Long id, MemberDto dto) {
        Member member = getMemberById(id);
        member.setFullNameEn(dto.getFullNameEn());
        member.setFullNameKh(dto.getFullNameKh());
        member.setGender(dto.getGender());
        member.setDateOfBirth(dto.getDateOfBirth());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        member.setBio(dto.getBio());
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) { memberRepository.deleteById(id); }
}

