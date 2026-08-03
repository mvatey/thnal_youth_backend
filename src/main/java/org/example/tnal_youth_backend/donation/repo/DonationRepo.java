package org.example.tnal_youth_backend.donation.repo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.tnal_youth_backend.donation.dto.DonationDTO;
import org.example.tnal_youth_backend.donation.dto.DonationSummaryDTO;
import org.example.tnal_youth_backend.donation.model.DonationModel;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * All SQL is written against the committed schema (V8 donations, V13 lookups,
 * V14 users.full_name_km + users.branch_id, V23 donation_no_seq +
 * client_request_id, V24 donations.updated_by). Every column is aliased
 * explicitly because map-underscore-to-camel-case is NOT configured for MyBatis
 * in this project (see the note in {@code NotificationRepo}).
 *
 * <p>The enriched SELECT block (columns + joins) is duplicated across findById /
 * list because MyBatis annotation mappers cannot share a &lt;sql&gt; fragment;
 * keeping them identical is deliberate.
 */
@Mapper
public interface DonationRepo {

    // ===================== lookup validation =====================

    @Select("""
        SELECT COUNT(*)
        FROM donation_types
        WHERE id = #{typeId} AND is_active = TRUE
        """)
    int countActiveType(@Param("typeId") Short typeId);

    @Select("""
        SELECT code
        FROM donation_types
        WHERE id = #{typeId}
        """)
    String findTypeCode(@Param("typeId") Short typeId);

    @Select("""
        SELECT id
        FROM donation_types
        WHERE UPPER(code) = UPPER(#{code})
          AND is_active = TRUE
        LIMIT 1
        """)
    Short findActiveTypeIdByCode(@Param("code") String code);

    @Select("""
        SELECT COUNT(*)
        FROM payment_methods
        WHERE id = #{pmId} AND is_active = TRUE
        """)
    int countActivePaymentMethod(@Param("pmId") Short paymentMethodId);

    @Select("""
        SELECT COUNT(*)
        FROM branches
        WHERE id = #{branchId}
        """)
    int countBranch(@Param("branchId") Long branchId);

    @Select("""
        SELECT COUNT(*)
        FROM members
        WHERE id = #{memberId}
        """)
    int countMember(@Param("memberId") Long memberId);

    @Select("""
        SELECT COUNT(*)
        FROM sponsors
        WHERE id = #{sponsorId} AND is_active = TRUE
        """)
    int countActiveSponsor(@Param("sponsorId") Long sponsorId);

    @Select("""
        SELECT COUNT(*)
        FROM activities
        WHERE id = #{activityId}
        """)
    int countActivity(@Param("activityId") Long activityId);

    @Select("""
        SELECT COUNT(*)
        FROM files
        WHERE id = #{fileId}
        """)
    int countFile(@Param("fileId") Long fileId);

    // ===================== authorization scoping =====================

    /**
     * The branch a user is bound to (users.branch_id, added in V14), or
     * {@code null} if unset. Used to confine a BRANCH_LEADER's donation
     * reads/writes to their own branch. ADMIN / SECRETARY are org-wide and never
     * call this.
     */
    @Select("""
        SELECT branch_id
        FROM users
        WHERE id = #{userId}
        """)
    Long findBranchIdByUserId(@Param("userId") Long userId);

    // ===================== number + idempotency =====================

    /** Atomic; the service formats the value into DON-{yyyyMMdd}-{seq}. */
    @Select("SELECT nextval('donation_no_seq')")
    long nextDonationNoSeq();

    /**
     * Returns the id of a prior donation recorded by the same actor with the same
     * idempotency key, or {@code null}. Backed by the partial unique index
     * uq_donations_recorder_client_request (V23).
     */
    @Select("""
        SELECT id
        FROM donations
        WHERE recorded_by = #{recordedBy}
          AND client_request_id = #{clientRequestId}::uuid
        """)
    Long findIdByRecorderAndClientRequestId(@Param("recordedBy") Long recordedBy,
                                            @Param("clientRequestId") String clientRequestId);

    // ===================== writes =====================

