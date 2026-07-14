package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberCredentialDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberCredential;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberCredentialService {
    private final MemberCredentialRepository credentialRepository;
    private final MemberRepository memberRepository;

    public MemberCredentialService(MemberCredentialRepository credentialRepository, MemberRepository memberRepository) {
        this.credentialRepository = credentialRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberCredential> getCredentialsByMember(Long memberId) {
        return credentialRepository.findByMemberId(memberId);
    }

    public MemberCredential getCredentialById(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
    }

    public MemberCredential createCredential(MemberCredentialDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberCredential credential = new MemberCredential();
        mapDtoToCredential(credential, dto, member);
        return credentialRepository.save(credential);
    }

    public MemberCredential updateCredential(Long id, MemberCredentialDto dto) {
        MemberCredential credential = getCredentialById(id);
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        mapDtoToCredential(credential, dto, member);
        return credentialRepository.save(credential);
    }

    public void deleteCredential(Long id) {
        credentialRepository.deleteById(id);
    }

    private void mapDtoToCredential(MemberCredential credential, MemberCredentialDto dto, Member member) {
        credential.setMember(member);
        credential.setCredentialNameEn(dto.getCredentialNameEn());
        credential.setCredentialNameKh(dto.getCredentialNameKh());
        credential.setIssuedByEn(dto.getIssuedByEn());
        credential.setIssuedByKh(dto.getIssuedByKh());
        credential.setIssueDate(dto.getIssueDate());
        credential.setExpiryDate(dto.getExpiryDate());
        credential.setCertificateNumber(dto.getCertificateNumber());
        credential.setDescriptionEn(dto.getDescriptionEn());
        credential.setDescriptionKh(dto.getDescriptionKh());
        credential.setFileUrl(dto.getFileUrl()); // NEW
    }
}
