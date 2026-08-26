# Phase 5.1 baseline snapshot (pre-Jakarta)

Captured 2026-08-12, on branch `jdk11to25`, immediately after Phase 4
completed — `maven.compiler.release=21`, real JDK 25 runtime, full
javax-namespace stack (Spring 5.3.39 / Hibernate 5.6.15 / CXF 3.5.11 / ZK
8.6.0.1 / `javax.servlet-api` 4.0.1).

## Files

- `<module>-tree.txt` — `dependency:tree` for all 3 modules, the
  pre-Jakarta dependency graph to diff Phase 5.2's post-migration tree
  against.

## Test-suite baseline

Not re-run here — no code changed between Phase 4's completion and the
start of Phase 5.1, so the authoritative baseline is Phase 4's own final
result: **full reactor `mvn clean test` green, 940 (`libreplan-business`) +
153 (`ganttzk`) + 222 (`libreplan-webapp`) = 1315 tests, 0 failures, 0
errors** (13 pre-existing skips in `libreplan-webapp`). See
`JDK25_MIGRATION_PLAN.md` Phase 4 step 4 and
`CHANGES-and-WHY.md` §11 for the full detail.
