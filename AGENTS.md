# AGENTS.md — tnal_youth_backend

> **Read this first, every session, before analyzing or changing code.**
> It is the fast-path context so you don't re-derive the whole repo. If anything
> here conflicts with the actual source, **the source wins** — update this file
> in the same PR that changes the behaviour.

---

## 1. What this is

Spring Boot backend for a youth organisation (CYNA / "Tnal Youth"). Feature
modules under `org.example.tnal_youth_backend`:

`authentication`, `activity`, `branch`, `dashboard`, `document`, `history`,
`member`, `notification`, `donation`.

**Stack (verified):**
- Java **21**, Spring Boot 3.x, Spring Security (JWT, method security).
- **MyBatis** (annotation mappers) — *not* JPA for the notification/donation modules.
- **PostgreSQL** + **Flyway** migrations (`src/main/resources/db/migration`, V1..V23).
- Build: Maven wrapper (`./mvnw`). CI: `.github/workflows/ci.yml` (Java 21).
- Tests: JUnit 5, Mockito 5 (inline static mocking available by default),
  `spring-boot-starter-test`, `@WebMvcTest` slices.

---

## 2. Module status

| Module | State |
|---|---|
| notification | **Done** — reference implementation. Copy its patterns. |
| donation | **Done, shippable** (2026-07-27). Service implemented; controller/DTOs/repo/migrations pre-existed. Full suite green under Docker Postgres — 98/98 incl. contextLoads (Flyway V1..V23 applied cleanly). |
| authentication | Production hardening added in V309: the designated admin must reactivate by email OTP, all accounts using the compromised demo password are disabled, and their refresh tokens are revoked. |
| others | Pre-existing, out of current scope. |

The donation `DonationService` was previously an **empty stub** while the
controller already injected it and called 6 methods — the module could not
compile. That gap is now filled.

---

## 3. Project-wide conventions (do not deviate without reason)

- **MyBatis has NO `map-underscore-to-camel-case`.** Every SELECT column MUST be
  aliased explicitly (`n.donation_no AS donationNo`). Annotation mappers cannot
  share `<sql>` fragments, so the enriched SELECT is intentionally duplicated
  across `findById` / `list`; keep them identical.
- **Models are plain POJOs** (`@Data @Builder`), not JPA entities. Column mapping
  lives in the repo SQL.
- **Business errors** → throw `common.exception.BusinessException(code, message)`.
  `common.exception.GlobalExceptionHandler` maps:
  - `BusinessException` → **400** with `errorCode` (except code `UNAUTHENTICATED` → **401**).
  - `MethodArgumentNotValidException` → **400** `VALIDATION_FAILED`.
  - `DataIntegrityViolationException` → **400** `DATA_INTEGRITY_VIOLATION`.
  - `AccessDeniedException` → **403** `FORBIDDEN`; `AuthenticationException` → **401**.
- **Response envelope**: `common.response.ApiResponse<T>` (`ok(...)` / `error(...)`),
  UTC timestamp, `@JsonInclude(NON_NULL)`.
- **Current user id**: `security.SecurityUtils.getCurrentUserId()` (throws
  `UNAUTHENTICATED` if no principal). Note there are two other `SecurityUtil`
  classes in `authentication.*` — use `security.SecurityUtils` for user id.
- **Time**: inject the `Clock` bean from `config.TimeConfig` (`utcClock`,
  `Clock.system(UTC)`). Do not call `Instant.now()`/`LocalDate.now()` unbound —
  makes tests deterministic.
- **Idempotency pattern** (see V22 notifications, V23 donations): optional
  client UUID, pre-check `findIdBy...ClientRequestId`, partial unique index
  catches concurrent dupes → `DATA_INTEGRITY_VIOLATION`.
- **Blank→null normalisation** before persisting so `BTRIM(...) <> ''` CHECKs are
  never violated (`normalizeToNull`).
- **Money** = `BigDecimal` end-to-end. Never `double`. Amounts NUMERIC(14,2),
  exchange rate NUMERIC(14,4).

---

## 4. Donation module — contract cheat-sheet

