# Phase 7 — remaining dependency backlog (findings)

Companion to `JDK25_MIGRATION_PLAN.md`, `Phase5-found-bugs.md`, and
`phase6/Phase6-remaining-points-plan.md`. Phases 1–6 closed out the JDK 11→25 bump, the full
Jakarta EE namespace migration, and every cataloged behavioral bug found along the way. This is a
different kind of debt: **dependencies nobody touched during any of that**, because upgrading them
wasn't required to reach JDK 25 or Jakarta EE — they were never blocking anything, just quietly
aging. Prompted by Jeroen asking directly: "are there any old libraries left that need updating? I
don't mean minor updates, but crossing several years of back log?"

**Status as of 2026-08-15: findings only, nothing fixed yet.** This document is the survey;
execution (if/when Jeroen wants it) is its own follow-up, same discipline as every other phase —
small, independently-verified steps, not a big-bang rewrite.

**Update (2026-08-19): the two drop-in Category 1 bumps are done.** `commons-collections4`
4.1 → 4.6.0 and `quartz` 2.3.2 → 2.5.2, both in root `pom.xml`'s `dependencyManagement`. Full
reactor `clean install`, then the full test suite for both modules: `libreplan-business`
1217/1217 (0 failures, 0 errors, unchanged), `libreplan-webapp` 222 tests / 1 failure (the
same pre-existing, already-confirmed-unrelated `ResourceServiceTest` failure from Phase 6 —
nothing new). The 13 "skipped" webapp tests are pre-existing `@Ignore("Only working if you
have a JIRA server configured")`/Tim-SOAP-server markers, unrelated to this change.

**Update (2026-08-19, later same day): `jgrapht-core` 0.9.2 → 1.5.3 is also done.** Turned out
smaller in scope than the caution below implied — investigated the actual breaking changes
directly against the real 1.5.3 jar (`javap`, not just changelog reading) before touching
anything:
- `org.jgrapht.DirectedGraph` (deprecated since 1.1.0) is gone entirely by 1.5.3 — its methods
  live on the base `org.jgrapht.Graph` interface now. Every declaration typed as
  `DirectedGraph<V,E>` became `Graph<V,E>`.
- `org.jgrapht.alg.CycleDetector` moved package to `org.jgrapht.alg.cycle.CycleDetector` — same
  constructor shape, just the import changed.
- `SimpleDirectedGraph`, `DirectedMultigraph` (still constructed via their `Class<? extends E>`
  constructor), `TopologicalOrderIterator`, and the whole `Graph` interface's core methods
  (`addVertex`, `addEdge`, `removeVertex`, `removeEdge`, `containsEdge`, `vertexSet`, `edgeSet`,
  `inDegreeOf`, `outDegreeOf`, `incomingEdgesOf`, `outgoingEdgesOf`, `getEdge`) — all unchanged,
  confirmed via `javap` diff against the actual 1.5.3 jar.

