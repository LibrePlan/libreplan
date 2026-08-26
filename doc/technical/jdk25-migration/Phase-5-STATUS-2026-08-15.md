# Phase 5 (Jakarta EE migration) — status report

Branch: `phase5-jakarta-migration`.

Continues directly from `Phase-5-STATUS-2026-08-14.md`, which ended with two open items: §4
"`libreplan-webapp` does not compile" (192 errors, 14 files) and §5 "Suggested next steps". Both
are closed by this report: `libreplan-webapp` now compiles, boots, and has been driven end-to-end
in a real browser (login, planner/Gantt view, resource load, order planning), including a full
replacement of the one widget that turned out not to survive the migration intact (the ZK Timeplot
load/earned-value chart).

```
mvn -pl libreplan-webapp -am -o -DskipTests -P-reports,-userguide,-i18n compile
```
compiles clean, zero errors, across the whole reactor.

## 1. `libreplan-webapp`'s 192 compile errors (previous report §4) — all resolved

Each of the five clusters from the previous report's finding was a distinct, unrelated fix:

- **`TimSoapClient` (SAAJ)** — `javax.xml.soap.*` was removed from the JDK itself in Java 11 and
  has no JDK-native fallback. Now imports `jakarta.xml.soap.*`, backed by the
  `jakarta.xml.soap-api` + `saaj-impl` dependencies already present in the reactor.
- **`ganttzk`'s `CallbackServlet.IServletRequestHandler`** — the previous report treated this as a
  possible third-party-jar interop gap ("needs a newer `ganttz` build, or an adapter/shim layer").
  It turned out not to need one: `ganttzk` is a **module of this same reactor**
  (`<module>ganttzk</module>` in the root `pom.xml`), not an external jar, so its
  `handle(HttpServletRequest, HttpServletResponse)` signature was migrated to
  `jakarta.servlet.*` directly, in step with every other servlet type in the codebase. No shim
  needed.
- **`BandboxSearch`/`Finder`/`JiraSynchronizationController` — missing `DataBinder`/
  `objectToString`** — `DataBinder.saveAttribute(Component, String)` is gone in ZK 10; there's no
  per-attribute equivalent. Replaced with `Util.saveBindings(this)` (ZK 10's `AnnotateBinder`
  saves a whole component's bound properties at once — `SELECTED_ELEMENT_ATTRIBUTE` was the only
  one `BandboxSearch` ever saved, so this is behavior-equivalent, not a workaround).
- **`DateConverter`/`DateTimeConverter`/`LocalDateConverter`/`TimeConverter`/`ChartFiller` — "does
  not override or implement a method from a supertype"** — ZK 10's `Converter` interface changed
  from a raw type to `IConverter<T>` (generic). Every converter now implements the correctly-typed
  interface (e.g. `IConverter<LocalDate>`), matching the signature ZK 10 actually calls.
- **`PersonalTimesheetController` — `Assert.notNull(Object)`** — Spring 6 removed the
  one-argument overload of `org.springframework.util.Assert.notNull`; a message argument is now
  required. Fixed by adding one (`"orderElemement must not be null"`).

None of these touched business logic — all five are framework-API-surface adaptations, confirming
the previous report's read that this was a second, independent body of work from the
Hibernate/Criteria migration.

## 2. Getting the app to actually boot and render under ZK 10 / Jakarta

Compiling clean was not the same as working — three more issues only showed up by actually
starting Jetty and loading a page:

- **`fixedLayout="true"`** on several `.zul` files (`leftPane.zul`, `leftTasksTree.zul`,
  `resourcesLoadLayout.zul`, `timetrackedtable.zul`) is a ZK-8-era attribute ZK 10 no longer
  supports the same way; removed from all of them. Two of the four live inside `ganttzk`'s own
  bundled resources (`ganttzk/src/main/resources/web/ganttz/**`), not `libreplan-webapp`'s source
  tree — worth remembering for any future bulk `.zul` fix, since a script scoped only to the
  webapp module misses these.
- **`AnnotateBinderInit` NPE** — ZK 10's binder-initialization hook
  (`org.zkoss.bind.impl.BindEvaluatorXUtil`/`AnnotateBinderInit.doAfterCompose`) was being invoked
  on page-root components with no `apply="..."` controller, and `Util.createBindingsFor` assumed
  one always exists. Fixed with a null-guard in
  `libreplan-webapp/src/main/java/org/libreplan/web/common/Util.java`.
