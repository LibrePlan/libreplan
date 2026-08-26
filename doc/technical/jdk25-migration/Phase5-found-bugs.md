# Phase 5 — bugs found, deliberately not fixed (all 9 fixed now, in Phase 6)

Companion to `Phase-5-dao-criteria-punchlist.md`, `Phase-5-STATUS-2026-08-14.md`,
`Phase-5-STATUS-2026-08-15.md`, and `phase6/Phase6-remaining-points-plan.md`. Phase 5's rule for
characterization-tested code was: verify a bug is real, verify it's pre-existing (reproduces
against the pinned pre-Jakarta baseline, not migration-caused), then **preserve it as-is** rather
than silently fix it — fixing behavior while also changing the underlying API is how a "boring,
verifiable migration" turns into an unreviewable one. This file exists so those findings don't
just live in prose buried inside the punch list and get forgotten.

**Status as of 2026-08-15 (Phase 6 execution): all 9 original data-layer bugs are now fixed**
(characterization tests updated to assert the corrected behavior instead of the bug).
`libreplan-business`'s full test suite is **fully green: 1217 tests, 0 failures, 0 errors** — the
one long-standing pre-existing failure (`ScenariosBootstrapTest`, item 9) is gone. Items 1
(`LimitsDAO`) and 3 (`ResourcesSearcher`) each needed a product decision from Jeroen first
(given directly, not guessed at), then were fixed. Item 3's investigation also surfaced and fixed
a related, previously-undiscovered issue: the `Worker`/external "ID" field being mandatory
(item 3a).

Each entry: what's wrong, where, how it was confirmed pre-existing (not migration-caused), and
either the fix that landed or why it's still waiting on a decision.

## Data-layer bugs (found during the DAO Criteria migration, mostly fixed in Phase 6)

### 1. [FIXED, 2026-08-15] `LimitsDAO.save()` was fundamentally broken
`libreplan-business/.../common/daos/LimitsDAO.java`. `Limits.hbm.xml` mapped `Limits` as
`abstract="true"` with **no subclass anywhere** in the codebase — Hibernate's `increment` id
generator can't initialize against an abstract-mapped class, so `save()` always threw
`SQLGrammarException`. Jeroen's answer on what `Limits` is for: it's a cloud-deployment
per-seat-license control (e.g. a maximum-users cap for a hosted instance), and rows are meant to
be set directly in the database by the deployment's DB administrator — no in-app GUI is needed or
planned. That confirmed `Limits` genuinely is meant to hold real, persistable rows (it's just
`type`/`value`, e.g. `('MAX_USERS', 10)`), so `abstract="true"` was simply wrong, not intentional.
Removed it. `save()`/`getAll()`/`getLimitsByType()` are already read by `MachineCRUDController`/
`WorkerCRUDController`/`UserCRUDController` to enforce these limits — that read path was always
fine; only the (previously unused-in-practice, but broken) write path is what this fixed. Added
two characterization tests proving `save()` + `getLimitsByType()`/`getAll()` round-trip correctly
now.

### 2. [FIXED] Three DAO methods always threw due to an unmapped `limitingResource` property
- `MachineDAO.findByNameOrCode` — confirmed zero callers.
- `ResourceDAO.getAllLimitingResources` / `getAllNonLimitingResources`
- `WorkerDAO.findByNameSubpartOrNifCaseInsensitive`

All four filtered on a property named `"limitingResource"` that was never actually mapped on
`Resource`/`Machine` — every call threw `QueryException`. Confirmed zero callers anywhere in the
codebase for all four (repo-wide grep, not just an assumption). **Deleted outright** — interface
methods, implementations, and their "confirms it always throws" characterization tests all
removed. `MachineDAOTest`/`ResourceDAOTest`/`WorkerDAOTest` still pass (10/6/6 tests).

