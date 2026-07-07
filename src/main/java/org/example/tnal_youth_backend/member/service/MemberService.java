package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberDto;
import org.example.tnal_youth_backend.member.entity.Branch;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberPosition;
import org.example.tnal_youth_backend.member.entity.MemberStatus;
import org.example.tnal_youth_backend.member.repository.BranchRepository;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberPositionRepository;
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

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    public Member saveMember(MemberDto dto) {
        Member member = mapDtoToEntity(dto);
        return memberRepository.save(member);
    }

//    public Member updateMember(Long id, MemberDto dto) {
//        Member member = mapDtoToEntity(dto);
//        member.setId(id);
//        return memberRepository.save(member);
//    }

    public Member updateMember(Long id, MemberDto dto) {
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Update fields on the managed entity
        existing.setFullNameEn(dto.getFullNameEn());
        existing.setFullNameKh(dto.getFullNameKh());
        existing.setGender(dto.getGender());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setAddress(dto.getAddress());
        existing.setBio(dto.getBio());

        // Resolve position/status by code
//        if (dto.getPositionCode() != null) {
//            existing.setPosition(positionRepository.findByCode(dto.getPositionCode()));
//        }
//        if (dto.getStatusCode() != null) {
//            existing.setStatus(statusRepository.findByCode(dto.getStatusCode()));
//        }

        if (dto.getPositionCode() != null) {
            MemberPosition position = positionRepository.findByCode(dto.getPositionCode())
                    .orElseThrow(() -> new RuntimeException("Position not found"));
            existing.setPosition(position);
        }

        if (dto.getStatusCode() != null) {
            MemberStatus status = statusRepository.findByCode(dto.getStatusCode())
                    .orElseThrow(() -> new RuntimeException("Status not found"));
            existing.setStatus(status);
        }

        return memberRepository.save(existing);
    }


    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    private Member mapDtoToEntity(MemberDto dto) {
        Member member = new Member();
        member.setMemberNo(dto.getMemberNo());
        member.setFullNameKh(dto.getFullNameKh());
        member.setFullNameEn(dto.getFullNameEn());
        member.setGender(dto.getGender());
        member.setDateOfBirth(dto.getDateOfBirth());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        member.setBio(dto.getBio());

        if (dto.getBranchCode() != null) {
            Branch branch = branchRepository.findByBranchCode(dto.getBranchCode())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            member.setBranch(branch);
        }

        if (dto.getPositionCode() != null) {
            MemberPosition position = positionRepository.findByCode(dto.getPositionCode())
                    .orElseThrow(() -> new RuntimeException("Position not found"));
            member.setPosition(position);
        }

        if (dto.getStatusCode() != null) {
            MemberStatus status = statusRepository.findByCode(dto.getStatusCode())
                    .orElseThrow(() -> new RuntimeException("Status not found"));
            member.setStatus(status);
        }

        return member;
    }



}
