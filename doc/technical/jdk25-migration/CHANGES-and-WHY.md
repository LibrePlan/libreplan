# Design and code decisions made during the JDK 25 migration, and why

This is a companion to `JDK25_MIGRATION_PLAN.md` (repo root) and
`jdk25-migration-baseline/README.md`. Those describe the *plan* and the
*dependency baseline*; this file explains the individual judgment calls made
while executing each phase — things that weren't mechanical version bumps,
and the reasoning behind each one, so a future reader (including
future-Jeroen) doesn't have to reconstruct the "why" from a git blame.
Sections 1–9 are Phase 2 (JDK 17); Phase 3 (JDK 21) continues from §10.

---

## 1. `SchedulerManager.getJobClass` — raw `Class` → `Class<? extends Job>`

**File:** `libreplan-webapp/src/main/java/org/libreplan/importers/SchedulerManager.java`

**Trigger:** Spring 4.3.9 → 5.3.39 (Phase 2 step 3).

**What broke:** `JobDetailFactoryBean.setJobClass(...)` changed signature between
Spring 4 (`setJobClass(Class jobClass)` — a raw, unchecked type) and Spring 5
(`setJobClass(Class<? extends Job> jobClass)` — properly generic). Our code
loaded the job class by name via `Class.forName(String)`, which can only ever
return `Class<?>` — the compiler has no way to prove at compile time what a
runtime string names.

**Fix:** `Class.forName(name).asSubclass(Job.class)` instead of a raw
`Class.forName(name)` return, and typed `getJobClass`'s return as
`Class<? extends Job>`.

**Why `asSubclass` and not an unchecked cast:** an unchecked cast
(`(Class<? extends Job>) someClass`) would silence the compiler without
checking anything — if `JobClassNameEnum` ever pointed at a class that isn't
actually a `Job`, the failure would surface later, as a confusing
`ClassCastException` deep inside Quartz internals, far from the actual
mistake. `asSubclass` verifies the relationship *at the point of loading* and
fails there with a clear message if it doesn't hold. Strictly safer than what
Spring 4's looser API allowed, not just a compile-error silencer.

---

## 2. Password encoding: SHA-512+salt → BCrypt with transparent migration

**Files:**
- `libreplan-webapp/src/main/java/org/libreplan/web/users/services/IDBPasswordEncoderService.java`
- `libreplan-webapp/src/main/java/org/libreplan/web/users/services/DBPasswordEncoderService.java`
- `libreplan-webapp/src/main/java/org/libreplan/web/users/services/LDAPCustomAuthenticationProvider.java`
- `libreplan-webapp/src/main/java/org/libreplan/web/users/settings/PasswordModel.java`
- `libreplan-webapp/src/main/resources/libreplan-webapp-spring-security-config.xml`
- `libreplan-webapp/src/test/resources/libreplan-webapp-spring-security-config-test.xml`
- `libreplan-webapp/src/test/java/org/libreplan/web/test/users/services/DBPasswordEncoderServiceTest.java`

**Trigger:** Spring Security 4.2.3 → 5.8.16 (Phase 2 step 4).

### What broke

Spring Security 5.0 deleted the entire legacy password-encoding package
(`org.springframework.security.authentication.encoding.*` — `PasswordEncoder`,
`ShaPasswordEncoder`, `MessageDigestPasswordEncoder` — and
`org.springframework.security.authentication.dao.SaltSource`/
`ReflectionSaltSource`) outright, not renamed or deprecated-but-kept. The code
was already flagged as tech debt before this migration touched it:

```java
// TODO resolve deprecated
private PasswordEncoder passwordEncoder;
```

### What the old scheme actually did (verified from source, not memory)

