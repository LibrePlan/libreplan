# Phase 5 (Jakarta EE migration) — status report

Branch: `phase5-jakarta-migration`.

Revised 2026-08-14 (second pass, same day): the Criteria API migration is complete, all
Hibernate-6-caused test failures are fixed, and `libreplan-business`'s full test suite now matches
the pre-migration (Hibernate 5) baseline exactly. A new, separate finding closes this report:
`libreplan-webapp` doesn't compile, for reasons unrelated to Hibernate/Criteria.

Context: this is Phase 5 of a larger JDK 11→25 + Jakarta EE migration for LibrePlan. Phases 1–4
(JDK version bump, javax→jakarta namespace rename, CI workflow switch, dependency tree audit) are
done and merged. Phase 5's specific task: Hibernate 6 removed the legacy `org.hibernate.Criteria`
API entirely, so every DAO using it had to be rewritten to JPA Criteria API
(`jakarta.persistence.criteria.*`).

## 1. The Criteria API rewrite — COMPLETE, repo-wide

All 51 DAO files in `libreplan-business` (plus the shared `GenericDAOHibernate`/`IntegrationEntityDAO`
base classes) and the one remaining file in `libreplan-webapp`
(`libreplan-webapp/src/test/java/org/libreplan/web/test/ws/workreports/WorkReportServiceTest.java`)
have been rewritten from `org.hibernate.Criteria`/`org.hibernate.criterion.*` to
`jakarta.persistence.criteria.*`. Repo-wide grep confirms zero remaining hits anywhere in the repo:

```
grep -rln "org.hibernate.Criteria\|org.hibernate.criterion" libreplan-business/src libreplan-webapp/src
```

Methodology used for every DAO file (per explicit instruction — no shortcuts): read the DAO's
Criteria usage → check/add characterization tests → run those tests against the **old**
pre-migration implementation in a pinned git worktree to establish a correctness baseline → copy
tests to the main tree → rewrite the DAO to JPA Criteria → compile-verify. The per-file punch list
is `Phase-5-dao-criteria-punchlist.md`, alongside this file.

The pinned-baseline branch is `characterization-tests-pre-jakarta`, at commit `427ce2297` (the last
commit before this migration started). A worktree against it currently exists at
`/tmp/claude-1000/.../463a1e39-.../scratchpad/pre-jakarta-worktree` from an earlier session — that
path is session-scoped and may be gone by the time this is read; recreate with:
`git worktree add <some-durable-path> characterization-tests-pre-jakarta`.

## 2. `libreplan-business` test suite — matches the Hibernate 5 baseline exactly

`mvn -pl libreplan-business test`:

```
Tests run: 1215, Failures: 0, Errors: 1, Skipped: 0
```

The same command against the pinned pre-Jakarta baseline gives the identical result — same count,
same single failing test, same exception. The migration is now behavior-preserving: every test that
passed before still passes, and the one that didn't, still doesn't, for the same reason.

### 2.1 The one remaining error is pre-existing, not migration-caused

`ScenariosBootstrapTest.loadBasicDataAssociatedWithCurrentOrders` fails with:

```
ConstraintViolationException: update or delete on table "order_table" violates foreign key
constraint "fk...y" on table "order_sync_info" - Key (order_element_id)=(N) is still referenced
from table "order_sync_info".
```

`loadRequiredData()`'s test-cleanup routine deletes `Order`s directly
(`orderDAO.remove(order.getId())`) without first deleting their `OrderSyncInfo` rows. `Order` has no
mapped association to `OrderSyncInfo` (it's a separate top-level entity, not cascaded), so nothing
does this automatically. Verified via the pinned baseline: this reproduces identically on Hibernate
5 (full-suite run there: 1215 tests, 0 failures, 1 error, same test, same exception). Left as-is,
undocumented in code beyond this report, matching the project's policy of preserving
characterization-tested pre-existing bugs rather than silently fixing them.

### 2.2 Bugs found via characterization testing, fixed because Hibernate 6 changed their nature

These are different from §2.1: they weren't silently-broken-forever pre-existing bugs but places
where Hibernate 6 changed observable behavior around a real, load-bearing defect. Each was verified
against the pinned baseline before deciding whether to fix:

- **`EmailNotificationDAO.deleteAllByType`** compared the enum-typed `type` column against
  `enumeration.ordinal()` (an int) instead of the enum itself. Hibernate 5's legacy Criteria API
  rejected this client-side with `ClassCastException`, even though `type` is stored as the enum's
  ordinal in the database and the comparison is valid at the SQL level. Hibernate 6's JPA Criteria
  API doesn't perform that client-side check, so the method now runs and returns the correct
  result — a genuine fix, confirmed by checking the column's mapping (`org.hibernate.type.EnumType`
  without `useNamed`, i.e. ordinal storage). Cleaned up to compare against the enum directly
  (identical SQL, clearer code) and the test rewritten to verify the real behavior instead of
  asserting the old crash.

## 3. Unrelated compile/runtime blockers fixed along the way (all in `libreplan-business`)

