# Phase 5 — Jakarta EE migration: scope findings

Companion to `JDK25_MIGRATION_PLAN.md` and `CHANGES-and-WHY.md` in this
same directory. Phase 4 (reaching JDK 25 as the runtime) is done, but
`maven.compiler.release` had to be capped at **21** rather than **25**,
because Spring Framework 5.3.x — the last javax-namespace Spring
generation, an EOL branch — bundles its own private ASM inside
`spring-core.jar` that cannot parse Java 25 class files at all (full
diagnosis in `CHANGES-and-WHY.md` §11b). The only real fix is Spring 6,
which requires the full Jakarta EE namespace migration this plan always
scoped as optional future work ("Phase 5"). This document captures what
was actually investigated when Jeroen asked "is this just a global
`javax`→`jakarta` string replace, or is there more involved" — the answer
is: much more involved, and here's exactly how much, checked against this
codebase rather than assumed.

**Status as of 2026-08-12: findings only, no Phase 5 work started.** This
is deliberately scoped as a separate future project, not something folded
into reaching JDK 25.

## Why it's not a safe global find-and-replace

Not every `javax.*` package is a Jakarta EE spec package. A number of
`javax.*` packages are part of the **JDK itself** (`java.base`,
`java.xml`, `java.naming`, `java.management` modules) and have no
`jakarta.*` equivalent at all — renaming them would just produce code that
references packages that don't exist.

Enumerated every distinct `javax.*` import actually used in this
codebase's application source (`libreplan-business`, `libreplan-webapp`,
`ganttzk` — raw data in `Phase-5-javax-import-inventory.txt` alongside this
file):

### Jakarta EE spec packages (would need renaming to `jakarta.*`) — ~600+ import sites

| Package | Import sites | Jakarta EE artifact |
|---|---:|---|
| `javax.xml.bind` (JAXB) | 263 | `jakarta.xml.bind` |
| `javax.ws.rs` (JAX-RS, used by CXF) | 147 | `jakarta.ws.rs` |
| `javax.validation.*` (Bean Validation) | ~173 | `jakarta.validation` |
| `javax.servlet.*` | 46 | `jakarta.servlet` |
| `javax.annotation.*` (`@Resource`, `@PostConstruct`) | 38 | `jakarta.annotation` |
| `javax.xml.soap` | 9 | `jakarta.xml.soap` |
| `javax.mail.*` | 13 | Jakarta Mail (new project name too, not just package — see below) |
| `javax.transaction.*` | 2 | `jakarta.transaction` |

### JDK-native packages (must NEVER be renamed) — ~53 import sites

| Package | Import sites | Why it stays `javax.*` forever |
|---|---:|---|
| `javax.xml.datatype` | 37 | Part of the `java.xml` JDK module |
| `javax.management.*` (JMX) | 7 | Part of the `java.management` JDK module |
| `javax.naming.*` (JNDI) | 5 | Part of the `java.naming` JDK module |
| `javax.net.ssl` | 4 | Part of `java.base` |

