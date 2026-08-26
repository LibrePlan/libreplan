# Phase 6 — remaining points

Branch: `phase5-jakarta-migration` (or a follow-on branch cut from it — Phase 5 itself is
functionally done: the app compiles, boots, and its Criteria migration matches the pre-Jakarta
baseline).

This is the punch list for everything still open after Phase 5, gathered from
`Phase-5-STATUS-2026-08-15.md` §6 and `Phase5-found-bugs.md`. Two categories of "still open" got
merged into one plan here, deliberately: things Phase 5 didn't quite finish (a config gap, 18 test
failures, a stale version cap) and pre-existing bugs Phase 5's characterization testing turned up
and consciously chose not to fix on the spot. Both belong in the same backlog — the point of
`Phase5-found-bugs.md` was specifically so the second category doesn't quietly get forgotten.

**Explicitly out of scope for this phase:** the Earned Value tab's checkbox/legend row spacing.
Jeroen is already migrating this app from the Sapphire to the Breeze ZK theme in a separate
branch and wants to fold that fix into that work, not this one.

## Guiding rule for every step (same discipline as Phases 1–5)

- **No blind fixes.** For anything that changes observable behavior (not just an API-surface
  rename), characterize the current behavior with a test first, confirm whether it's
  migration-caused or pre-existing (the pinned `characterization-tests-pre-jakarta` branch, commit
  `427ce2297`, is still available for this — recreate the worktree with
  `git worktree add <path> characterization-tests-pre-jakarta` if it's gone), then fix, then
  re-verify. This is exactly the methodology `Phase-5-dao-criteria-punchlist.md` used, and it's
  what caught the loop-variable-shadowing self-inflicted bug and the two fork-introduced bugs
  during Phase 5 — don't skip it because these are "just bugfixes."
- **Every step should leave `mvn clean install` green.** Same rule as every prior phase: don't
  let the tree go red between steps.
- **Steps are ordered by risk and dependency, not by file/package.** Do 6.1–6.2 first — they're
  low-risk, high-value, and 6.2 in particular (the version-bookkeeping sweep) will directly inform
  how much rigor 6.4's older cousins in `libreplan-business` actually needed, since it's the same
  bug pattern seen from the webapp side.

## 6.1 — Commit the pending config fix, then close the remaining `libreplan-webapp` compile/runtime gaps

### 6.1.1 Commit the `libreplan-webapp-spring-config-test.xml` mapping fix
Already made (2026-08-15), not yet committed: 4 missing `<value>` entries
(`JobSchedulerConfiguration.hbm.xml`, `Limits.hbm.xml`, `ExpenseSheets.hbm.xml`, `Logs.hbm.xml`)
added to the duplicated `sessionFactory` bean's `mappingResources` list, matching what
`libreplan-business-spring-config.xml` already has. This alone took `libreplan-webapp`'s test
suite from 201/222 errors to 18/222. Just needs a commit — no further work.

### 6.1.2 `net.sf.mpxj` — javax JAXB gap breaking `OrderImporterTest`
`net.sf.mpxj:mpxj:9.0.0` throws `NoClassDefFoundError: javax/xml/bind/JAXBException` from its own
`ProjectReaderUtility.getProjectReader` — it's compiled against the pre-Jakarta JAXB namespace,
which this project no longer ships anywhere.

Steps:
1. Check Maven Central for a newer `mpxj` release and whether it ships a `jakarta.xml.bind`-based
   build (mpxj's own release notes/changelog should say). If yes: bump the version in root
   `pom.xml`, rebuild, confirm `OrderImporterTest` passes, check for any API surface changes mpxj
   made along the way (same "don't assume a version bump is just a number" rule as every dependency
   bump in Phases 2–4).
2. If no jakarta-namespace `mpxj` release exists: add the old `javax.xml.bind:jaxb-api` (plus a
   matching impl, e.g. `com.sun.xml.bind:jaxb-impl`) as a **test/runtime-scoped** dependency,
   scoped as tightly as possible (ideally only reachable by `mpxj`, not by this project's own
   code — check with `dependency:tree` that nothing in `org.libreplan.*` picks it up by accident).
3. Verify: `mvn -pl libreplan-webapp test -Dtest=OrderImporterTest`.

Exit criteria: `OrderImporterTest.testCreatingImportDataFromPlannerFile` passes, no `javax.xml.bind`
import appears anywhere in `org.libreplan.*` source (grep-verify, matching the discipline
`Phase-5-jakarta-migration-scope.md` used for the original inventory).

## 6.2 — Sweep `libreplan-webapp`'s `isNewObject()`/version-bookkeeping failures (17 tests)

**Status update (2026-08-15, executed): investigated in depth, one sub-fix landed, NOT
complete.** The original framing below ("one bug pattern, 17 tests, apply the same fix
everywhere") turned out to be wrong. Full diagnosis is in `../Phase5-found-bugs.md` §11 — three
distinct root causes, not one:
- **Cluster A** (`OrderModelTest` + `ChartFillerTest`, 11 tests): a real **production** bug in
  `PlanningStateCreator`/`Scenario.orders`' cascade mapping (`increment` id generator leaking a
  live id out of a read-only, never-flushed transaction). Diagnosed precisely via a temporary
  debug print (removed). Not fixed — needs a dedicated pass touching core Scenario/Order-
  versioning cascade behavior, too risky to rush.
- **Cluster B** (`EmailTest`, 2 tests): test-fixture-only — heavy EasyMock mocking of
  `SchedulingDataForVersion`/`OrderVersion` doesn't model a real persisted graph, which
  Hibernate 6 now validates more strictly. Needs the test fixture rewritten to use real DAO-
  persisted entities for that part of the graph, not mocks.
- **Cluster C** (`OrderElementServiceTest`, 4 tests): the REST/WS `addOrders` DTO-conversion
  path, not yet traced beyond confirming it's a third, separate code path (`Criterion`
  transient-reference, not `TaskSource`/`Order`).

One real fix landed and is committed: `ExternalCompany.dontPoseAsTransientObjectAnymore()` in
`OrderModelTest`'s `createValidExternalCompany()` helper (same established pattern as
`TaskSource.RealPersistence.save()`). It doesn't turn any test green by itself — Cluster A's
`Order`-level issue gates the same tests one step further down — but it's independently correct
and was verified necessary (removing it reintroduces the identical failure on `ExternalCompany`
specifically).