None of these are Criteria-API work, but each was blocking `mvn test`/`test-compile` or causing test
failures, and had to be fixed to make progress:

- `GenericDAOHibernate.lock()` used `LockMode.UPGRADE`, removed in Hibernate 6 → changed to
  `LockMode.PESSIMISTIC_WRITE`.
- `HibernateDatabaseModificationsListener` had a typo'd override `requiresPostCommitHanding`
  (missing the second "l") that silently failed to implement the real interface method
  `requiresPostCommitHandling` → fixed the typo.
- `EffortDurationType`/`ResourcesPerDayType` (custom Hibernate `UserType` implementations) used the
  pre-6.6 `UserType` interface shape. Rewrote both to Hibernate 6's generic `UserType<J>` interface,
  reading/writing the JDBC value directly (the idiomatic Hibernate-6 pattern for hbm.xml-mapped
  custom types).
- `org.jadira.usertype:usertype.core` (third-party Joda-Time↔Hibernate type library) is incompatible
  with Hibernate 6. Removed the dependency and replaced its one usage
  (`WorkReportLine.clockStart`/`clockFinish`, millis-of-day storage) with a hand-written
  `UserType<LocalTime>` —
  `libreplan-business/src/main/java/org/libreplan/business/workingday/hibernate/LocalTimeAsMillisIntegerType.java`.
- `jakarta.persistence-api` bumped from 3.0.0 to 3.1.0 (Hibernate 6.6.55 needs `GenerationType.UUID`,
  added in Jakarta Persistence 3.1).
- `hibernate.dialect` Maven properties updated: `PostgreSQL82Dialect` → `PostgreSQLDialect`,
  `MySQL5InnoDBDialect` → `MySQLDialect`.
- `LDAPConfiguration` had two accessor methods for the same field
  (`getLdapSavePasswordsDB()`/`isLdapSavePasswordsDB()`); Hibernate 6's stricter bean-introspection
  rejects that ambiguity. Removed the dead, unused one.
- **The full-test-suite blocker** (`AssertionError` in `AbstractEntityPersister.prepareMappingModel`,
  affecting every test via the shared `sessionFactory` bean): `Orders.hbm.xml` mapped
  `OrderElement.externalCode` twice — once correctly on `OrderElement`, once as an orphaned leftover
  on the `Order` joined-subclass pointing at `order_table.external_code`, a column that's been
  written identically to `order_element.external_code` on every save since 2010 (confirmed via the
  Hibernate 5 SQL log). Hibernate 5 silently tolerated the duplicate property mapping; Hibernate 6's
  stricter fetchable-index bookkeeping does not. Fixed by removing the orphaned mapping and adding a
  Liquibase backfill (`db.changelog-1.6.xml`) for any pre-2010 rows where the two columns diverge.
- **The dominant test-failure cluster (21 of the original 26 failures)**: entities using Hibernate's
  `increment` id generator (`CalendarExceptionType`, `TaskSource`/`TaskElement`/`TaskGroup`,
  `SubcontractedTaskData`, `SubcontractorDeliverDate`, `BaseCalendar`) all rely on
  `BaseEntity.getVersion()` deliberately returning `null` while a `newObject` flag is `true` — this
  is what lets Hibernate treat a not-yet-flushed entity (which already has an id, since `increment`
  assigns it client-side, synchronously) as transient rather than ambiguous. The app's own contract
  requires calling `dontPoseAsTransientObjectAnymore()` once an entity has really been persisted and
  the same instance needs to survive into a later transaction/session. This was done inconsistently:
  - `CalendarBootstrap`/`PredefinedCalendarExceptionTypes` (production code): the enum cached one
    mutable `CalendarExceptionType` instance and reused it, unflipped, across every bootstrap call —
    each independent test transaction re-attempted to save the same already-id-bearing instance.
    Fixed by making `getCalendarExceptionType()` build a fresh instance every call.
  - `TaskSource.RealPersistence.save()` (production code): never flipped the `TaskSource` it saved.
    Fixed by flipping it right after the save (not the `Task`/`TaskGroup` it points to — callers
    rely on that one still posing as new, see `TaskElementDAOTest.afterSavingTheVersionIsIncreased`).
  - Several test fixtures (`SubcontractorCommunicationDAOTest`, `TaskElementDAOTest.flushAndEvict`,
    `BaseCalendarDAOTest`, `ExternalCompanyDAOTest`) evicted-and-later-reused entities without the
    flip, including cascade-evicted children. Fixed per file; `TaskElementDAOTest.flushAndEvict` now
    flips a `TaskGroup`'s children recursively since eviction cascades to them.
  - One test (`ExternalCompanyDAOTest.testUniqueCompanyNameCheck`) turned out to rely on the *bug*:
    it reused one entity instance across two transactions expecting the second save to look like a
    duplicate-name insert. Once the flag is correctly flipped, Hibernate recognizes it as an update
    of the same row instead — which is more correct, but no longer exercises the test's intent.
    Rewritten to use two distinct entities with the same name (matching the sibling test
    `testUniqueCompanyNifCheck`, which already did this correctly).
  - One instance (`BaseCalendarDAOTest.notAllowRemoveCalendarWithChildrenInOtherVersions`) needed
    the flip but wasn't fixed by it alone — see §3 continuation below for the remaining, distinct
    cause found in that same test.
