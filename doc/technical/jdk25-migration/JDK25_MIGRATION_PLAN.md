# LibrePlan JDK 11 → JDK 25 Migration Plan

Branch: `jdk11to25`
Author: drafted with Claude Code, 2026-08-11

## 1. Where we actually stand today

This plan was written after auditing the current `jdk11to25` branch, not from
memory of a previous conversation — the earlier plan was never saved. Findings:

| Item | Current state |
|---|---|
| Runtime JDK used to build | 11 (`.forgejo/workflows/ubuntu_24.04-jdk-11.yml`, `.github/workflows/ubuntu-24.04-jdk-11.yml`) |
| `maven-compiler-plugin` source/target | **1.8/1.8**, hardcoded in root `pom.xml` (not even using `<release>`) |
| `maven-compiler-plugin` version | 3.11.0 |
| `maven-surefire-plugin` version | 2.19.1 (2015, pre-JPMS) |
| Spring Framework | 4.3.9.RELEASE (2017, EOL, javax namespace) |
| Spring Security | 4.2.3.RELEASE |
| Hibernate Core | 5.1.1.Final (2016) |
| Hibernate Validator | 5.3.6.Final |
| Apache CXF | 3.1.7 (2016) |
| ZK Framework | 8.6.0.1 |
| Servlet API | javax.servlet-api **3.1.0** (Servlet 3.1 / Tomcat 8 era) |
| Quartz | 2.3.2 (fine, current-ish) |
| Guava | 33.5.0-android (already modern) |
| commons-io / commons-fileupload | 2.19.0 / 1.6.0 (already modern) |
| JasperReports | 6.20.0 (already modern) |
| MySQL driver | 5.1.46 (2018, ancient) |
| PostgreSQL driver | 42.7.3 (current) |
| Jakarta namespace migration | **not started** — everything is still `javax.*` |

There is already a **manual, tag-triggered Forgejo workflow**
(`.forgejo/workflows/ubuntu_24.04-jdk-25.yml`) that builds the whole reactor
under Temurin 25 with the compiler still pinned to `1.8/1.8`. It's unclear
whether it has ever been run to completion — the only related pom change so
far is bumping `commons-fileupload` to 1.6.0. Because source/target is still
8 and a source scan found no removed JDK APIs in application code
(no `SecurityManager`, `sun.misc.Unsafe`, `javax.script`/Nashorn,
`finalize()`, or CORBA usage), that workflow plausibly *compiles*. That is
not the same as *runs correctly*: Spring 4's CGLIB proxying, Hibernate 5.1's
bytecode enhancement/proxy generation, and ZK's reflection all rely on
libraries that bundle old ASM versions and do illegal reflective access —
things that fail at **runtime**, not compile time, once the JVM's default
module encapsulation tightens (JDK 16+) and keeps tightening through 17/21/25.

**Implication for the plan:** the risk here isn't really "can javac target
25" — it's whether decade-old Spring 4 / Hibernate 5.1 / CXF 3.1 / ZK 8 can
run correctly on a JVM that's 14 years newer than they are. So this plan
treats the JDK bump and the dependency uplift as one coupled effort, done in
small, always-buildable steps, rather than two separate tracks.

## 2. Guiding rule for every phase

**Every phase must end with `mvn clean install` green on its target JDK, and
every phase must be its own commit (or small stack of commits) that leaves
`main`/`jdk11to25` in a working, deployable state.** No phase depends on a
later phase's changes to compile. If a phase turns out to be too large to
keep green, split it further rather than let it go red temporarily.

Within each phase below, steps are ordered so each one is independently
buildable — you can stop after any step and still have a working reactor.

## 3. Phase overview

```
Phase 0  Baseline audit                         [done — this document]
Phase 1  Build-tooling uplift (still JDK 11)     JDK 11 runtime  [done]
Phase 2  JDK 17 + javax-compatible dep uplift    JDK 17 runtime  [done]
Phase 3  JDK 21 currency pass                    JDK 21 runtime  [done]
Phase 4  JDK 25 (target)                         JDK 25 runtime  [done]
Phase 5  Jakarta EE namespace migration (stretch, post-25)
```

Each numbered JDK phase is itself an LTS (11 → 17 → 21 → 25), so every
intermediate state is something you could actually ship and run in
production if the project needed to pause partway through.

