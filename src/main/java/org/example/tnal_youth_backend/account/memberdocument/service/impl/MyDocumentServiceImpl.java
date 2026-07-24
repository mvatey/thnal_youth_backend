package org.example.tnal_youth_backend.account.memberdocument.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentResponse;
import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentSummaryResponse;
import org.example.tnal_youth_backend.account.memberdocument.service.MyDocumentService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDocumentServiceImpl
        implements MyDocumentService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    /*
     * ==========================================================
     * BASE DOCUMENT QUERY
     * ==========================================================
     */

    private static final String BASE_DOCUMENT_SQL = """
            SELECT
                d.id                            AS document_id,
                d.title                         AS title,
                d.description                   AS description,

                dt.id                           AS document_type_id,
                dt.code                         AS document_type_code,
                dt.label_km                     AS document_type_label_km,
                dt.label_en                     AS document_type_label_en,

                f.id                            AS file_id,
                f.file_path                     AS file_path,
                f.original_name                 AS original_name,
                f.mime_type                     AS mime_type,
                f.size_bytes                    AS size_bytes,
                f.created_at                    AS file_created_at,

                m.id                            AS member_id,
                m.member_no                     AS member_no,
                m.full_name_km                  AS member_full_name_km,
                m.full_name_en                  AS member_full_name_en,

                uploader.id                     AS uploaded_by_user_id,
                uploader.full_name_km           AS uploaded_by_full_name_km,
                uploader.full_name_en           AS uploaded_by_full_name_en,

                d.created_at                    AS created_at,
                d.updated_at                    AS updated_at

            FROM documents d

            INNER JOIN document_types dt
                    ON dt.id = d.document_type_id

            INNER JOIN files f
                    ON f.id = d.file_id

            INNER JOIN members m
                    ON m.id = d.member_id

            INNER JOIN users uploader
                    ON uploader.id = d.uploaded_by

            WHERE d.member_id = :memberId
            """;

    /*
     * ==========================================================
     * GET ALL MY DOCUMENTS
     * ==========================================================
     */

    @Override
    public List<MyDocumentResponse> getMyDocuments() {

        Long memberId = getCurrentMemberId();

        String sql = BASE_DOCUMENT_SQL + """
                
                ORDER BY
                    d.created_at DESC,
                    d.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("memberId", memberId),
                this::mapDocument
        );
    }

    /*
     * ==========================================================
     * GET ONE MY DOCUMENT
     * ==========================================================
     */

    @Override
    public MyDocumentResponse getMyDocumentById(
            Long documentId
    ) {
        validateDocumentId(documentId);

        Long memberId = getCurrentMemberId();

        String sql = BASE_DOCUMENT_SQL + """
                
                AND d.id = :documentId
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("documentId", documentId);

        List<MyDocumentResponse> results =
                jdbcTemplate.query(
                        sql,
                        parameters,
                        this::mapDocument
                );

        if (results.isEmpty()) {
            /*
             * Return the same result when:
             *
             * - the document does not exist;
             * - the document belongs to another member;
             * - the document belongs to a branch or activity.
             *
             * This avoids exposing private document ownership.
             */
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document was not found for the logged-in member"
            );
        }

        return results.get(0);
    }

    /*
     * ==========================================================
     * GET MY DOCUMENT SUMMARY
     * ==========================================================
     */

    @Override
    public MyDocumentSummaryResponse getMyDocumentSummary() {

        Long memberId = getCurrentMemberId();

        String sql = """
                SELECT
                    COUNT(*) AS total_documents,

                    COUNT(*) FILTER (
                        WHERE LOWER(f.mime_type)
                              = 'application/pdf'
                    ) AS pdf_documents,

                    COUNT(*) FILTER (
                        WHERE LOWER(f.mime_type)
                              LIKE 'image/%'
                    ) AS image_documents,

                    COUNT(*) FILTER (
                        WHERE LOWER(f.mime_type)
                              <> 'application/pdf'
                          AND LOWER(f.mime_type)
                              NOT LIKE 'image/%'
                    ) AS other_documents,

                    COALESCE(
                        SUM(f.size_bytes),
                        0
                    ) AS total_size_bytes,

                    MAX(d.created_at)
                        AS latest_document_created_at

                FROM documents d

                INNER JOIN files f
                        ON f.id = d.file_id

                WHERE d.member_id = :memberId
                """;

        MyDocumentSummaryResponse summary =
                jdbcTemplate.queryForObject(
                        sql,
                        Map.of("memberId", memberId),
                        (resultSet, rowNumber) ->
                                new MyDocumentSummaryResponse(
                                        resultSet.getLong(
                                                "total_documents"
                                        ),

                                        resultSet.getLong(
                                                "pdf_documents"
                                        ),

                                        resultSet.getLong(
                                                "image_documents"
                                        ),

                                        resultSet.getLong(
                                                "other_documents"
                                        ),

                                        resultSet.getLong(
                                                "total_size_bytes"
                                        ),

                                        resultSet.getObject(
                                                "latest_document_created_at",
                                                OffsetDateTime.class
                                        )
                                )
                );

        if (summary == null) {
            return new MyDocumentSummaryResponse(
                    0,
                    0,
                    0,
                    0,
                    0,
                    null
            );
        }

        return summary;
    }

    /*
     * ==========================================================
     * ROW MAPPER
     * ==========================================================
     */

    private MyDocumentResponse mapDocument(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {

        return new MyDocumentResponse(
                getNullableLong(
                        resultSet,
                        "document_id"
                ),

                resultSet.getString(
                        "title"
                ),

                resultSet.getString(
                        "description"
                ),

                getNullableLong(
                        resultSet,
                        "document_type_id"
                ),

                resultSet.getString(
                        "document_type_code"
                ),

                resultSet.getString(
                        "document_type_label_km"
                ),

                resultSet.getString(
                        "document_type_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "file_id"
                ),

                resultSet.getString(
                        "file_path"
                ),

                resultSet.getString(
                        "original_name"
                ),

                resultSet.getString(
                        "mime_type"
                ),

                getNullableLong(
                        resultSet,
                        "size_bytes"
                ),

                resultSet.getObject(
                        "file_created_at",
                        OffsetDateTime.class
                ),

                getNullableLong(
                        resultSet,
                        "member_id"
                ),

                resultSet.getString(
                        "member_no"
                ),

                resultSet.getString(
                        "member_full_name_km"
                ),

                resultSet.getString(
                        "member_full_name_en"
                ),

                getNullableLong(
                        resultSet,
                        "uploaded_by_user_id"
                ),

                resultSet.getString(
                        "uploaded_by_full_name_km"
                ),

                resultSet.getString(
                        "uploaded_by_full_name_en"
                ),

                resultSet.getObject(
                        "created_at",
                        OffsetDateTime.class
                ),

                resultSet.getObject(
                        "updated_at",
                        OffsetDateTime.class
                )
        );
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED MEMBER
     * ==========================================================
     */

    private Long getCurrentMemberId() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        /*
         * Reload from the database so My Account always uses
         * the current users.member_id link.
         */
        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found "
                                                + "in the database"
                                )
                        );

        Long memberId = currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to "
                            + "a member profile"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to "
                            + "this account was not found"
            );
        }

        return memberId;
    }

    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

    private void validateDocumentId(
            Long documentId
    ) {
        if (documentId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document ID is required"
            );
        }

        if (documentId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document ID must be greater than zero"
            );
        }
    }

    /*
     * ==========================================================
     * JDBC NULL HELPER
     * ==========================================================
     */

    private Long getNullableLong(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        long value =
                resultSet.getLong(columnName);

        return resultSet.wasNull()
                ? null
                : value;
    }
}