- **Whole page rendering as a blank white screen.** Root cause: ZK 10's `sapphire` skin CSS bundle
  ships no `html`/`body` height rule at all, so the app's own `height:100%` layout chain had
  nothing to anchor to under a standards-mode DOM — everything was present and correct in the
  accessibility tree, but the page painted as blank. Fixed with an explicit
  `html, body { height: 100%; margin: 0; }` rule added to LibrePlan's own
  `common/css/libreplan.css` (not a ZK skin file — those aren't meant to be patched locally).

## 3. The Timeplot chart widget did not survive the migration — replaced with Chart.js

The company/order/resource-load "workload chart" (built on the ~2010-era vendored
`org.zkforge.timeplot`/Simile Widgets JS library) turned out to have a real, actively-triggered
bug once ported to ZK 10: its bundled Simile error handler called a function
(`SimileAjax.parseURLParameters`) that was never defined anywhere in the vendored jars, and with
that crash patched around, the widget's self-rearming `paint()` loop (re-arms itself via
`window.setTimeout` on every repaint) ran unbounded against ZK's AU update cycle — roughly 13–14
`/zkau` POST requests per second, forever, every one a 200 (a pure client-side runaway loop, not a
server error).

Rather than archaeology-debug 2010s minified JS with no upstream support — for a widget explicitly
called out as one of the application's strongpoints — the decision was to **replace** it with a
modern, actively-maintained charting library, reusing 100% of the existing server-side
data-preparation logic. Full design in `/home/jeroen/.claude/plans/swirling-wishing-bear.md`. In
short:

- **Chart.js 4.4.0**, pulled in via `org.webjars.npm:chart.js` (resolves through the project's
  existing Maven-mirror setup, no raw npm/CDN access needed) — same dependency-management workflow
  as everything else in this project. The jar's `META-INF/resources/**` webjar resources are
  auto-served by Jetty/Servlet 3.0+ with no servlet wiring, the same mechanism `zkwebfragment`
  already relies on.
- Server-side data prep (`ChartFiller`, `LoadChartFiller`/`StandardLoadChartFiller`,
  `EarnedValueChartFiller`) is **unchanged in its actual math** — same interpolation, same colors,
  same series ordering. Only the transport/rendering layer changed: a new `ChartSeries` value
  class replaces `Plotinfo`, and `ChartFiller.renderChart(...)` serializes the series to JSON and
  pushes it to the browser via `Clients.evalJavaScript("LibreplanChart.render(...)")` — the same
  `evalJavaScript` idiom this class already used, not a new pattern.
  `GraphicSpecificationCreator`/`CallbackServlet`-based data transport is gone entirely; the data
  Chart.js needs was already fully computed server-side, so there's no reason to round-trip it
  through a separate HTTP endpoint.
- New file: `libreplan-webapp/src/main/webapp/common/js/libreplan-chart.js` —
  `window.LibreplanChart.render(divId, config)`, ~120 lines. Destroys and recreates a Chart.js
  instance per call (matches how the old widget already worked: every zoom/checkbox change is
  already a full synchronous server round-trip, never a live/streaming update, so there's no need
  for incremental diffing).
- `Timeplot`/`Plotinfo` (ZK/timeplotz types) replaced by `Div`/`ChartSeries` at all three call
  sites: `CompanyPlanningModel`, `OrderPlanningModel`, `ResourceLoadController`.
- `dev.libreplan.zkoss:timeplotz` and `org.zkoss.zkforge:timelinez` dependencies removed entirely
  from both poms, along with the compatibility shims that existed only to patch them
  (`org.zkoss.zk.ui.util.DeferredValue`, the `timelinez/ext/simile-ajax/scripts/debug.js`
  resource-shadow patch) — both deleted once no longer needed.

### 3.1 Fixes made getting the Chart.js port visually correct

Three follow-on rendering bugs, found and fixed by comparing the new chart against the old one's
actual behavior (all in `libreplan-chart.js` unless noted):

- **Chart pinned to the top-left corner, not filling its container.** Chart.js with
  `responsive:false` sizes itself from the bare `<canvas>` element's own `width`/`height`
  *attributes* (native default 300×150), not the parent `<div>`'s CSS size. Fixed by setting
  `canvas.width`/`height` and `canvas.style.width`/`height` explicitly from the server-provided
  size.
- **Wrong layering — solid orange fill, no black capacity line or green load area.** The server's
  load-chart data encodes a specific "three full-height overlapping fills painted
  background-to-foreground" trick (orange = capacity+excess, opaque white = capacity line erasing
  the orange below it, green = actual load capped at capacity) — a real semantic in
  `ResourceLoadChartData`, not an artifact. Chart.js datasets all default to `order:0` and don't
  paint in array order when tied, so the intended-background series was landing on top. Fixed with
  an explicit `order` per dataset (`seriesArray.length - 1 - index`, so array position controls
  z-stacking correctly).
- **X-axis dates redundant, and wasted whitespace below the chart.** Since Chart.js's built-in
  hover tooltip (`interaction.mode:'index'`) already shows the date and every series' value at the
  cursor, matching the old widget's own hover behavior, the x-axis labels were dropped
  (`scales.x.display:false`) and the reclaimed space used to grow the chart from 150px to 190px.
- **Earned Value tab's legend clipped below the visible viewport** after the height increase — the
  `Tabbox` container's height was hardcoded to 200px at three separate call sites, independent of
  the chart's own height, so it didn't grow when the chart did. Made
  `ChartFiller.CHART_HEIGHT_PX` public and set the container height to
  `CHART_HEIGHT_PX + 50` (tab bar + padding overhead) at all three sites, verified via
  `scrollHeight === clientHeight` (no clipping).

