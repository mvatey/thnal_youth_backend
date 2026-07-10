package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberTrainingDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberTraining;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberTrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberTrainingService {
    private final MemberTrainingRepository trainingRepository;
    private final MemberRepository memberRepository;

    public MemberTrainingService(MemberTrainingRepository trainingRepository, MemberRepository memberRepository) {
        this.trainingRepository = trainingRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberTraining> getTrainingsByMember(Long memberId) {
        return trainingRepository.findByMemberId(memberId);
    }

    public MemberTraining getTrainingById(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training record not found"));
    }

    public MemberTraining createTraining(MemberTrainingDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberTraining training = new MemberTraining();
        training.setMember(member);
        training.setInstitutionEn(dto.getInstitutionEn());
        training.setInstitutionKh(dto.getInstitutionKh());
        training.setProvinceEn(dto.getProvinceEn());
        training.setProvinceKh(dto.getProvinceKh());
        training.setCountryEn(dto.getCountryEn());
        training.setCountryKh(dto.getCountryKh());
        training.setDegreeEn(dto.getDegreeEn());
        training.setDegreeKh(dto.getDegreeKh());
        training.setLink(dto.getLink());
        training.setStartDate(dto.getStartDate());
        training.setEndDate(dto.getEndDate());
        training.setDescriptionEn(dto.getDescriptionEn());
        training.setDescriptionKh(dto.getDescriptionKh());

        return trainingRepository.save(training);
    }

    public MemberTraining updateTraining(Long id, MemberTrainingDto dto) {
        MemberTraining training = getTrainingById(id);
        training.setInstitutionEn(dto.getInstitutionEn());
        training.setInstitutionKh(dto.getInstitutionKh());
        training.setProvinceEn(dto.getProvinceEn());
        training.setProvinceKh(dto.getProvinceKh());
        training.setCountryEn(dto.getCountryEn());
        training.setCountryKh(dto.getCountryKh());
        training.setDegreeEn(dto.getDegreeEn());
        training.setDegreeKh(dto.getDegreeKh());
        training.setLink(dto.getLink());
        training.setStartDate(dto.getStartDate());
        training.setEndDate(dto.getEndDate());
        training.setDescriptionEn(dto.getDescriptionEn());
        training.setDescriptionKh(dto.getDescriptionKh());
        return trainingRepository.save(training);
    }

    public void deleteTraining(Long id) {
        trainingRepository.deleteById(id);
    }
}
