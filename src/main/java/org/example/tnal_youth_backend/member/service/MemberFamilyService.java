package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberFamilyDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberFamily;
import org.example.tnal_youth_backend.member.repository.MemberFamilyRepository;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberFamilyService {
    private final MemberFamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    public MemberFamilyService(MemberFamilyRepository familyRepository, MemberRepository memberRepository) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberFamily> getFamiliesByMember(Long memberId) {
        return familyRepository.findByMemberId(memberId);
    }

    public MemberFamily getFamilyById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family record not found"));
    }

    public MemberFamily createFamily(MemberFamilyDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberFamily family = new MemberFamily();
        family.setMember(member);
        family.setRelation(dto.getRelation());
        family.setFullNameEn(dto.getFullNameEn());
        family.setFullNameKh(dto.getFullNameKh());
        family.setPhone(dto.getPhone());
        family.setEmail(dto.getEmail());
        family.setOccupation(dto.getOccupation());

        return familyRepository.save(family);
    }

    public MemberFamily updateFamily(Long id, MemberFamilyDto dto) {
        MemberFamily family = getFamilyById(id);
        family.setRelation(dto.getRelation());
        family.setFullNameEn(dto.getFullNameEn());
        family.setFullNameKh(dto.getFullNameKh());
        family.setPhone(dto.getPhone());
        family.setEmail(dto.getEmail());
        family.setOccupation(dto.getOccupation());
        return familyRepository.save(family);
    }

    public void deleteFamily(Long id) {
        familyRepository.deleteById(id);
    }
}