    @Insert("""
        INSERT INTO donations
            (donation_no, donation_type_id, member_id, sponsor_id, donor_name,
             activity_id, branch_id, donation_period,
             amount_khr, amount_usd, exchange_rate_khr_per_usd, total_amount_usd,
             payment_method_id, paid_at, payment_reference, receipt_file_id,
             recorded_by, note, client_request_id)
        VALUES
            (#{donationNo}, #{donationTypeId}, #{memberId}, #{sponsorId}, #{donorName},
             #{activityId}, #{branchId}, #{donationPeriod},
             #{amountKhr}, #{amountUsd}, #{exchangeRateKhrPerUsd}, #{totalAmountUsd},
             #{paymentMethodId}, #{paidAt}, #{paymentReference}, #{receiptFileId},
             #{recordedBy}, #{note}, #{clientRequestId}::uuid)
        """)
    @Options(useGeneratedKeys = true,
            keyProperty = "id,createdAt",
            keyColumn = "id,created_at")
    int insertDonation(DonationModel d);

    /**
     * Full-replace update. donation_no / recorded_by / client_request_id are
     * intentionally NOT updatable. Records the editor in updated_by (V24).
     *
     * <p>OPTIMISTIC LOCK: when {@code expectedUpdatedAt} is supplied the WHERE
     * clause additionally requires the row's current {@code updated_at} to match,
     * so a stale edit affects 0 rows and the service can distinguish a conflict
     * from a not-found. When it is null the guard is skipped (last-writer-wins,
     * as before). Returns the number of rows affected.
     */
    @Update({
            "<script>",
            "UPDATE donations",
            "SET donation_type_id          = #{donationTypeId},",
            "    member_id                 = #{memberId},",
            "    sponsor_id                = #{sponsorId},",
            "    donor_name                = #{donorName},",
            "    activity_id               = #{activityId},",
            "    branch_id                 = #{branchId},",
            "    donation_period           = #{donationPeriod},",
            "    amount_khr                = #{amountKhr},",
            "    amount_usd                = #{amountUsd},",
            "    exchange_rate_khr_per_usd = #{exchangeRateKhrPerUsd},",
            "    total_amount_usd          = #{totalAmountUsd},",
            "    payment_method_id         = #{paymentMethodId},",
            "    paid_at                   = #{paidAt},",
            "    payment_reference         = #{paymentReference},",
            "    receipt_file_id           = #{receiptFileId},",
            "    note                      = #{note},",
            "    updated_by                = #{updatedBy},",
            "    updated_at                = NOW()",
            "WHERE id = #{id}",
            "<if test='expectedUpdatedAt != null'> AND updated_at = #{expectedUpdatedAt} </if>",
            "</script>"
    })
    int updateDonation(DonationModel d);

