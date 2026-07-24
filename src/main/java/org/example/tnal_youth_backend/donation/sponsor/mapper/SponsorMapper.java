package org.example.tnal_youth_backend.donation.sponsor.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.sponsor.dto.response.SponsorResponse;
import org.example.tnal_youth_backend.donation.sponsor.entity.Sponsor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SponsorMapper {

    private final JdbcTemplate jdbcTemplate;

    public SponsorResponse toResponse(
            Sponsor sponsor
    ) {
        Map<String, Object> sponsorType =
                findSponsorType(
                        sponsor.getSponsorTypeId()
                );

        return new SponsorResponse(
                sponsor.getId(),

                sponsor.getSponsorTypeId(),
                getString(
                        sponsorType,
                        "code"
                ),
                getString(
                        sponsorType,
                        "label_km"
                ),
                getString(
                        sponsorType,
                        "label_en"
                ),

                sponsor.getName(),
                sponsor.getPhone(),
                sponsor.getEmail(),
                sponsor.getAddress(),
                sponsor.getNote(),
                sponsor.getIsActive(),
                sponsor.getCreatedById(),
                sponsor.getCreatedAt(),
                sponsor.getUpdatedAt()
        );
    }

    private Map<String, Object> findSponsorType(
            Short sponsorTypeId
    ) {
        if (sponsorTypeId == null) {
            return Map.of();
        }

        return jdbcTemplate.query(
                """
                SELECT
                    code,
                    label_km,
                    label_en
                FROM sponsor_types
                WHERE id = ?
                """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return Map.of();
                    }

                    return Map.of(
                            "code",
                            safeValue(
                                    resultSet.getString(
                                            "code"
                                    )
                            ),
                            "label_km",
                            safeValue(
                                    resultSet.getString(
                                            "label_km"
                                    )
                            ),
                            "label_en",
                            safeValue(
                                    resultSet.getString(
                                            "label_en"
                                    )
                            )
                    );
                },
                sponsorTypeId
        );
    }

    private String getString(
            Map<String, Object> values,
            String key
    ) {
        Object value =
                values.get(key);

        if (value == null) {
            return null;
        }

        String text =
                value.toString();

        return text.isBlank()
                ? null
                : text;
    }

    private String safeValue(
            String value
    ) {
        return value == null
                ? ""
                : value;
    }
}