- **`BaseCalendarDAOTest.notAllowRemoveCalendarWithChildrenInOtherVersions`** (separate from the
  cluster above, found in the same test after fixing the flag issue): Hibernate 6 added a pre-flush
  consistency check (`ACTION_CHECK_ON_FLUSH`) that now catches, client-side, a `cascade="none"`
  many-to-one reference to an entity that's queued for deletion in the same flush
  (`CalendarData.parent`, pointing at a `BaseCalendar` being removed while another calendar's older,
  non-cascaded version still references it). Verified against the baseline: Hibernate 5 let this
  same scenario reach the database and get rejected there as `DataIntegrityViolationException`; the
  underlying invariant ("can't remove a calendar still referenced by an older calendar version") is
  still enforced under Hibernate 6, just via an earlier check and a different, untranslated exception
  type (Hibernate's own `SessionImpl.doFlush()` wraps the resulting `TransientObjectException` in a
  plain `IllegalStateException` that Spring's exception translator doesn't recognize). Test's
  expected exception type updated accordingly.
- **`ResourceAllocationDAO`'s scenario-scoped allocation query**: HQL used
  `orderElement.schedulingDataForVersion[version]` (bracket map-index syntax on a
  `Map<OrderVersion, SchedulingDataForVersion>`) together with `, OrderVersion as version` in the
  same query. Hibernate 6's HQL translator desugars a map-index expression whose key is itself a
  from-clause alias into an *implicit* join reusing that alias, colliding with the explicit one —
  reproducibly, under any alias name (renaming to `orderVersion` reproduced the identical collision
  with the new name). Rewritten to avoid bracket map-index syntax entirely: `join
  orderElement.schedulingDataForVersion as versionEntry`, then `KEY(versionEntry)` in the `WHERE`
  clause instead.

## 4. New finding: `libreplan-webapp` does not compile — unrelated to Hibernate/Criteria

`libreplan-webapp` has never been compiled or run against this migration's dependency set. Doing so
now (`mvn -pl libreplan-webapp compile`) fails with **192 errors across 14 files**, all unrelated to
Hibernate or Criteria:

- `org.libreplan.importers.TimSoapClient` — uses `javax.xml.soap.{SOAPMessage,SOAPException}`, part
  of Java EE's SAAJ API, removed from the JDK itself since Java 11. Needs an explicit
  `jakarta.xml.soap-api` + implementation dependency (e.g. `com.sun.xml.messaging.saaj:saaj-impl`).
- A cluster of files (`ChartFiller`, `CutyPrint`, `ReportAdvancesController`,
  `SubcontractedTasksController`, `JiraSynchronizationController`) implement
  `org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler`, whose `handle(...)` method
  signature is still typed to `javax.servlet.http.HttpServletRequest`/`HttpServletResponse` in this
  version of the `ganttz`/ZK library, while the implementing code has already been migrated to
  `jakarta.servlet.*`. This is a genuine interop gap between an old third-party jar and the
  jakarta.servlet rename, not something fixable purely in application code — needs either a newer
  `ganttz` build, or an adapter/shim layer.
- `BandboxSearch`/`Finder`/`JiraSynchronizationController` — `cannot find symbol: DataBinder` /
  `objectToString(Object)`: looks like a ZK API surface change (method removed/renamed) between the
  ZK version this code targets and `zk:10.3.0.1-jakarta`, the version actually on the classpath.
- `DateConverter`/`DateTimeConverter`/`LocalDateConverter`/`TimeConverter`/`ChartFiller` — "method
  does not override or implement a method from a supertype": a ZK `Converter`/callback interface's
  generic signature appears to have changed.
- `PersonalTimesheetController` — `Assert.notNull(OrderElement)`: Spring 6's
  `org.springframework.util.Assert.notNull` no longer has a one-argument overload; needs a message
  argument.

None of this touches Hibernate, Criteria, or entity mappings — it's a second, independent body of
migration work (Jakarta Servlet interop with the ZK/ganttz library, a Java-11-removed API, and a
Spring 6 API signature change), bounded to 14 files. It was not investigated further this session;
scoping and fixing it is a natural next phase.

## 5. Suggested next steps

- Decide how to handle `libreplan-webapp`'s 192 compile errors (§4) — likely its own phase. The
  `ganttz`/ZK `IServletRequestHandler` mismatch in particular may require sourcing a newer library
  build rather than an application-code fix.
- Decide whether to keep the pinned pre-Jakarta branch `characterization-tests-pre-jakarta` (commit
  `427ce2297`) for future re-verification, or delete it now that everything is verified against it.
- `libreplan-business`'s test suite and Criteria migration are otherwise done; no further action
  needed there barring new work.