### 3. [FIXED, 2026-08-15] `ResourcesSearcher`: NIF matching was case-sensitive, name/surname matching wasn't
`libreplan-business/.../resources/daos/ResourcesSearcher.java`. `nif` used a plain `like`
(case-sensitive); `name`/`surname` used `ilike` (case-insensitive). Investigated first (see 3a
below): the field labeled "NIF" in the Java/DB layer is shown to users only as the generic
**"ID"** (worker) / **"Company ID"** (external company) — the app never surfaces the word "NIF"
anywhere, and there's no Spanish-tax-ID format/checksum validation on it either, just "must be
unique" (the "not blank" requirement was removed for `Worker`, see 3a). So it isn't a formal
government identifier in practice — it's a free-text code, same in spirit as name/surname. Also
found a *different* existing lookup, `WorkerDAO.findUniqueByNif`, that already does
case-insensitive + trimmed matching — this codebase already treats the field as case-insensitive
elsewhere. Presented this to Jeroen; he agreed case-insensitive makes more sense. Fixed by
wrapping the `nif` predicate in `cb.lower(...)`, same as `firstName`/`surname` right above it.
`ResourcesSearcherTest.byNameMatchesWorkerNifCaseSensitivelyOnly` (which had asserted the old
behavior on purpose) rewritten to `byNameMatchesWorkerNifCaseInsensitively`, asserting the
corrected behavior.

### 3a. [FIXED, 2026-08-15] `Worker.getNif()` ("ID" field) was mandatory — now optional
Prompted by the item-3 discussion: Jeroen didn't like that the worker "ID" field was mandatory,
and asked whether it's used as a unique identifier anywhere for referential integrity. Checked
directly: every entity has its own real, DB-generated surrogate primary key
(`worker.worker_id`, Hibernate `increment` generator) used for all foreign keys throughout the
app — `nif` is never a PK or FK anywhere. Checked the actual Liquibase schema too: `nif` has
**no `NOT NULL` and no `UNIQUE` constraint at the database level at all** (for `worker`; unlike
`external_company`, which does have a DB-level unique constraint on `nif` — not touched here).
The "mandatory" and "must be unique" behavior was 100% a Java-layer thing
(`@NotEmpty` + a custom `@AssertTrue` uniqueness check that already no-ops when the field is
blank). Removed `@NotEmpty` from `Worker.getNif()` and the matching `constraint="no empty"` on
the worker-edit form's textbox. Uniqueness is left as-is — still enforced when a value *is*
entered, just no longer required to enter one.

