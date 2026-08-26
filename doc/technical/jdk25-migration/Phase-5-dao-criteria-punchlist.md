# Jakarta migration: DAO Criteria-API punch list (51 files — ALL DONE)

Workflow per file (per user's explicit instruction - no shortcuts):
1. Read the DAO's Criteria/criterion usage.
2. Check/add characterization tests covering every Criteria-based method, run against the OLD
   (pre-Jakarta) implementation in a pinned git worktree to confirm baseline passes.
3. Copy new/updated test file to main tree.
4. Rewrite the DAO to JPA Criteria API (jakarta.persistence.criteria.*).
5. Compile-check the specific file in main tree (errors for other not-yet-migrated files are expected/ignored).
6. Mark done below.

Status legend: [ ] not started, [x] fully done (tests+rewrite+compile, verified against old impl in worktree)

NOTE: a background fork agent went rogue earlier in this effort and did unsolicited work on BaseCalendarDAO/LabelDAO/IntegrationEntityDAO - verified and reconciled, its work is legitimate and counted below.

## advance
- [x] AdvanceTypeDAO (10 char. tests, verified pass on old impl, rewritten+compiled)

## calendars
- [x] BaseCalendarDAO (3 new char. tests for checkIsReferencedByOtherEntities, verified 18/18 pass on old impl, rewritten+compiled; findByParent uses JPA Join for the calendarDataVersions collection join)
- [x] CalendarExceptionTypeDAO (11 char. tests, verified pass on old impl, rewritten+compiled)

## common
- [x] ConnectorDAO (4 char. tests, verified pass on old impl, rewritten+compiled)
- [x] EntitySequenceDAO (8 char. tests, verified pass on old impl, rewritten+compiled; preserved a real Hibernate NOT-IN-empty-list=always-false quirk explicitly; found isNewObject()-never-reset-after-save bookkeeping gotcha)
- [x] IntegrationEntityDAO (SHARED BASE CLASS for 26+ DAOs incl. LabelDAO, CriterionDAO, MaterialDAO, OrderDAO, ResourceDAO, WorkerDAO, WorkReportDAO etc. findByCode/existsByCode/findAll rewritten+compiled; 7 char. tests via a dedicated IntegrationEntityDAOTest using IBaseCalendarDAO, verified pass on old impl)
- [x] JobSchedulerConfigurationDAO (4 char. tests, verified pass on old impl, rewritten+compiled)
- [x] LimitsDAO (1 char. test only - save() is fundamentally broken pre-existing, Limits.hbm.xml has abstract="true"; rewritten+compiled anyway since query logic itself still needs migrating)
- ALSO FIXED: libreplan-business-spring-config-test.xml was missing Connector.hbm.xml + JobSchedulerConfiguration.hbm.xml mappingResources entries (pre-existing gap, confirmed present even on main HEAD - these DAOs were literally untestable via Spring test context before this)

## costcategories
- [x] CostCategoryDAO (8 new char. tests, verified 14/14 pass on old impl, rewritten+compiled)
- [x] ResourcesCostCategoryAssignmentDAO (existing tests already covered it, rewritten+compiled)
- [x] TypeOfWorkHoursDAO (13 new char. tests, verified 17/17 pass on old impl, rewritten+compiled)

## email
- [x] EmailNotificationDAO (9 char. tests incl. one confirming a genuine pre-existing bug in deleteAllByType - always throws ClassCastException, comparing enum column to enum.ordinal() int - preserved as-is, verified 7/7 pass on old impl, rewritten+compiled)
- [x] EmailTemplateDAO (5 char. tests, verified pass on old impl, rewritten+compiled)

## expensesheet
- [x] ExpenseSheetLineDAO (4 char. tests, verified pass on old impl, rewritten+compiled; hit isNewObject()-never-reset gotcha again + ExpenseSheet.add() not setting back-reference)
- ALSO FIXED: libreplan-business-spring-config-test.xml was also missing Email.hbm.xml + Logs.hbm.xml mappingResources entries (found+fixed proactively while investigating the Connector/JobSchedulerConfiguration gap)

## externalcompanies
- [x] CustomerCommunicationDAO (2 new char. tests, verified 7/7 pass on old impl, rewritten+compiled)
- [x] ExternalCompanyDAO (9 new char. tests, verified 19/19 pass on old impl, rewritten+compiled; isNewObject()-never-reset gotcha bit isAlreadyInUse tests too)

## labels
- [x] LabelDAO (10 char. tests incl. IntegrationEntityDAO generic methods, verified 14/14 pass on old impl, rewritten+compiled)
- [x] LabelTypeDAO (9 char. tests after fixing 2 fork-introduced bugs - a DB-unique-constraint-violated duplicate-name test, and an isUnique() REQUIRES_NEW same-transaction-visibility test - verified pass on old impl, rewritten+compiled)

## logs
- [x] IssueLogDAO (2 char. tests, verified pass on old impl, rewritten+compiled)
- [x] RiskLogDAO (2 char. tests, verified pass on old impl, rewritten+compiled)

## materials
- [x] MaterialAssignmentDAO (existing test sufficient, rewritten+compiled)
- [x] MaterialCategoryDAO (11 new char. tests, verified 13/13 pass on old impl, rewritten+compiled)
- [x] MaterialDAO (9 new char. tests, verified 13/13 pass on old impl, rewritten+compiled; ALSO found+fixed ProfileDAO.java had a silent org.hibernate.Query import bug since way earlier in the migration)
- [x] UnitTypeDAO (8 char. tests, verified pass on old impl, rewritten+compiled; note - a leftover unmigrated isUnitTypeUsedInAnyMaterial() method was later found and fixed too, see below)
- ALSO FIXED: swept whole repo for stale `org.hibernate.Query` imports (Hibernate 6 moved it to org.hibernate.query.Query) - fixed 18 files total incl. ProfileDAO.java (previously silently broken) and several files outside the Criteria punchlist (ConfigurationDAO, ResourceAllocationDAO, OrderElementTemplateDAO, ExpenseSheetDAO, HourCostDAO)
- ALSO FIXED (found during the final full-module compile pass): UnitTypeDAO.isUnitTypeUsedInAnyMaterial() had been missed in the original migration pass - still used legacy Criteria. Rewritten to JPA Criteria; existing UnitTypeDAOTest already covered it (one of its two tests for this method already fails against the OLD impl too, due to an unrelated pre-existing UnitTypeBootstrap.getDefaultUnitType() NPE gap in test setup - confirmed NOT a regression from this fix)

## orders
- [x] HoursGroupDAO (4 char. tests, verified pass on old impl, rewritten+compiled)
- [x] OrderDAO (9 new char. tests, verified 12/12 pass on old impl, rewritten+compiled - complex getOrdersByReadAuthorizationBetweenDatesByLabelsCriteriaCustomerAndState AND/OR/IN combo logic preserved exactly)
- [x] OrderElementDAO (16 new char. tests, verified 31/31 pass on old impl, rewritten+compiled; found WorkReport.setOrderElement() silently no-ops unless WorkReportType.orderElementIsSharedInLines - unrelated pre-existing gotcha, not a bug, just surprising)
- [x] OrderFileDAO (3 char. tests, verified pass on old impl, rewritten+compiled)
- [x] OrderSyncInfoDAO (5 char. tests, verified pass on old impl, rewritten+compiled; needed real cross-transaction commits + re-fetch-by-id pattern due to isUniqueOrderSyncInfoConstraint's own REQUIRES_NEW validation)
- [x] SumChargedEffortDAO (3 char. tests, verified pass on old impl, rewritten+compiled)
- [x] SumExpensesDAO (3 char. tests, verified pass on old impl, rewritten+compiled)
- ORDERS PACKAGE COMPLETE (7/7 files)

## planner
- [x] DayAssignmentDAO (3 char. tests - empty/no-match branches only, positive-match needs infra this codebase's tests don't have; verified pass on old impl, rewritten+compiled)
- [x] DependencyDAO (1 char. test, verified pass on old impl, rewritten+compiled; entity model doesn't allow constructing a genuinely unattached Dependency via public API, documented)
- [x] SubcontractedTaskDataDAO (2 char. tests, verified pass on old impl, rewritten+compiled; uses JPA Join for the taskSource->schedulingData chained join)
- [x] SubcontractorCommunicationDAO (2 new char. tests, verified 6/6 pass on old impl, rewritten+compiled; found+worked around a fragile shared test helper - evict()/reattach dance breaks on later auto-flush, undetected until now since no prior test queried after using it)
- [x] TaskElementDAO (6 new char. tests, verified 17/17 pass on old impl, rewritten+compiled; found evicted-entity-as-Criteria-param throws TransientObjectException even with a real id - legacy Hibernate quirk, recurs elsewhere too)
- PLANNER PACKAGE COMPLETE (5/5 files)

## planner.limiting
- [x] LimitingResourceQueueDAO (3 char. tests, verified pass on old impl, rewritten+compiled)
- [x] LimitingResourceQueueElementDAO (3 char. tests, verified pass on old impl, rewritten+compiled)
- PLANNER.LIMITING PACKAGE COMPLETE (2/2 files)

## qualityforms
- [x] QualityFormDAO (7 new char. tests, verified 12/12 pass on old impl, rewritten+compiled; found isUnique() swallows InstanceNotFoundException via generic catch, returning false for "name not used" instead of true - documented as-is)
- QUALITYFORMS PACKAGE COMPLETE (1/1 files)

## resources
- [x] ICriterionTypeDAO (interface - Criteria import was unused, only appeared in a Javadoc {@link}; removed, no rewrite needed)
- [x] CriterionDAO (6 new char. tests, verified 17/17 pass on old impl, rewritten+compiled)
- [x] CriterionTypeDAO (2 new char. tests, verified 11/11 pass on old impl, rewritten+compiled)
- [x] MachineDAO (1 new char. test, verified 7/7 pass on old impl, rewritten+compiled; found findByNameOrCode is dead/broken code - filters on "limitingResource" which was never a mapped property on Machine, always throws QueryException, no callers anywhere - preserved as always-throwing)
- [x] ResourceDAO (3 new char. tests, verified 8/8 pass on old impl, rewritten+compiled; getAllLimitingResources/getAllNonLimitingResources also always throw - same unmapped "limitingResource" bug)
- [x] ResourcesSearcher (5 new char. tests via new ResourcesSearcherTest, verified 5/5 pass on old impl, rewritten+compiled; join+distinct for byCriteria via JPA Join + cq.distinct(true); found nif matching is case-sensitive (like) while name/surname are case-insensitive (ilike) - preserved exactly)
- [x] WorkerDAO (11 new char. tests via new WorkerDAOTest, verified 11/11 pass on old impl, rewritten+compiled; findByNameSubpartOrNifCaseInsensitive also always throws - same unmapped "limitingResource" bug)
- RESOURCES PACKAGE COMPLETE (7/7 files, incl. ICriterionTypeDAO stray-import fix)
- ALSO FIXED (unrelated pre-existing compile blockers found while compiling this package, not part of Criteria migration): GenericDAOHibernate.lock() used LockMode.UPGRADE which doesn't exist in Hibernate 6 (renamed to PESSIMISTIC_WRITE); HibernateDatabaseModificationsListener had a typo'd override requiresPostCommitHanding (missing "l") that silently failed to implement the real interface method requiresPostCommitHandling - both fixed, both were blocking any full `mvn compile` of libreplan-business regardless of Criteria work

## scenarios
- [x] OrderVersionDAO (4 new char. tests via new OrderVersionDAOTest, verified 4/4 pass on old impl, rewritten+compiled)
- [x] ScenarioDAO (9 new char. tests, verified 9/9 pass on old impl, rewritten+compiled)
- SCENARIOS PACKAGE COMPLETE (2/2 files)

## workreports
- [x] WorkReportTypeDAO (6 new char. tests, verified 10/10 pass on old impl, rewritten+compiled)
- [x] WorkReportLineDAO (17 new char. tests, verified 18/18 pass on old impl, rewritten+compiled; findFinishedByOrderElementNotInWorkReportAnotherTransaction always throws TransientObjectException across the REQUIRES_NEW session boundary - pre-existing, unrelated to migration; separately found WorkReportLine.isOrderElementFinishedInAnotherWorkReportConstraint() always throws HV000090 via Hibernate Validator reflection failure when finished=true is saved, confirmed pre-existing on old impl too)
- [x] WorkReportDAO (8 new char. tests, verified 8/8 pass on old impl, rewritten+compiled; isAnyPersonalTimesheetAlreadySaved() literally returns list.isEmpty(), named backwards from apparent intent - preserved as-is)
- WORKREPORTS PACKAGE COMPLETE (3/3 files)

## Final state
- ALL 51 DAO FILES + BASE CLASSES + the one stray file in `libreplan-webapp`
  (`WorkReportServiceTest.java`): DONE. Repo-wide grep for `org.hibernate.Criteria`/
  `org.hibernate.criterion` returns zero hits anywhere in the repo.
- `libreplan-business` compiles fully (main+test) and its full test suite matches the pre-migration
  Hibernate 5 baseline exactly: `Tests run: 1215, Failures: 0, Errors: 1` (the one error is a
  verified pre-existing bug, not migration-caused - see status doc §2.1).
- The full-suite blocker (shared Spring test context failing to load at all) was not a Hibernate bug
  but a duplicate `externalCode` property in our own `Orders.hbm.xml`, orphaned since 2010. See
  `Phase-5-STATUS-2026-08-14.md` §2 for the full diagnosis, §3 for every other fix made along the
  way, and §4 for a new, separate finding: `libreplan-webapp` doesn't compile for reasons unrelated
  to Hibernate/Criteria (192 errors, 14 files).

## Also fixed along the way, outside the Criteria/DAO scope (see status doc §1.2 for full detail)
- `GenericDAOHibernate.lock()`: `LockMode.UPGRADE` → `PESSIMISTIC_WRITE`
- `HibernateDatabaseModificationsListener`: typo'd interface override fixed
- `EffortDurationType`/`ResourcesPerDayType`: rewritten for the Hibernate 6 `UserType<J>` interface
- `org.jadira.usertype:usertype.core` dependency removed (Hibernate-6-incompatible); replaced its
  one usage with a new hand-written `LocalTimeAsMillisIntegerType`
- `jakarta.persistence-api` bumped 3.0.0 → 3.1.0
- Stale Hibernate 5 dialect class names in `pom.xml` renamed to their Hibernate 6 equivalents
- `LDAPConfiguration`: removed a dead duplicate `getLdapSavePasswordsDB()` accessor
