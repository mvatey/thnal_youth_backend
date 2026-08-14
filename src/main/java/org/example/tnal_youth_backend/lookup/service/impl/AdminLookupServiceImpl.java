package org.example.tnal_youth_backend.lookup.service.impl;

import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.example.tnal_youth_backend.activity.repository.ActivitySectorRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityTypeRepository;

import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;

import org.example.tnal_youth_backend.donation.paymentmethod.model.entity.PaymentMethod;
import org.example.tnal_youth_backend.donation.paymentmethod.repository.PaymentMethodRepository;

import org.example.tnal_youth_backend.lookup.dto.variable.AdminLookupResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.CreateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.LookupCategoryResponse;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupRequest;
import org.example.tnal_youth_backend.lookup.dto.variable.UpdateLookupStatusRequest;

import org.example.tnal_youth_backend.lookup.enums.LookupCategory;
import org.example.tnal_youth_backend.lookup.service.AdminLookupService;

import org.example.tnal_youth_backend.member.education.entity.EducationLevel;
import org.example.tnal_youth_backend.member.education.repository.EducationLevelRepository;

import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;

import org.example.tnal_youth_backend.member.language.entity.Language;
import org.example.tnal_youth_backend.member.language.repository.LanguageRepository;

import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;

import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.example.tnal_youth_backend.member.nationality.repository.NationalityRepository;

import org.example.tnal_youth_backend.member.proficiency.entity.ProficiencyLevel;
import org.example.tnal_youth_backend.member.proficiency.repository.ProficiencyLevelRepository;

import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;

