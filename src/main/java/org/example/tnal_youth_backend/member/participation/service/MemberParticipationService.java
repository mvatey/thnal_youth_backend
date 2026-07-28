package org.example.tnal_youth_backend.member.participation.service;

import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;

import java.util.List;

public interface MemberParticipationService {

    List<MemberParticipationResponse>
    getParticipationsByMemberId(Long memberId);

    MemberParticipationResponse create(
            Long memberId,
            MemberParticipationRequest request
    );

    MemberParticipationResponse update(
            Long memberId,
            Long participationId,
            MemberParticipationRequest request
    );

    void delete(
            Long memberId,
            Long participationId
    );
}