Given the stakes — get this wrong and every existing user's password
silently stops working — the exact legacy algorithm was verified against the
**actual Spring Security 4.2.3 source** (still available locally via
`spring-security-core-4.2.3.RELEASE-sources.jar` and downloaded
`spring-security-crypto-4.2.3.RELEASE-sources.jar`, cross-checked against
LibrePlan's own bean wiring in `libreplan-webapp-spring-security-config.xml`),
rather than reconstructed from memory:

- `ShaPasswordEncoder(512)` → algorithm string `"SHA-512"`
  (`ShaPasswordEncoder.java`: `super("SHA-" + strength)`).
- `ReflectionSaltSource` with `userPropertyToUse="username"` → salt is the
  result of calling `getUsername()` on the `UserDetails` object
  (`ReflectionSaltSource.java`). `DBPasswordEncoderService` constructed that
  `UserDetails` as `new User(loginName, clearPassword, ...)`, so
  **salt = loginName**.
- `BaseDigestPasswordEncoder`/`MessageDigestPasswordEncoder`: merge format is
  `password + "{" + salt + "}"` (`BasePasswordEncoder.mergePasswordAndSalt`),
  digested once (no stretching — `iterations` defaults to 1 and was never
  configured otherwise), output as **lowercase hex**, not Base64
  (`encodeHashAsBase64` defaults to `false` and was never set in LibrePlan's
  XML — confirmed by reading `crypto/codec/Hex.java`'s alphabet, which is
  lowercase `0-9a-f`).

So, exactly: `lowercase_hex( SHA-512( UTF8(clearPassword + "{" + loginName + "}") ) )`.

### The decision: preserve compatibility, but modernize (not just patch around it)

Two options were considered:

- **A — reimplement the legacy algorithm as the permanent scheme.** Lowest
  effort, zero login disruption, but locks in a scheme already considered
  weak (deterministic salt = same salt every time for a given username,
  which defeats much of the purpose of salting).
- **B — migrate to BCrypt, with transparent verify-old-then-rehash-on-login**
  (the option chosen). More work, but actually moves off the weak scheme
  instead of just avoiding a compile error.

**B was chosen.** Rationale: this migration already treats "keep behaviour
identical while modernizing the plumbing" as the norm (see the
`SchedulerManager` fix above, and generally the whole phased-JDK-bump
approach) — but password hashing is exactly the kind of thing where "keep
the exact old behavior forever" is itself the wrong default when the old
behavior is already known-weak and modernizing it is genuinely tractable.

### Why this was less painful than it might have been

`LDAPCustomAuthenticationProvider` does **not** use Spring Security's
built-in `DaoAuthenticationProvider` + `PasswordEncoder.matches()`
delegation — `additionalAuthenticationChecks` is overridden to a no-op, and
verification happens by hand in `authenticateInDatabase`. Originally this
was a plain recompute-and-`String.equals()` comparison — which is exactly
the pattern that's fundamentally incompatible with BCrypt (BCrypt embeds a
fresh random salt on every `encode()` call, so encoding the same password
twice never produces the same string twice; there is no way to "recompute
and compare equal" with it, only "verify against an existing hash"). Because
this comparison was already fully custom code, not something wired through
Spring's internals, there was no framework machinery to fight — just replace
the comparison itself.

