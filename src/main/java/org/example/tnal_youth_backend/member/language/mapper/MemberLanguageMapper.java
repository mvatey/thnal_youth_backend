package org.example.tnal_youth_backend.member.language.mapper;

import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.entity.MemberLanguage;
import org.springframework.stereotype.Component;

@Component
public class MemberLanguageMapper {

    public MemberLanguageResponse toResponse(
            MemberLanguage language
    ) {
        if (language == null) {
            return null;
        }

        return new MemberLanguageResponse(
                language.getId(),
                language.getMember().getId(),
                language.getLanguageName(),
                language.getListeningLevelId(),
                language.getSpeakingLevelId(),
                language.getReadingLevelId(),
                language.getWritingLevelId(),
                language.getCreatedAt(),
                language.getUpdatedAt()
        );
    }
}