    @org.apache.ibatis.annotations.Delete("""
        DELETE FROM donations
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);

    // ===================== reads =====================

    @Select("""
        SELECT
            n.id                         AS id,
            n.donation_no                AS donationNo,
            n.donation_type_id           AS donationTypeId,
            dt.code                      AS typeCode,
            dt.label_km                  AS typeLabelKm,
            dt.label_en                  AS typeLabelEn,
            n.member_id                  AS memberId,
            m.full_name_km               AS memberName,
            n.sponsor_id                 AS sponsorId,
            s.name                       AS sponsorName,
            n.donor_name                 AS donorName,
            COALESCE(m.full_name_km, s.name, n.donor_name) AS donorDisplay,
            n.activity_id                AS activityId,
            a.title_km                   AS activityTitle,
            n.branch_id                  AS branchId,
            b.name_km                    AS branchName,
            n.donation_period            AS donationPeriod,
            n.amount_khr                 AS amountKhr,
            n.amount_usd                 AS amountUsd,
            n.exchange_rate_khr_per_usd  AS exchangeRateKhrPerUsd,
            n.total_amount_usd           AS totalAmountUsd,
            n.payment_method_id          AS paymentMethodId,
            pm.code                      AS paymentMethodCode,
            pm.label_km                  AS paymentMethodLabelKm,
            pm.label_en                  AS paymentMethodLabelEn,
            n.paid_at                    AS paidAt,
            n.payment_reference          AS paymentReference,
            n.receipt_file_id            AS receiptFileId,
            n.recorded_by                AS recordedBy,
            ru.full_name_km              AS recordedByName,
            n.updated_by                 AS updatedBy,
            ub.full_name_km              AS updatedByName,
            n.note                       AS note,
            n.created_at                 AS createdAt,
            n.updated_at                 AS updatedAt
        FROM donations n
        JOIN donation_types  dt ON dt.id = n.donation_type_id
        JOIN payment_methods pm ON pm.id = n.payment_method_id
        JOIN branches        b  ON b.id  = n.branch_id
        LEFT JOIN members    m  ON m.id  = n.member_id
        LEFT JOIN sponsors   s  ON s.id  = n.sponsor_id
        LEFT JOIN activities a  ON a.id  = n.activity_id
        LEFT JOIN users      ru ON ru.id = n.recorded_by
        LEFT JOIN users      ub ON ub.id = n.updated_by
        WHERE n.id = #{id}
        """)
    DonationDTO findById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT",
            "    n.id                         AS id,",
            "    n.donation_no                AS donationNo,",
            "    n.donation_type_id           AS donationTypeId,",
            "    dt.code                      AS typeCode,",
            "    dt.label_km                  AS typeLabelKm,",
            "    dt.label_en                  AS typeLabelEn,",
            "    n.member_id                  AS memberId,",
            "    m.full_name_km               AS memberName,",
            "    n.sponsor_id                 AS sponsorId,",
            "    s.name                       AS sponsorName,",
            "    n.donor_name                 AS donorName,",
            "    COALESCE(m.full_name_km, s.name, n.donor_name) AS donorDisplay,",
            "    n.activity_id                AS activityId,",
            "    a.title_km                   AS activityTitle,",
            "    n.branch_id                  AS branchId,",
            "    b.name_km                    AS branchName,",
            "    n.donation_period            AS donationPeriod,",
            "    n.amount_khr                 AS amountKhr,",
            "    n.amount_usd                 AS amountUsd,",
            "    n.exchange_rate_khr_per_usd  AS exchangeRateKhrPerUsd,",
            "    n.total_amount_usd           AS totalAmountUsd,",
            "    n.payment_method_id          AS paymentMethodId,",
            "    pm.code                      AS paymentMethodCode,",
            "    pm.label_km                  AS paymentMethodLabelKm,",
            "    pm.label_en                  AS paymentMethodLabelEn,",
            "    n.paid_at                    AS paidAt,",
            "    n.payment_reference          AS paymentReference,",
            "    n.receipt_file_id            AS receiptFileId,",
            "    n.recorded_by                AS recordedBy,",
            "    ru.full_name_km              AS recordedByName,",
            "    n.updated_by                 AS updatedBy,",
            "    ub.full_name_km              AS updatedByName,",
            "    n.note                       AS note,",
            "    n.created_at                 AS createdAt,",
            "    n.updated_at                 AS updatedAt",
            "FROM donations n",
            "JOIN donation_types  dt ON dt.id = n.donation_type_id",
            "JOIN payment_methods pm ON pm.id = n.payment_method_id",
            "JOIN branches        b  ON b.id  = n.branch_id",
            "LEFT JOIN members    m  ON m.id  = n.member_id",
            "LEFT JOIN sponsors   s  ON s.id  = n.sponsor_id",
            "LEFT JOIN activities a  ON a.id  = n.activity_id",
            "LEFT JOIN users      ru ON ru.id = n.recorded_by",
            "LEFT JOIN users      ub ON ub.id = n.updated_by",
            "<where>",
            "  <if test='branchId != null'>        AND n.branch_id = #{branchId}        </if>",
            "  <if test='donationTypeId != null'>  AND n.donation_type_id = #{donationTypeId} </if>",
            "  <if test='paymentMethodId != null'> AND n.payment_method_id = #{paymentMethodId} </if>",
            "  <if test='memberId != null'>        AND n.member_id = #{memberId}        </if>",
            "  <if test='sponsorId != null'>       AND n.sponsor_id = #{sponsorId}      </if>",
            "  <if test='activityId != null'>      AND n.activity_id = #{activityId}    </if>",
            "  <if test='paidFrom != null'>        AND n.paid_at &gt;= #{paidFrom}      </if>",
            "  <if test='paidTo != null'>          AND n.paid_at &lt;= #{paidTo}        </if>",
            "  <if test='search != null and search != \"\"'>",
            "     AND (",
            "        n.donation_no ILIKE ('%' || #{search} || '%')",
            "        OR n.payment_reference ILIKE ('%' || #{search} || '%')",
            "        OR COALESCE(m.full_name_km, s.name, n.donor_name) ILIKE ('%' || #{search} || '%')",
            "     )",
            "  </if>",
            "</where>",
            "ORDER BY n.paid_at DESC, n.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<DonationDTO> list(@Param("branchId") Long branchId,
                           @Param("donationTypeId") Short donationTypeId,
                           @Param("paymentMethodId") Short paymentMethodId,
                           @Param("memberId") Long memberId,
                           @Param("sponsorId") Long sponsorId,
                           @Param("activityId") Long activityId,
                           @Param("paidFrom") OffsetDateTime paidFrom,
                           @Param("paidTo") OffsetDateTime paidTo,
                           @Param("search") String search,
                           @Param("limit") int limit,
                           @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM donations n",
            "LEFT JOIN members  m ON m.id = n.member_id",
            "LEFT JOIN sponsors s ON s.id = n.sponsor_id",
            "<where>",
            "  <if test='branchId != null'>        AND n.branch_id = #{branchId}        </if>",
            "  <if test='donationTypeId != null'>  AND n.donation_type_id = #{donationTypeId} </if>",
            "  <if test='paymentMethodId != null'> AND n.payment_method_id = #{paymentMethodId} </if>",
            "  <if test='memberId != null'>        AND n.member_id = #{memberId}        </if>",
            "  <if test='sponsorId != null'>       AND n.sponsor_id = #{sponsorId}      </if>",
            "  <if test='activityId != null'>      AND n.activity_id = #{activityId}    </if>",
            "  <if test='paidFrom != null'>        AND n.paid_at &gt;= #{paidFrom}      </if>",
            "  <if test='paidTo != null'>          AND n.paid_at &lt;= #{paidTo}        </if>",
            "  <if test='search != null and search != \"\"'>",
            "     AND (",
            "        n.donation_no ILIKE ('%' || #{search} || '%')",
            "        OR n.payment_reference ILIKE ('%' || #{search} || '%')",
            "        OR COALESCE(m.full_name_km, s.name, n.donor_name) ILIKE ('%' || #{search} || '%')",
            "     )",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countList(@Param("branchId") Long branchId,
                   @Param("donationTypeId") Short donationTypeId,
                   @Param("paymentMethodId") Short paymentMethodId,
                   @Param("memberId") Long memberId,
                   @Param("sponsorId") Long sponsorId,
                   @Param("activityId") Long activityId,
                   @Param("paidFrom") OffsetDateTime paidFrom,
                   @Param("paidTo") OffsetDateTime paidTo,
                   @Param("search") String search);

    @Select({
            "<script>",
            "SELECT",
            "  COUNT(*)                          AS count,",
            "  COALESCE(SUM(n.total_amount_usd), 0) AS sumTotalUsd,",
            "  COALESCE(SUM(n.amount_khr), 0)       AS sumAmountKhr,",
            "  COALESCE(SUM(n.amount_usd), 0)       AS sumAmountUsd",
            "FROM donations n",
            "LEFT JOIN members  m ON m.id = n.member_id",
            "LEFT JOIN sponsors s ON s.id = n.sponsor_id",
            "<where>",
            "  <if test='branchId != null'>        AND n.branch_id = #{branchId}        </if>",
            "  <if test='donationTypeId != null'>  AND n.donation_type_id = #{donationTypeId} </if>",
            "  <if test='paymentMethodId != null'> AND n.payment_method_id = #{paymentMethodId} </if>",
            "  <if test='memberId != null'>        AND n.member_id = #{memberId}        </if>",
            "  <if test='sponsorId != null'>       AND n.sponsor_id = #{sponsorId}      </if>",
            "  <if test='activityId != null'>      AND n.activity_id = #{activityId}    </if>",
            "  <if test='paidFrom != null'>        AND n.paid_at &gt;= #{paidFrom}      </if>",
            "  <if test='paidTo != null'>          AND n.paid_at &lt;= #{paidTo}        </if>",
            "  <if test='search != null and search != \"\"'>",
            "     AND (",
            "        n.donation_no ILIKE ('%' || #{search} || '%')",
            "        OR n.payment_reference ILIKE ('%' || #{search} || '%')",
            "        OR COALESCE(m.full_name_km, s.name, n.donor_name) ILIKE ('%' || #{search} || '%')",
            "     )",
            "  </if>",
            "</where>",
            "</script>"
    })
    DonationSummaryDTO summary(@Param("branchId") Long branchId,
                               @Param("donationTypeId") Short donationTypeId,
                               @Param("paymentMethodId") Short paymentMethodId,
                               @Param("memberId") Long memberId,
                               @Param("sponsorId") Long sponsorId,
                               @Param("activityId") Long activityId,
                               @Param("paidFrom") OffsetDateTime paidFrom,
                               @Param("paidTo") OffsetDateTime paidTo,
                               @Param("search") String search);
}