---

## Phase 0 — Baseline audit (this document)

- No code changes.
- Deliverable: this plan, committed to the repo.

---

## Phase 1 — Build-tooling uplift (stays on JDK 11)

Goal: make the build itself capable of targeting newer bytecode levels and
running on newer JVMs, without changing the runtime JDK or any framework
version yet. This is the lowest-risk phase and should be done first because
several later phases are blocked by tooling that's simply too old to run
under JDK 17+.

Steps (each independently buildable, still on JDK 11):

1. **[done]** Bumped `maven-compiler-plugin` 3.11.0 → **3.15.0**. Replaced
   the hardcoded `<source>1.8</source><target>1.8</target>` pair with a
   single `<maven.compiler.release>8</maven.compiler.release>` property (the
   one property to bump in each later phase). Verified: `mvn clean compile`
   green across all 3 reactor modules.
2. **[done]** Bumped `maven-surefire-plugin` 2.19.1 → **3.5.6**. This was the
   important one to get right before any JDK bump — 2.19.1 forks test JVMs
   using internal APIs that break under JDK 17+'s stronger encapsulation.
   Verified: full suite green on JDK 11 — `libreplan-business` 940 tests,
   `ganttzk` 153 tests, `libreplan-webapp` 220 tests (13 pre-existing
   skips) — 0 failures, 0 errors.
3. **[done]** Bumped `maven-war-plugin` 2.6 → **3.5.1**. Verified: WAR
   packages correctly, `web.xml` present, the woodstox-core-asl exclusion
   from issue #2067 (commit `edcb32b98`) still applies.
4. **[done]** Added a **Maven Wrapper** (`mvnw`/`mvnw.cmd`,
   `.mvn/wrapper/maven-wrapper.properties`) pinned to **Maven 3.9.16**,
   decoupling the build from whatever `mvn` happens to be installed locally
   (was 3.8.7, from Dec 2022 — predates JDK 21/25 entirely) or on a CI
   runner. Updated all `.github/workflows/*.yml(.disabled)` to run `./mvnw`
   instead of `mvn`. `.forgejo/workflows/*` deliberately left on their
   existing `apt-get install maven` path for now (out of scope — ask before
   touching shared Forgejo CI). Updated `HACKING.rst` throughout to use
   `./mvnw`/`../mvnw`/`mvnw.cmd` and dropped the manual Maven install steps
   (notably the openSUSE section's old hand-rolled Maven 3.0.5 tarball
   install, and the Windows manual-download step) since the wrapper makes
   them unnecessary.
5. **[done]** Captured a `dependency:tree` + `dependency:analyze` baseline
   for all 3 reactor modules — see [`jdk25-migration-baseline/`](jdk25-migration-baseline/README.md).
   Key finding for Phase 2: Spring 4.3.9's CGLIB/ASM usage is invisible to
   `dependency:tree` (bundled/shaded inside `spring-core.jar`, not a
   separate artifact) — don't rely on tree output alone to judge that risk.
   Also found two different old `javassist` versions in play (3.20.0-GA via
   Hibernate, 3.18.2-GA via ZK) worth rechecking once Hibernate/ZK are
   bumped.

Exit criteria: `mvn clean install` green on JDK 11, no behavior change,
tooling now new enough to survive later JDK bumps. **Phase 1 complete.**

---

## Phase 2 — JDK 17 + javax-compatible dependency uplift

