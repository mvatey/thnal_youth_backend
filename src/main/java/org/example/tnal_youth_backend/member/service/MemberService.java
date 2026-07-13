package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberDto;
import org.example.tnal_youth_backend.member.entity.*;
import org.example.tnal_youth_backend.member.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public Member saveMember(MemberDto dto) {
        Branch branch = branchRepository.findByBranchCode(dto.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        MemberPosition position = positionRepository.findByCode(dto.getPositionCode())
                .orElseThrow(() -> new RuntimeException("Position not found"));
        MemberStatus status = statusRepository.findByCode(dto.getStatusCode())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        Member member = new Member();
        mapDtoToMember(member, dto, branch, position, status);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        return memberRepository.save(member);
    }

    public Member updateMember(Long id, MemberDto dto) {
        Member member = getMemberById(id);
        Branch branch = branchRepository.findByBranchCode(dto.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        MemberPosition position = positionRepository.findByCode(dto.getPositionCode())
                .orElseThrow(() -> new RuntimeException("Position not found"));
        MemberStatus status = statusRepository.findByCode(dto.getStatusCode())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        mapDtoToMember(member, dto, branch, position, status);
        member.setUpdatedAt(LocalDateTime.now());

        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    private void mapDtoToMember(Member member, MemberDto dto, Branch branch, MemberPosition position, MemberStatus status) {
        member.setMemberNo(dto.getMemberNo());
        member.setFullNameKh(dto.getFullNameKh());
        member.setFullNameEn(dto.getFullNameEn());
        member.setGender(dto.getGender());
        member.setDateOfBirth(dto.getDateOfBirth());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        member.setBio(dto.getBio());
        member.setBranch(branch);
        member.setPosition(position);
        member.setStatus(status);
        member.setProfilePhoto(dto.getProfilePhoto());
        member.setCvFile(dto.getCvFile());
        member.setMembershipExpiry(dto.getMembershipExpiry());
    }
}
