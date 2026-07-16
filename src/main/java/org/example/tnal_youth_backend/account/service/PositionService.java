package org.example.tnal_youth_backend.account.service;

import org.example.tnal_youth_backend.account.entity.AccountStatus;
import org.example.tnal_youth_backend.account.entity.Position;
import org.example.tnal_youth_backend.account.repository.AccountStatusRepository;
import org.example.tnal_youth_backend.account.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public Position createPosition(String name, String description) {
        Position position = new Position();
        position.setName(name);
        position.setDescription(description);
        return positionRepository.save(position);
    }
}