A blind `sed 's/javax\./jakarta\./g'` across the codebase would corrupt
all four of the JDK-native categories immediately. This is exactly why
[OpenRewrite](https://docs.openrewrite.org/) ships a dedicated Jakarta EE
migration recipe (`org.openrewrite.java.migrate.jakarta.*`) that knows the
real EE-vs-JDK boundary — that's the right tool for the mechanical part of
this, not a hand-rolled regex.

No `javax.persistence` usage was found anywhere — this codebase maps
entities purely through Hibernate's native `.hbm.xml` mapping files, not
JPA annotations, so that's one whole category of Jakarta migration
complexity (`javax.persistence` → `jakarta.persistence`) that doesn't
apply here.

## The dependency chain that has to move together

Renaming imports alone does nothing without the underlying libraries
actually shipping Jakarta-namespace artifacts. Everything below has to
move in lockstep, the same discipline used for Phases 2–4 (small,
independently-verified steps, never assume a version bump is "just a
number"):

- **Spring Framework** 5.3.39 → 6.x (this is *why* Phase 5 exists — 6.x
  ships a modern bundled ASM that can parse Java 25 class files, closing
  the gap that capped Phase 4 at release 21)
- **Spring Security** 5.8.16 → 6.x
- **Hibernate ORM** 5.6.15 → 6.x (jakarta.persistence-based; also carries
  real behavioral changes beyond namespace — criteria API restructuring,
  implicit-naming-strategy changes — not just a rename)
- **hibernate-validator** 6.2.5 → 8.x (jakarta.validation 3.x line)
- **Apache CXF** 3.5.11 → 4.x
- **`javax.servlet-api`** 4.0.1 → `jakarta.servlet-api` 5.0/6.0 (Tomcat
  10/11 baseline)
- **`javax.mail`** → Jakarta Mail / Angus Mail — this one is a project
  rename too (Eclipse-hosted successor), not just a package/groupId swap
- **ZK Framework** 8.6.0.1 → 9.6.0-jakarta or later (see below) — its own
  UI-framework migration, independent risk from everything else on this
  list

## ZK Framework's Jakarta support — checked, not assumed

This was the one genuine unknown going in: if ZK had no Jakarta-compatible
release line at all, Option B would have been dead on arrival regardless
of how much Spring/Hibernate/CXF work got done. Checked via web search
rather than assumed:

- ZK supports Jakarta Servlet from **ZK 9.6.0-jakarta** onward (Jakarta EE
  9 / Servlet 5.0).
- ZK 10.x also ships `-jakarta`-suffixed releases
  (`org.zkoss.zk:zk:10.0.0-jakarta` on Maven Central, under the same
  free/community artifact coordinates this project already uses — not a
  separate paid artifact).
- Sources: [Getting started with ZK-Jakarta](https://docs.zkoss.org/zk_installation_guide/getting_started_with_zk_jakarta),
  [ZK Installation Guide — New to Jakarta Servlet](https://www.zkoss.org/wiki/ZK_Installation_Guide/Before_You_Start/New_to_Jakarta_Servlet),
  [org.zkoss.zk:zk:10.0.0-jakarta on MvnRepository](https://mvnrepository.com/artifact/org.zkoss.zk/zk/10.0.0-jakarta)

**[RESOLVED, Phase 6.6.1, 2026-08-15]** ZK's licensing model has shifted across major versions
historically, so this was checked directly rather than assumed from the free Maven coordinate
alone. Pulled the actual `<licenses>` block out of the published POM (not a marketing page — the
authoritative metadata for what's actually distributed) for every core ZK artifact this project
depends on, at the exact version pinned in `pom.xml` (`10.3.0.1-jakarta`):

```
curl -s https://repo1.maven.org/maven2/org/zkoss/zk/zk/10.3.0.1-jakarta/zk-10.3.0.1-jakarta.pom \
  | grep -A3 "<license>"
```

`zk`, `zul`, `zkbind`, `zkplus` (groupId `org.zkoss.zk`) and `zcommon`, `zweb` (groupId
`org.zkoss.common`) are all published under:

```
<license>
    <name>GNU LESSER GENERAL PUBLIC LICENSE, Version 3</name>
    <url>https://www.gnu.org/licenses/lgpl.html</url>
    <distribution>repo</distribution>
</license>
```

This is ZK CE's license (free for open-source and commercial development/deployment), not the
GPL-compliant-only ZOL tier or a commercial-only license — same free tier the pre-migration ZK
8.6.0.1 relied on. Nothing about the `-jakarta` line changes this; it's the same open-source
artifact, just built against the Jakarta EE 9 namespace.

## Infrastructure outside this repo

- Tomcat 9 → Tomcat 10/11 in the externally-managed Docker deployment
  (same environment Jeroen manually smoke-tested for Phase 4 — no
  Dockerfile/docker-compose for the application lives in this repo, so
  this can't be verified from inside it).
- Local `jetty-maven-plugin` (currently 9.4.56, itself capped at Servlet
  3.1 — see `CHANGES-and-WHY.md` §6) would need Jetty 11/12 to actually
  serve `jakarta.servlet` for local `jetty:run` testing.

## Why this surfaced more risk than it might look like on paper

Every one of Phases 2–4 needed real, unpredictable debugging even for
"just" version bumps within the same generation of libraries — Hibernate's
proxy factory, `jadira usertype`'s removed SPI method, EasyMock's bundled
CGLIB, a Maven dependency-mediation conflict over ByteBuddy, Spring
Security's default firewall rejecting ZK's own URLs, ZK's stray old
javassist, `aspectjweaver`'s zip64 quirk under JDK 25 — none of these were
predictable from a changelog, all were found by actually compiling,
testing, and running the app. Phase 5 changes six major library versions
simultaneously across a namespace rename that touches ~600 import sites.
Expect a materially longer list of this kind of surprise, not zero.

## Recommendation

Treat Phase 5 as its own dedicated project with its own phased plan
document (small, always-compiling steps, same discipline as
`JDK25_MIGRATION_PLAN.md`), started deliberately rather than folded into
"reach JDK 25" — which has already been achieved (Phase 4 complete, JDK 25
is the CI-required runtime). Not started as of this writing.