Usage was confined to exactly the 3 files the table below already named, no test files touch
jgrapht directly. Mechanically renamed the type in all 3 (`Graph` for `DirectedGraph`,
`org.jgrapht.alg.cycle.CycleDetector` import) via a precision script that skipped
`SimpleDirectedGraph`/`DirectedMultigraph` (verified afterward: zero bare `DirectedGraph<` left
anywhere, `SimpleDirectedGraph`/`DirectedMultigraph` counts unchanged). Full reactor
`clean install`, then both modules' full test suites — `libreplan-business` 1217/1217,
`libreplan-webapp` 222/1 (the same pre-existing `ResourceServiceTest` failure, nothing new) — plus
a live Playwright smoke test (logged in, opened a project's Gantt chart with task dependencies
rendering via `GanttDiagramGraph`, opened Queue-based Resources Planning which exercises
`QueuesState`'s graph-building/cycle-detection/topological-sort path) with zero console errors
and zero server-side exceptions.

## Methodology

For every dependency in the root `pom.xml`'s `dependencyManagement` (the effective source of
truth for every module's actual resolved version), checked:
1. Current published version, via `curl` against the real Maven Central `maven-metadata.xml` for
   that artifact — not a web search, not an assumption.
2. Whether it's actually imported/used anywhere in application source
   (`grep -rl "import <package>"` across all three modules) — a large version gap on something
   nobody references isn't really "debt," it's just dead weight to delete.

## Category 1 — worth bumping (real, maintained upstream, several years behind)

| Library | Installed | Latest (checked 2026-08-15) | Gap | Used where |
|---|---|---|---|---|
| `org.jgrapht:jgrapht-core` | ~~0.9.2~~ **1.5.3 [DONE, 2026-08-19]** | 1.5.3 | ~8 years, API break was smaller than expected — see update above | `ganttzk/.../data/GanttDiagramGraph.java`, `libreplan-webapp/.../limitingresources/QueuesState.java`, `.../LimitingResourceQueueModel.java` |
| `org.apache.commons:commons-collections4` | ~~4.1~~ **4.6.0 [DONE, 2026-08-19]** | 4.6.0 | ~9 years | (not individually enumerated here — widely used utility library, standard collections helpers) |
| `org.quartz-scheduler:quartz` | ~~2.3.2~~ **2.5.2 [DONE, 2026-08-19]** | 2.5.2 | ~5 years | Scheduled jobs (`SchedulerManager`, `JobSchedulerConfiguration`) |

These are genuinely maintained, actively-released libraries where the gap is just accumulated
neglect, not a deliberate/frozen state. `jgrapht-core` is the one to treat carefully — going from a
0.x line to 1.x is very likely a real API migration (like the Hibernate 5→6 Criteria rewrite this
project already did once), not a version-number bump; the other two are much more likely to be
safe, close-to-drop-in bumps, but should still get the same "bump, compile, run the full test
suite, then manually smoke-test" treatment every other dependency in this migration got — no
exceptions just because they feel small.

## Category 2 — dead weight, confirmed unused (delete, don't "update")

**Revised 2026-08-15, before touching anything, per Jeroen's explicit request to make absolutely
sure first.** The original version of this section also listed `jfreechartengine`,
`org.jfree:jfreechart`, and `org.jfree:jcommon` as dead weight, based on
`grep -rl "import ..."` across Java source only. That was **wrong**, and re-verifying caught it:

- `libreplan-webapp/src/main/webapp/WEB-INF/zk.xml` sets ZK's global chart-rendering engine via a
  `<library-property>`:
  ```
  <name>org.zkoss.zul.chart.engine.class</name>
  <value>com.libreplan.java.zk.components.JFreeChartEngine</value>
  ```
  ZK loads this class **reflectively by string name** — invisible to both a Java `import` grep
  and to Maven's own `dependency:analyze` (which only sees compiled bytecode references, not
  string-configured reflection). This is exactly the trap `dependency:analyze` fell into when it
  flagged `jfreechartengine` "unused" all the way back in the Phase 1 baseline audit — that finding
  was a false positive, carried forward uncorrected until now.
- Confirmed the config isn't dead either: `org.zkoss.zul.Chart` (ZK's native `<chart>` component,
  the thing this engine actually renders) is genuinely used in three ZUL templates
  (`_listOrderElementAdvances.zul`, `montecarlo_function.zul`, `stretches_function.zul`) and two
  controllers (`MonteCarloGraphController.java`, `ManageOrderElementAdvancesController.java`) —
  live features (Monte Carlo simulation results, order-element advances charts), not leftovers
  from the Timeplot/Chart.js work.
- Confirmed `jfreechartengine`'s own compiled classes need `jfreechart`/`jcommon` at runtime by
  disassembling the jar directly (`javap -p -c` on every `.class` inside it): `JFreeChartEngine`
  and all five of its inner chart-type classes (`PieChart`, `Pie3dChart`, `BarChart`, `Bar3dChart`,
  `TimeSeriesChart`) reference `org.jfree.*` classes directly.

**Conclusion: `jfreechartengine`, `jfreechart`, and `jcommon` are all genuinely load-bearing and
must stay.** `SecurityUtils.java`'s `org.jfree.util.Log` import (the finding that originally made
`jfreechart`/`jcommon` look almost-unused) turned out to be irrelevant either way — those two
libraries were never going anywhere regardless of that one import, since `jfreechartengine` needs
them itself. Not worth even simplifying that one log import now, since it no longer changes
whether the dependency can be removed.

Only one item from the original survey survives full verification — checked via `grep -rl`
across **all** of `libreplan-business`, `libreplan-webapp`, and `ganttzk` (`src/main` **and**
`src/test`), plus a separate pass for string references in every `.xml`/`.properties`/`.zul` file
in the repo (the exact blind spot that missed the `zk.xml` chart-engine wiring above) — zero hits
anywhere, by either method:

- **`commons-lang` 2.6** (the pre-`commons-lang3` legacy artifact) — the pom's own comment already
  calls it out: `<!-- Commons Lang (legacy) -->`. Zero imports of `org.apache.commons.lang.*`
  (non-`lang3`) anywhere, zero string references anywhere. `commons-lang3` (already a separate,
  actively-used dependency at 3.4) covers everything this project needs.

## Category 3 — abandoned upstream (no newer version will ever exist)

Checked each against Maven Central's `maven-metadata.xml` `<release>` tag — in every case below,
the "latest" version returned is identical to what's already installed, because there's nothing
newer:

- **`com.jolbox:bonecp` 0.8.0.RELEASE** — the connection pool. Its author discontinued the project
  years ago in favor of HikariCP. **Confirmed test-scope only**: production uses a JNDI-provided
  datasource from the servlet container
  (`libreplan-business-spring-config.xml`'s `dataSource` bean is a plain
  `org.springframework.jndi.JndiObjectFactoryBean` looking up `java:comp/env/jdbc/libreplan-ds`),
  not BoneCP — BoneCP only appears in `libreplan-business-spring-config-test.xml` and
  `libreplan-webapp-spring-config-test.xml`. Not a production risk today, but worth swapping to
  HikariCP (the de facto modern standard, actively maintained, gets real security patches) since
  BoneCP never will again.
- **`org.beanshell:bsh` 2.0b5** — same story, final release ever. Likely an optional JasperReports
  scripting-engine dependency rather than something the app calls directly; lowest priority of
  this group to chase.
- **`com.googlecode.gettext-commons:gettext-commons` 0.9.8** — also its final release. This is the
  i18n tooling the whole project's translation pipeline is built around (`gettext-maven-plugin`,
  `.po` files, etc.) — not something to casually swap out; flagged for awareness, not action.
- **Cobertura** (`cobertura-maven-plugin` 2.7) and **`org.codehaus.mojo:tomcat-maven-plugin` 1.1**
  — both declared in root `pom.xml`'s `<pluginManagement>` but **not bound to any execution** (no
  `<executions>` block), so neither runs as part of the normal build lifecycle — confirmed by
  reading the plugin declarations directly. Cobertura itself is dead upstream (JaCoCo is the
  modern replacement for code-coverage reporting); the Tomcat plugin hasn't been touched by its
  maintainers in a long time either. Since neither actually executes today, low priority — but
  worth removing outright rather than leaving inert legacy config lying around indefinitely.

## Category 4 — frozen by design, not neglect (don't chase version numbers here)

- **Joda-Time** (2.9.3 → 2.14.3 exists) — Joda-Time's own maintainers declared it feature-frozen
  years ago, with `java.time` (built into the JDK since 8) as its official, explicitly-recommended
  successor. This codebase uses Joda-Time pervasively — `LocalDate`, `LocalTime`,
  `SortedMap<LocalDate, ...>`, etc. throughout `libreplan-business` and `libreplan-webapp`. Bumping
  the version number is trivial and safe (same library, just patched); actually retiring Joda-Time
  in favor of `java.time` would be its own large migration, comparable in scope to the
  Hibernate 5→6 Criteria rewrite this project already did in Phase 5 — every date/time-handling
  call site would need auditing. Worth knowing this exists as a large, real piece of debt; not
  something to fold into a routine version-bump pass.
- **`org.apache.commons:commons-math3`** and **`org.ehcache:ehcache`** — both already at their
  current latest release (3.6.1 and 3.12.0 respectively, matching Maven Central's `<release>`
  exactly). Not behind at all; included here only to record that they were checked.
- **`org.apache.commons:commons-fileupload2-jakarta-servlet5` 2.0.0-M4** — checked, and
  `2.0.0-M5` (still a *milestone*, not a GA release) is the newest thing that exists on Maven
  Central for this artifact. Apache hasn't shipped a stable 2.0 GA for the Jakarta-Servlet-5
  variant yet, project-wide, not just for LibrePlan. Nothing actionable here — just waiting on
  upstream.

## Suggested priority, if/when this becomes real work

Lowest risk, highest value first — matches the "small, independently-verified steps" discipline
every other phase in this migration used:

1. **Category 2 deletion** (`commons-lang` only — `jfreechartengine`/`jfreechart`/`jcommon`
   turned out to be load-bearing, see the revised Category 2 section above) — zero functional
   risk, confirmed-unused-everywhere code removal, shrinks the dependency tree for free.
2. **[DONE, 2026-08-19] `commons-collections4` and Quartz bumps** — were close to drop-in as
   predicted; full reactor compile + both modules' full test suites, zero regressions.
3. **BoneCP → HikariCP** (test-scope only) — contained blast radius (only touches the two
   test Spring configs), but needs its own care since it's a real behavioral swap of the
   connection-pool implementation used by the whole test suite, not just a version bump.
4. **[DONE, 2026-08-19] `jgrapht-core` 0.9.2 → 1.5.3** — see the update above; smaller than
   feared, two type renames across three files.
5. **Cobertura / tomcat-maven-plugin removal** — pure config cleanup, no runtime effect either way
   since neither is bound to an execution today.
6. **BeanShell, gettext-commons, Joda-Time→java.time, commons-fileupload2 GA wait** — no action
   available or warranted right now; revisit if/when upstream changes (a GA fileupload2 release
   ships) or if there's a specific reason to prioritize the Joda-Time migration.

No code changes have been made as part of writing this document — it's a survey, per Jeroen's
request, to decide from before doing anything.