Goal: get a real, CI-gated green build **and** passing test suite running
on JDK 17, using the last dependency generation that still speaks `javax.*`
(so we deliberately avoid coupling the JDK bump to the Jakarta rename here —
that's Phase 5).

Steps:

1. **[done]** Set `maven.compiler.release` to `17`, built with a real JDK 17
   (`sudo update-java-alternatives -s java-1.17.0-openjdk-amd64` — machine is
   dedicated to LibrePlan dev, no need to keep any version as the system
   default). Compiled clean across all 3 modules on the first try, even
   still running old Spring 4/Hibernate 5.1/CXF 3.1 — confirmed the JDK
   bump's real risk is runtime (see below), not compilation. Running the
   `libreplan-business` test suite at this point (still pre-Spring/Hibernate
   bump) reproduced the exact failure predicted in Phase 0: Hibernate
   5.1.1's javassist proxy factory doing illegal reflective access into
   `ClassLoader.defineClass`, blocked by JDK 16+'s default encapsulation —
   573 of 940 tests failed this way.
2. **[done]** CI build JVM switch. Deferred until the dependency uplift
   steps below landed (no point gating CI on JDK 17 before the frameworks
   that actually run on it were in place), then done together with step 9:
   swapped `.github/workflows/ubuntu-24.04-jdk-17.yml.disabled` ↔
   `ubuntu-24.04-jdk-11.yml` (via `git mv`, so git sees clean renames) —
   JDK 17 is now the active required PR gate, JDK 11 kept
   disabled-but-present as the safety net the plan called for.
   `.forgejo/workflows/*` still deliberately untouched, same as Phase 1.
3. **[done]** Uplifted Spring Framework 4.3.9 → **5.3.39** (`spring-orm`,
   `spring-web`, `spring-context-support`, `spring-test` together). One
   compile break: `SchedulerManager.setJobClass` needed a
   `Class<? extends Job>` instead of a raw `Class` (Spring 5 tightened the
   generic signature) — see `jdk25-migration-baseline/CHANGES-and-WHY.md` §1.
   Verified compiling clean; re-ran the business test suite and confirmed
   failures were *exactly* the same pre-existing Hibernate issue as step 1
   (342 `InaccessibleObjectException` occurrences, same aggregate
   940/573) — i.e. Spring alone doesn't fix or break anything test-wise,
   as expected, since the proxy generation that breaks lives in Hibernate.
4. **[done]** Uplifted Spring Security 4.2.3 → **5.8.16**. This one had a
   real (not just mechanical) break: Spring Security 5 deleted the legacy
   salted password-encoding API (`ShaPasswordEncoder`/`ReflectionSaltSource`)
   outright. Migrated to `BCryptPasswordEncoder` with transparent
   verify-old-then-rehash-on-login, rather than just reimplementing the old
   (already known-weak) scheme to dodge the compile error. Full writeup,
   including the byte-exact legacy-algorithm verification against actual
   Spring Security 4.2.3 source and the DB-column-size check, in
   `jdk25-migration-baseline/CHANGES-and-WHY.md` §2. Also surfaced and fixed
   a separate, unrelated issue: every Spring XML config across both modules
   had hardcoded old schema versions (`spring-security-4.2.xsd` etc.), which
   Spring Security 5.8 refuses to parse — switched all of them to Spring's
   version-less schema URLs so this can't recur on the next dependency bump
   (§3 of the same doc). Verified: compiles clean, and
   `DBPasswordEncoderServiceTest` (extended with dedicated legacy-hash and
   rehash tests) fails only at Spring context startup for the same
   already-diagnosed Hibernate reason as steps 1 and 3 — no new failure mode
   introduced. A real green run of the BCrypt logic itself is still pending
   the Hibernate uplift (step 5).
5. **[done]** Uplifted Hibernate Core 5.1.1 → **5.6.15.Final**
   (`hibernate-ehcache` in lockstep, same version), plus
   `hibernate-validator` 5.3.6.Final → **6.2.5.Final** (last javax-namespace
   line — 5.3.x doesn't exist for validator, its own version numbering is
   independent of hibernate-core's). This was the step that actually fixed
   the Hibernate/javassist proxy issue chased since Phase 0/step 1: two
   custom `UserType` implementations (`EffortDurationType`,
   `ResourcesPerDayType`) needed `SessionImplementor` →
   `SharedSessionContractImplementor` in their `nullSafeGet`/`nullSafeSet`
   signatures (Hibernate renamed/generalized this interface across the 5.x
   line). That alone wasn't enough — two more issues surfaced by actually
   running the test suite, both the same "old bundled/incompatible
   bytecode-manipulation library" pattern as Hibernate's own, just in
   different dependencies:
   - `org.jadira.usertype:usertype.core` 5.0.0.GA (2015-era Joda-Time↔Hibernate
     bridge, used for exactly two column mappings in
     `WorkReports.hbm.xml`) called a Hibernate SPI method
     (`SessionFactoryImplementor.getProperties()`) that Hibernate 5.6
     removed → `NoSuchMethodError` at every `sessionFactory` bean startup.
     Bumped to 6.0.1.GA (latest stable GA; skipped 7.0.0.CR1, a
     candidate release).
   - EasyMock 3.4 bundles its own ancient shaded CGLIB copy
     (`org.easymock.cglib.*`, confirmed by inspecting the jar contents
     directly) for mocking concrete classes, which hit the identical
     `InaccessibleObjectException` pattern as Hibernate's javassist proxy
     factory. Bumped EasyMock 3.4 → **5.6.0**. Also removed the
     `easymockclassextension` 3.2 dependency entirely (from all 4 poms) —
     confirmed via jar inspection it contributes zero `cglib` classes and
     confirmed via grep nothing imports its `org.easymock.classextension`
     package; that capability has lived in core `easymock` since 3.2, so
     the separate artifact was dead weight, not something that needed
     migrating.
   Full writeup in `jdk25-migration-baseline/CHANGES-and-WHY.md` §4.
   **Verified: all 940 `libreplan-business` tests and all 153 `ganttzk`
   tests pass — 0 failures, 0 errors.** `libreplan-webapp` is at 222/223
   passing (13 pre-existing skips); the one remaining failure
   (`JiraRESTClientTest`) is CXF 3.1.7 doing the exact same illegal-access
   pattern via `CXFAuthenticator` — expected, and scoped to step 6 below,
   not a new issue from this step.
6. **[done]** Uplifted Apache CXF 3.1.7 → **3.5.11** (`cxf-rt-transports-http`,
   `cxf-rt-frontend-jaxrs`, `cxf-rt-rs-client`, all three declared directly
   in root `pom.xml`). No compile changes needed. The pre-existing
   woodstox-core-asl exclusion (issue #2067) was left in place as a
   defensive safety net even though `dependency:tree
   -Dincludes=org.codehaus.woodstox:woodstox-core-asl` now shows nothing —
   3.5.11 doesn't appear to pull that transitive at all any more, but there's
   no cost to keeping the exclusion regardless.
   **Verified: full reactor `mvn clean test` green — 940 + 153 + 222 = 1315
   tests, 0 failures, 0 errors** (13 pre-existing skips in webapp).
   `JiraRESTClientTest`, the one test still failing after step 5, now
   passes — confirming the diagnosis from step 5 was right.
7. **[done]** Uplifted `javax.servlet-api` 3.1.0 → **4.0.1** (Servlet 4.0 /
   Tomcat 9 baseline, matches what `HACKING.rst` already documents as the
   Windows/manual deployment target). Compiles clean, full reactor test
   suite still green (940+153+222, 0 failures/errors), WAR packages
   correctly with `servlet-api` correctly absent (still `provided` scope,
   supplied by the container at deploy time, same as before).
   **Caveat worth knowing for step 9's manual smoke test:** this bump only
   changes what *our own code* compiles against. Local `mvn jetty:run` dev
   testing (`jetty-maven-plugin` 9.4.56.v20240826) is unaffected either way
   — confirmed by checking what servlet-api version Jetty 9.4's own jars
   actually resolve against (3.1.0; Jetty 9.4 never moved past Servlet 3.1,
   that's Jetty 10). Servlet 4.0 is a backward-compatible superset of 3.1
   (mostly HTTP/2 Server Push additions), so this is expected to keep
   working fine under `jetty:run` as long as nothing in the codebase calls
   a Servlet-4.0-only method — just don't mistake a working `jetty:run`
   smoke test for confirmation of true Servlet 4.0 behavior; that only
   really gets exercised under an actual Tomcat 9+ deployment.
8. **[done]** Re-ran `dependency:tree` for all 3 modules and diffed against
   the Phase 1 baseline (saved under `jdk25-migration-baseline/phase2-jdk17/`).
   Confirmed: Hibernate 5.6.15 no longer uses javassist at all for proxy
   generation — it pulls in `net.bytebuddy:byte-buddy:1.12.18` instead,
   which is exactly why the illegal-access issue from §4 of
   `CHANGES-and-WHY.md` is fully gone, not just patched around.
   EasyMock 5.6.0 likewise now uses `org.ow2.asm:asm:9.8` (a current,
   actively-maintained ASM release) instead of its old bundled copy.
   One real remaining risk found: **ZK 8.6.0.1's own `zel` module directly
   declares `javassist:3.18.2-GA`** (2014-era) as a dependency in its
   published POM — not something excludable by bumping anything else,
   since it's baked into ZK's own metadata. Checked ZK's official JDK 17
   compatibility stance (web search): ZK 8.6 only ever claimed Java 6+
   *binary* compatibility, not real JDK 17/module-system support — that
   arrived with ZK 9/10.x. Rather than leave this as an unverified risk for
   the browser smoke test, forced `org.javassist:javassist` to 3.32.0-GA
   reactor-wide via a `<dependencyManagement>` override (javassist's core
   bytecode API is stable across this range). Verified: compiles clean,
   full reactor `mvn clean test` still green (940+153+222, 0 failures/errors).
   A full ZK major-version bump (9 or 10.x) would be the "properly
   supported" fix, but that's out of scope here the same way the Jakarta EE
   migration is (Phase 5) — this override closes the one concrete risk that
   was findable and fixable without a UI-level framework migration; ZK's
   actual AJAX/rendering behavior under JDK 17 still isn't something a unit
   test suite can confirm, hence step 9's manual smoke test.
9. **[done]** Full regression pass: unit tests (green, see step 6) +
   a manual smoke test of the running app — exactly the step that turned
   out to matter. Manually running the app surfaced a real bug the entire
   automated suite couldn't have caught: Spring Security's default
   `HttpFirewall` started rejecting every ZK AU (Ajax Update) request with
   `RequestRejectedException: ... potentially malicious String ";"`, since
   ZK's own URL scheme uses `;` to encode component/desktop metadata inline
   in the path, and Spring Security's `StrictHttpFirewall` blocks raw `;`
   by default. Fixed by wiring a `StrictHttpFirewall` bean with only
   `allowSemicolon` relaxed via the namespace config's `<http-firewall ref="..."/>`
   element (not the fully-permissive `DefaultHttpFirewall` — every other
   protection stays in place). Verified both via the existing test suite
   (still 222/222 green — this XML loads through the same Spring context)
   and by actually starting the app and curling semicolon-containing URLs
   directly (200/302, no more `RequestRejectedException`). Full writeup in
   `jdk25-migration-baseline/CHANGES-and-WHY.md` §8 — this is the concrete
   proof of why step 9 can't be skipped even after a fully green `mvn test`:
   the servlet filter chain (where `HttpFirewall` lives) is never exercised
   by `SpringJUnit4ClassRunner`-style tests, only by a real HTTP request.
   Jeroen manually exercised the running app afterward and confirmed
   everything works — planner/Gantt views, workers, configuration all fine.

Exit criteria: JDK 17 CI workflow is green and required on PRs; JDK 11
workflow can be retired once the team is confident (keep it disabled-but-
present for one release cycle as a safety net). **Met — see step 2. Phase 2
complete.**

**Phase 2 complete marker:** commit `948d021b83f05c63a25f2f2daabf8e5ce0957de4`
("Switching CI workflows from 11 to 17 for GitHub.", 2026-08-12) is the last
commit of Phase 2 — `git checkout 948d021b83f05c63a25f2f2daabf8e5ce0957de4`
to go back to this exact state if Phase 3 needs to be rolled back to a known
good point. (Note: `JDK25_MIGRATION_PLAN.md` and `jdk25-migration-baseline/`
were untracked at this point — not part of this commit's tree — see the
reminder at the end of this document about sorting out where project docs
like these actually belong.)

---

## Phase 3 — JDK 21 currency pass

Goal: this hop should be much smaller than Phase 2 — JDK 17→21 has a far
shorter list of behavior changes than 11→17, and no framework-generation
change is required (Spring 5.3.x / Hibernate 5.6.x / CXF 3.5.x are all
JDK 21-compatible).

Steps:

1. **[done]** Set `maven.compiler.release` to `21`, built with a real JDK 21
   (`sudo update-java-alternatives -s java-1.21.0-openjdk-amd64`). Compiled
   clean, but the full test suite regressed on the first try: `libreplan-business`
   went from 940/940 to 254 errors. Root cause (found by writing a standalone
   reproduction rather than trusting surefire's truncated stack traces):
   Hibernate 5.6.15's own `byte-buddy:1.12.18` (`compile` scope) was winning
   Maven's dependency mediation over the newer `1.17.5` EasyMock itself
   prefers, and 1.12.18 predates full JDK 21 support — a ByteBuddy
   class-injection failure, not the illegal-reflective-access pattern from
   every Phase 2 fix. Forced `net.bytebuddy:byte-buddy` to **1.18.11**
   reactor-wide via `<dependencyManagement>` (same override pattern as
   javassist in Phase 2 step 8). Full writeup in
   `jdk25-migration-baseline/CHANGES-and-WHY.md` §10. **Verified: full
   reactor green again — 940+153+222 = 1315 tests, 0 failures, 0 errors.**
2. **[done]** Added the JDK 21 GitHub workflow (parked disabled at first,
   then swapped active once step 4 confirmed — see below).
   `.forgejo/workflows/*` still deliberately untouched.
3. **[done]** Checked every Phase-2-pinned dependency against Maven Central
   for a newer patch: Spring Framework (5.3.39), Spring Security (5.8.16),
   Hibernate (5.6.15.Final), hibernate-validator (6.2.5.Final), CXF
   (3.5.11), EasyMock (5.6.0), javassist (3.32.0-GA), `usertype.core`
   (6.0.1.GA — 7.0.0.CR1 exists but is a candidate release, still not
   chosen). All already on the latest patch in their respective lines —
   nothing to bump.
4. **[done]** Regression pass: full reactor `mvn clean test` green
   (940+153+222 = 1315, 0 failures/errors) under JDK 21 (see step 1), plus
   Jeroen manually exercised the running app under JDK 21 and confirmed
   everything works — no repeat of Phase 2's firewall-style surprise this
   time, the automated suite's ByteBuddy fix from step 1 was the only real
   issue this phase had.

Exit criteria: JDK 21 CI green and required; JDK 17 workflow kept as a
secondary/non-blocking check for one cycle, then retired. **Met** — swapped
`.github/workflows/ubuntu-24.04-jdk-21.yml` ↔ `ubuntu-24.04-jdk-17.yml.disabled`
via `git mv` (same mechanism as the Phase 2 CI switch). JDK 21 is now the
active required PR gate; JDK 17 kept disabled-but-present as the safety net.
**Phase 3 complete.**

**Phase 3 complete marker:** commit `022e6d93c0c30f3f226da2c4c2fd1988bdc22297`
("Done with phase 3. Switching GitHub workflows. Fixing ByteBuddy
dependencie.", 2026-08-12) is the last commit of Phase 3 —
`git checkout 022e6d93c0c30f3f226da2c4c2fd1988bdc22297` to go back to this
exact state if Phase 4 needs to be rolled back to a known good point. (Same
caveat as the Phase 2 marker: `JDK25_MIGRATION_PLAN.md` and
`jdk25-migration-baseline/` are still untracked, not part of this commit's
tree — see §5.)

---

## Phase 4 — JDK 25 (target)

Goal: reach the actual target. Same shape as Phase 3 — small delta expected
if Phase 2/3 were done properly, since the framework versions chosen in
Phase 2 were already selected for their JDK 17+ track record.

Steps:

1. **[done]** Set `maven.compiler.release` to `25`, built with a real JDK 25
   (`sudo update-java-alternatives -s java-1.25.0-openjdk-amd64`). Two real
   findings, not the "small delta" the plan expected:
   - `aspectjweaver:1.8.9`'s jar has a zip64 extra field JDK 25's zip
     reader rejects outright (confirmed genuine — not local corruption —
     by testing the same jar with `unzip -t`, JDK 17's `jar tf`, and JDK
     25's `jar tf`: only JDK 25 rejected it). Bumped to **1.9.25**.
   - **Bigger finding:** Spring Framework 5.3.x bundles its own private,
     shaded copy of ASM inside `spring-core.jar` for classpath component
     scanning — not a swappable dependency the way javassist/byte-buddy
     were — and that bundled ASM cannot parse Java 25 class files (major
     version 69) at all. Spring 5.3.x is EOL and won't be patched for this.
     **Decision (Jeroen's call, presented as an explicit choice, not made
     unilaterally):** capped `maven.compiler.release` at **21** rather than
     `25`, while continuing to build/test/run everything under the actual
     JDK 25 JVM. The bytecode format and the runtime JVM version are
     genuinely independent — a JDK 25 JVM executes class-file-version-65
     bytecode identically to version-69, and every JDK-25-runtime property
     that actually matters (module encapsulation, reflection access,
     deprecated-API removal) is a JVM-version property, not a
     bytecode-format property. The alternative (pulling Phase 5's Jakarta
     EE migration forward just for a modern bundled ASM) would have been a
     far bigger scope change than "reach JDK 25" was meant to be. Full
     reasoning in `jdk25-migration-baseline/CHANGES-and-WHY.md` §11.
   **Verified: full reactor `mvn clean test` green — 940+153+222 = 1315
   tests, 0 failures, 0 errors — genuinely running on JDK 25 end to end.**
2. **[done, adapted]** `.forgejo/workflows/ubuntu_24.04-jdk-25.yml` left
   untouched at Jeroen's explicit request (same GitHub-only boundary as
   every other CI change this migration made). Added the GitHub-side
   equivalent instead — `.github/workflows/ubuntu-24.04-jdk-25.yml` — since
   no such workflow existed yet to promote. Parked disabled at first, then
   swapped active together with step 5 below, same pattern as every
   previous phase's CI switch.
3. **[done]** Nothing further found beyond the two issues already surfaced
   and fixed in step 1 (`aspectjweaver`, Spring's bundled ASM) — the full
   green test suite from step 1 already covers this; no additional
   `--add-opens`/`--add-exports` flags or deprecated-API removals turned up.
4. **[done]** Full regression pass: green in step 1 (940+153+222, 0
   failures/errors under real JDK 25). Docker deployment smoke test: no
   Dockerfile/docker-compose for the *application* exists in this repo (the
   Docker deployment this project runs in is managed externally), so
   Jeroen tested it himself on his own infrastructure and confirmed it
   works correctly.
5. **[done]** Swapped `.github/workflows/ubuntu-24.04-jdk-25.yml` ↔
   `ubuntu-24.04-jdk-21.yml.disabled` via `git mv`, same mechanism as every
   previous CI switch. JDK 25 is now the active required PR gate; JDK 21
   kept disabled-but-present as the safety net.

Exit criteria: JDK 25 is the CI-required build/runtime JDK; `jdk11to25`
branch merges to `main`. **CI-required build/runtime JDK: met.** Merging
`jdk11to25` to `main` is a separate, deliberate step for Jeroen to trigger
whenever he's ready — not something to do automatically as part of this
plan. **Phase 4 complete.**

---

## Phase 5 — Jakarta EE namespace migration (stretch, do after Phase 4)

Not required to reach JDK 25 as a *runtime* — Spring 5.3.x / Hibernate
5.6.x / CXF 3.5.x / Tomcat 9 all run on a JDK 25 JVM while still speaking
`javax.*`, and Phase 4 proved that. But it turned out to matter more than
"just EOL risk": Spring 5.3.x's own bundled ASM can't parse Java 25 class
files at all, which is exactly why Phase 4 had to cap
`maven.compiler.release` at 21 instead of reaching 25 — see Phase 4 step 1
and `jdk25-migration-baseline/CHANGES-and-WHY.md` §11b. Spring 6 is the
only real fix for that specific gap.

Full scope investigation, phased execution plan, and the raw
classification data are in this same directory:

- **`Phase-5-jakarta-migration-scope.md`** — the findings: which `javax.*`
  packages actually need renaming vs. which are JDK-native and must never
  be touched (checked against this codebase, not assumed), the full
  dependency chain that has to move together, and the confirmed ZK
  Jakarta-compatibility research.
- **`Phase-5-javax-import-inventory.txt`** — the raw import data behind
  that classification.
- **`Phase-5-JAKARTA_MIGRATION_PLAN.md`** — the actual phased plan (5.1
  through 5.7), same discipline as this document.

Not started as of this writing — scoped as its own follow-up project,
deliberately not folded into reaching JDK 25, which is already done.

---

## 4. Tracking

Suggested commit/PR granularity: one PR per numbered step within a phase
(not one PR per phase) so review stays small and `git bisect` stays useful
if a step turns out to break something Surefire didn't catch.

## 5. Documentation placement — resolved

This file and `jdk25-migration-baseline/` (including `CHANGES-and-WHY.md`)
sat untracked at the repo root for the whole migration. Once Phase 4
completed, they were moved into `doc/technical/` (which already had
precedent for this kind of document — `GUVA_UPGRADE_ANALYSIS_REPORT.md`
from a prior upgrade), for Jeroen to `git add` from there himself.
