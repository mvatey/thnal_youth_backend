package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberWorkHistoryDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberWorkHistory;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberWorkHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberWorkHistoryService {
    private final MemberWorkHistoryRepository workHistoryRepository;
    private final MemberRepository memberRepository;

    public MemberWorkHistoryService(MemberWorkHistoryRepository workHistoryRepository, MemberRepository memberRepository) {
        this.workHistoryRepository = workHistoryRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberWorkHistory> getWorkHistoryByMember(Long memberId) {
        return workHistoryRepository.findByMemberId(memberId);
    }

    public MemberWorkHistory getWorkHistoryById(Long id) {
        return workHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work history not found"));
    }

    public MemberWorkHistory createWorkHistory(MemberWorkHistoryDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberWorkHistory history = new MemberWorkHistory();
        history.setMember(member);
        history.setOrganization(dto.getOrganization());
        history.setPositionTitle(dto.getPositionTitle());
        history.setStartDate(dto.getStartDate());
        history.setEndDate(dto.getEndDate());
        history.setDescription(dto.getDescription());

        return workHistoryRepository.save(history);
    }

    public MemberWorkHistory updateWorkHistory(Long id, MemberWorkHistoryDto dto) {
        MemberWorkHistory history = getWorkHistoryById(id);
        history.setOrganization(dto.getOrganization());
        history.setPositionTitle(dto.getPositionTitle());
        history.setStartDate(dto.getStartDate());
        history.setEndDate(dto.getEndDate());
        history.setDescription(dto.getDescription());
        return workHistoryRepository.save(history);
    }

    public void deleteWorkHistory(Long id) {
        workHistoryRepository.deleteById(id);
    }
}