**Revised recommendation**: tackle Cluster A next, on its own, with the same rigor as the
original Hibernate 6 migration bugs — it's the highest-value fix (11 of 17 failures) and the
only one confirmed to reach production code, not just test fixtures. Clusters B and C are lower
priority (test-only, smaller blast radius) and can follow independently. The rest of this
section is kept as originally written for the process/discipline it describes, but the "it's one
pattern" framing in the prose below is superseded by the three-cluster breakdown above.

The other 17 of the 18 remaining `libreplan-webapp` failures are one bug pattern, not seventeen:
`DataIntegrityViolationException: Detached entity ... has an uninitialized version value 'null'`
or `TransientObjectException: persistent instance references an unsaved transient instance`, on
`Order`, `SchedulingDataForVersion`, `ExternalCompany`, `TaskSource`, `Criterion`. This is the
exact pattern already fixed at several `libreplan-business` call sites during Phase 5 (see
`Phase-5-STATUS-2026-08-14.md` §3: `CalendarBootstrap`, `TaskSource.RealPersistence.save()`, and
several test fixtures) — that sweep just never reached `libreplan-webapp`'s own test bootstrap
fixtures and web-layer entity-creation code, which is where these 17 live:

- `OrderModelTest` (10 errors)
- `ChartFillerTest.testBAC` (1 error)
- `EmailTest` (2 errors)
- `OrderElementServiceTest` (4 errors)

Steps, per the guiding rule above — **do not blanket-apply the `libreplan-business` fix pattern
without checking each site**, since the earlier sweep already found one place (`ExternalCompanyDAOTest`)
where a test was accidentally relying on the *buggy* behavior:

1. For each failing test, find the entity-creation/reuse call chain that leads to the failure
   (test fixture helper, `OrderModel`/`ChartFiller`/webapp-layer production code, or both).