**Base path:** `/api/donations`  ·  Controller: `donation/controller/DonationController.java`

**Authz (method-level `@PreAuthorize` + service-level row scoping):**
- create / get / list / summary / update → **STAFF** = `hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')`.
- delete → **ADMIN only**.
- **Object-level (branch) scoping, enforced in `DonationService` (not the annotation):**
  a `BRANCH_LEADER` is confined to their own branch (`users.branch_id`, V14) for
  create/get/update, and list/summary are force-narrowed to it. Cross-branch →
  `AccessDeniedException` → **403 FORBIDDEN**. `ADMIN`/`SECRETARY` are org-wide.
  A branch leader with no `branch_id` **fails closed** (403).

**Money rule (server-authoritative, never trust client `totalAmountUsd`):**
```
totalUsd = amountUsd + (amountKhr / exchangeRateKhrPerUsd)
```
KHR→USD carried at 6 dp, final result HALF_UP to 2 dp.
Pinned example (in `http/donations.http` #3): `25.00 + 100000/4100 → 49.39`.
Exchange rate is required **iff** `amountKhr > 0`.

**Exchange rate normalisation:** the rate is persisted only when `amountKhr > 0`;
a rate sent with a zero-KHR donation is stored as `NULL`.

**`paidAt` bound:** `@PastOrPresent` — future timestamps are rejected; backdating
is allowed.

**Donor source:** EXACTLY one of `memberId` / `sponsorId` / `donorName`
(DB `chk_donation_source`; pre-validated in service).

**Type-specific required fields** (by `donation_types.code`):
- `ACTIVITY_DONATION` → `activityId` required.
- `MONTHLY_DONATION` → `donationPeriod` required.

**Donation number:** minted server-side as `DON-{yyyyMMdd}-{seq:06}` from
`donation_no_seq` (V23), UTC date. e.g. `DON-20260727-000042`.

**Idempotency:** optional `clientRequestId` (UUID). Sequential replay from the
same recorder returns the original; concurrent dupes blocked by
`uq_donations_recorder_client_request` (partial, `WHERE client_request_id IS NOT NULL`).

**Editing & concurrency:**
- **Audit:** `donations.updated_by` (V24) records the last editor; set on every
  `PUT`, exposed as `updatedBy`/`updatedByName` on reads. NULL until first edit.
- **Optimistic locking (optional):** send `expectedUpdatedAt` (the `updatedAt`
  you read) on update; a stale token yields `DONATION_UPDATE_CONFLICT` instead of
  a silent overwrite. Omit it → last-writer-wins (backward compatible).

**Validation order in `DonationService` (matters for tests):** donor source →
**amounts** → exchange rate → lookups (type, payment method, branch) →
referential (member, sponsor, activity, receipt) → type-specific required
fields. A donation missing a valid amount short-circuits at `DONATION_AMOUNTS_INVALID`
BEFORE any lookup check — so a failure-path unit test must supply an otherwise
valid amount to reach the code it asserts.

**Service error codes (all → 400 unless noted):**

| Code | Cause |
|---|---|
| `DONATION_SOURCE_INVALID` | not exactly one donor source |
| `DONATION_AMOUNTS_INVALID` | negative, or both amounts zero |
| `DONATION_EXCHANGE_RATE_REQUIRED` | KHR > 0 but no rate |
| `DONATION_EXCHANGE_RATE_INVALID` | rate ≤ 0 |
| `DONATION_TYPE_INACTIVE` | type missing/inactive |
| `DONATION_PAYMENT_METHOD_INACTIVE` | payment method missing/inactive |
| `DONATION_BRANCH_NOT_FOUND` | branch missing |
| `DONATION_MEMBER_NOT_FOUND` | member missing |
| `DONATION_SPONSOR_NOT_FOUND` | sponsor missing/inactive |
| `DONATION_ACTIVITY_NOT_FOUND` | activityId points nowhere |
| `DONATION_RECEIPT_NOT_FOUND` | receiptFileId points nowhere |
| `DONATION_ACTIVITY_REQUIRED` | ACTIVITY_DONATION without activityId |
| `DONATION_PERIOD_REQUIRED` | MONTHLY_DONATION without donationPeriod |
| `DONATION_NOT_FOUND` | get/update/delete on missing id |
| `DONATION_UPDATE_CONFLICT` | optimistic-lock mismatch: `expectedUpdatedAt` didn't match current row |
| `DONATION_INSERT_FAILED` | insert returned no generated key |

**Seed reference (V13):** donation_types `MONTHLY_DONATION`(1),
`ACTIVITY_DONATION`(2), `SPONSOR_DONATION`(3). payment_methods
`CASH`(1),`ABA`(2),`ACLEDA`(3),`WING`(4),`TRUEMONEY`(5),`OTHER`(6).

**Schema of record:** `V8__create_donation_tables.sql` (table + CHECKs + indexes),
`V23__donation_support.sql` (`donation_no_seq` + `client_request_id` + partial unique index),
`V24__donation_audit.sql` (`updated_by` column + FK + index). **V1–V23 immutable.**

---

## 5. Testing

**Donation test inventory (all green, verified 2026-07-27):**

| Class | Count | Scope |
|---|---|---|
| `DonationServiceTest` | **34/34** | mock `DonationRepo` + fixed `Clock` + `mockStatic(SecurityUtils)` (`getCurrentUserRole` stubbed for branch-leader cases). Money math (49.39/10.00/9.76), every error code, idempotency, not-found, edit-conflict, branch scoping, pagination clamping. No DB. |
| `DonationControllerSecurityTest` | **18/18** | `@WebMvcTest`, JWT filter excluded, `AuthenticatedSecurityConfig` mirrors prod `anyRequest().authenticated()` + `@EnableMethodSecurity`. STAFF vs MEMBER vs anonymous; delete is ADMIN-only (SECRETARY/BRANCH_LEADER 403). No DB. |
| `DonationControllerTest` | **16/16** | `@WebMvcTest`, permit-all chain. `ApiResponse` envelope, `totalAmountUsd` echo, `@NotNull`/UUID/`@DecimalMin` validation 400s, `BusinessException`→errorCode, param binding + defaults. No DB. |

- **Manual smoke:** `http/donations.http` (IntelliJ / VS Code REST Client).
  Login `admin1@gmail.com` / seed password `12345`, server port `8081`.
- **Jackson note:** `BigDecimal` money fields serialize as JSON **numbers** (not
  strings) — controller-test assertions use `.value(49.39)`. If a future Jackson
  config switches to string scale-preservation, update those to `.value("49.39")`.

**Environment gotcha:** `TnalYouthBackendApplicationTests.contextLoads` is a full
`@SpringBootTest` that boots Flyway against Postgres on `localhost:5433`. With no
DB running it fails with `Connection refused` — this is environmental, unrelated
to the donation code, and pre-dates this work. The donation unit + web-slice
tests need NO database (repo/clock/security are mocked; `@WebMvcTest` excludes the
JWT filter). Start the DB (see `docker-compose.yml`) before running the full suite,
or scope runs with `-Dtest=Donation*Test`.

**Commands:**
```bash
./mvnw test                                   # full suite
./mvnw -Dtest=DonationServiceTest test        # service unit only
./mvnw -Dtest=DonationControllerSecurityTest test
```

Follow the existing test conventions: strict Mockito by default (use
`@MockitoSettings(strictness = LENIENT)` only when a shared valid-path stub
helper over-stubs), `@WithMockUser(roles = "...")` for authz, `csrf()` on
mutating requests (production disables CSRF; tests supply it belt-and-braces).

---

## 6. Working agreement (how the repo owner wants changes)

- **Evidence over assumption.** Read the real file before editing; if a tool read
  fails or returns empty, say so and retry — never infer file contents from
  filenames or memory.
- **Smallest safe complete change.** Match surrounding conventions; don't
  introduce new patterns/libraries without cause.
- **Every delivery reports:** Changed / Verified / Risks / Next step. Don't claim
  "done"/"tested" without running it — if it wasn't run here, say so.
- **When you add/adjust a module contract** (error code, money rule, authz,
  migration), update §4 of this file in the same change.