Fixing this exposed two latent `NullPointerException` risks that a blank ID would now actually
reach in practice (previously only reachable via unusual paths like a WS import bypassing
validation), both hardened as part of this fix:
- `JiraTimesheetSynchronizer.getWorker()` — matched Jira usernames via
  `worker.getNif().equals(nif)`; flipped to `nif.equals(worker.getNif())` (the search parameter,
  `nif`, is never null; a worker's `getNif()` now can be).
- `ResourcePredicate.acceptNif()` — the NIF/ID search filter called `.getNif().toLowerCase()`
  unguarded; added a null check.

Added `WorkerDAOTest.testSaveWorkerWithNoId()` proving a worker with `nif == null` saves and
round-trips correctly. Full `libreplan-business` suite verified green (1217/1217, 0 failures/
errors) after the change.

### 4. [FIXED] `QualityFormDAO.isUnique()` swallowed `InstanceNotFoundException`, inverting its result
`libreplan-business/.../qualityforms/daos/QualityFormDAO.java`. The generic `catch` around the
existence lookup returned `false` ("name is in use") when the lookup actually failed with
"instance not found" — i.e. exactly the case where the name is *not* in use, which should return
`true`. Confirmed zero production callers (only the DAO's own test exercised it), so safe to fix
outright. Narrowed the catch to `InstanceNotFoundException` specifically (return `true`), removed
the swallow-everything-and-return-false fallback. Replaced the one test that asserted the buggy
behavior with three covering the real cases: name unused, name used only by itself (editing),
name used by another `QualityForm`.

### 5. [FIXED] `WorkReportDAO.isAnyPersonalTimesheetAlreadySaved()` was named backwards from what it did
`libreplan-business/.../workreports/daos/WorkReportDAO.java`. The method body was literally
`return list.isEmpty();` — true when **no** personal timesheet has been saved, the opposite of
what the name promised. Audited callers first: found exactly one,
`ConfigurationModel.isAnyPersonalTimesheetAlreadySaved()` (webapp layer, coincidentally same
name), which was already compensating by negating the DAO's result. Renamed the DAO method to
`noPersonalTimesheetsSavedYet()` (behavior unchanged, name now honest) and left the one caller's
negation in place, since the webapp-layer method's own name has the opposite (correct) sense.

### 6. [FIXED, was really the same bug as #7] `WorkReportLineDAO.findFinishedByOrderElementNotInWorkReportAnotherTransaction` always threw
Threw `TransientObjectException` inside its own `REQUIRES_NEW` transaction, every time. Root
cause: it's a brand new Hibernate session, and the Criteria restriction compared directly against
the `OrderElement`/`WorkReport` entity *references* passed in, which belong to a different
persistence context by the time this runs — Hibernate can't tell if they're transient or detached
and throws, even for an entity with a real, persisted id. Fixed by comparing **ids** instead
(`root.get("orderElement").get("id")`, the same idiom `findByOrderElement()` already used two
methods up in this same class) — reading an id needs no session at all, so the whole
transient-vs-detached ambiguity never comes up.

### 7. [FIXED — turned out to be caused by #6] `WorkReportLine.isOrderElementFinishedInAnotherWorkReportConstraint()` always threw when `finished=true`
This `@AssertTrue` bean-validation method's *only* job is to call the DAO method from item 6.
Hibernate Validator reported the resulting `TransientObjectException` as `HV000090: Unable to
access isOrderElementFinishedInAnotherWorkReportConstraint` — which reads like a
reflection/accessibility bug in the validator method itself, but is just Hibernate Validator's own
wrapping of whatever the constraint method throws. Fixing item 6 fixed this automatically; no
separate change needed. Verified by actually saving a `finished=true` `WorkReportLine` end to end
— it now succeeds, and the "no other finished line exists" / "a finished line exists" branches of
both `isFinished()` and this constraint are now both testable (the positive branch was previously
unreachable, since you could never get a finished line saved in the first place).

### 8. [FIXED — real production bug, not just a test artifact] `ScenariosBootstrapTest` FK-violation cleanup bug
`loadBasicDataAssociatedWithCurrentOrders` failed with a foreign-key violation because the test's
cleanup routine deleted `Order`s directly without first deleting their referencing `OrderSyncInfo`
rows (`order_sync_info.order_element_id` has a non-cascading FK back to `order_table`). Checking
whether real order-deletion code paths shared the gap (as flagged as worth doing) found that
**they did**: `OrderModel.removeOrderFromDB()` and `ScenarioModel.remove()`'s per-scenario order
cleanup both called `orderDAO.remove(order.getId())` directly, with the identical gap — meaning
deleting any order that had ever been synced with an external connector (Tim, Jira, ...) would
fail with a raw constraint-violation exception in production, not just in this one test. Added
`IOrderSyncInfoDAO.findByOrder(Order)` and used it to delete every `OrderSyncInfo` for an order
immediately before removing the order, at all three call sites (both production ones plus the
test). `libreplan-business`'s full suite is now fully green — this was the one remaining error.

### 9. [not a bug — no action taken] `UnitTypeDAOTest` — one test fails independent of the migration
One of `UnitTypeDAOTest`'s two tests for `isUnitTypeUsedInAnyMaterial()` fails due to an unrelated,
pre-existing gap: `UnitTypeBootstrap.getDefaultUnitType()` NPEs in this test's setup. Confirmed
this is not a regression — fails the same way on the pinned baseline. Left alone; not part of the
7 fixes above, and not blocking anything.

## Known quirks (not necessarily bugs, but easy to trip over later)

These aren't "wrong" so much as surprising, and are worth knowing about before touching nearby
code:

- **`isNewObject()` bookkeeping**: several entities rely on `BaseEntity.getVersion()` returning
  `null` while `newObject` is `true`, and require an explicit
  `dontPoseAsTransientObjectAnymore()` call once genuinely persisted. Hibernate 6 enforces this
  much more strictly than Hibernate 5 did. Known production call sites were fixed during Phase 5
  (`CalendarBootstrap`/`PredefinedCalendarExceptionTypes`, `TaskSource.RealPersistence.save()` —
  see `Phase-5-STATUS-2026-08-14.md` §3) and Phase 6 (`OrderModelTest`'s `ExternalCompany`
  helper), but this pattern recurred often enough across unrelated code during the migration that
  other, not-yet-exercised call sites may still have it — see `libreplan-webapp`'s Cluster A below
  for a variant of this same family that turned out NOT to be fixable with the usual flip. If a
  new `TransientObjectException`/duplicate-insert-vs-update surprise shows up on an
  `increment`-id entity, check this first.
- **`OrderElementDAO`**: `WorkReport.setOrderElement()` silently no-ops unless
  `WorkReportType.orderElementIsSharedInLines` is set. Confirmed intentional-if-surprising
  behavior, not a bug — documented here only so it doesn't get "fixed" by accident later.
- **`TaskElementDAO`/entity-reference-as-Criteria-param**: passing an entity reference (rather
  than its id) as a Criteria/JPA query parameter, when that entity might belong to a different
  Hibernate session than the query, throws `TransientObjectException` even when it has a real,
  valid id — Hibernate can't tell transient from detached in that situation. Recurred at more than
  one call site during this migration (`TaskElementDAO` during Phase 5,
  `WorkReportLineDAO` item 6 above during Phase 6) — the fix is always the same: compare by
  `.get("id")` instead of the entity reference. Worth checking any DAO method that's `REQUIRES_NEW`
  and takes an entity parameter for this pattern proactively.

## `libreplan-webapp`'s own test suite (found 2026-08-15)

Running `libreplan-webapp`'s test suite for the first time this migration (it had only been
compiled and manually browser-tested before) surfaced two more findings:

### 10. [FIXED] `libreplan-webapp-spring-config-test.xml` had its own incomplete, duplicated `sessionFactory` bean
`libreplan-webapp/src/test/resources/libreplan-webapp-spring-config-test.xml` hand-duplicates the
`mappingResources` list that belongs in `libreplan-business-spring-config.xml` (the file's own
`FIXME` comment already flags the duplication as a bad idea). This duplicate list was missing 4
entries the real config has: `JobSchedulerConfiguration.hbm.xml`, `Limits.hbm.xml`,
`ExpenseSheets.hbm.xml`, `Logs.hbm.xml`. The missing `JobSchedulerConfiguration` mapping meant the
`schedulerManager` bean's init method threw `IllegalArgumentException: Not an entity` during
`ApplicationContext` startup — which, because Spring's test context caching treats a failed
context load as permanently failed for every test class sharing that same
`MergedContextConfiguration`, cascaded into **201 of 222 `libreplan-webapp` tests erroring**.
Fixed by adding the 4 missing `<value>` entries.

### 11. [PARTIALLY FIXED] The remaining `libreplan-webapp` test failures — mpxj fixed, three DAO/entity-graph clusters left

**mpxj (`OrderImporterTest`) — [FIXED, Phase 6.1.2].** `NoClassDefFoundError:
javax/xml/bind/JAXBException` from `net.sf.mpxj.reader.ProjectReaderUtility.getProjectReader`.
`net.sf.mpxj:mpxj:9.0.0` reflectively needs `javax.xml.bind`, gone from the JDK since Java 11.
Bumping mpxj itself was evaluated and rejected — 13.0.0 (the last release on the old package name)
already rewrote its calendar/date API from `java.util.Date` to `java.time.LocalDate`/
`LocalDateTime`, and 14.0.0+ renames the whole `net.sf.mpxj` package to `org.mpxj` — a real,
behavior-sensitive rewrite of `MPXJProjectFileConverter.java`'s own calendar-conversion logic, not
a version bump, so out of scope here (see the comment on the `mpxj` dependency in `pom.xml`).
Fixed instead with a scoped `javax.xml.bind:jaxb-api:2.3.1` +
`com.sun.xml.bind:jaxb-impl:2.3.3` compatibility shim, purely for mpxj's benefit — verified
`org.libreplan.*` still imports nothing from `javax.xml.bind` anywhere.

**17 remaining failures — three distinct clusters, not one bug** (the original guess of "one
pattern, 17 tests" was wrong):

- **Cluster A — `OrderModelTest` (10) + `ChartFillerTest.testBAC` (1) — real production bug,
  diagnosed, NOT yet fixed.** Traced with a temporary debug print (removed after use):
  `order.getId()` is already non-null **before `orderDAO.save(order)` is ever called**. Root
  cause: `PlanningStateCreator.setupScenario()` calls `Scenario.addOrder(order)`, which does
  `orders.put(order, orderVersion)` on `Scenario.orders` — mapped `cascade="save-update"`. `Order`
  uses the `increment` id generator (assigns ids client-side, synchronously, the moment Hibernate
  schedules an insert — not deferred to flush). Adding a brand-new `Order` to this cascading
  collection mints it a real id **immediately**, even inside a nominally "read-only" transaction
  (`IAdHocTransactionService.runOnAnotherReadOnlyTransaction`, used by both the test's
  `createPlanningStateFor()` helper and production's `PlanningStateCreator.createOn()`) that never
  actually flushes anything. The `Order` walks out of that transaction with a live id but no real
  backing DB row; when `orderModel.save()` later tries to really save it in a genuinely new
  transaction, Hibernate treats it as a detached reattach, checks `getVersion()` for optimistic
  locking, gets `null` (correctly, since nothing was ever flushed) → `PropertyValueException:
  Detached entity with generated id 'N' has an uninitialized version value 'null'`. **Not fixable
  with the usual `dontPoseAsTransientObjectAnymore()` trick** — the version genuinely doesn't
  exist. The real fix has to stop the id from leaking out of the throwaway
  planning-state-computation step in the first place — either change `Scenario.orders`' cascade
  behavior, or rework `PlanningStateCreator.setupScenario()`/`createOn()`. Both directions touch
  core Order-versioning/Scenario code exercised everywhere in the app — needs its own dedicated,
  carefully-tested fix. Since `PlanningStateCreator.createOn()` is real production code (called
  whenever the order-planning view opens for a new order), this is likely a genuine production
  defect surfaced by Hibernate 6's stricter checks, not merely a test-only gap — **highest
  priority of the three clusters** for that reason (explains 11 of the 17 failures too).
  A real, independently-correct fix landed alongside this diagnosis:
  `OrderModelTest.createValidExternalCompany()` now calls
  `externalCompany.dontPoseAsTransientObjectAnymore()` after saving it (same established pattern
  as `TaskSource.RealPersistence.save()`) — necessary once Cluster A itself is fixed, but doesn't
  turn any test green by itself since Cluster A's `Order`-level issue gates the same tests one
  step further down.
- **Cluster B — `EmailTest` (2) — test-fixture-only, not yet fixed.** `createTaskGroup()`/
  `createTask()` build a `TaskGroup`/`Task` whose `TaskSource` references an **EasyMock mock** of
  `SchedulingDataForVersion`/`OrderVersion`, never actually persisted — only `TaskGroup`/`Task`
  get a real `taskElementDAO.save(parent)` call. Hibernate 6 validates the cascade more strictly
  than Hibernate 5 did and throws on the mocked reference. Needs the test fixture rewritten to use
  real DAO-persisted entities for that part of the graph instead of mocks — not a one-line fix.
- **Cluster C — `OrderElementServiceTest` (4) — a third, separate path, not yet traced.**
  `orderElementService.addOrders(orderListDTO)` (the SOAP/REST order-import endpoint) fails with
  `TransientObjectException: ... unsaved transient instance of 'Criterion'`, via
  `OrderElementConverter`/DTO-to-entity conversion. Not yet traced to a specific line.

## Cosmetic, explicitly deferred by Jeroen

- **Earned Value tab**: the checkbox/legend rows render visibly taller than the pre-migration
  widget's. Root-caused as far as "ZK's sapphire skin doesn't give these the same compact styling
  the old widget's own CSS did" but not fixed — Jeroen is already migrating this app from the
  Sapphire to the Breeze ZK theme in a separate branch and wants to fold this into that work rather
  than patch Sapphire-specific CSS now.

## Resolved since this file was first written

- `pom.xml`'s `maven.compiler.release` cap at 21 was re-tested once Spring 6.2.19 landed and
  **bumped to 25 for real** (Phase 6.3) — full reactor compiles clean, test results byte-for-byte
  identical to the release-21 run. See `Phase-5-STATUS-2026-08-15.md` §6 and the updated comment
  in `pom.xml`.
