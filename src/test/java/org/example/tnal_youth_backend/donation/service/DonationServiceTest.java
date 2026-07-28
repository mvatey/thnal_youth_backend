package org.example.tnal_youth_backend.donation.service;

import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.dto.DonationDTO;
import org.example.tnal_youth_backend.donation.dto.DonationPageDTO;
import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
import org.example.tnal_youth_backend.donation.model.DonationModel;
import org.example.tnal_youth_backend.donation.repo.DonationRepo;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DonationService}. The repo and {@link Clock} are mocked;
 * {@link SecurityUtils#getCurrentUserId()} / {@code getCurrentUserRole()} are
 * stubbed via a {@code MockedStatic} so no Spring context / SecurityContext is
 * needed.
 *
 * <p>Coverage focus (the parts a reader can't eyeball for correctness):
 * <ul>
 *   <li>server-side USD total math (the {@code 49.39} the .http suite pins),</li>
 *   <li>exchange-rate normalisation (dropped when there is no KHR component),</li>
 *   <li>every cross-field rule -&gt; its stable {@link BusinessException} code,</li>
 *   <li>type-specific required fields (ACTIVITY / MONTHLY),</li>
 *   <li>referential checks,</li>
 *   <li>idempotent replay short-circuit,</li>
 *   <li>donation number format,</li>
 *   <li>not-found / edit-conflict paths on get / update / delete,</li>
 *   <li>branch-leader object-level scoping (create / get / update / list),</li>
 *   <li>list pagination clamping.</li>
 * </ul>
 *
 * <p>{@code Strictness.LENIENT} is used because the "all lookups valid" helper
 * stubs more than any single failure-path test consumes. By default
 * {@code getCurrentUserRole()} is left UNSTUBBED (returns null) so the org-wide
 * (ADMIN/SECRETARY) path is exercised; the branch-leader tests stub it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DonationServiceTest {

    private static final long ACTOR_ID = 7L;
    // 2026-07-27 -> donation-number date component
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-07-27T08:00:00Z"), ZoneOffset.UTC);

    @Mock
    private DonationRepo repo;

    private DonationService service;
    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        service = new DonationService(repo, FIXED_CLOCK);
        securityUtils = org.mockito.Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(ACTOR_ID);
        // getCurrentUserRole() intentionally left unstubbed -> null -> org-wide.
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    // =================================================================
    // create — happy paths + money
    // =================================================================

    @Test
    void create_mixedKhrUsd_computesTotalUsd_andMintsNumber() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(42L);
        stubInsertAssigningKeys(100L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto();
        req.setAmountKhr(new BigDecimal("100000.00"));
        req.setAmountUsd(new BigDecimal("25.00"));
        req.setExchangeRateKhrPerUsd(new BigDecimal("4100.0000"));

        DonationCreateResultDTO res = service.create(req);

        // 25.00 + 100000/4100 = 49.39  (the pinned donations.http assertion)
        assertEquals(0, new BigDecimal("49.39").compareTo(res.getTotalAmountUsd()));
        assertEquals(100L, res.getId());
        assertEquals("DON-20260727-000042", res.getDonationNo());

        DonationModel saved = captureInserted();
        assertEquals(0, new BigDecimal("49.39").compareTo(saved.getTotalAmountUsd()));
        assertEquals(ACTOR_ID, saved.getRecordedBy());
        assertEquals("DON-20260727-000042", saved.getDonationNo());
    }

    @Test
    void create_usdOnly_noRateRequired() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(1L);
        stubInsertAssigningKeys(101L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto();
        req.setAmountUsd(new BigDecimal("10.00")); // no KHR, no rate

        DonationCreateResultDTO res = service.create(req);

        assertEquals(0, new BigDecimal("10.00").compareTo(res.getTotalAmountUsd()));
        assertEquals("DON-20260727-000001", res.getDonationNo());
    }

    @Test
    void create_khrOnly_convertsToUsd() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(5L);
        stubInsertAssigningKeys(102L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto();
        req.setAmountUsd(BigDecimal.ZERO); // KHR-only: cancel the base USD amount
        req.setAmountKhr(new BigDecimal("40000.00"));
        req.setExchangeRateKhrPerUsd(new BigDecimal("4100.0000"));

        DonationCreateResultDTO res = service.create(req);

        // 40000 / 4100 = 9.7560... -> 9.76
        assertEquals(0, new BigDecimal("9.76").compareTo(res.getTotalAmountUsd()));
    }

    @Test
    void create_zeroKhr_withStrayRate_dropsRate() {
        // A rate sent with a zero-KHR donation is meaningless; the service must not
        // persist it (avoids a misleading number on the record).
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(7L);
        stubInsertAssigningKeys(110L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto();
        req.setAmountUsd(new BigDecimal("10.00"));
        req.setAmountKhr(BigDecimal.ZERO);
        req.setExchangeRateKhrPerUsd(new BigDecimal("4100.0000")); // stray

        service.create(req);

        DonationModel saved = captureInserted();
        assertNull(saved.getExchangeRateKhrPerUsd());
        assertEquals(0, new BigDecimal("10.00").compareTo(saved.getTotalAmountUsd()));
    }

    @Test
    void create_donorName_isTrimmed_andNullsAreNormalized() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(9L);
        stubInsertAssigningKeys(103L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = new DonationCreateDTO();
        req.setDonationTypeId((short) 3);
        req.setDonorName("  Walk-in donor  ");
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("10.00"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T10:15:00Z"));
        req.setNote("   ");             // blank -> null
        req.setPaymentReference("");    // blank -> null

        service.create(req);

        DonationModel saved = captureInserted();
        assertEquals("Walk-in donor", saved.getDonorName());
        assertNull(saved.getNote());
        assertNull(saved.getPaymentReference());
        assertNull(saved.getMemberId());
        assertNull(saved.getSponsorId());
    }

    // =================================================================
    // create — cross-field validation
    // =================================================================

    @Test
    void create_twoDonorSources_throwsSourceInvalid() {
        DonationCreateDTO req = baseSponsorDto();
        req.setDonorName("Also a name"); // sponsor + name = 2 sources

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_SOURCE_INVALID", ex.getCode());
        verify(repo, never()).insertDonation(any());
    }

    @Test
    void create_noDonorSource_throwsSourceInvalid() {
        DonationCreateDTO req = new DonationCreateDTO();
        req.setDonationTypeId((short) 3);
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("5.00"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T10:00:00Z"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_SOURCE_INVALID", ex.getCode());
    }

    @Test
    void create_zeroAmounts_throwsAmountsInvalid() {
        DonationCreateDTO req = baseSponsorDto();
        req.setAmountKhr(BigDecimal.ZERO);
        req.setAmountUsd(BigDecimal.ZERO);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_AMOUNTS_INVALID", ex.getCode());
        verify(repo, never()).insertDonation(any());
    }

    @Test
    void create_khrWithoutRate_throwsExchangeRateRequired() {
        DonationCreateDTO req = baseSponsorDto();
        req.setAmountKhr(new BigDecimal("40000.00")); // KHR present, no rate

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_EXCHANGE_RATE_REQUIRED", ex.getCode());
        verify(repo, never()).insertDonation(any());
    }

    // =================================================================
    // create — lookup / referential validation
    // =================================================================

    @Test
    void create_inactiveType_throwsTypeInactive() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.countActiveType(anyShort())).thenReturn(0); // override

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(baseSponsorDto()));
        assertEquals("DONATION_TYPE_INACTIVE", ex.getCode());
    }

    @Test
    void create_inactivePaymentMethod_throwsPaymentMethodInactive() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.countActivePaymentMethod(anyShort())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(baseSponsorDto()));
        assertEquals("DONATION_PAYMENT_METHOD_INACTIVE", ex.getCode());
    }

    @Test
    void create_missingBranch_throwsBranchNotFound() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.countBranch(anyLong())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(baseSponsorDto()));
        assertEquals("DONATION_BRANCH_NOT_FOUND", ex.getCode());
    }

    @Test
    void create_missingSponsor_throwsSponsorNotFound() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.countActiveSponsor(anyLong())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(baseSponsorDto()));
        assertEquals("DONATION_SPONSOR_NOT_FOUND", ex.getCode());
    }

    @Test
    void create_missingReceiptFile_throwsReceiptNotFound() {
        stubValidLookups("SPONSOR_DONATION");
        when(repo.countFile(anyLong())).thenReturn(0);

        DonationCreateDTO req = baseSponsorDto();
        req.setReceiptFileId(999L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_RECEIPT_NOT_FOUND", ex.getCode());
    }

    // =================================================================
    // create — type-specific required fields
    // =================================================================

    @Test
    void create_activityDonationWithoutActivity_throwsActivityRequired() {
        stubValidLookups("ACTIVITY_DONATION");

        DonationCreateDTO req = baseSponsorDto(); // no activityId set
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_ACTIVITY_REQUIRED", ex.getCode());
    }

    @Test
    void create_monthlyDonationWithoutPeriod_throwsPeriodRequired() {
        stubValidLookups("MONTHLY_DONATION");

        // MONTHLY is member-sourced in practice; use a member donor + no period.
        DonationCreateDTO req = new DonationCreateDTO();
        req.setDonationTypeId((short) 1);
        req.setMemberId(1L);
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("10.00"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T10:00:00Z"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("DONATION_PERIOD_REQUIRED", ex.getCode());
    }

    // =================================================================
    // create — idempotency
    // =================================================================

    @Test
    void create_idempotentReplay_returnsExisting_withoutInserting() {
        String key = "11111111-1111-1111-1111-111111111111";
        when(repo.findIdByRecorderAndClientRequestId(ACTOR_ID, key)).thenReturn(500L);
        when(repo.findById(500L)).thenReturn(DonationDTO.builder()
                .id(500L)
                .donationNo("DON-20260724-000010")
                .totalAmountUsd(new BigDecimal("7.50"))
                .createdAt(OffsetDateTime.parse("2026-07-24T10:35:00Z"))
                .build());

        DonationCreateDTO req = new DonationCreateDTO();
        req.setDonationTypeId((short) 3);
        req.setDonorName("Idem donor");
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("7.50"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T10:35:00Z"));
        req.setClientRequestId(key);

        DonationCreateResultDTO res = service.create(req);

        assertEquals(500L, res.getId());
        assertEquals("DON-20260724-000010", res.getDonationNo());
        verify(repo, never()).insertDonation(any());
        verify(repo, never()).nextDonationNoSeq();
    }

    @Test
    void create_firstUseOfKey_insertsNormally() {
        stubValidLookups("SPONSOR_DONATION");
        String key = "22222222-2222-2222-2222-222222222222";
        when(repo.findIdByRecorderAndClientRequestId(ACTOR_ID, key)).thenReturn(null);
        when(repo.nextDonationNoSeq()).thenReturn(3L);
        stubInsertAssigningKeys(104L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto();
        req.setAmountUsd(new BigDecimal("7.50"));
        req.setClientRequestId(key);

        DonationCreateResultDTO res = service.create(req);

        assertEquals(104L, res.getId());
        DonationModel saved = captureInserted();
        assertEquals(key, saved.getClientRequestId());
    }

    // =================================================================
    // get / update / delete — not found
    // =================================================================

    @Test
    void get_missing_throwsNotFound() {
        when(repo.findById(77L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.get(77L));
        assertEquals("DONATION_NOT_FOUND", ex.getCode());
    }

    @Test
    void update_missing_throwsNotFound_withoutUpdating() {
        when(repo.findById(88L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(88L, baseUpdateDto()));
        assertEquals("DONATION_NOT_FOUND", ex.getCode());
        verify(repo, never()).updateDonation(any());
    }

    @Test
    void update_existing_revalidates_andReturnsFreshRow() {
        stubValidLookups("SPONSOR_DONATION");
        DonationDTO before = DonationDTO.builder().id(5L).donationNo("DON-20260724-000001").build();
        DonationDTO after = DonationDTO.builder().id(5L).donationNo("DON-20260724-000001")
                .totalAmountUsd(new BigDecimal("30.00")).build();
        when(repo.findById(5L)).thenReturn(before, after); // pre-check, then post-update read
        when(repo.updateDonation(any(DonationModel.class))).thenReturn(1);

        DonationUpdateDTO req = baseUpdateDto();
        req.setAmountUsd(new BigDecimal("30.00"));

        DonationDTO res = service.update(5L, req);

        assertEquals(0, new BigDecimal("30.00").compareTo(res.getTotalAmountUsd()));
        ArgumentCaptor<DonationModel> cap = ArgumentCaptor.forClass(DonationModel.class);
        verify(repo).updateDonation(cap.capture());
        assertEquals(5L, cap.getValue().getId());
        // donationNo / recordedBy / clientRequestId must not be carried by the update model
        assertNull(cap.getValue().getDonationNo());
        assertNull(cap.getValue().getRecordedBy());
        assertNull(cap.getValue().getClientRequestId());
        // ...but the editor MUST be attributed (V24 audit).
        assertEquals(ACTOR_ID, cap.getValue().getUpdatedBy());
    }

    @Test
    void update_staleVersion_throwsConflict() {
        stubValidLookups("SPONSOR_DONATION");
        DonationDTO before = DonationDTO.builder().id(9L).donationNo("DON-20260724-000009").build();
        // pre-check finds it, post-0-row re-check still finds it -> conflict, not delete.
        when(repo.findById(9L)).thenReturn(before, before);
        when(repo.updateDonation(any(DonationModel.class))).thenReturn(0);

        DonationUpdateDTO req = baseUpdateDto();
        req.setExpectedUpdatedAt(OffsetDateTime.parse("2026-07-24T09:00:00Z")); // stale token

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(9L, req));
        assertEquals("DONATION_UPDATE_CONFLICT", ex.getCode());
    }

    @Test
    void update_versionedButRowDeleted_throwsNotFound() {
        stubValidLookups("SPONSOR_DONATION");
        DonationDTO before = DonationDTO.builder().id(10L).donationNo("DON-20260724-000010").build();
        when(repo.findById(10L)).thenReturn(before, (DonationDTO) null); // vanished between check and re-read
        when(repo.updateDonation(any(DonationModel.class))).thenReturn(0);

        DonationUpdateDTO req = baseUpdateDto();
        req.setExpectedUpdatedAt(OffsetDateTime.parse("2026-07-24T09:00:00Z"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(10L, req));
        assertEquals("DONATION_NOT_FOUND", ex.getCode());
    }

    @Test
    void delete_missing_throwsNotFound() {
        when(repo.deleteById(66L)).thenReturn(0);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(66L));
        assertEquals("DONATION_NOT_FOUND", ex.getCode());
    }

    @Test
    void delete_existing_ok() {
        when(repo.deleteById(5L)).thenReturn(1);
        service.delete(5L); // no throw
        verify(repo).deleteById(5L);
    }

    // =================================================================
    // object-level authz — branch-leader scoping
    // =================================================================

    @Test
    void create_branchLeader_ownBranch_isAllowed() {
        asBranchLeaderOfBranch(1L);
        stubValidLookups("SPONSOR_DONATION");
        when(repo.nextDonationNoSeq()).thenReturn(11L);
        stubInsertAssigningKeys(120L, OffsetDateTime.parse("2026-07-27T08:00:00Z"));

        DonationCreateDTO req = baseSponsorDto(); // branchId = 1L
        DonationCreateResultDTO res = service.create(req);

        assertEquals(120L, res.getId());
        assertEquals(1L, captureInserted().getBranchId());
    }

    @Test
    void create_branchLeader_otherBranch_isForbidden() {
        asBranchLeaderOfBranch(1L);

        DonationCreateDTO req = baseSponsorDto();
        req.setBranchId(2L); // not their branch

        assertThrows(AccessDeniedException.class, () -> service.create(req));
        verify(repo, never()).insertDonation(any());
    }

    @Test
    void create_branchLeader_withNoBranchAssigned_isForbidden() {
        securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("BRANCH_LEADER");
        when(repo.findBranchIdByUserId(ACTOR_ID)).thenReturn(null); // misconfigured -> fail closed

        assertThrows(AccessDeniedException.class, () -> service.create(baseSponsorDto()));
        verify(repo, never()).insertDonation(any());
    }

    @Test
    void get_branchLeader_ownBranch_isAllowed() {
        asBranchLeaderOfBranch(1L);
        when(repo.findById(5L)).thenReturn(DonationDTO.builder().id(5L).branchId(1L).build());

        DonationDTO dto = service.get(5L);
        assertEquals(5L, dto.getId());
    }

    @Test
    void get_branchLeader_otherBranch_isForbidden() {
        asBranchLeaderOfBranch(1L);
        when(repo.findById(5L)).thenReturn(DonationDTO.builder().id(5L).branchId(2L).build());

        assertThrows(AccessDeniedException.class, () -> service.get(5L));
    }

    @Test
    void update_branchLeader_otherBranch_isForbidden() {
        asBranchLeaderOfBranch(1L);
        when(repo.findById(5L)).thenReturn(DonationDTO.builder().id(5L).branchId(2L).build());

        assertThrows(AccessDeniedException.class, () -> service.update(5L, baseUpdateDto()));
        verify(repo, never()).updateDonation(any());
    }

    @Test
    void list_branchLeader_isForcedToOwnBranch() {
        asBranchLeaderOfBranch(1L);
        when(repo.list(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(repo.countList(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        // Caller asks for branch 2, but a branch leader is confined to branch 1.
        service.list(2L, null, null, null, null, null, null, null, null, 0, 20);

        verify(repo).list(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(20), eq(0));
    }

    // =================================================================
    // list — pagination clamping
    // =================================================================

    @Test
    void list_clampsSize_andComputesOffset() {
        when(repo.list(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(repo.countList(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull()))
                .thenReturn(0L);

        // size 999 -> clamped to 100; page 2 -> offset 2*100 = 200
        DonationPageDTO res = service.list(null, null, null, null, null, null,
                null, null, null, 2, 999);

        assertEquals(100, res.getSize());
        assertEquals(2, res.getPage());
        verify(repo).list(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(100), eq(200));
    }

    @Test
    void list_blankSearch_isNormalizedToNull() {
        when(repo.list(any(), any(), any(), any(), any(), any(), any(), any(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(repo.countList(any(), any(), any(), any(), any(), any(), any(), any(), isNull()))
                .thenReturn(0L);

        service.list(1L, null, null, null, null, null, null, null, "   ", 0, 20);

        verify(repo).list(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(20), eq(0));
    }

    // =================================================================
    // helpers
    // =================================================================

    /**
     * A fully-valid sponsor donation: donationTypeId=3 (SPONSOR), sponsorId=1,
     * branch 1, cash, USD 10.00, valid paidAt.
     *
     * <p>The amount is set here deliberately: the service validates amounts BEFORE
     * lookups/referential checks, so a base without an amount would short-circuit
     * every failure-path test with DONATION_AMOUNTS_INVALID. Tests that need a
     * different amount (KHR-only, mixed) override these fields.
     */
    private DonationCreateDTO baseSponsorDto() {
        DonationCreateDTO req = new DonationCreateDTO();
        req.setDonationTypeId((short) 3);
        req.setSponsorId(1L);
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("10.00"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T09:00:00Z"));
        return req;
    }

    private DonationUpdateDTO baseUpdateDto() {
        DonationUpdateDTO req = new DonationUpdateDTO();
        req.setDonationTypeId((short) 3);
        req.setSponsorId(1L);
        req.setBranchId(1L);
        req.setAmountUsd(new BigDecimal("10.00"));
        req.setPaymentMethodId((short) 1);
        req.setPaidAt(OffsetDateTime.parse("2026-07-24T09:00:00Z"));
        return req;
    }

    /** Make the current principal a BRANCH_LEADER bound to the given branch. */
    private void asBranchLeaderOfBranch(long branchId) {
        securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("BRANCH_LEADER");
        when(repo.findBranchIdByUserId(ACTOR_ID)).thenReturn(branchId);
    }

    private void stubValidLookups(String typeCode) {
        when(repo.countActiveType(anyShort())).thenReturn(1);
        when(repo.countActivePaymentMethod(anyShort())).thenReturn(1);
        when(repo.countBranch(anyLong())).thenReturn(1);
        when(repo.countActiveSponsor(anyLong())).thenReturn(1);
        when(repo.countMember(anyLong())).thenReturn(1);
        when(repo.countActivity(anyLong())).thenReturn(1);
        when(repo.countFile(anyLong())).thenReturn(1);
        when(repo.findTypeCode(anyShort())).thenReturn(typeCode);
    }

    /** Simulates the useGeneratedKeys population MyBatis does on the model. */
    private void stubInsertAssigningKeys(long id, OffsetDateTime createdAt) {
        doAnswer(inv -> {
            DonationModel m = inv.getArgument(0);
            m.setId(id);
            m.setCreatedAt(createdAt);
            return 1;
        }).when(repo).insertDonation(any(DonationModel.class));
    }

    private DonationModel captureInserted() {
        ArgumentCaptor<DonationModel> cap = ArgumentCaptor.forClass(DonationModel.class);
        verify(repo).insertDonation(cap.capture());
        return cap.getValue();
    }
}
