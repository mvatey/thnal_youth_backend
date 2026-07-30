package org.example.tnal_youth_backend.document.document.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentFilterRequest;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DocumentSpecification {

    private static final String BRANCH_DOCUMENT_CODE =
            "BRANCH_DOCUMENT";

    private DocumentSpecification() {
    }

    // =========================================================
    // INSTITUTIONAL DOCUMENTS
    // Existing behavior unchanged
    // =========================================================

    public static Specification<Document> institutionalDocuments(
            DocumentFilterRequest filter
    ) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates =
                    new ArrayList<>();

            Join<Document, DocumentType> typeJoin =
                    root.join(
                            "documentType",
                            JoinType.INNER
                    );

            /*
             * Existing Institutional type restriction.
             */
            predicates.add(
                    criteriaBuilder.equal(
                            typeJoin.get("code"),
                            BRANCH_DOCUMENT_CODE
                    )
            );

            /*
             * Existing Institutional ownership restriction.
             */
            predicates.add(
                    criteriaBuilder.isNotNull(
                            root.get("branchId")
                    )
            );

            predicates.add(
                    criteriaBuilder.isNull(
                            root.get("memberId")
                    )
            );

            predicates.add(
                    criteriaBuilder.isNull(
                            root.get("activityId")
                    )
            );

            if (filter != null) {
                addSearch(
                        predicates,
                        root,
                        typeJoin,
                        criteriaBuilder,
                        filter
                );

                addBranch(
                        predicates,
                        root,
                        criteriaBuilder,
                        filter
                );

                addDate(
                        predicates,
                        root,
                        criteriaBuilder,
                        filter
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }

    // =========================================================
    // MEMBER PERSONAL DOCUMENTS
    // =========================================================

    public static Specification<Document> memberDocuments(
            DocumentFilterRequest filter
    ) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates =
                    new ArrayList<>();

            Join<Document, DocumentType> typeJoin =
                    root.join(
                            "documentType",
                            JoinType.INNER
                    );

            /*
             * This join is used only for Member Document search and
             * member-branch filtering.
             */
            Join<Document, Member> memberJoin =
                    root.join(
                            "member",
                            JoinType.INNER
                    );

            /*
             * Valid member-owned personal document:
             *
             * member_id   is not null
             * branch_id   is null
             * activity_id is null
             */
            predicates.add(
                    criteriaBuilder.isNotNull(
                            root.get("memberId")
                    )
            );

            predicates.add(
                    criteriaBuilder.isNull(
                            root.get("branchId")
                    )
            );

            predicates.add(
                    criteriaBuilder.isNull(
                            root.get("activityId")
                    )
            );

            if (filter != null) {
                /*
                 * Member-only search includes the selected member's name.
                 */
                addMemberSearch(
                        predicates,
                        root,
                        typeJoin,
                        memberJoin,
                        criteriaBuilder,
                        filter
                );

                addMemberType(
                        predicates,
                        root,
                        criteriaBuilder,
                        filter
                );

                addMemberBranch(
                        predicates,
                        memberJoin,
                        criteriaBuilder,
                        filter
                );

                addDate(
                        predicates,
                        root,
                        criteriaBuilder,
                        filter
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }

    // =========================================================
    // MEMBER FILTERS
    // =========================================================

    private static void addMemberType(
            List<Predicate> predicates,
            Root<Document> root,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        if (!filter.hasTypeId()) {
            return;
        }

        predicates.add(
                criteriaBuilder.equal(
                        root.get("typeId"),
                        filter.typeId()
                )
        );
    }

    /**
     * Filters using members.branch_id instead of documents.branch_id.
     */
    private static void addMemberBranch(
            List<Predicate> predicates,
            Join<Document, Member> memberJoin,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        if (!filter.hasBranchId()) {
            return;
        }

        predicates.add(
                criteriaBuilder.equal(
                        memberJoin.get("branchId"),
                        filter.branchId()
                )
        );
    }

    /**
     * Member Document search.
     *
     * Searches:
     *
     * - Document title
     * - Document description
     * - Type code
     * - Khmer type label
     * - English type label
     * - Member Khmer name
     * - Member English name
     */
    private static void addMemberSearch(
            List<Predicate> predicates,
            Root<Document> root,
            Join<Document, DocumentType> typeJoin,
            Join<Document, Member> memberJoin,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        String search =
                filter.normalizedSearch();

        if (search == null) {
            return;
        }

        String escapedSearch =
                escapeLikePattern(
                        search.toLowerCase()
                );

        String pattern =
                "%" + escapedSearch + "%";

        Predicate titlePredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("title")
                        ),
                        pattern,
                        '\\'
                );

        Predicate descriptionPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        root.get("description"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeCodePredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                typeJoin.get("code")
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeNameKmPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        typeJoin.get("labelKm"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeNameEnPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        typeJoin.get("labelEn"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate memberNameKmPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        memberJoin.get("fullNameKm"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate memberNameEnPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        memberJoin.get("fullNameEn"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        predicates.add(
                criteriaBuilder.or(
                        titlePredicate,
                        descriptionPredicate,
                        typeCodePredicate,
                        typeNameKmPredicate,
                        typeNameEnPredicate,
                        memberNameKmPredicate,
                        memberNameEnPredicate
                )
        );
    }

    // =========================================================
    // SHARED FILTERS
    // =========================================================

    /**
     * Existing Institutional search.
     *
     * This method remains separate from Member search so Institutional
     * behavior is not changed.
     */
    private static void addSearch(
            List<Predicate> predicates,
            Root<Document> root,
            Join<Document, DocumentType> typeJoin,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        String search =
                filter.normalizedSearch();

        if (search == null) {
            return;
        }

        String escapedSearch =
                escapeLikePattern(
                        search.toLowerCase()
                );

        String pattern =
                "%" + escapedSearch + "%";

        Predicate titlePredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("title")
                        ),
                        pattern,
                        '\\'
                );

        Predicate descriptionPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        root.get("description"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeCodePredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                typeJoin.get("code")
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeNameKmPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        typeJoin.get("labelKm"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        Predicate typeNameEnPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        typeJoin.get("labelEn"),
                                        ""
                                )
                        ),
                        pattern,
                        '\\'
                );

        predicates.add(
                criteriaBuilder.or(
                        titlePredicate,
                        descriptionPredicate,
                        typeCodePredicate,
                        typeNameKmPredicate,
                        typeNameEnPredicate
                )
        );
    }

    /**
     * Existing Institutional branch filter.
     */
    private static void addBranch(
            List<Predicate> predicates,
            Root<Document> root,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        if (!filter.hasBranchId()) {
            return;
        }

        predicates.add(
                criteriaBuilder.equal(
                        root.get("branchId"),
                        filter.branchId()
                )
        );
    }

    /**
     * Filters using documentDate with createdAt as fallback.
     */
    private static void addDate(
            List<Predicate> predicates,
            Root<Document> root,
            CriteriaBuilder criteriaBuilder,
            DocumentFilterRequest filter
    ) {
        LocalDate from =
                filter.effectiveDateFrom();

        LocalDate to =
                filter.effectiveDateTo();

        if (from == null && to == null) {
            return;
        }

        Expression<LocalDate> createdDate =
                criteriaBuilder.function(
                        "DATE",
                        LocalDate.class,
                        root.get("createdAt")
                );

        Expression<LocalDate> effectiveDate =
                criteriaBuilder.coalesce(
                        root.get("documentDate"),
                        createdDate
                );

        if (from != null) {
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            effectiveDate,
                            from
                    )
            );
        }

        if (to != null) {
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            effectiveDate,
                            to
                    )
            );
        }
    }

    /**
     * Escapes SQL LIKE wildcard characters.
     */
    private static String escapeLikePattern(
            String value
    ) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}