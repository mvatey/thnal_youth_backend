package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.entity.MemberPosition;
import org.example.tnal_youth_backend.member.repository.MemberPositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberPositionService {
    private final MemberPositionRepository positionRepository;

    public MemberPositionService(MemberPositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<MemberPosition> getAllPositions() { return positionRepository.findAll(); }
    public MemberPosition createPosition(MemberPosition position) { return positionRepository.save(position); }
}