### 3.2 Verified unchanged: server-side data output

Explicitly checked, since this replaces a rendering layer around numbers that must not have
shifted: `ChartFiller`'s interpolation helpers (`calculatedValueForEveryDay`, `groupByWeek`,
`groupAsNeededByZoom`, etc.) were read line-by-line against the pre-Chart.js version and are
untouched. Only the container class and the hand-off to the browser changed.

## 4. `LazyInitializationException` / a second runaway AU loop — root-caused and fixed

Separately from the Timeplot issue, the planner's Gantt task tree started throwing
`LazyInitializationException: Could not initialize proxy [SchedulingDataForVersion#N] - no
session`, which itself produced another runaway `/zkau` polling loop (ZK retrying the failed
render indefinitely).

- **First attempt (reverted):** setting `lazy="false"` on the relevant `Orders.hbm.xml`
  associations. This fixed the symptom but broke `ScenariosBootstrapTest` with a
  flush-ordering `TransientObjectException` — the wrong fix, backed out.
- **Root cause**, found via a temporary diagnostic try/catch injected into `ganttzk`'s
  `LeftTasksTreeRow.updateComponents()` (removed again once done): a Hibernate proxy is
  permanently bound to the session that created it. Several `TaskElementAdapter` accessor methods
  were reusing an already-instantiated, possibly-stale `TaskElement`/`OrderElement` reference
  across transaction boundaries — opening a *new* transaction doesn't let you initialize an
  already-stale proxy from a *closed* session; the entity has to be re-fetched by id inside the
  new session instead.
- **Fix:** `TaskElementAdapter.TaskElementWrapper.getSafeOrderElement()` re-fetches the owning
  `TaskElement` by id via `taskDAO.findExistingEntity(id)` inside the ambient transaction rather
  than trusting the in-memory reference, and the affected accessor methods (`getProjectHoursStatus`,
  `getProjectBudgetStatus`, `getTooltipText*`, `getBudget`, `getMoneyCost`, `toGantt`, and others)
  are wrapped in `transactionService.runOnReadOnlyTransaction(...)`. Two child-iteration loops
  (inside `getProjectHoursStatus`/`getProjectBudgetStatus`) needed the per-child id passed
  explicitly rather than reusing the wrapper's own field, to avoid a loop-variable-shadowing bug
  caught during self-review before it shipped.

## 5. Deferred, not part of this report's scope

- The Earned Value tab's checkbox/legend rows are visually taller than the old widget's. Jeroen has
  already started a separate Sapphire→Breeze theme migration in another branch and will fold this
  into that work rather than patch it here.

## 6. Suggested next steps

- **`libreplan-webapp`'s own test suite had never been run this migration** — running it for the
  first time (2026-08-15) found a config gap that failed 201 of 222 tests via a cascading
  `ApplicationContext` load failure; fixed (see `Phase5-found-bugs.md` #10), dropping that to 18
  genuine remaining failures (§11 of the same file) that still need the same
  characterization-and-fix treatment the DAO migration used, just scoped to `libreplan-webapp`
  instead of `libreplan-business`.
- **Two dependencies flagged since Phase 0 as outdated were never actually touched**:
  `mysql-connector-java` (still 5.1.46, 2018 — MySQL is the deprecated/alternate profile, Postgres
  is the real one, which is likely why this got skipped) and `net.sf.mpxj` (still 9.0.0, and
  confirmed by the finding above to still be javax-JAXB-based — this one's a real bug, not just
  staleness, see `Phase5-found-bugs.md` #11).
- **`Phase5-found-bugs.md`** (new, alongside this file) collects every pre-existing bug found
  during Phase 5's characterization testing that was deliberately left unfixed (data-layer bugs
  found during the DAO Criteria migration, plus the Earned Value cosmetic issue from §5) — a
  standing to-do list, not something to action as part of this report.
- **`maven.compiler.release` is still capped at 21**, with a comment in `pom.xml` explaining the
  cap was needed because Spring 5.3.x's bundled ASM couldn't parse Java 25 class files. Spring is
  now on **6.2.19** (see `pom.xml` dependency management) — the library that justified the cap is
  gone. Worth explicitly re-testing whether `maven.compiler.release` can now go to `25` for real
  (not just "runs on a JDK 25 JVM at bytecode level 21"), and updating/removing the stale comment
  either way.
- `characterization-tests-pre-jakarta` (pinned pre-Jakarta branch, commit `427ce2297`) is still
  around from the DAO migration work — same open question as the previous report: keep for future
  re-verification, or delete now that everything's verified against it.
- A small pile of untracked scratch/debug files sat at the repo root during this session
  (`build*.sh`, `*.log`, `JDK25_MIGRATION_PLAN.{odt,pdf}`, screenshots, `fix-sapphire.md`,
  `old-lpadmin-password.txt`) — worth a pass to decide what's worth committing vs. deleting.
  **`old-lpadmin-password.txt` in particular contains a real password hash and must never be
  committed.**
