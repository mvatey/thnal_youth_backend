package org.example.tnal_youth_backend.donation.sponsorflow.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SponsorDonationRepository {

    @Select("""
        SELECT id
        FROM donation_types
        WHERE UPPER(code) = 'SPONSOR_DONATION'
          AND is_active = TRUE
        LIMIT 1
        """)
    Short typeId();

    @Select("""
        SELECT id
        FROM sponsor_types
        WHERE UPPER(code) = UPPER(#{code})
          AND is_active = TRUE
        LIMIT 1
        """)
    Short sponsorTypeId(@Param("code") String code);

    @Select("""
        SELECT code
        FROM payment_methods
        WHERE id = #{id}
        LIMIT 1
        """)
    String paymentMethodCode(@Param("id") Short id);

    @Select("""
        SELECT EXISTS(
            SELECT 1
            FROM branches
            WHERE id = #{id}
        )
        """)
    boolean branchExists(@Param("id") Long id);

    @Select("""
        SELECT EXISTS(
            SELECT 1
            FROM activities
            WHERE id = #{id}
        )
        """)
    boolean activityExists(@Param("id") Long id);

    @Select("""
        SELECT EXISTS(
            SELECT 1
            FROM sponsors
            WHERE id = #{id}
              AND is_active = TRUE
        )
        """)
    boolean sponsorExists(@Param("id") Long id);

    @Select("""
        SELECT EXISTS(
            SELECT 1
            FROM members m
            JOIN member_statuses ms
              ON ms.id = m.status_id
            WHERE m.id = #{id}
              AND m.branch_id = #{branchId}
              AND ms.code = 'ACTIVE'
        )
        """)
    boolean activeMemberExistsInBranch(
            @Param("id") Long id,
            @Param("branchId") Long branchId
    );

    @Insert("""
        INSERT INTO sponsors (
            sponsor_type_id,
            name,
            phone,
            email,
            address,
            note,
            created_by
        )
        VALUES (
            #{typeId},
            #{name},
            #{phone},
            #{email},
            #{address},
            #{note},
            #{actorId}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
    int insertSponsor(SponsorInsert row);

    /**
     * Use this only when the UI explicitly edits the sponsor profile.
     * Normal donation editing should not modify a shared existing sponsor.
     */
    @Update("""
        UPDATE sponsors
        SET name = #{name},
            phone = #{phone},
            email = #{email},
            address = #{address},
            note = #{note},
            updated_at = NOW()
        WHERE id = #{id}
        """)
    int updateSponsor(SponsorInsert row);

    @Insert("""
        INSERT INTO donation_sponsor_details (
            donation_id,
            donor_kind,
            material_category,
            material_quantity,
            material_quantity_type,
            purpose,
            updated_at
        )
        VALUES (
            #{donationId},
            #{donorKind},
            #{materialCategory},
            #{materialQuantity},
            #{materialQuantityType},
            #{purpose},
            NOW()
        )
        ON CONFLICT (donation_id)
        DO UPDATE SET
            donor_kind = EXCLUDED.donor_kind,
            material_category = EXCLUDED.material_category,
            material_quantity = EXCLUDED.material_quantity,
            material_quantity_type = EXCLUDED.material_quantity_type,
            purpose = EXCLUDED.purpose,
            updated_at = NOW()
        """)
    int upsertDetails(
            @Param("donationId") Long donationId,
            @Param("donorKind") String donorKind,
            @Param("materialCategory") String materialCategory,
            @Param("materialQuantity") BigDecimal materialQuantity,
            @Param("materialQuantityType") String materialQuantityType,
            @Param("purpose") String purpose
    );

    @Select("""
        SELECT
            d.id AS donationId,
            d.donation_no AS donationNo,

            COALESCE(
                sd.donor_kind,
                CASE
                    WHEN d.member_id IS NOT NULL THEN 'MEMBER'
                    ELSE 'INDIVIDUAL'
                END
            ) AS donorKind,

            d.sponsor_id AS sponsorId,
            d.member_id AS memberId,

            COALESCE(
                m.full_name_km,
                s.name,
                d.donor_name
            ) AS name,

            COALESCE(
                m.phone,
                s.phone
            ) AS phone,

            COALESCE(
                m.email::TEXT,
                s.email::TEXT
            ) AS email,

            COALESCE(
                m.current_address,
                s.address
            ) AS address,

            d.branch_id AS branchId,
            b.name_km AS branchNameKm,

            d.activity_id AS activityId,
            a.title_km AS activityTitleKm,

            d.paid_at AS paidAt,
            d.amount_khr AS amountKhr,
            d.amount_usd AS amountUsd,
            d.total_amount_usd AS totalAmountUsd,

            d.payment_method_id AS paymentMethodId,
            pm.code AS paymentMethodCode,
            pm.label_km AS paymentMethodLabelKm,

            d.payment_reference AS paymentReference,
            d.receipt_file_id AS receiptFileId,

            sd.material_category AS materialCategory,
            sd.material_quantity AS materialQuantity,
            sd.material_quantity_type AS materialQuantityType,
            sd.purpose AS purpose,

            d.note AS note,
            d.updated_at AS updatedAt

        FROM donations d

        JOIN donation_types dt
          ON dt.id = d.donation_type_id
         AND dt.code = 'SPONSOR_DONATION'

        JOIN branches b
          ON b.id = d.branch_id

        JOIN payment_methods pm
          ON pm.id = d.payment_method_id

        LEFT JOIN sponsors s
          ON s.id = d.sponsor_id

        LEFT JOIN members m
          ON m.id = d.member_id

        LEFT JOIN activities a
          ON a.id = d.activity_id

        LEFT JOIN donation_sponsor_details sd
          ON sd.donation_id = d.id

        WHERE d.id = #{id}
        """)
    SponsorDonationRowResponse findOne(
            @Param("id") Long id
    );

    @Select({
            "<script>",
            """
            SELECT
                d.id AS donationId,
                d.donation_no AS donationNo,

                COALESCE(
                    sd.donor_kind,
                    CASE
                        WHEN d.member_id IS NOT NULL THEN 'MEMBER'
                        ELSE 'INDIVIDUAL'
                    END
                ) AS donorKind,

                d.sponsor_id AS sponsorId,
                d.member_id AS memberId,

                COALESCE(
                    m.full_name_km,
                    s.name,
                    d.donor_name
                ) AS name,

                COALESCE(
                    m.phone,
                    s.phone
                ) AS phone,

                COALESCE(
                    m.email::TEXT,
                    s.email::TEXT
                ) AS email,

                COALESCE(
                    m.current_address,
                    s.address
                ) AS address,

                d.branch_id AS branchId,
                b.name_km AS branchNameKm,

                d.activity_id AS activityId,
                a.title_km AS activityTitleKm,

                d.paid_at AS paidAt,
                d.amount_khr AS amountKhr,
                d.amount_usd AS amountUsd,
                d.total_amount_usd AS totalAmountUsd,

                d.payment_method_id AS paymentMethodId,
                pm.code AS paymentMethodCode,
                pm.label_km AS paymentMethodLabelKm,

                d.payment_reference AS paymentReference,
                d.receipt_file_id AS receiptFileId,

                sd.material_category AS materialCategory,
                sd.material_quantity AS materialQuantity,
                sd.material_quantity_type AS materialQuantityType,
                sd.purpose AS purpose,

                d.note AS note,
                d.updated_at AS updatedAt

            FROM donations d

            JOIN donation_types dt
              ON dt.id = d.donation_type_id
             AND dt.code = 'SPONSOR_DONATION'

            JOIN branches b
              ON b.id = d.branch_id

            JOIN payment_methods pm
              ON pm.id = d.payment_method_id

            LEFT JOIN sponsors s
              ON s.id = d.sponsor_id

            LEFT JOIN members m
              ON m.id = d.member_id

            LEFT JOIN activities a
              ON a.id = d.activity_id

            LEFT JOIN donation_sponsor_details sd
              ON sd.donation_id = d.id

            <where>
                <if test="branchId != null">
                    AND d.branch_id = #{branchId}
                </if>

                <if test="kind != null and kind != ''">
                    AND COALESCE(
                        sd.donor_kind,
                        CASE
                            WHEN d.member_id IS NOT NULL THEN 'MEMBER'
                            ELSE 'INDIVIDUAL'
                        END
                    ) = #{kind}
                </if>

                <if test="paidFrom != null">
                    AND d.paid_at &gt;= #{paidFrom}
                </if>

                <if test="paidTo != null">
                    AND d.paid_at &lt;= #{paidTo}
                </if>

                <if test="search != null and search != ''">
                    AND (
                        COALESCE(
                            m.full_name_km,
                            s.name,
                            d.donor_name,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            m.phone,
                            s.phone,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            m.email::TEXT,
                            s.email::TEXT,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            d.donation_no,
                            ''
                        ) ILIKE ('%' || #{search} || '%')
                    )
                </if>
            </where>

            ORDER BY
                d.paid_at DESC,
                d.id DESC

            LIMIT #{limit}
            OFFSET #{offset}
            """,
            "</script>"
    })
    List<SponsorDonationRowResponse> list(
            @Param("branchId") Long branchId,
            @Param("kind") String kind,
            @Param("paidFrom") OffsetDateTime paidFrom,
            @Param("paidTo") OffsetDateTime paidTo,
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select({
            "<script>",
            """
            SELECT COUNT(*)

            FROM donations d

            JOIN donation_types dt
              ON dt.id = d.donation_type_id
             AND dt.code = 'SPONSOR_DONATION'

            LEFT JOIN sponsors s
              ON s.id = d.sponsor_id

            LEFT JOIN members m
              ON m.id = d.member_id

            LEFT JOIN donation_sponsor_details sd
              ON sd.donation_id = d.id

            <where>
                <if test="branchId != null">
                    AND d.branch_id = #{branchId}
                </if>

                <if test="kind != null and kind != ''">
                    AND COALESCE(
                        sd.donor_kind,
                        CASE
                            WHEN d.member_id IS NOT NULL THEN 'MEMBER'
                            ELSE 'INDIVIDUAL'
                        END
                    ) = #{kind}
                </if>

                <if test="paidFrom != null">
                    AND d.paid_at &gt;= #{paidFrom}
                </if>

                <if test="paidTo != null">
                    AND d.paid_at &lt;= #{paidTo}
                </if>

                <if test="search != null and search != ''">
                    AND (
                        COALESCE(
                            m.full_name_km,
                            s.name,
                            d.donor_name,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            m.phone,
                            s.phone,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            m.email::TEXT,
                            s.email::TEXT,
                            ''
                        ) ILIKE ('%' || #{search} || '%')

                        OR COALESCE(
                            d.donation_no,
                            ''
                        ) ILIKE ('%' || #{search} || '%')
                    )
                </if>
            </where>
            """,
            "</script>"
    })
    long count(
            @Param("branchId") Long branchId,
            @Param("kind") String kind,
            @Param("paidFrom") OffsetDateTime paidFrom,
            @Param("paidTo") OffsetDateTime paidTo,
            @Param("search") String search
    );

    @Select("""
        SELECT
            COUNT(
                DISTINCT COALESCE(
                    'm' || d.member_id::TEXT,
                    's' || d.sponsor_id::TEXT,
                    'n' || d.donor_name,
                    'd' || d.id::TEXT
                )
            ) AS donorCount,

            COALESCE(
                SUM(d.amount_khr),
                0
            ) AS totalKhr,

            COALESCE(
                SUM(d.amount_usd),
                0
            ) AS totalUsd,

            COALESCE(
                SUM(d.total_amount_usd),
                0
            ) AS overallTotalUsd

        FROM donations d

        JOIN donation_types dt
          ON dt.id = d.donation_type_id
         AND dt.code = 'SPONSOR_DONATION'

        WHERE (
            #{branchId} IS NULL
            OR d.branch_id = #{branchId}
        )
          AND (
            #{from} IS NULL
            OR d.paid_at >= #{from}
        )
          AND (
            #{to} IS NULL
            OR d.paid_at <= #{to}
        )
        """)
    SponsorDonationSummaryResponse summary(
            @Param("branchId") Long branchId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Select("""
        SELECT
            s.id,
            s.name,
            s.phone,
            s.email::TEXT AS email,
            s.address,

            CASE
                WHEN st.code = 'ORGANIZATION'
                    THEN 'INSTITUTION'
                ELSE 'INDIVIDUAL'
            END AS donorKind

        FROM sponsors s

        JOIN sponsor_types st
          ON st.id = s.sponsor_type_id

        WHERE s.is_active = TRUE
          AND (
              CAST(#{search} AS TEXT) IS NULL
              OR s.name ILIKE ('%' || #{search} || '%')
              OR COALESCE(
                  s.phone,
                  ''
              ) ILIKE ('%' || #{search} || '%')
          )

        ORDER BY s.name

        LIMIT 30
        """)
    List<SponsorLookupResponse> sponsors(
            @Param("search") String search
    );

    @Select("""
        SELECT
            m.id,
            m.member_no AS memberNo,
            m.full_name_km AS name,
            m.full_name_en AS nameEn,
            m.phone,
            m.email::TEXT AS email,
            m.current_address AS address,
            'MEMBER' AS donorKind,
            m.branch_id AS branchId,
            b.name_km AS branchNameKm,
            b.name_en AS branchNameEn
        FROM members m
        JOIN branches b
          ON b.id = m.branch_id
        JOIN member_statuses ms
          ON ms.id = m.status_id
         AND ms.code = 'ACTIVE'
        WHERE m.branch_id = #{branchId}
          AND (
              CAST(#{search} AS TEXT) IS NULL
              OR m.full_name_km ILIKE ('%' || #{search} || '%')
              OR COALESCE(m.full_name_en, '') ILIKE ('%' || #{search} || '%')
              OR m.member_no ILIKE ('%' || #{search} || '%')
              OR COALESCE(m.phone, '') ILIKE ('%' || #{search} || '%')
          )
        ORDER BY m.full_name_km
        LIMIT 50
        """)
    List<SponsorLookupResponse> members(
            @Param("branchId") Long branchId,
            @Param("search") String search
    );

    @Select("""
        SELECT role
        FROM users
        WHERE id = #{id}
        """)
    String userRole(
            @Param("id") Long id
    );

    @Select("""
        SELECT m.branch_id
        FROM users u
        LEFT JOIN members m
          ON m.id = u.member_id
        WHERE u.id = #{id}
        """)
    Long userBranch(
            @Param("id") Long id
    );

    class SponsorInsert {
        public Long id;
        public Short typeId;
        public String name;
        public String phone;
        public String email;
        public String address;
        public String note;
        public Long actorId;
    }
}
