# Phase 1 dependency baseline (JDK 11, pre-Phase-2)

Captured 2026-08-11 on branch `jdk11to25`, after Phase 1 steps 1–4
(compiler/surefire/war-plugin bumps + Maven Wrapper) but before any Phase 2
framework uplift. Purpose: a diffable reference so Phase 2 changes can be
checked against "what was actually here before" rather than against memory.

Regenerate with:

```
./mvnw -pl <module> dependency:tree -DoutputType=text -DoutputFile=<path>
./mvnw -pl <module> dependency:analyze > <path>
```

(one module at a time — `dependency:tree`'s `-DoutputFile` does not
auto-namespace per module in a multi-module `-pl` invocation, it just
overwrites the same file for each module in turn).

## Files

- `<module>-tree.txt` — full `dependency:tree` for `libreplan-business`,
  `ganttzk`, `libreplan-webapp`
- `<module>-analyze.txt` — full `dependency:analyze` output (raw, includes
  build log — `dependency:analyze` forks the lifecycle to `test-compile` to
  get real bytecode usage, so a Liquibase `update` execution runs as a side
  effect; harmless/idempotent against a normal dev DB)
- `SUMMARY.txt` — just the "Used undeclared" / "Unused declared" warning
  blocks extracted from each `*-analyze.txt`

## Findings worth carrying into Phase 2

- **`javassist` is on two different, both very old, versions across the
  reactor**: `3.20.0-GA` (pulled in by Hibernate 5.1.1) vs `3.18.2-GA`
  (pulled in via ZK 8.6.0.1, both `ganttzk` and `libreplan-webapp`). Neither
  predates JDK 9 module support cleanly — javassist's ability to *read and
  generate* classfiles matching the running JVM's major version only
  matured in later 3.2x/3.3x releases. When Hibernate and ZK get bumped in
  Phase 2, check whether the new versions bring a newer javassist
  transitively; if not, this may need forcing to a newer version explicitly
  via `<dependencyManagement>`.
- **Spring's CGLIB/ASM usage will not show up in any dependency tree** —
  Spring 4.3.9 bundles a repackaged/shaded copy of both
  (`org.springframework.cglib.*`, `org.springframework.asm.*`) inside
  `spring-core.jar` itself, not as separate artifacts. This is the biggest
  runtime-encapsulation risk flagged in `JDK25_MIGRATION_PLAN.md` Phase 0/2,
  and it is invisible to `dependency:tree` — don't rely on tree/analyze
  output to rule it out. The Spring 5.3.x uplift in Phase 2 is what
  actually addresses this (Spring 5 ships a newer bundled ASM that
  understands newer classfile versions).
- No separate `cglib` or `asm` artifacts appear anywhere in the three trees
  — consistent with the above (everything using it bundles its own copy).
- `dependency:analyze`'s "used undeclared"/"unused declared" findings
  (see `SUMMARY.txt`) are pre-existing dependency-hygiene debt, not
  something introduced by the Phase 1 tooling bumps. Not in scope for the
  JDK migration itself, but worth a separate cleanup pass at some point —
  e.g. `libreplan-business` compiles against `spring-beans`/`spring-context`/
  `spring-tx` without declaring them directly (relies on `spring-orm`
  pulling them in transitively).