2. Confirm against the pinned `characterization-tests-pre-jakarta` baseline whether the test passed
   there. If it did, this is migration-caused (Hibernate 6 legitimately enforces the version-flag
   contract more strictly) and needs the same kind of fix as before: either flip
   `dontPoseAsTransientObjectAnymore()` at the right point in production code, or fix a test fixture
   that reuses an already-saved instance across transactions without flipping it.
3. If a test turns out to rely on the old, buggy behavior (like `ExternalCompanyDAOTest` did),
   don't just "fix" it silently — rewrite it the same way that one was, to exercise the intended
   behavior with fresh instances instead of reused ones.
4. Re-run the full `libreplan-webapp` suite after each fix, not just the one test, since these 4
   test classes likely share bootstrap helpers and a fix in one place may reveal or resolve issues
   in another.

Exit criteria: `mvn -pl libreplan-webapp test` — 222/222 accounted for (pass, or one of the
already-known 13 pre-existing skips), 0 unexplained errors.

## 6.3 — Re-test whether `maven.compiler.release` can go from 21 to 25

The cap to 21 (Phase 4) was forced by Spring 5.3.x's bundled ASM not parsing Java 25 class files —
see the comment in root `pom.xml` and `CHANGES-and-WHY.md` §11b. Spring is now on **6.2.19**
(landed as part of Phase 5), which ships a modern ASM. The justification for the cap may no longer
apply, but this hasn't been re-tested since the Spring 6 upgrade.

Steps:
1. On a throwaway branch, bump `maven.compiler.release` to `25`.
2. `mvn clean install` (full reactor, real JDK 25 — already the dev box's runtime).
3. If green: bump for real, update/remove the now-stale explanatory comment in `pom.xml`, note the
   change in a new dated status doc or an addendum to this file.
4. If it fails: identify what's still blocking it (some other dependency's bundled bytecode
   tooling, most likely — same investigative pattern as every prior JDK bump), document the new
   finding, and decide whether to chase it now or leave the cap in place with an updated,
   accurate reason.

Exit criteria either way: the `pom.xml` comment reflects the current, verified truth — not
Phase 4's now-possibly-stale reasoning.

**Update (2026-08-17): a real regression from this step was found and fixed.** `mvn clean
install` passing (the only verification originally done for this step) does **not** exercise
`jetty:run`'s own annotation-scanning code path, and that path broke: Jetty's `jetty-annotations`
module bundles `org.ow2.asm:asm`, and `jetty-maven-plugin:11.0.24` pins ASM 9.7, which cannot
parse Java 25 (major class file version 69) at all — every `jetty:run` startup failed with
`IllegalArgumentException: Unsupported class file major version 69` on every one of this
project's own compiled classes, and the webapp never came up (503). Verified the root cause
directly, not just by reading changelogs: fed the same compiled `.class` file to ASM 9.7 (fails)
and ASM 9.8 (parses fine) via a standalone test program. `jetty-maven-plugin:11.0.26` is the
first 11.0.x release bundling ASM 9.8 instead of 9.7 (confirmed by diffing the `asm.version`
property between the two releases' `jetty-project` POMs on Maven Central). Bumped
`jetty-maven-plugin` 11.0.24 → 11.0.26 in root `pom.xml`; `jetty:run` now starts cleanly with
zero errors and the app was verified working end-to-end (login, planner page) via Playwright.
This has nothing to do with `maven.compiler.release` itself (that's a javac/JVM bytecode-target
setting) — it's purely that a *build-time tool* (Jetty's own embedded annotation scanner) needed
to be new enough to read the bytecode this project now produces. Worth remembering for any
future JDK bump: `mvn test`/`mvn install` alone doesn't prove `jetty:run` still works — always
do the real manual smoke test too, same lesson Phase 2 step 9 already taught with the
`StrictHttpFirewall` semicolon issue.

## 6.4 — `mysql-connector-java` staleness (low priority — MySQL is the deprecated profile) — [DONE]

