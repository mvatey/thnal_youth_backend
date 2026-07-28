package org.example.tnal_youth_backend.member.participation.service;

import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationPageResponse;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;

public interface MemberParticipationService {

    MemberParticipationPageResponse
    getParticipationsByMemberId(
            Long memberId,
            int page,
            int size,
            String search,
            Short typeId
    );

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