package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberPoliticalAffiliationDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberPoliticalAffiliation;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberPoliticalAffiliationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberPoliticalAffiliationService {
    private final MemberPoliticalAffiliationRepository affiliationRepository;
    private final MemberRepository memberRepository;

    public MemberPoliticalAffiliationService(MemberPoliticalAffiliationRepository affiliationRepository,
                                             MemberRepository memberRepository) {
        this.affiliationRepository = affiliationRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberPoliticalAffiliation> getAffiliationsByMember(Long memberId) {
        return affiliationRepository.findByMemberId(memberId);
    }

    public MemberPoliticalAffiliation getAffiliationById(Long id) {
        return affiliationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affiliation not found"));
    }

    public MemberPoliticalAffiliation createAffiliation(MemberPoliticalAffiliationDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberPoliticalAffiliation affiliation = new MemberPoliticalAffiliation();
        mapDtoToAffiliation(affiliation, dto, member);
        return affiliationRepository.save(affiliation);
    }

    public MemberPoliticalAffiliation updateAffiliation(Long id, MemberPoliticalAffiliationDto dto) {
        MemberPoliticalAffiliation affiliation = getAffiliationById(id);
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        mapDtoToAffiliation(affiliation, dto, member);
        return affiliationRepository.save(affiliation);
    }

    public void deleteAffiliation(Long id) {
        affiliationRepository.deleteById(id);
    }

    private void mapDtoToAffiliation(MemberPoliticalAffiliation affiliation,
                                     MemberPoliticalAffiliationDto dto,
                                     Member member) {
        affiliation.setMember(member);
        affiliation.setPartyNameEn(dto.getPartyNameEn());
        affiliation.setPartyNameKh(dto.getPartyNameKh());
        affiliation.setRoleEn(dto.getRoleEn());
        affiliation.setRoleKh(dto.getRoleKh());
        affiliation.setCountryEn(dto.getCountryEn());
        affiliation.setCountryKh(dto.getCountryKh());
        affiliation.setAppointmentCode(dto.getAppointmentCode());
        affiliation.setWorkplaceEn(dto.getWorkplaceEn());
        affiliation.setWorkplaceKh(dto.getWorkplaceKh());
        affiliation.setStartDate(dto.getStartDate());
        affiliation.setEndDate(dto.getEndDate());
        affiliation.setStatus(dto.getStatus()); // NEW field
    }
}