**Status (closed 2026-08-19, bump itself landed earlier in Phase 6): driver bumped, current,
Jeroen confirmed closing this.** Was `mysql:mysql-connector-java:5.1.46` (2018); now
`com.mysql:mysql-connector-j:26.7.0` (the artifact renamed groupId/artifactId along the way) —
re-verified against Maven Central on 2026-08-19, still the current release. Also switched to the
modern driver class/URL combo (`com.mysql.cj.jdbc.Driver` + `useSSL=false`) the old pom comment
had already flagged as correct but never applied. `hibernate.dialect` already reads
`org.hibernate.dialect.MySQLDialect` (updated from the old `MySQL5InnoDBDialect` name during the
Hibernate 6 bump) and needed no further change. Compiles clean; **no MySQL instance available to
manually smoke-test against** (same caveat as every other manual-smoke-test step in this
migration) — Jeroen accepted closing on that basis rather than blocking on it, since the real,
default-tested path has always been PostgreSQL (`HACKING.rst` labels the MySQL section
"(Deprecated)").

## 6.5 — Fix the cataloged pre-existing bugs from `Phase5-found-bugs.md`

**Status (2026-08-15, executed): 9 of 9 done — all cataloged items fixed.** See
`../Phase5-found-bugs.md` for the full writeup of each, including the discovery that items 7/8
(`WorkReportLineDAO`) were actually one bug, and that item 9 (`ScenariosBootstrapTest`) was also
a real production bug in `OrderModel`/`ScenarioModel`, not just a test artifact. Items 1
(`LimitsDAO`) and 4 (`ResourcesSearcher` NIF case-sensitivity) both needed a product decision
from Jeroen first: `Limits` turned out to be a real, DB-admin-managed cloud-deployment license
control (e.g. max users) with `abstract="true"` simply wrong in its mapping, not intentional; the
NIF/"ID" field turned out to be free text with no actual government-ID validation anywhere,
confirmed via investigation and agreed by Jeroen, so case-insensitive matching (consistent with
name/surname, and with the already-case-insensitive `findUniqueByNif` elsewhere) was the right
call. That same investigation also surfaced and fixed a related issue Jeroen raised directly:
the `Worker`/external "ID" field being mandatory when nothing in the app actually needs it to
be (see item 3a in `Phase5-found-bugs.md`). `libreplan-business`'s full test suite is now
**fully green: 1217 tests, 0 failures, 0 errors** (was 1215/0/1 at the start of Phase 6).

Every item below is detailed with evidence and a suggested fix direction in
`../Phase5-found-bugs.md` §"Data-layer bugs" — this section just turns that catalog into an
ordered work list. **Each of these changes real, observable behavior** (not just an API rename),
so the guiding rule at the top of this document applies in full: characterize first, confirm
pre-existing vs. migration-caused, then fix.

1. **[DONE, 2026-08-15] `LimitsDAO.save()` fundamentally broken** (`Limits.hbm.xml` was
   `abstract="true"` with no subclass). Jeroen confirmed `Limits` is a per-seat/per-license cloud
   deployment control, DB-admin-managed directly, no in-app GUI needed or planned — so it is
   meant to be concretely persisted, `abstract="true"` was just wrong. Removed it; added
   characterization tests proving `save()` round-trips correctly now.
2. **`MachineDAO.findByNameOrCode`** — confirmed zero callers. Safe to delete outright, no
   characterization test needed beyond confirming (again, at fix time) that it's still uncalled.
3. **`ResourceDAO.getAllLimitingResources`/`getAllNonLimitingResources`,
   `WorkerDAO.findByNameSubpartOrNifCaseInsensitive`** — same unmapped-property bug as #2, but
   callers weren't confirmed absent. Check for real callers first; if any exist, understand how
   they're currently tolerating the permanent exception before changing anything.
4. **[DONE, 2026-08-15] `ResourcesSearcher` NIF case-sensitivity inconsistency.** Investigated
   first: the field is shown to users only as generic "ID"/"Company ID", never "NIF", and has no
   government-ID format validation — just "must be unique". Jeroen agreed case-insensitive is the
   right call (consistent with name/surname matching right next to it, and with the
   already-case-insensitive `WorkerDAO.findUniqueByNif` elsewhere). Fixed by wrapping the `nif`
   predicate in `cb.lower(...)`.
5. **`QualityFormDAO.isUnique()` inverted logic on `InstanceNotFoundException`** — narrow the
   `catch`, return `true` specifically for "not found" instead of swallowing it as `false`. Add a
   characterization test for the corrected behavior first.
