package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.entity.MemberStatus;
import org.example.tnal_youth_backend.member.repository.MemberStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberStatusService {
    private final MemberStatusRepository statusRepository;

    public MemberStatusService(MemberStatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<MemberStatus> getAllStatuses() { return statusRepository.findAll(); }
    public MemberStatus createStatus(MemberStatus status) { return statusRepository.save(status); }
}