The exact same recompute-and-equals anti-pattern also existed independently
in `PasswordModel.validateCurrentPassword` (the "confirm your current
password before changing it" flow) — found by grepping for other
`.equals(...user.getPassword())` call sites rather than assuming the login
path was the only one. Both were fixed the same way.

### The new design

- `IDBPasswordEncoderService` gained two methods alongside the existing
  `encodePassword`:
  - `matches(clearPassword, loginName, encodedPassword)` — the real
    verification entry point, replacing every "recompute and equals"
    call site.
  - `needsRehash(encodedPassword)` — true if a stored hash is still in the
    legacy format. **Why this exists as its own method, rather than
    inferring "needs rehash" from whether re-encoding changes the string:**
    that inference is wrong for BCrypt specifically — re-encoding an
    *already-BCrypt* hash also never matches the original (different random
    salt each time), so "did the encoding change" can't be the signal
    for "is this still on the old scheme." The format itself has to be
    checked directly (BCrypt hashes self-identify with a `$2` prefix).
  - The `@FunctionalInterface` annotation was removed from the interface,
    since it now has two abstract methods, not one. (Checked first that
    nothing instantiated it as a lambda — only one implementation exists,
    always injected as a field.)
- `DBPasswordEncoderService` now holds a single
  `org.springframework.security.crypto.password.PasswordEncoder` (BCrypt,
  wired via Spring config, same "configurable from XML" flexibility the
  class's javadoc always promised) plus the hand-written legacy digest
  function for the verification fallback.
- `LDAPCustomAuthenticationProvider.authenticateInDatabase`: on a successful
  `matches()` against a legacy-format hash, immediately re-encodes with the
  current scheme and saves it, reusing the same "update user, then
  `saveUserOnTransaction`" pattern the class already used for the LDAP
  password-sync branch a few lines above.
- `PasswordModel.validateCurrentPassword`: switched to `matches()`, but
  deliberately does **not** trigger a rehash itself — whenever this check
  succeeds, the caller's very next step is always to set a *new* password,
  which already writes with the current scheme via `encodePassword`
  regardless. Adding a rehash here would just be redundant, immediately
  overwritten work.
- Spring XML: the `passwordEncoder`/`saltSource` bean pair became a single
  `BCryptPasswordEncoder` bean (both main and test security config).

### Known limitation, accepted deliberately

Rehashing only happens on a successful *login*. A user who never logs in
again after this migration ships stays on the legacy SHA-512 scheme
indefinitely. Closing that gap completely would need a forced reset for
dormant accounts — treated as a separate, later decision, not bundled into
this migration (consistent with how the plan already scopes the Jakarta EE
namespace migration as separate future work rather than folding it in here).

### Test coverage added

The existing `DBPasswordEncoderServiceTest.testEncodePassword` asserted
`encodePassword(...).equals(user.getPassword())` — which would now be a
**false failure** on every run, since BCrypt never reproduces the same
output twice for the same input. Rewritten to use `matches()`.

Two new tests were added, since the legacy-verification path had zero
coverage otherwise (the bootstrap users used throughout this test class are
always created fresh with the *current* scheme — they never exercise the
legacy branch at all):

- `testMatchesAcceptsLegacyHash` — checks a hardcoded legacy-format hash
  (independently computed offline with Python's `hashlib`, not with any
  code from this codebase, specifically to cross-check the Java
  implementation against an independent source) verifies correctly for the
  right password+username, and rejects a wrong password and a wrong
  username (confirming the salt really is username-derived).
- `testNeedsRehashDistinguishesLegacyFromCurrentHashes` — confirms a legacy
  hash reports `needsRehash() == true` and a freshly-encoded current hash
  reports `false`.

### One more thing checked before any of this shipped: does BCrypt fit the DB column?

`user_table.password` was traced back to its origin in
`db.changelog-database.xml` (changeset `initial-database-creation-95`):
`VARCHAR(255) NOT NULL` (a later changeset only drops the `NOT NULL`, never
touches the size). Old SHA-512 hex digests are 128 characters; BCrypt's
default output (`$2a$10$<22-char-salt><31-char-hash>`) is 60. Both fit
comfortably — no schema migration needed. Worth checking explicitly rather
than assuming: a column sized tightly to the old format (say, exactly 128)
would have silently truncated every new hash on write.

---

## 3. Spring XML schema declarations: pinned versions → version-less URLs

**Files:** every `*-spring-config*.xml` and `*-spring-security-config*.xml`
under `libreplan-business` and `libreplan-webapp` (main and test).

**Trigger:** discovered via `DBPasswordEncoderServiceTest` failing with:

```
BeanDefinitionParsingException: Configuration problem: You cannot use a
spring-security-2.0.xsd or spring-security-3.0.xsd or spring-security-3.1.xsd
schema or spring-security-3.2.xsd schema or spring-security-4.0.xsd schema
with Spring Security 5.8. Please update your schema declarations to the 5.8
schema.
```

**What was happening:** the XML configs' `xsi:schemaLocation` attributes
hardcoded specific old schema versions, e.g.
`.../spring-beans/spring-beans-4.3.xsd`,
`.../spring-security/spring-security-4.2.xsd`. Spring Security 5.8 refuses
to parse configuration declared against schema versions that old.

**Fix:** switched every hardcoded version to Spring's own documented
version-less form, e.g. `spring-beans.xsd`, `spring-security.xsd`,
`spring-context.xsd`, `spring-tx.xsd`, `spring-aop.xsd`. These always
resolve to whatever schema matches the Spring version actually on the
classpath. Chosen over just bumping the numbers to match 5.3/5.8 today,
because this migration will bump these dependencies again in later phases
(Phase 3/4) — version-less URLs mean this class of break can't recur from a
routine version bump; it only has to be gotten right once.

---

## 4. Hibernate 5.1.1 → 5.6.15.Final: the fix that closed out the illegal-access chain

**Files:**
- `libreplan-business/src/main/java/org/libreplan/business/workingday/hibernate/EffortDurationType.java`
- `libreplan-business/src/main/java/org/libreplan/business/workingday/hibernate/ResourcesPerDayType.java`
- root `pom.xml` (`hibernate-core`, `hibernate-ehcache`, `hibernate-validator`,
  `org.jadira.usertype:usertype.core`, `org.easymock:easymock`, and removal
  of `org.easymock:easymockclassextension`)

**Trigger:** Hibernate Core 5.1.1 → 5.6.15.Final (Phase 2 step 5) — the step
that was always going to be the real test of whether this migration works,
since this is the illegal-reflective-access failure that's been the driving
reason for Phase 2 since before any code was touched (see
`JDK25_MIGRATION_PLAN.md` §1 and the original diagnosis: Hibernate's
javassist-based proxy factory calling `ClassLoader.defineClass` illegally,
blocked by JDK 16+'s default module encapsulation).

### Three separate breaks, same underlying pattern

Getting from "compiles" to "tests actually pass" took three fixes, each one
surfaced only by actually *running* the test suite after the previous fix,
not predictable from a dependency version bump alone:

1. **Two custom `UserType` implementations didn't match Hibernate's current
   interface.** `EffortDurationType` and `ResourcesPerDayType` implemented
   `nullSafeGet`/`nullSafeSet` against
   `org.hibernate.engine.spi.SessionImplementor`; Hibernate's `UserType`
   interface itself now declares these methods against the broader
   `SharedSessionContractImplementor`. Straightforward exact-signature fix
   (grepped the whole codebase first to confirm these were the only two
   `UserType` implementations, so nothing else needed the same change).

2. **`org.jadira.usertype:usertype.core` 5.0.0.GA (2015) called a Hibernate
   SPI method that no longer exists.** `NoSuchMethodError:
   SessionFactoryImplementor.getProperties()`, thrown from this library's
   own `AbstractUserTypeHibernateIntegrator` at every `sessionFactory`
   startup — Hibernate removed that method somewhere in the 5.x line, after
   usertype.core 5.0.0.GA was compiled against it. This library is used for
   exactly two Joda-Time `LocalTime` column mappings in
   `WorkReports.hbm.xml` (confirmed by grep before touching anything).
   Bumped to 6.0.1.GA (latest stable GA — 7.0.0.CR1 exists but is a
   candidate release, not chosen for a production dependency).

3. **EasyMock 3.4 bundles its own ancient shaded CGLIB.** Confirmed this
   directly rather than assuming: `unzip -l` on `easymock-3.4.jar` showed
   237 classes under `org/easymock/cglib/*` packaged *inside* the jar
   itself (not a separate `cglib` dependency that could be version-bumped
   independently). Generating a mock proxy for a concrete class hit the
   identical illegal-access pattern as Hibernate's javassist proxy factory
   — same root cause, different library. Bumped EasyMock 3.4 → 5.6.0.
   While investigating this, also found and removed the
   `easymockclassextension` 3.2 dependency (declared in all 4 module poms):
   `unzip -l` on *that* jar showed **zero** `cglib` classes, and a
   repo-wide grep found nothing importing its
   `org.easymock.classextension` package — this artifact's functionality
   was merged into core `easymock` back in version 3.2, so it had been dead
   weight for years, not something that needed migrating. Removed rather
   than bumped.

### Result

`libreplan-business`: all 940 tests pass (0 failures, 0 errors) — the
`InaccessibleObjectException` count in the full test-run log went from 342
occurrences (before any of this) to 0. `ganttzk`: all 153 tests pass.
`libreplan-webapp`: 222 of 223 pass (13 pre-existing skips); the one
remaining failure is covered in §5 below.

---

## 5. Apache CXF 3.1.7 → 3.5.11: the fourth (and last, for Phase 2) instance of the same pattern

**Trigger:** Phase 2 step 6.

`libreplan-webapp`'s `JiraRESTClientTest.testGetAllLablesFromInValidLabelUrl`
was, at the end of §4, the one test still failing:

```
InaccessibleObjectException: Unable to make field private static volatile
java.net.Authenticator java.net.Authenticator.theAuthenticator accessible:
module java.base does not "opens java.net" to unnamed module
	at org.apache.cxf.common.util.ReflectionUtil.setAccessible(...)
	at org.apache.cxf.transport.http.CXFAuthenticator.addAuthenticator(...)
```

Same illegal-reflective-access pattern as every fix in §4 — a fourth
library (Apache CXF 3.1.7) doing the same kind of thing Hibernate's
javassist proxy factory, jadira's SPI integrator, and EasyMock's bundled
CGLIB all did: `CXFAuthenticator.addAuthenticator` reflectively opens a
private JDK field to install a global `java.net.Authenticator`, blocked by
JDK 16+'s default encapsulation.

**Fix:** plain version bump, `cxf-rt-transports-http` /
`cxf-rt-frontend-jaxrs` / `cxf-rt-rs-client` 3.1.7 → **3.5.11** (all three
declared directly in root `pom.xml`). No compile-level changes needed
anywhere. The pre-existing woodstox-core-asl exclusion (issue #2067, see
the comment already in `pom.xml`) was left in place as a defensive safety
net, even though `dependency:tree -Dincludes=org.codehaus.woodstox:woodstox-core-asl`
no longer shows that transitive at all with 3.5.11 — costs nothing to keep,
and removing a safety net that's still documented and harmless isn't worth
the risk of some other CXF module reintroducing it later.

**Result:** full reactor `mvn clean test` green —
**940 (`libreplan-business`) + 153 (`ganttzk`) + 222 (`libreplan-webapp`) =
1315 tests, 0 failures, 0 errors** (13 pre-existing skips in webapp).
`JiraRESTClientTest` passes. This closes out every illegal-reflective-access
failure predicted back in Phase 0/§1 of `JDK25_MIGRATION_PLAN.md` — four
separate libraries (Hibernate, jadira usertype, EasyMock, CXF), same root
cause each time, each one only discoverable by actually running the tests
after the previous fix, not from a dependency changelog alone.

---

## 6. `javax.servlet-api` 3.1.0 → 4.0.1: plain bump, one caveat for the smoke test

**Trigger:** Phase 2 step 7.

Plain version bump, matches the Servlet 4.0/Tomcat 9 baseline `HACKING.rst`
already documents for Windows/manual deployment. No compile changes, WAR
still packages correctly with `servlet-api` correctly absent (still
`provided` scope).

**Caveat worth remembering for the manual smoke test in §7 below:** checked
what servlet-api version `jetty-maven-plugin` 9.4.56.v20240826 (local
`mvn jetty:run` dev testing) actually resolves against internally — still
3.1.0. Jetty 9.4 never implemented Servlet 4.0 at all (that arrived with
Jetty 10). Servlet 4.0 is a backward-compatible superset of 3.1 (mostly
HTTP/2 Server Push additions), so this is expected to keep working fine
under `jetty:run` regardless — but a working `jetty:run` smoke test doesn't
actually prove Servlet 4.0 behavior, only a real Tomcat 9+ deployment does.

---

## 7. Forcing `javassist` to a modern version reactor-wide

**File:** root `pom.xml` (`<dependencyManagement>`, new entry after the ZK block).

**Trigger:** Phase 2 step 8 (`dependency:tree` recheck flagged in Phase 1's
baseline as a thing to revisit once Hibernate/ZK were touched).

With Hibernate's own javassist usage gone (§4 — Hibernate 5.6 uses
`net.bytebuddy` instead now, confirmed by the regenerated dependency tree),
the one remaining old-javassist source in the whole reactor is **ZK
8.6.0.1's own `zel` (expression-language) module**, which declares
`javassist:3.18.2-GA` (2014-era) as a direct dependency in its own
published POM. This isn't something excludable by bumping some other
artifact — it's baked into ZK's own metadata, and EL expression evaluation
is real, constantly-exercised runtime code in ZK page rendering, not a
theoretical corner.

Checked ZK's official JDK 17 stance before deciding what to do about it: ZK
8.6 only ever claimed Java 6+ *binary* compatibility, never real JDK 17 or
module-system support — that arrived with ZK 9/10.x. A full ZK major-version
bump would be the "properly supported" fix, but that's a UI-framework
migration on the scale of the Jakarta EE work already deliberately scoped
out to Phase 5 — not something to fold into a JDK compatibility pass.

**What was done instead:** forced `org.javassist:javassist` to **3.32.0-GA**
reactor-wide via a `<dependencyManagement>` override, rather than leaving
the old version as an unverified risk. javassist's core bytecode-generation
API has been stable across this version range, so this is a low-risk way to
close the one concrete, checkable part of the ZK-on-JDK17 risk (an old
library doing illegal reflective access, the same pattern fixed four times
already in §4–§5) without touching ZK itself. Verified:
`dependency:tree -pl ganttzk` (run without `-q`, which otherwise swallows
the plugin's own console output — same gotcha as regenerating the wrapper
in Phase 1) confirms `javassist:3.32.0-GA` is what actually resolves now,
and the full reactor test suite is still green after the change
(940+153+222, 0 failures/errors).

**What this does *not* cover:** ZK's actual AJAX/rendering behavior in a
real browser isn't something a unit test suite exercises — `ganttzk`'s 153
tests passing is good evidence at the code level, but the manual smoke
test (Phase 2 step 9) is what actually confirms the UI itself works under
JDK 17. Sure enough, exactly that surfaced a real bug — see §8.

---

## 8. ZK's own URLs vs. Spring Security's stricter default firewall

**File:** `libreplan-webapp/src/main/resources/libreplan-webapp-spring-security-config.xml`

**Trigger:** Phase 2 step 9, the manual smoke test — this is precisely why
that step exists rather than treating a green `mvn test` as sufficient.
Running the actual app and clicking around produced a live failure the
entire automated test suite never could:

```
org.springframework.security.web.firewall.RequestRejectedException: The
request was rejected because the URL contained a potentially malicious
String ";"
	at org.springframework.security.web.firewall.StrictHttpFirewall.rejectedBlocklistedUrls(...)
```

**What's happening:** ZK's own URL scheme uses `;` to encode metadata
inline in the path for its AU (Ajax Update) requests — e.g. desktop and
component ids get appended after a semicolon. Spring Security's default
`HttpFirewall` (`StrictHttpFirewall`) rejects any URL containing a raw `;`
outright, as a defense against a known historical class of path-based
security-filter-bypass attack. Once Spring Security was upgraded as part
of this migration, every ZK AU request started failing this check — this
wasn't something the app did differently, it's Spring Security enforcing a
stricter default than before.

**Fix:** declared a `StrictHttpFirewall` bean with only `allowSemicolon`
relaxed, and wired it in via the namespace-config `<http-firewall ref="..."/>`
element (confirmed to be the correct, documented mechanism for this by
extracting and reading the actual `spring-security-5.8.xsd` from the
`spring-security-config` jar rather than assuming from memory — its
`<xs:documentation>` says exactly "Allows a custom instance of HttpFirewall
to be injected into the FilterChainProxy created by the namespace").
Deliberately **not** switched to the fully-permissive `DefaultHttpFirewall`
— `setAllowSemicolon(true)` on `StrictHttpFirewall` (confirmed to exist via
`javap` on the actual 5.8.16 jar before using it) relaxes only the one
check ZK's URL scheme needs, keeping every other protection
(URL-encoded slash, backslash, null bytes, etc.) intact.

**Verified two ways:**
- The XML change loads correctly and the full `libreplan-webapp` test suite
  is still green (222/222, 0 failures/errors) — this file is loaded by the
  same Spring context the tests already exercise.
- Actually started the live app (`jetty:run`, port 8099 to avoid clashing
  with whatever was already using the default 8080) and issued real HTTP
  requests containing `;` (`/zkau;ns_1=foo`, `/;jsessionid=ABC123`) — both
  returned normal responses (200, 302) with no `RequestRejectedException`
  in the response body, where before this fix they'd 500. Stopped the
  verification instance afterward.

This is a good example of why step 9 can't be skipped or treated as
optional even after a fully green test suite: Spring Security's servlet
filter chain, and specifically the `HttpFirewall` that sits in front of
every request, is simply never exercised by `SpringJUnit4ClassRunner`-style
tests the way this codebase writes them — those load an `ApplicationContext`
and call service/DAO methods directly, never a real HTTP request through
Jetty's servlet pipeline. A behavior change in default filter-chain-level
security policy was always going to be invisible to `mvn test`, no matter
how green.

Jeroen then manually exercised the running app himself afterward (planner/
Gantt views, workers, configuration) and confirmed everything works. That
closes out Phase 2 — see the CI workflow swap (§9) for the last piece.

---

## 9. Closing out Phase 2: switching the CI gate from JDK 11 to JDK 17

**Files:** `.github/workflows/ubuntu-24.04-jdk-11.yml` ↔
`ubuntu-24.04-jdk-17.yml.disabled`.

**Trigger:** Phase 2 step 2, deferred back when it was written ("no point
gating CI on JDK 17 before the frameworks that actually run on it are in
place") until the rest of Phase 2 landed and the manual smoke test (§8)
confirmed the running app actually works.

**What was done:** swapped the two files' active/disabled state via
`git mv` (`ubuntu-24.04-jdk-11.yml` → `...jdk-11.yml.disabled`,
`...jdk-17.yml.disabled` → `ubuntu-24.04-jdk-17.yml`), so git records these
as clean renames rather than a delete+add. JDK 17 is now the active
required PR gate; JDK 11 stays present-but-disabled as the safety net the
plan called for, matching the existing convention this repo already used
for other disabled workflows (`.yml.disabled` suffix, picked up by neither
GitHub Actions nor any tooling, but still tracked and easy to re-enable by
renaming back). `.forgejo/workflows/*` deliberately untouched, same
boundary as every other CI change this migration made (Jeroen asked
specifically for GitHub-only in Phase 1).

**Phase 2 is now complete.** Every framework in the reactor that needed
bumping for JDK 17 has been (Spring, Spring Security, Hibernate, jadira
usertype, EasyMock, CXF, the servlet API, and ZK's own stray old
javassist), the full automated test suite is green (1315 tests, 0
failures/errors), and the running app has been manually verified to work.
Next up per `JDK25_MIGRATION_PLAN.md`: Phase 3, the JDK 21 currency pass —
expected to be a much smaller hop than Phase 2, since no framework
generation change is required this time.

---

## 10. Phase 3 (JDK 21): a dependency-mediation conflict, not an old-library problem this time

**File:** root `pom.xml` (`<dependencyManagement>`, new `net.bytebuddy:byte-buddy` entry).

**Trigger:** Phase 3 step 1 (`maven.compiler.release` → `21`). Compiling
was clean, same as every JDK bump so far — but this time the full test
suite immediately regressed: `libreplan-business` went from 940/940 green
(JDK 17) to 254 errors, and the reactor build stopped before `ganttzk` or
`libreplan-webapp` even ran their tests (Maven halts on the first failing
module by default).

**Diagnosis process, since the surefire text report truncated the real
cause (same limitation hit repeatedly in Phase 2 — nested causes under an
outer `ExceptionInInitializerError`/`RuntimeException` don't always print
fully):** wrote a tiny standalone reproduction (`Repro.java`, using
`dependency:build-classpath` for the exact test classpath) that called
`EasyMock.createNiceMock(OrderVersion.class)` directly and printed every
exception in the cause chain by hand, rather than trust surefire's
truncated output. That surfaced the real error:

```
java.lang.IllegalArgumentException: org.libreplan.business.scenarios.entities.
OrderVersion$$$EasyMock$1 must be defined in the same package as
org.easymock.internal.ClassProxyFactory
	at net.bytebuddy.dynamic.loading.ClassInjector$UsingLookup.injectRaw(...)
```

A ByteBuddy class-injection failure — genuinely different from every fix in
§4–§7, which were all "old library does illegal reflective access."
`dependency:tree -Dverbose` explained why: **Hibernate 5.6.15 declares
`net.bytebuddy:byte-buddy:1.12.18` as a direct `compile`-scope dependency**,
and Maven's "nearest declaration wins" mediation picked that over the
newer `1.17.5` EasyMock itself actually prefers (a deeper, test-scope
transitive) — confirmed directly in the verbose tree output:
`(net.bytebuddy:byte-buddy:jar:1.17.5:test - omitted for conflict with 1.12.18)`.
1.12.18 (2022) predates full JDK 21 support; EasyMock's own preferred
1.17.5 would likely have worked fine, but never got the chance to, because
Hibernate's older declaration silently won the resolution on every module
that depends on `hibernate-core` (`libreplan-business` and, transitively,
`libreplan-webapp` — this is why only `libreplan-business` failed first:
Maven stopped the reactor there before `libreplan-webapp` got a chance to
hit the same thing).

**Fix:** forced `net.bytebuddy:byte-buddy` to **1.18.11** (latest stable)
reactor-wide via a `<dependencyManagement>` override — same pattern as the
javassist fix in §7, for the same underlying reason: a version-pinned
`<dependencyManagement>` entry always wins Maven's mediation regardless of
which transitive path declares it nearest, so this fixes it for every
module at once rather than patching around it per-module. Verified the
override actually took effect via `dependency:tree -Dverbose` again
(`version managed from 1.12.18`/`1.17.5`), then the full reactor test suite:
**940 + 153 + 222 = 1315 tests, 0 failures, 0 errors** — the exact same
green result as the end of Phase 2, now under JDK 21.

**Why this is worth calling out even though it "wasn't the old-library
pattern":** it's a reminder that `dependencyManagement` overrides made
earlier in the migration (javassist in §7) aren't the only place version
conflicts can hide — any time two dependencies each bring their own
transitive copy of the same library, whichever one Maven's mediation
happens to pick wins for the *entire* reactor, even for consumers that
have nothing to do with the dependency that "won." Worth remembering for
Phase 4 (JDK 25): re-run `dependency:tree -Dverbose` early rather than
assuming a clean compile means dependency resolution is fine.

**Steps 2–4 (workflow, patch-check, regression pass):** added the JDK 21
GitHub workflow, checked every Phase-2-pinned dependency against Maven
Central (all already on the latest patch in their line, nothing to bump),
and Jeroen manually exercised the running app under JDK 21 himself and
confirmed everything works — no repeat of Phase 2's firewall-style
surprise this time; the ByteBuddy fix above was the only real issue Phase
3 turned up. Closed out with the same CI-gate swap mechanism as Phase 2:
`git mv` to flip `ubuntu-24.04-jdk-21.yml` ↔ `ubuntu-24.04-jdk-17.yml.disabled`.
**Phase 3 complete.**

---

## 11. Phase 4 (JDK 25) — a corrupted-looking jar that wasn't, and a hard limit that made us cap `maven.compiler.release` at 21

### 11a. `aspectjweaver:1.8.9`'s zip64 extra field

**File:** root `pom.xml` (`aspectjweaver` version bump).

**Trigger:** Phase 4 step 1 (`maven.compiler.release` → `25`, first attempt).
Compilation failed immediately, before a single LibrePlan source file was
even touched:

```
error reading .../aspectjweaver/1.8.9/aspectjweaver-1.8.9.jar; Invalid CEN
header (invalid zip64 extra data field size)
```

**Diagnosis, checked rather than assumed:** this looked like it could be
local jar corruption (a bad download, unrelated to the JDK bump) or a
genuine JDK 25 behavior change. Tested directly instead of guessing:
`unzip -t` on the jar reported no errors; JDK 17's own `jar tf` read it
fine; **JDK 25's `jar tf` failed on the exact same file.** That's a
real, confirmed difference in how JDK 25's zip reader validates zip64
extra fields — stricter than every previous JDK tested — not a corrupted
download. `aspectjweaver` 1.8.9 is from 2016; its jar's zip64 extra field
was apparently just permissive enough that every zip tool up to (and
including) JDK 21 tolerated it, and JDK 25 doesn't.

**Fix:** bumped `aspectjweaver` 1.8.9 → **1.9.25** (latest). Before assuming
this was the *only* jar with the issue, proactively tested every jar on
all three modules' resolved classpaths (175 jars) against JDK 25's `jar tf`
directly, rather than waiting to discover more one compile-failure at a
time — none of the others had the same problem. Compiles clean afterward.

### 11b. Spring 5.3.x's bundled ASM can't parse Java 25 class files — capping `maven.compiler.release` at 21

**File:** root `pom.xml` (`maven.compiler.release` property).

**Trigger:** Phase 4 step 1, second attempt (after 11a) — compiled clean
that time, but the full test suite regressed hard: `libreplan-business`
went from 940/940 to 410 errors, and the reactor stopped before `ganttzk`/
`libreplan-webapp` ran at all.

**Root cause:**

```
NestedIOException: ASM ClassReader failed to parse class file - probably
due to a new Java class file version that isn't supported yet: ...
IllegalArgumentException: Unsupported class file major version 69
```

Major version 69 = Java 25. This is Spring's `<context:component-scan>`
machinery (`ClassPathScanningCandidateComponentProvider`) trying to read
compiled `.class` files to check for annotations — and it uses ASM to do
that. Critically, **this isn't a separate, swappable dependency** the way
javassist (§7) or byte-buddy (§10) were — Spring Framework bundles and
repackages its own private copy of ASM directly inside `spring-core.jar`
under the shaded package `org.springframework.asm.*`. There's no
`<dependencyManagement>` override possible for code that's been relocated
into another jar's own package namespace. And Spring 5.3.x — already at
its latest possible patch, 5.3.39, confirmed in Phase 2 §1 and re-checked
in Phase 3 §10 — is an EOL branch that will not receive further updates,
so a fix for Java 25's class file format was never coming for this line.

**The decision, and why it's not actually a compromise:** the migration's
real goal is "the app runs on a JDK 25 JVM," not "the compiled `.class`
files are literally in Java 25's on-disk format." Those are genuinely
different things — JVMs have always executed older-format bytecode
natively; a JDK 25 JVM runs class-file-version-65 (Java 21) bytecode
exactly as well as it runs version-69 bytecode, with zero behavioral
difference for anything that actually matters to "does this run on JDK 25"
(module encapsulation, reflection access, deprecated-API removal, all of
that is a *runtime JVM* property, not a *bytecode format* property).
Presented this as an explicit decision rather than making it unilaterally,
since it's a real, if narrow, trade-off. Jeroen chose to cap
`maven.compiler.release` at **21** while continuing to build, test, and run
everything under the actual JDK 25 toolchain (`update-java-alternatives -s
java-1.25.0-openjdk-amd64`) — over the alternative of pulling the Jakarta
EE migration (Phase 5: Spring 6, Hibernate 6, Tomcat 10/11, javax→jakarta
across the whole codebase) forward into this phase just to get a modern
bundled ASM, which would have been a much larger, riskier scope change
than "reach JDK 25" was ever meant to be.

**Result:** with `maven.compiler.release` at 21 and the actual JVM at JDK
25 (confirmed via `java -version` before every build/test run in this
phase), full reactor `mvn clean test` is green — **940 + 153 + 222 = 1315
tests, 0 failures, 0 errors** — the exact same result as the end of every
previous phase, now genuinely running on JDK 25 end to end.

### Closing out Phase 4

Step 2 (CI): `.forgejo/workflows/ubuntu_24.04-jdk-25.yml` left untouched at
Jeroen's explicit request — same GitHub-only boundary applied to every CI
change this migration made. Added the GitHub-side equivalent instead
(`.github/workflows/ubuntu-24.04-jdk-25.yml`), since no such workflow
existed yet for this JDK. Step 3 turned up nothing beyond what §11a/11b
already found. Step 4's Docker deployment smoke test: no
Dockerfile/docker-compose for the *application* lives in this repo (that
deployment is managed externally), so Jeroen tested it himself on his own
infrastructure and confirmed it works. Closed out with the same CI-gate
swap mechanism as every previous phase: `git mv` to flip
`ubuntu-24.04-jdk-25.yml` ↔ `ubuntu-24.04-jdk-21.yml.disabled`. JDK 25 is
now the active required PR gate.

**Phase 4 complete — the actual JDK 25 target has been reached.** Merging
`jdk11to25` to `main` is left as a deliberate, separate action for Jeroen
to trigger when he's ready, not something folded into this plan.
Phase 5 (the Jakarta EE namespace migration — javax→jakarta, Spring 6,
Hibernate 6, CXF 4, Tomcat 10/11) remains explicitly out of scope here,
same as it's been scoped since `JDK25_MIGRATION_PLAN.md` was first written:
optional future work, not required to reach JDK 25.