6. **`WorkReportDAO.isAnyPersonalTimesheetAlreadySaved()` named backwards** — audit every caller
   before touching anything (a caller may already be compensating for the inverted name); then
   either rename to match behavior or flip the body, not both independently.
7. **`WorkReportLineDAO.findFinishedByOrderElementNotInWorkReportAnotherTransaction`** always
   throws across its `REQUIRES_NEW` boundary — needs its own dedicated investigation into what the
   method was supposed to see across that boundary; not a quick fix.
8. **`WorkReportLine.isOrderElementFinishedInAnotherWorkReportConstraint()`** always throws
   `HV000090` when `finished=true` — likely a Bean Validation constraint method with a signature
   Hibernate Validator can't reflectively invoke; needs its own look at the constraint annotation
   and method visibility/signature.
9. **`ScenariosBootstrapTest` FK-violation cleanup bug** — first confirm whether real
   order-deletion code paths share the same gap (missing `OrderSyncInfo` cleanup) or if it's
   test-fixture-only; fix accordingly (test-only fix vs. a real cascade/guard in the mapping).

Exit criteria: each item above is either fixed-with-a-passing-characterization-test, or explicitly
re-affirmed as "preserved, here's the product reason why" — nothing should silently fall off this
list without one of those two outcomes.

## 6.6 — Housekeeping

None of these are code bugs; all are small decisions or cleanup that have been sitting open since
Phase 5.

1. **[DONE, 2026-08-15] ZK licensing verification** — pulled the `<licenses>` block directly out
   of the published POM (not a marketing page) for every core ZK artifact LibrePlan depends on,
   at the exact pinned version (`10.3.0.1-jakarta`): all LGPL v3 (ZK CE's free tier), same as
   pre-migration ZK 8.6.0.1. See the addendum in `Phase-5-jakarta-migration-scope.md`.
2. **[DECIDED, 2026-08-15] `characterization-tests-pre-jakarta` pinned branch** (commit
   `427ce2297`) — **keep**, not delete. Rationale: still actively in use — it was used again
   during 6.2's investigation this same session, and 6.5's cataloged bugs may still need it
   (several don't have a characterization test copied to the main tree yet, only a description
   of the bug). Revisit this decision once 6.2's Cluster A/B/C and 6.5 are actually finished, not
   before.
3. **[PARTIALLY DONE, 2026-08-15] Untracked scratch files at the repo root** —
   **`old-lpadmin-password.txt` is now covered by `.gitignore`** (belt-and-suspenders on top of
   never `git add`-ing it), so it can no longer be committed by accident even via a broad
   `git add -A`. The rest of the pile (`build*.sh`, `*.log`, `JDK25_MIGRATION_PLAN.{odt,pdf}`,
   screenshots, `fix-sapphire.md`, the `libreplan_zk.css-*` experiment files, `t1`/`t2`,
   `current-status.txt`, `libreplan-webapp/src/main/webapp/help/`) was deliberately **not**
   deleted or sorted further — ownership of most of these isn't clear-cut (some are Jeroen's own
   dev scripts/notes, not something to delete unilaterally), so this is left as a decision for
   Jeroen to make explicitly rather than a code-review-style cleanup call.

## Tracking

Same granularity convention as `JDK25_MIGRATION_PLAN.md` §4: one PR per numbered step
(6.1.1, 6.1.2, 6.2, 6.3, 6.4, 6.5.1–6.5.9, 6.6.1–6.6.3), not one PR for the whole phase, so review
stays small. 6.5's nine items are independent of each other and of 6.1–6.4 — they can be picked up
in any order, or split across multiple people/sessions, without blocking the rest of the phase.

Suggested order given risk/value: **6.1 → 6.2 → 6.3 → 6.6.2/6.6.3 (quick housekeeping) → 6.5 → 6.4
→ 6.6.1** — get the test suite fully green and the compiler-release question answered first (both
cheap, both high-value), clear easy housekeeping out of the way, then work through the cataloged
behavioral bugs (6.5, the slowest section — several need a product decision before any code
changes), leave the MySQL bump for whenever a real MySQL instance is available to smoke-test
against, and close with the ZK licensing check whenever there's time for it (not blocking, but
shouldn't be forgotten indefinitely either).