import org.example.tnal_youth_backend.member.skill.entity.Skill;
import org.example.tnal_youth_backend.member.skill.repository.SkillRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminLookupServiceImpl
        implements AdminLookupService {

    private final ActivityTypeRepository
            activityTypeRepository;

    private final ActivitySectorRepository
            activitySectorRepository;

    private final MemberLevelRepository
            memberLevelRepository;

    private final NationalityRepository
            nationalityRepository;

    private final ReligionRepository
            religionRepository;

    private final EducationLevelRepository
            educationLevelRepository;

    private final LanguageRepository
            languageRepository;

    private final SkillRepository
            skillRepository;

    private final ProficiencyLevelRepository
            proficiencyLevelRepository;

    private final DocumentTypeRepository
            documentTypeRepository;

    private final EthnicityRepository
            ethnicityRepository;

    private final PaymentMethodRepository
            paymentMethodRepository;

    private static final java.util.Set<String> PAYMENT_METHOD_CATEGORIES =
            java.util.Set.of("CASH", "BANK", "OTHER");


    /*
     * ==========================================================
     * CATEGORIES
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<LookupCategoryResponse>
    getCategories() {

        return List.of(
                categoryResponse(
                        LookupCategory.ACTIVITY_TYPE,
                        activityTypeRepository.count()
                ),

                categoryResponse(
                        LookupCategory.ACTIVITY_SECTOR,
                        activitySectorRepository.count()
                ),

                categoryResponse(
                        LookupCategory.MEMBER_LEVEL,
                        memberLevelRepository.count()
                ),

                categoryResponse(
                        LookupCategory.NATIONALITY,
                        nationalityRepository.count()
                ),

                categoryResponse(
                        LookupCategory.RELIGION,
                        religionRepository.count()
                ),

                categoryResponse(
                        LookupCategory.EDUCATION_LEVEL,
                        educationLevelRepository.count()
                ),

                categoryResponse(
                        LookupCategory.LANGUAGE,
                        languageRepository.count()
                ),

                categoryResponse(
                        LookupCategory.SKILL,
                        skillRepository.count()
                ),

                categoryResponse(
                        LookupCategory.PROFICIENCY_LEVEL,
                        proficiencyLevelRepository.count()
                ),

                categoryResponse(
                        LookupCategory.DOCUMENT_TYPE,
                        documentTypeRepository.count()
                ),

                categoryResponse(
                        LookupCategory.ETHNICITY,
                        ethnicityRepository.count()
                ),

                categoryResponse(
                        LookupCategory.PAYMENT_METHOD,
                        paymentMethodRepository.count()
                )
        );
    }


    /*
     * ==========================================================
     * LIST ITEMS
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<AdminLookupResponse>
    getItems(
            String category,
            String search,
            String status
    ) {

        LookupCategory resolvedCategory =
                resolveCategory(
                        category
                );

        String normalizedSearch =
                normalizeSearch(
                        search
                );

        String normalizedStatus =
                normalizeStatus(
                        status
                );

        List<AdminLookupResponse> rows =
                switch (
                        resolvedCategory
                        ) {

                    case ACTIVITY_TYPE ->
                            activityTypeRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case ACTIVITY_SECTOR ->
                            activitySectorRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case MEMBER_LEVEL ->
                            memberLevelRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case NATIONALITY ->
                            nationalityRepository
                                    .findAllByOrderByDisplayOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case RELIGION ->
                            religionRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case EDUCATION_LEVEL ->
                            educationLevelRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case LANGUAGE ->
                            languageRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case SKILL ->
                            skillRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case PROFICIENCY_LEVEL ->
                            proficiencyLevelRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case DOCUMENT_TYPE ->
                            documentTypeRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case ETHNICITY ->
                            ethnicityRepository
                                    .findAllByOrderByLabelKmAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();

                    case PAYMENT_METHOD ->
                            paymentMethodRepository
                                    .findAllByOrderBySortOrderAscIdAsc()
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();
                };

        return rows
                .stream()
                .filter(row ->
                        matchesSearch(
                                row,
                                normalizedSearch
                        )
                )
                .filter(row ->
                        matchesStatus(
                                row,
                                normalizedStatus
                        )
                )
                .toList();
    }


    /*
     * ==========================================================
     * CREATE
     * ==========================================================
     */

    @Override
    @Transactional
    public AdminLookupResponse
    createItem(
            String category,
            CreateLookupRequest request
    ) {

        LookupCategory resolvedCategory =
                resolveCategory(
                        category
                );

        String labelKm =
                normalizeRequired(
                        request.labelKm(),
                        "Khmer label"
                );

        String labelEn =
                trimToNull(
                        request.labelEn()
                );

        String description =
                trimToNull(
                        request.description()
                );

        boolean active =
                request.active() == null
                        || request.active();

        String code =
                generateUniqueCode(
                        resolvedCategory,
                        labelEn != null
                                ? labelEn
                                : labelKm
                );

        return switch (
                resolvedCategory
                ) {

            case ACTIVITY_TYPE -> {

                ActivityType entity =
                        ActivityType.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .active(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        activityTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ACTIVITY_SECTOR -> {

                ActivitySector entity =
                        ActivitySector.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .active(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        activitySectorRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case MEMBER_LEVEL -> {

                MemberLevel entity =
                        MemberLevel.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        memberLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case NATIONALITY -> {

                Nationality entity =
                        Nationality.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(
                                        Objects.requireNonNullElse(
                                                labelEn,
                                                labelKm
                                        )
                                )
                                .displayOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .isActive(active)
                                .build();

                yield toResponse(
                        nationalityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case RELIGION -> {

                Religion entity =
                        Religion.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        religionRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case EDUCATION_LEVEL -> {

                EducationLevel entity =
                        EducationLevel.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(
                                        Objects.requireNonNullElse(
                                                labelEn,
                                                labelKm
                                        )
                                )
                                .description(description)
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        educationLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case LANGUAGE -> {

                Language entity =
                        Language.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(
                                        Objects.requireNonNullElse(
                                                labelEn,
                                                labelKm
                                        )
                                )
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        languageRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case SKILL -> {

                Skill entity =
                        Skill.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(
                                        Objects.requireNonNullElse(
                                                labelEn,
                                                labelKm
                                        )
                                )
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        skillRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PROFICIENCY_LEVEL -> {

                ProficiencyLevel entity =
                        ProficiencyLevel.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        proficiencyLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case DOCUMENT_TYPE -> {

                DocumentType entity =
                        DocumentType.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        documentTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ETHNICITY -> {

                Ethnicity entity =
                        Ethnicity.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .isActive(active)
                                .build();

                yield toResponse(
                        ethnicityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PAYMENT_METHOD -> {

                PaymentMethod entity =
                        PaymentMethod.builder()
                                .code(code)
                                .labelKm(labelKm)
                                .labelEn(labelEn)
                                .description(description)
                                .category(
                                        normalizePaymentCategory(
                                                request.category()
                                        )
                                )
                                .isActive(active)
                                .sortOrder(
                                        nextSortOrder(
                                                resolvedCategory
                                        )
                                )
                                .build();

                yield toResponse(
                        paymentMethodRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }
        };
    }


    /*
     * ==========================================================
     * UPDATE
     * ==========================================================
     */

    @Override
    @Transactional
    public AdminLookupResponse
    updateItem(
            String category,
            Short id,
            UpdateLookupRequest request
    ) {

        LookupCategory resolvedCategory =
                resolveCategory(
                        category
                );

        String labelKm =
                normalizeRequired(
                        request.labelKm(),
                        "Khmer label"
                );

        String labelEn =
                trimToNull(
                        request.labelEn()
                );

        String description =
                trimToNull(
                        request.description()
                );

        return switch (
                resolvedCategory
                ) {

            case ACTIVITY_TYPE -> {
                ActivityType entity =
                        activityTypeRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        activityTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ACTIVITY_SECTOR -> {
                ActivitySector entity =
                        activitySectorRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        activitySectorRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case MEMBER_LEVEL -> {
                MemberLevel entity =
                        memberLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        memberLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case NATIONALITY -> {
                Nationality entity =
                        nationalityRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        Objects.requireNonNullElse(
                                labelEn,
                                labelKm
                        )
                );

                yield toResponse(
                        nationalityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case RELIGION -> {
                Religion entity =
                        religionRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        religionRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case EDUCATION_LEVEL -> {
                EducationLevel entity =
                        educationLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        Objects.requireNonNullElse(
                                labelEn,
                                labelKm
                        )
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        educationLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case LANGUAGE -> {
                Language entity =
                        languageRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        Objects.requireNonNullElse(
                                labelEn,
                                labelKm
                        )
                );

                yield toResponse(
                        languageRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case SKILL -> {
                Skill entity =
                        skillRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        Objects.requireNonNullElse(
                                labelEn,
                                labelKm
                        )
                );

                yield toResponse(
                        skillRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PROFICIENCY_LEVEL -> {
                ProficiencyLevel entity =
                        proficiencyLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        proficiencyLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case DOCUMENT_TYPE -> {
                DocumentType entity =
                        documentTypeRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                yield toResponse(
                        documentTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ETHNICITY -> {
                Ethnicity entity =
                        ethnicityRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                yield toResponse(
                        ethnicityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PAYMENT_METHOD -> {
                PaymentMethod entity =
                        paymentMethodRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setLabelKm(
                        labelKm
                );

                entity.setLabelEn(
                        labelEn
                );

                entity.setDescription(
                        description
                );

                if (
                        request.category() != null
                ) {

                    entity.setCategory(
                            normalizePaymentCategory(
                                    request.category()
                            )
                    );
                }

                yield toResponse(
                        paymentMethodRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }
        };
    }


    /*
     * ==========================================================
     * STATUS
     * ==========================================================
     */

    @Override
    @Transactional
    public AdminLookupResponse
    updateStatus(
            String category,
            Short id,
            UpdateLookupStatusRequest request
    ) {

        LookupCategory resolvedCategory =
                resolveCategory(
                        category
                );

        boolean active =
                Boolean.TRUE.equals(
                        request.active()
                );

        return switch (
                resolvedCategory
                ) {

            case ACTIVITY_TYPE -> {
                ActivityType entity =
                        activityTypeRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setActive(
                        active
                );

                yield toResponse(
                        activityTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ACTIVITY_SECTOR -> {
                ActivitySector entity =
                        activitySectorRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setActive(
                        active
                );

                yield toResponse(
                        activitySectorRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case MEMBER_LEVEL -> {
                MemberLevel entity =
                        memberLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        memberLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case NATIONALITY -> {
                Nationality entity =
                        nationalityRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        nationalityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case RELIGION -> {
                Religion entity =
                        religionRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        religionRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case EDUCATION_LEVEL -> {
                EducationLevel entity =
                        educationLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        educationLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case LANGUAGE -> {
                Language entity =
                        languageRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        languageRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case SKILL -> {
                Skill entity =
                        skillRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        skillRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PROFICIENCY_LEVEL -> {
                ProficiencyLevel entity =
                        proficiencyLevelRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        proficiencyLevelRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case DOCUMENT_TYPE -> {
                DocumentType entity =
                        documentTypeRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        documentTypeRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case ETHNICITY -> {
                Ethnicity entity =
                        ethnicityRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        ethnicityRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }

            case PAYMENT_METHOD -> {
                PaymentMethod entity =
                        paymentMethodRepository
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                notFound(
                                                        resolvedCategory,
                                                        id
                                                )
                                );

                entity.setIsActive(
                        active
                );

                yield toResponse(
                        paymentMethodRepository
                                .saveAndFlush(
                                        entity
                                )
                );
            }
        };
    }


    /*
     * ==========================================================
     * CATEGORY RESPONSE
     * ==========================================================
     */

    private LookupCategoryResponse
    categoryResponse(
            LookupCategory category,
            long count
    ) {

        return new LookupCategoryResponse(
                category.name(),
                category.getPath(),
                category.getLabelKm(),
                category.getLabelEn(),
                count
        );
    }


    /*
     * ==========================================================
     * RESPONSE MAPPERS
     * ==========================================================
     */

    private AdminLookupResponse
    toResponse(
            ActivityType entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            ActivitySector entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            MemberLevel entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            Nationality entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                null,
                entity.getIsActive(),
                entity.getDisplayOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            Religion entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            EducationLevel entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            Language entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                null,
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            Skill entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                null,
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            ProficiencyLevel entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            DocumentType entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            Ethnicity entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                null,
                entity.getIsActive(),
                null,
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AdminLookupResponse
    toResponse(
            PaymentMethod entity
    ) {

        return new AdminLookupResponse(
                entity.getId(),
                entity.getCode(),
                entity.getLabelKm(),
                entity.getLabelEn(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getSortOrder(),
                entity.getCategory(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }


    /*
     * ==========================================================
     * SEARCH
     * ==========================================================
     */

    private boolean matchesSearch(
            AdminLookupResponse row,
            String search
    ) {

        if (search == null) {
            return true;
        }

        return containsIgnoreCase(
                row.code(),
                search
        )
                || containsIgnoreCase(
                row.labelKm(),
                search
        )
                || containsIgnoreCase(
                row.labelEn(),
                search
        );
    }


    /*
     * ==========================================================
     * STATUS FILTER
     * ==========================================================
     */

    private boolean matchesStatus(
            AdminLookupResponse row,
            String status
    ) {

        return switch (status) {

            case "ACTIVE" ->
                    Boolean.TRUE.equals(
                            row.active()
                    );

            case "INACTIVE" ->
                    !Boolean.TRUE.equals(
                            row.active()
                    );

            default ->
                    true;
        };
    }


    /*
     * ==========================================================
     * AUTO CODE GENERATION
     * ==========================================================
     */

    private String generateUniqueCode(
            LookupCategory category,
            String label
    ) {

        String baseCode =
                generateCode(
                        label
                );

        String candidate =
                baseCode;

        int suffix =
                2;

        while (
                codeExists(
                        category,
                        candidate
                )
        ) {

            candidate =
                    baseCode
                            + "_"
                            + suffix;

            suffix++;
        }

        return candidate;
    }


    private String generateCode(
            String value
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {

            return "VARIABLE";
        }

        normalized =
                normalized
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );

        if (
                normalized.isBlank()
        ) {

            normalized =
                    "VARIABLE";
        }

        return normalized;
    }


    private boolean codeExists(
            LookupCategory category,
            String code
    ) {

        return switch (
                category
                ) {

            case ACTIVITY_TYPE ->
                    activityTypeRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case ACTIVITY_SECTOR ->
                    activitySectorRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case MEMBER_LEVEL ->
                    memberLevelRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case NATIONALITY ->
                    nationalityRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case RELIGION ->
                    religionRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case EDUCATION_LEVEL ->
                    educationLevelRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case LANGUAGE ->
                    languageRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case SKILL ->
                    skillRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case PROFICIENCY_LEVEL ->
                    proficiencyLevelRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case DOCUMENT_TYPE ->
                    documentTypeRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case ETHNICITY ->
                    ethnicityRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );

            case PAYMENT_METHOD ->
                    paymentMethodRepository
                            .existsByCodeIgnoreCase(
                                    code
                            );
        };
    }


    /*
     * ==========================================================
     * SORT ORDER
     * ==========================================================
     */

    private int nextSortOrder(
            LookupCategory category
    ) {

        return getItems(
                category.getPath(),
                null,
                "ALL"
        )
                .stream()
                .map(
                        AdminLookupResponse
                                ::sortOrder
                )
                .filter(
                        Objects::nonNull
                )
                .max(
                        Integer::compareTo
                )
                .orElse(0)
                + 1;
    }


    /*
     * ==========================================================
     * CATEGORY RESOLUTION
     * ==========================================================
     */

    private LookupCategory
    resolveCategory(
            String category
    ) {

        try {

            return LookupCategory
                    .fromPath(
                            category
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }


    /*
     * ==========================================================
     * VALIDATION / NORMALIZATION
     * ==========================================================
     */

    private String normalizeRequired(
            String value,
            String fieldName
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName
                            + " is required"
            );
        }

        return normalized;
    }


    private String normalizeSearch(
            String search
    ) {

        String normalized =
                trimToNull(
                        search
                );

        if (normalized == null) {
            return null;
        }

        return normalized
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String normalizeStatus(
            String status
    ) {

        String normalized =
                trimToNull(
                        status
                );

        if (normalized == null) {
            return "ALL";
        }

        normalized =
                normalized
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !normalized.equals("ALL")
                        && !normalized.equals("ACTIVE")
                        && !normalized.equals("INACTIVE")
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be ALL, ACTIVE, or INACTIVE"
            );
        }

        return normalized;
    }


    /*
     * ==========================================================
     * PAYMENT METHOD CATEGORY
     * ==========================================================
     */

    private String normalizePaymentCategory(
            String category
    ) {

        String normalized =
                trimToNull(
                        category
                );

        if (normalized == null) {

            return "OTHER";
        }

        normalized =
                normalized
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !PAYMENT_METHOD_CATEGORIES.contains(
                        normalized
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category must be one of: "
                            + String.join(
                                    ", ",
                                    PAYMENT_METHOD_CATEGORIES
                            )
            );
        }

        return normalized;
    }


    private String trimToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }


    private boolean containsIgnoreCase(
            String value,
            String search
    ) {

        if (
                value == null
                        || search == null
        ) {
            return false;
        }

        return value
                .toLowerCase(
                        Locale.ROOT
                )
                .contains(
                        search
                );
    }


    private ResponseStatusException
    notFound(
            LookupCategory category,
            Short id
    ) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                category.getLabelEn()
                        + " not found with ID: "
                        + id
        );
    }
}