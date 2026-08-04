package org.example.tnal_youth_backend.member.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("ប្រុស", "Male"),
    FEMALE("ស្រី", "Female"),
    MONK("ព្រះសង្ឃ", "Monk");

    private final String labelKm;
    private final String labelEn;
}
