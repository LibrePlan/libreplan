# LibrePlan Jakarta EE Migration Plan (Phase 5)

Branch: TBD (a new branch off `main`, once `jdk11to25` has been merged)
Status: **not started** — planning only, as of 2026-08-12
Companion docs (same directory): `JDK25_MIGRATION_PLAN.md` (Phases 0–4,
done), `Phase-5-jakarta-migration-scope.md` (the findings behind this
plan — read that first), `Phase-5-javax-import-inventory.txt` (raw data)

## 0. Why this exists, and why it's separate from the JDK 25 plan

Phase 4 of `JDK25_MIGRATION_PLAN.md` reached the actual JDK 25 runtime
target, but had to cap `maven.compiler.release` at **21** rather than
**25** — Spring Framework 5.3.x (the last javax-namespace Spring
generation, EOL, won't be patched further) bundles its own private ASM
that cannot parse Java 25 class files at all. The only real fix is
Spring 6, which is jakarta-namespace-only. That's this plan.

This was always scoped as optional, separate future work (see Phase 5 in
`JDK25_MIGRATION_PLAN.md`, written before Phase 1 even started) —
deliberately not folded into "reach JDK 25," which is already done. Read
`Phase-5-jakarta-migration-scope.md` for the full investigation this plan
is based on: which `javax.*` packages actually need renaming vs. which are
JDK-native and must never be touched (~600 EE import sites vs. ~53
JDK-native ones, in this codebase specifically), the full list of
dependencies that have to move together, and the confirmed-not-assumed
finding that ZK has a Jakarta-compatible line (9.6.0-jakarta+).

## 1. Guiding rule (same as the JDK 25 plan)

**Every phase must end with `mvn clean install` green on its target state,
and every phase must be its own commit (or small stack of commits).** The
difference from `JDK25_MIGRATION_PLAN.md`: that plan could do the JDK bump
and the dependency uplift as small, independently-reversible steps because
JDK versions are backward compatible one hop at a time. Jakarta is not
like that — Spring 6 only speaks `jakarta.*`, ZK-jakarta only speaks
`jakarta.servlet`, so several of the steps below are necessarily "flag day"
changes for their subsystem (can't half-migrate a single servlet
container). Where a step can't be made incremental, it's still kept as
small and as independently verifiable as possible, and isolated from
unrelated changes (e.g. the `maven.compiler.release` bump back to 25 is
its own last step, deliberately not bundled with the namespace change, so
a regression can be attributed to one or the other).

## 2. Phase overview

```
5.1  Prep & verification (no code changes)
5.2  Mechanical namespace rewrite + dependency bump (the "flag day")
5.3  Get the test suite green again
5.4  Local dev tooling (Jetty, build scripts)
5.5  CI workflows
5.6  Manual smoke test + Docker deployment smoke test (Tomcat 10/11)
5.7  Close the loop: maven.compiler.release back to 25
```

---

## Phase 5.1 — Prep & verification

Goal: de-risk the big step (5.2) as much as possible before touching any
code. Nothing in this phase changes behavior.

Steps:

1. **[done]** Confirmed ZK's Jakarta edition covers what LibrePlan needs.
   Checked all 6 ZK artifacts this project actually depends on (`zk`,
   `zul`, `zkplus`, `zkbind`, `zcommon`, `zweb`), not just the core `zk`
   one — every single one has `-jakarta`-suffixed versions on Maven
   Central through `10.3.0.1-jakarta` (latest), under the same free
   coordinates already in use. Licensing checked separately (web search):
   ZK CE remains free under LGPL for both open-source and commercial use,
   no indication the free tier lost anything relevant for this jump.
2. **[done]** Tomcat **10** chosen (Jeroen's call). Target
   `jakarta.servlet-api` **5.0.x** accordingly in step 5.2, not 6.x.
3. **[done]** Ran OpenRewrite's Jakarta EE migration recipe in dry-run
   mode:
   ```
   ./mvnw org.openrewrite.maven:rewrite-maven-plugin:6.45.0:dryRun \
     -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:3.41.0 \
     -Drewrite.activeRecipes=org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta
   ```
   Produced `target/rewrite/rewrite.patch` — **315 files touched**, no
   `pom.xml`/source files modified (dry-run only). Cross-checked directly
   against the manual classification in `Phase-5-javax-import-inventory.txt`:
   confirmed `javax.xml.datatype` (20 occurrences) and `javax.management.*`
   (3 occurrences) appear in the patch only as unchanged diff-context
   lines (no `-`/`+` prefix) — the recipe correctly left them as `javax.*`.
   `javax.naming`/`javax.net.ssl` don't appear in the patch at all. This is
   a real, verified confirmation that the recipe respects the JDK-native
   boundary, not an assumption from its documentation.
4. **[done]** Captured a fresh `dependency:tree` baseline for all 3
   modules under `phase5-pre-jakarta/` (this directory). Test-suite
   baseline: no separate run needed — no code has changed since Phase 4's
   own final green result (1315 tests, 0 failures/errors), which is the
   authoritative baseline (see `phase5-pre-jakarta/README.md`).
5. **[decided, adapted]** Not waiting for `jdk11to25` to merge to `main` —
   Jeroen wants to keep moving rather than block on that PR. Branching
   `phase5-jakarta-migration` off `jdk11to25`'s current tip instead (a
   stacked branch/PR: `git checkout -b phase5-jakarta-migration` from
   `jdk11to25`, PR opened with base `jdk11to25` for now, retargeted to
   `main` once that PR merges — standard stacked-PR pattern, one GitHub
   setting change, no rebase needed since the branch already contains
   `jdk11to25`'s full history).

Exit criteria: ZK licensing/artifact coverage confirmed; Tomcat version
decided; OpenRewrite dry-run diff reviewed and understood; baseline
captured; branch created. No source code changed yet.

---

## Phase 5.2 — Mechanical namespace rewrite + dependency bump

**Version-choice finding (2026-08-12):** Spring Framework 6.2.x — the
version this plan originally assumed — reached end-of-life on 2026-06-30,
already in the past as of this writing; Spring Framework 7.0 is now the
only actively-patched generation (verified via
[OpenLogic](https://www.openlogic.com/blog/spring-framework-6-2-EOL),
[HeroDevs](https://www.herodevs.com/blog-posts/what-you-need-to-know-spring-frameworks-end-of-life-dates),
[endoflife.ai](https://endoflife.ai/spring-framework); Spring 7's own
bytecode confirmed to only require a JDK 17 floor by inspecting
`spring-core-7.0.8.jar` directly — class file major version 61 — so it's
not a JDK-compatibility blocker). **Deliberate decision (Jeroen's call):**
target Spring **6.x** anyway for this phase, not 7.x, because Spring 7 is
beyond Claude's knowledge cutoff and therefore a real risk to navigate
reliably in an agentic session — better to land on the well-understood 6.x
line now and treat the Spring 6→7 jump as its own explicit follow-up
project once Phase 5 is complete and stable, the same way this whole
Jakarta migration was deliberately kept separate from "reach JDK 25."
**Add this Spring 7 jump to the list of known follow-up work once Phase 5
ships** — don't let Spring 6.x quietly become the permanent target just
because it's what got landed first.

Goal: get to a *compiling* state on the full Jakarta stack. Deliberately
not gating this step on tests passing — that's 5.3 — because the
namespace rewrite and the framework major-version bumps are large enough
changes that conflating "does it compile" with "does it behave correctly"
makes failures much harder to isolate.

Steps:

1. Apply the OpenRewrite Jakarta recipe for real (not dry-run), reviewing
   the diff before committing — this handles the ~600 mechanical import
   renames without touching the ~53 JDK-native `javax.*` ones.
2. Bump the framework versions together (they're interdependent, can't be
   done one at a time and stay compiling):
   - Spring Framework → 6.x (latest stable)
   - Spring Security → 6.x (latest stable, matching Spring 6)
   - Hibernate ORM → 6.x (latest stable 6.x) — expect real API changes
     beyond namespace here (criteria API restructuring, implicit-naming
     strategy changes), not just a rename; the two custom `UserType`
     implementations (`EffortDurationType`, `ResourcesPerDayType`, already
     touched once in `JDK25_MIGRATION_PLAN.md` Phase 2 step 5) are likely
     to need another look given Hibernate 6's `UserType` interface changes
     again.
   - `hibernate-validator` → 8.x (jakarta.validation 3.x line)
   - Apache CXF → 4.x
   - `jakarta.servlet-api` → 5.0 or 6.x, per the Tomcat decision in 5.1
   - `javax.mail` → Jakarta Mail / Angus Mail (new project, not just a
     package rename — check the actual Maven coordinates, they changed
     groupId too)
   - ZK → 9.6.0-jakarta or later, per 5.1's licensing check
3. Re-check the `dependencyManagement` overrides added during the JDK 25
   migration for continued relevance: the `javassist` override
   (`JDK25_MIGRATION_PLAN.md` Phase 2 step 8) and the `byte-buddy` override
   (Phase 3 step 1) were both forced to specific versions because of
   version-mediation conflicts with the *previous* generation of
   Hibernate/EasyMock — re-verify whether Hibernate 6 and whatever EasyMock
   version is current still need these overrides, or pull compatible
   versions on their own now.
4. Get to a clean `mvn clean compile` across all 3 modules. Expect this to
   take several iterations — treat every compile error the same way every
   fix in `CHANGES-and-WHY.md` was handled: understand the actual API
   change before patching, don't blind-cast to silence the compiler.

Exit criteria: `mvn clean compile` green on all 3 modules, targeting
`jakarta.*` throughout, still on `maven.compiler.release=21` (bytecode
target stays where Phase 4 left it — that change is isolated to 5.7).

---

## Phase 5.3 — Get the test suite green again

Goal: same spirit as `JDK25_MIGRATION_PLAN.md` Phase 2 steps 3–6 — expect
several distinct runtime-only failures, each requiring real diagnosis, not
a single mechanical pass.

Steps:

1. Run the full reactor test suite, expect failures.
2. Diagnose and fix each distinct failure mode in turn, same discipline as
   every fix in `CHANGES-and-WHY.md`: understand the actual root cause
   (write a standalone reproduction if the surefire report truncates the
   real nested exception — this happened repeatedly during the JDK 25
   migration and will likely happen again here) before applying a fix.
   Specific things to watch for, based on patterns already seen once in
   this codebase:
   - Bean-mocking libraries (EasyMock) needing a version/config check
     against whatever Hibernate 6 changes about entity proxying.
   - Spring's default `HttpFirewall`/security config behavior potentially
     changing again between Security 5.8 and 6.x, the same way it did
     between 4.2 and 5.8 (`CHANGES-and-WHY.md` §8) — re-verify the
     `allowSemicolon` firewall override still exists and is still
     necessary/correctly wired after the Spring Security XML/Java config
     changes that came with 6.x (Spring Security 6 dropped a lot of the
     XML namespace surface in favor of Java config — this specific area
     may need re-architecting, not just re-verifying).
   - Any `dependencyManagement` version overrides inherited from the JDK
     25 migration that may now be *wrong* for the new dependency graph
     (forcing an old version to fix a JDK-25-era problem that Jakarta's
     newer libraries may have already fixed differently).
3. Full reactor `mvn clean test` green — same bar every previous phase
   used: 0 failures, 0 errors (skips only where already pre-existing and
   understood).

Exit criteria: full reactor test suite green, still on
`maven.compiler.release=21`.

---

## Phase 5.4 — Local dev tooling

Goal: get local development (`mvn jetty:run`, the `build*.sh` scripts)
working again on the Jakarta stack — these were never touched by the
namespace rewrite itself but depend on a servlet container that speaks
`jakarta.servlet`.

Steps:

1. Bump `jetty-maven-plugin` from 9.4.x to Jetty 11.x (Jakarta EE 9,
   Servlet 5.0) or 12.x (Jakarta EE 10, Servlet 6.0), matching whichever
   `jakarta.servlet-api` version was chosen in 5.1/5.2.
2. Verify `mvn jetty:run` actually serves the app — this is a good early,
   cheap signal before the full manual smoke test in 5.6.
3. Sanity-check the `build*.sh` scripts and `HACKING.rst` still work
   as-is — they invoke `mvnw`/`jetty:run` generically and shouldn't need
   changes, but confirm rather than assume, the same way every `./mvnw`
   change during the JDK 25 migration was verified with a real run before
   being called done.

Exit criteria: `mvn jetty:run` serves the app locally without errors.

---

## Phase 5.5 — CI workflows

Goal: get the automated build pipeline testing the Jakarta stack.

Note the key difference from every JDK-version CI switch in
`JDK25_MIGRATION_PLAN.md`: those were "add the new workflow disabled,
verify, then swap active/disabled with the old one via `git mv`" because
both javax-JDK-N and javax-JDK-(N+1) builds were simultaneously valid
states of the same codebase. That's not true here — once the namespace
rewrite lands, there is no javax-based version of the codebase to keep a
parallel CI workflow for. This is a one-way move, not a swap.

Steps:

1. Update the existing JDK 25 GitHub workflow
   (`.github/workflows/ubuntu-24.04-jdk-25.yml`) in place to build the
   Jakarta-based codebase — no separate workflow needed since it's testing
   the same branch's actual state, not an alternative.
2. `.forgejo/workflows/*` — per the established boundary throughout this
   whole migration, leave untouched unless Jeroen asks otherwise.

Exit criteria: the GitHub JDK 25 workflow is green against the Jakarta
codebase.

---

## Phase 5.6 — Manual smoke test + Docker deployment smoke test

Goal: same bar as `JDK25_MIGRATION_PLAN.md` Phase 2 step 9 and Phase 4
step 4 — a green test suite has never been sufficient on its own during
this migration. The Spring Security firewall issue (`CHANGES-and-WHY.md`
§8) was found *only* by manually running the app; there is no reason to
expect fewer surprises here, and good reason to expect more, given the
size of this change.

Steps:

1. Jeroen manually exercises the running app locally (`mvn jetty:run`) —
   planner/Gantt views, workers, configuration, same areas already
   validated in Phases 2–4.
2. Jeroen tests the real Docker deployment on his own infrastructure
   (Tomcat 10/11 per the 5.1 decision) — same pattern as Phase 4 step 4,
   since no Dockerfile/docker-compose for the application lives in this
   repo.

Exit criteria: both confirmed working by Jeroen.

---

## Phase 5.7 — Close the loop: `maven.compiler.release` back to 25

Goal: this is the actual payoff of the whole plan — Spring 6's modern
bundled ASM should finally be able to parse Java 25 class files, removing
the blocker that capped Phase 4 at release 21.

Deliberately last and deliberately isolated from every other change in
this plan, so that if something breaks here, it's unambiguous that the
bytecode-target bump caused it — not the namespace migration or the
framework bumps, which are already fully verified green by this point.

Steps:

1. Set `maven.compiler.release` back to `25`.
2. Full reactor `mvn clean compile` + `mvn clean test` — expect this to
   just work, given the root cause (Spring's ASM version) is what's
   actually changing here. If it doesn't, that's a new, narrower
   investigation than anything else in this plan, isolated to exactly
   this one property change.
3. Re-run the same jar-compatibility sweep done in
   `JDK25_MIGRATION_PLAN.md` Phase 4 step 1 (test every jar on the
   classpath against JDK 25's `jar tf`, the way `aspectjweaver`'s zip64
   issue was found) — the dependency graph has changed completely since
   then, so that specific finding doesn't carry over, but the *method* is
   worth repeating.

Exit criteria: full reactor green with `maven.compiler.release=25` and a
real JDK 25 JVM, genuinely running Java-25-format bytecode end to end —
the goal `JDK25_MIGRATION_PLAN.md` Phase 4 had to defer. **This closes out
the entire JDK 11 → 25 migration, Phase 5 included.**

---

## 3. Tracking

Same granularity guidance as `JDK25_MIGRATION_PLAN.md`: one PR per
numbered step within a sub-phase, not one PR per sub-phase — review stays
small and `git bisect` stays useful. Given 5.2 in particular is a large,
somewhat unavoidable "flag day" step, consider whether it can still be
split further in practice (e.g., namespace rewrite as one commit,
each framework bump as its own commit) even though they all have to land
before the codebase compiles again.
