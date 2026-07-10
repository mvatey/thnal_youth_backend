package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberStatusDto;
import org.example.tnal_youth_backend.member.entity.MemberStatus;
import org.example.tnal_youth_backend.member.repository.MemberStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberStatusService {

    private final MemberStatusRepository repository;

    public MemberStatusService(MemberStatusRepository repository) {
        this.repository = repository;
    }

    public List<MemberStatus> getAllStatuses() {
        return repository.findAll();
    }

    public MemberStatus getStatusById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found with id " + id));
    }

    public MemberStatus createStatus(MemberStatusDto dto) {
        MemberStatus status = new MemberStatus();
        status.setCode(dto.getCode());
        status.setLabelKh(dto.getLabelKh());
        status.setLabelEn(dto.getLabelEn());
        status.setIsActive(dto.getIsActive());
        status.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        return repository.save(status);
    }

    public MemberStatus updateStatus(Long id, MemberStatusDto dto) {
        MemberStatus status = getStatusById(id);
        status.setCode(dto.getCode());
        status.setLabelKh(dto.getLabelKh());
        status.setLabelEn(dto.getLabelEn());
        status.setIsActive(dto.getIsActive());
        status.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : status.getSortOrder());
        return repository.save(status);
    }

    public void deleteStatus(Long id) {
        repository.deleteById(id);
    }
}
