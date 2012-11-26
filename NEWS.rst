NEWS
====

Version 1.3.1 (15 Oct 2012)
---------------------------

Summary
~~~~~~~

New minor version of LibrePlan including all the bugfixes done since 1.3.0 and
also some new small features included in this version.

We would like to highlight the following changes:

* Allow to administrate the roles and profiles for the users imported from the
  LDAP.

* New language supported, this time Catalan thanks to Daniel Díaz Sañudo.
  Making the full list of languages fully supported to grow up to 6, apart from
  English: Catalan, Dutch, French, Galician, Italian and Spanish. Moreover,
  German and Polish are gradually approaching. Thanks to all our translators for
  their hard work.

* New option in work reports web service. Included the possibility to remove a
  work report or work report line from the web service.

* Added option to configure personal timesheets periodicity, the possible values
  are: weekly, twice-monthly and monthly.

* Improvements in reports:

  * Fixed font styles in generated PDF.
  * Created a new report called "Project Status" with the list of tasks from the
    WBS and using a new layout.

* Fixed date formats in the whole application (reports included). Now they
  follow the user locale conventions.

Notes
~~~~~

If you are upgrading from 1.3.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.3.1.sql``.

If you are upgrading from a previous version without using the Debian package,
review the *Notes* section for version 1.3.0.

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Javier Moran Rua
* Manuel Rego Casasnovas

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [ca] Daniel Díaz Sañudo
* [cs] Zbyněk Schwarz
* [de] Michael Taxis
* [es] Manuel Rego Casasnovas
* [fr] Philippe Poumaroux
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [nl] Jeroen Baten

Changes
~~~~~~~

* Update RPM package for LibrePlan 1.3.1
* Update Debian package for LibrePlan 1.3.1
* Update database scripts for LibrePlan 1.3.1
* Bug #1542: Fix problem with deadline indicator when project finish right at the deadline
* Bug #1542: Fix bug getting project end date from children tasks
* Prevent NPE in SecurityUtils::isSuperuserOrRolePlanningOrHasAnyAuthorization
* i18n: Mark label show in project status report to be translated
* i18n: Update Czech translation
* i18n: Update German translation
* i18n: Update Spanish translation
* i18n: Update Italian translation
* i18n: Update Galician translation
* i18n: Update French translation
* i18n: Update Dutch translation
* i18n: Update Catalan translation
* doc: Update TODO file with the results of the roadmap meeting
* Revert "Bug #1320: Fix issue changing methods to get constraints for a task"
* Revert "Bug #1320: Recalculate position of siblings closing task properties pop-up"
* Revert "Bug #1320: Recalculate position of siblings when moving a task"
* doc: Update documentation about Active Directory configuration
* Bug #1539: Do not reassociate with session resource bound to current user
* Trying to fix broken test in Jenkins
* Fix tests broken in commit b940c7882697833b696e54de5330a634e62ca701
* i18n: Update keys.pot files
* Fix typo in previous commit in message about maximum limit exceeded
* Add restrictions by number of users and resources
* Bug #1538: Detect inconsistent states on unsaved scheduling points.
* Bug #1537: Fix issue getting allocations from memory and not from database
* Simplify the way to calculate the length of money cost bars on the tasks in the Gantt diagram.
* Allow codes of 2 digits for LibrePlan entities
* Bug #1536: Do not regenerate project code when creating from template
* Bug #1320: Recalculate position of siblings when moving a task
* Bug #1320: Recalculate position of siblings closing task properties pop-up
* Bug #1320: Fix issue changing methods to get constraints for a task
* Bug #1534: Fix query to get info about expenses associated to an order
* Bug #1529: Avoid exception in Cost tab in project details
* Bug #1533: Change date format in reports footer to FULL instead of LONG
* Fix error in labels page in menu
* Configure properly file for Hibernate cache log
* Using debug method for logging some messages that are meant for debugging
* Fix unused id in bandbox_search.zul
* Set level INFO for Hibernate cache logging
* Bug #1533: Fix date formats in reports
* Bug #1533: Avoid hard-coding date formats
* Remove dependency to DejaVu fonts as are already included in jasperreport-fonts
* Update name of zul for project status report
* Increase size of prefix in tasks indentation
* Add information about total estimated, planned and imputed hours
* Indent tasks in project status report
* Implement first version of project status report
* Add basic report structure
* Add basic controller and zul for budget report
* Add option in menu and new role for new project status report
* doc: Update guide to create a report with the changes in the last commits
* Update documentation files and packages to add the dependency with DejaVu fonts
* Using DejaVu Sans font in reports to avoid problems with PDFs
* Add dependency to JasperReports fonts package
* Bump JasperReports version to 4.7.0
* Change method getOrder in IOrderModel to return an Order
* Merge branch 'personal-timesheets-periodicity'
* Use INTEGER instead of INT in Liquibase changelog
* Fix the remaining bits where periodicity was not taken into account
* Refactor source code to use personal timesheet instead of monthly timesheet
* Improve representation of personal timesheets including month and year information
* Improve documentation of new methods in PersonalTimesheetsPeriodicityEnum
* Implement navigation between personal timesheets depending on periodicity
* Update representation of personal timesheets in the UI depending on periodicity
* Change the basic methods related to personal timesheets to take into account the periodicity
* Refactoring code moving to methods in PersonalTimesheetsPeriodicityEnum
* Modify the list of personal timehseets depending on the periodicity
* doc: Fix typo "value gained" is "earned value"
* Disable personal timesheets periodicity in configuration window if any personal timesheet was already saved
* Add option to set personal timesheets periodicity in configuration window
* Add new field in Configuration class to store the timesheets periodicity
* Update name of personal timesheets work report type
* Rename monthly timesheets to personal timesheets in the UI
* Update web services documentation with information about the new delete services
* Add method to remove a work report line from the web service
* Add new method to delete a work report from the web service
* Simplify code of WorkReportServiceREST using beforeSaving method
* Prevent losing precision in TaskElementAdapter.calculateLimitDateByHours()
* Use EffortDuration.zero() properly instead of more complex alternatives.
* Bug #1528: Fix field TaskElement.notes in MySQL.
* Add method getAuthenticationType() to avoid problems in edit window
* doc: Update AUTHORS file info about new Catalan translator
* i18n: Add Catalan language to enum and modify pom.xml to use Spanish userguide
* i18n: Add Catalan translation
* Does the users list sortable by user type (LDAP or Database).
* Bug: Fixes sorting in both users and profiles list.
* Bug: Configures right ascending sorting in companies list.
* Bug #1527: Several interface disabling configurations modified.
* Bug #1528: Check if name is null before truncating it.
* Bug #1528: Change datatype for field TaskElement.notes to TEXT, which has no lenght limit.
* Bug #1528: Trucate too long task names so they don't cause problems on save.
* Small code refactor.
* Bug #1523: Fix NPE in company view returning zero if progress is null
* Fix parsing errors in NEWS file


Version 1.3.0 (26 Jul 2012)
---------------------------

Summary
~~~~~~~

After some delay the LibrePlan team is proud to announce the release of a new
major version of the tool, LibrePlan 1.3. Hence, for those of you who were
waiting for it, thank you for your patient and understanding! :)

In LibrePlan 1.3 we have fulfilled the targets that we had identified as top
priority in the roadmap and this makes us happy, because of the work done and
because we think that with the new features included we are providing LibrePlan
with new capabilities. With LibrePlan 1.3 we are making the planner better for
collaborative, real-time scenarios where many different people in the
organization interacts with the projects planning.

The main features which come with this version are:

* Resource binding to users
* Monthly timesheets
* Project dashboard
* Expenses
* Permission enhancements
* Currency support
* Work breakdown structure (WBS) setting up behavior
* Outsourcing improvements
* Concurrent usage improvements
* Revamped menu
* Languages supported
* Timesheets search window enhancements

Notes
~~~~~

.. WARNING::

  Remove web browser cache to avoid any problem with changes in JavaScript
  resources.

.. WARNING::

  If you are using PostgreSQL version 8 you need to execute the following
  command over LibrePlan database in order to use the script
  ``scripts/database/upgrade_1.3.0.sql``::

    su postgres -c "createlang -d libreplan plpgsql"

If you are upgrading from 1.2.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.1.sql``, ``scripts/database/upgrade_1.2.2.sql``,
``scripts/database/upgrade_1.2.3.sql`` and
``scripts/database/upgrade_1.3.0.sql``.

If you are upgrading from 1.2.1 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.2.sql``, ``scripts/database/upgrade_1.2.3.sql``,
and ``scripts/database/upgrade_1.3.0.sql``.

If you are upgrading from 1.2.2 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.3.sql`` and
``scripts/database/upgrade_1.3.0.sql``.

If you are upgrading from 1.2.3 or 1.2.4 versions without using the Debian
package, you will need to manually execute on your database the SQL sentences
from file: ``scripts/database/upgrade_1.3.0.sql``.

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Nacho Barrientos
* Ignacio Diaz Teijido
* Lucia Garcia Fernandez
* Óscar González Fernández
* Susana Montes Pedreira
* Javier Moran Rua
* Adrian Perez
* Diego Pino
* Manuel Rego Casasnovas
* Juan A. Suarez Romero
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [cs] Zbyněk Schwarz
* [de] Joern Knechtel <j.knechtel@gmx.de>, Michael Taxis <mxtaxis@gmx.de>
* [es] Manuel Rego Casasnovas
* [fr] Philippe Poumaroux
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [nl] Jeroen Baten

Changes
~~~~~~~

* Bump version number to 1.3.0
* Update NEWS file for LibrePlan 1.3.0
* Update Fedora and openSUSE README files with warning about plpgsql
* Update Debian package changelog for LibrePlan 1.3.0
* Update RPM package for LibrePlan 1.3.0
* doc: Update basic documentation files due to new major release
* Avoid update task end date when subcontracting a task
* Bug #1522: Use delivery date as deadline when subcontracting tasks
* Bug #1521: Avoid update task end date when changing delivery date
* Bug #1520: Fixed NPE when marking a task for subcontract
* doc: Remove unused file in user help
* doc: Update user help index files
* [doc] Added project dashboard help in Galician
* [doc] Added project dashboard help in Spanish
* [doc] Added project dashboard help in English
* doc: Update AUTHORS file info about new German translators
* 18n: Add German language to enum and modify pom.xml to use English userguide
* i18n: Add German translation
* Update Debian package for LibrePlan 1.3.0
* Debian: Enable plpgsql when using PostgreSQL 8.x
* Debian: Use ${dbc_dbserver} instead of ${dbc_dbhost}
* Update database scripts for LibrePlan 1.3.0
* i18n: Update Italian translation
* i18n: Update Czech translation
* i18n: Update French translation
* i18n: Update Dutch translation
* Add suite for scheduling functional tests
* Fix scheduling functional tests
* Fix resources functional tests
* Bug #1518: Prevent removing the same TaskSource twice.
* Fix users functional tests
* Fix administration-management functional tests
* Fix account functional tests
* Bug #123: Check the cases where the repeated criterion satisfaction was already deleted.
* i18n: Use the same error messages in the assigned criteria tab for both workers and machines.
* Merge branch 'bug-1513'
* Revert "Bug #1513: Lazy exception going to project dashboard"
* Fix data types functional tests
* Change validation messages in material units editing window
* Fix title in progress type editing window
* Bug #1513: Lazy exception going to project dashboard
* Bug #1508: Display the correct task end date in the task properties window.
* Small code refactor. With these changes, the code runs exactly the same way.
* Bug #1513: Lazy exception going to project dashboard
* Bug #1511: Take into account dependencies to parents to calculate task status chart
* Bug #1494: Add a valid SubcontractorDeliverDate to the SubcontractedTaskData used in ReportAdvancesServiceTest.
* Bug #1494: Add a valid SubcontractorDeliverDate to the SubcontractedTaskData used in tests.
* Bug #1507: Fix problem using runOnReadOnlyTransaction to calculate critical path
* Bug #1494: Check for null TaskSources before calling getTask().isSubcontracted().
* Allow to use decimal numbers to set effort in monthly timesheet
* Bug #1503: Fix calculation of min and max in histogram charts
* Do not take into account work report lines with effort zero for task completion chart
* Bug #1494: Deadline field disabled for subcontracted tasks also in Project Details perspective.
* Bug #1494: Deadline field disabled for subcontracted tasks, it corresponds to delivery date.
* Bug #1494: Use deadline as delivery date for subcontracted tasks, if present, or use task end date otherwise.
* Bug #1494: Check there is at least one delivery date to accept a subcontracted task.
* Bug #1505: Fix division by zero calculating margin with deadline
* Bug #1506: Prevent NPE in project dashboard if there is no tasks yet
* Bug #1503: Rename IntegerInterval to Interval
* Bug #1503: Fix intervals in task completion chart
* Bug #1503: Fix intervals in estimation accuracy chart
* Bug #1501: Translate label of GlobalProject chart
* Bug #1501: Global progress chart axis in project dashboard are wrong painted
* Bug #1489: Change attribute name and constructor in UpdateDeliveringDateDTO to resemble SubcontractedTaskDataDTO and prevent confusions.
* Bug #1489: Build UpdateDeliveringDate requests placing the client code in the correct place.
* Bug #1493: Modified project deadline vertical line position to show it after the deadline date
* Bug #1493: Modified task deadline mark position to show it just after the deadline date
* Fixed some graphical issues on subcontracting screens
* Moved Timesheet Lines List page from 'Reports' menu section to 'Cost'
* Modified styles of 'more options' element on search filters
* Revamped component on timesheet lines report to show found tasks information
* Fix problem with EffortDuration in CalculateFinishedTasksEstimationDeviationVisitor
* Bug #1497: Do not count the project root task as we have a explicit line for project deadline
* Use EffortDuration to calculate estimation deviation on completed tasks
* Bug #1502: Fix NPE in deviation indicator
* Bug #1500: Fix material needed at date report only showing information in projects with permissions
* Bug #1499: Fix project costs report only showing information in projects with permissions
* Bug #1497: Count also containers and milestones in deadline violations chart
* Bug #1496: Fix issue when visiting project dashboard in a project with milestones
* Bug #1486: Take into account i18n to sort UserRole list.
* Bug #1495: Fix resource usage ratios
* Bug #1492: Check if the role had been added before.
* Corrected a typo in the name of one of the predefined profiles.
* Bug #1486: Sort Profiles list before adding it to the combo box.
* Bug #1486: Replace the widget used to select the roles with a Combobox.
* Bug #1486: Sort UserRole list before adding it to the combo box.
* Bug #1479: Fix error loading jqplot Javascript files
* Bug #1491: Mark strings in GlobalChart to be translated
* Fix exception in progress dashboard if there are no progress in the project yet
* Fix translation of legend in task status indicator chart
* Bug #1485: Call model.initEdit to ensure proxies are initialized before loading monthly timesheet screen.
* Bug #1484: Allow to visit planning screens to ROLE_CREATE_PROJECTS
* Bug #1483: Fix problem with rounding in cost indicators and earned value legend
* Bug #1483: Improve labels in cost indicators
* Bug #1483: Refactored code in CostStatusController
* Bug #1483: Fix values in cost indicators
* Bug #1483: Fix problem getting the last value calculated
* Bug #1476: Mark title chart to be translated
* i18n: Small corrections in Spanish and Galician translation.
* Move frozen code to initTimesheet method
* Fixed width problems on monthly timesheet on low resolutions
* Bug #1475: Fix exception adding ROUNDING_MODE to divide method
* Fix NPE entering an empty value in the budget inputs
* Fix problem introduced in 896096272c2b3ee5ccf229726b42cb4f88dd8bd1
* i18n: Update reports subtitles translation in Spanish and Galician
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* i18n: Replace resource usage for resources load
* Remove unneeded code in LimitingResourcesTabCreator
* Set width other column
* Updated project dashboard piechart colors
* Increased project progress chart height to allow fitting of spread  bars
* Reduced font size on project dashboard labels
* Updated jqplot label styles and improved ok/warning image files
* Reordered indicators with more frequently populated with values charts first
* Updated captions and labels on project dashboard
* Updated dashboard ok/warning images for KPI values
* Inserted extra padding to add task to timesheet widget
* Added class to Total row on timesheets and moved up to be the first aggregation element
* Increased monthly timesheet columns to show better non integer hours
* Replaced 'Previous' and 'Next' monthly timesheet buttons for icons
* Made project and task columns Frozen on monthly timesheet
* Modified width calculations on timesheet grid
* Style revamps on user dashboards
* Added custom styles to monthly timesheet
* Fix problem due to limiting resources renaming
* i18n: Translate options in worker edition
* Remove unused code in ResourceType
* i18n: Rename limiting resources to queue-based
* Remove unneeded check in TabsRegistry related to limiting resources tab
* allows the limiting resource assignment when there are subcontractor progresses.
* i18n: Fixing some English strings
* i18n: Fix string
* Script for replacing strings in Java files
* i18n: Fix strings
* i18n: Fix messages in Project Costs report
* Change capitalization in task tooltip
* Fix LazyInitializationException editing a monthly timesheet
* Replace "Task code" for "Task" in timesheets edition form
* i18n: updated the titles of all the reports.
* i18n: review strings in workingProgressPerTask report.
* i18n: review strings in workingArrangementsPerOrder report.
* i18n: review strings in schedulingProgressPerOrder report.
* i18n: review strings in orderCostsPerResource report.
* i18n: change Spanish strings in hoursWorkedPerWorkerInAMonth report to utf-8.
* i18n: review strings in hoursWorkedPerWorkerInAMonth report.
* i18n: review strings in hoursWorkedPerWorker report.
* i18n: review strings in completedEstimatedHours report.
* Bug #1474: Fix issue adding a new option Any to the report filers
* i18n: Fixing strings
* Bug #1473: Using OrderVersion from project in updating
* i18n: Prevent extract empty strings in gettext-keys-generator.pl
* i18n: Do not mark to translate predefined criterion types
* i18n: Get week days translation automatically from Java libraries
* i18n: Get months translation automatically from Java libraries
* i18n: Remove unneeded spaces in strings marked to translate into QueueComponent
* i18n: Text string review
* i18n: Review and fix several strings to be translated
* i18n: Fix problem with break lines in strings extractor
* Bug #1472: Hide the bar with the arrow buttons of the WBS in read-only mode.
* i18n: Update keys.pot files
* i18n: Fixing strings
* Bug #1443: Review other places where InvalidValue is used and mark to translate
* Bug #1443: Fix gettext keys extractor
* Bug #1443: Show error messages translated and just once
* Bug #1369: Fix problem when a template defines a calendar in new project pop-up
* Bug #1369: Fix problems with dates from template in new project pop-up
* Remove column code in template finder as templates do not have code attribute anymore
* Bug #1369: Set calendar from selected template in new project pop-up
* Bug #1369: Fix problems in previous patch due to bandbox listener
* Bug #1440: Add calendar field while editing project templates
* Bug #1471: Remove duplicate map for codes in OrderElementTreeController
* Bug #1471: Update all the columns in the WBS and not only name, hours and budget
* Bug #1471: Create new method updateColumnsFor to include all similar calls
* Bug #1471: Add getter and setter for textbox of DynamicDatebox
* Bug #1470: Fixed problem calculating SumChargedEffort when the task was modified
* Bug #1464: Prevent NPE when setting progress in a task with parents hidden
* Bug #1466: Fix NPE when there are milestones in the planning
* Disable create new users from worker edition if user lacks ROLE_USER_ACCOUNTS
* Add permissions for ROLE_SUPERUSER where ROLE_EDIT_ALL_PROJECTS is used
* Fix columns in template assignment log tab
* Protect entry point in templates assignment log
* Fix SecurityUtils.isSuperuserOrRolePlanningOrHasAnyAuthorization
* Protect entry point in Timesheet Lines List report
* Refactor code related to timesheet lines list report
* Finally fixing users and profiles related tests changing the way to define default users
* New attempt to fix tests in Jenkins machine
* Fix tests failing on Jenkins due to new predefined users with profiles
* Fix reports to filter projects by user permissions
* Fix method SecurityUtils.isSuperuserOrRolePlanningOrHasAnyAuthorization
* Add role read all projects to reports responsible
* Review page titles due to menu revamp
* Fix compilation issue due to class rename in commit bdf731d4736730d26fb288f11e933758b48df003
* Fix wrong syntax in template.zul
* Fix issue in TemplateController and ProfileDAO due to new predefined users
* Modified behavior of UsersBootstrapInDB in order to create users only if there are no users yet
* Disable edition and removal of default user admin
* Fixed some users related tests due to previous patch
* Add new example users
* Remove default user with login user and password user
* Move default profiles bootstrap to proper package
* Fix title in access_forbidden.zul
* Configure error logging to add info about the request URI if a 403 status code is returned
* Configure page when 403 (forbidden status code) is send to the user
* Add info about status code in error logging
* Translate into English page_not_found.zul and mark strings to be translated
* Remove unused file error.jsp
* Move index.zul to common folder
* Protect monthly timesheet page depending on user roles
* Move sendForbiddenStatusCodeInHttpServletResponse method to Util class
* Protect entry points methods in expenses sheet window
* Add check to avoid bound users to go directly (via URL) to expenses page
* Bug #1468: Reload bindings only in the existing user panel
* fixes the error that happens at filtering by dates in the report about order cost per resource.
* Prevent users to create templates from project edition if they do not have ROLE_TEMPLATES
* Prevent users to create labels from project edition if they do not have ROLE_LABELS
* Change column "Administrator" for "Superuser" in users list
* Configure initial page when user clicks on LibrePlan logo depending on roles
* Set different initial page depending on user roles
* Protect main perspectives depending on user roles
* Limit visibility of planning pages depending on roles
* update web services documentation and add scripts to test this case and some example files.
* implements an empty method called "beforeSaving" in the GenericRESTService and overrides it in the ExpenseSheetServiceRest in order to save the sum of expenses.
* fixes the empty block with the apropriate condition and the needed behaviour.
* removes the validation in the method toDTO because there is already the annotation @NotEmpty in the ExpenseSheet class.
* renames this method getLabel to getExpenseSheet in IExpenseSheetService.
* changes the InstanceNotFoundException to ValidationException and if the entity is not found the function returns a null.
* update the end date of the subcontracted task according to the end date communication sent by the subcontractor.
* Remove ROLE_BOUND_USER from roles list in user and profile edition
* Disable buttons to go to user or worker edition in bound users depending on roles
* Configure permissions for ROLE_BOUND_USER
* Managing special role ROLE_BOUND_USER in workers and users windows
* Create default example profiles
* Show menus entries depending on user roles
* Configure basic permissions for each page in Spring Security file
* Add new roles in UserRole enum
* Review and rename current roles
* Refactoring LibrePlan menu
* doc: Fix problem in PDF generation for user help
* Fix changeset in MySQL
* Bug #1275: Montecarlo combo for selecting critical path is empty
* Fix bug: Remove 'Expected Spread Progress' bar from 'Global Progress' chart
* Fix bug: LazyInitializationException in WorkReportLines
* Refactoring: Class for creating GanttDiagram
* Bug #1451: Error rendering GanttView coming directly from MonteCarlo
* Fix bug: WorkReportLines filtering not working if there's no Task selected
* Add default user wssubcontracting/wssubcontracting
* Add new role to protect subcontracting services
* Bug #1463: Fix issue changing order in parameters of entry point
* Bug #1461: NullPointerException in WorkReportLines
* Add 'Spread Progress' bar to 'Global Progress' chart
* Add pop-up tooltip in 'Global Progress' chart
* keep sorted the expense sheet lines when some date is changed.
* changes some functions names in ExpenseSheetModel in order to understand its behaviour easily.
* Bug #1460: Fix issue checking if TaskElement is Task
* Bug #1439: Fix issue changing JavaScript to show/hide labels
* Remove unused methods and variable in TaskComponent
* Fix bug: NullPointerException in calculation of OvertimeRatio
* Fix bug: NullPointerException when opening Dashboard view
* Refactoring: Create class CriticalPathBuilder
* Fix bug: Cannot render GlobalProgress Chart
* Fix bug: Tomcat cannot load resource file
* Bug #1454: Force update task size after reassignations
* Bug #1459: Fix order of columns in OrderElementBandboxFinder renderer
* Add constraint to check that in a personal expense sheet the resource is the same in all the lines
* Revert "Add restriction to prevent remove all the lines in a personal expense sheet"
* Fix typo in GET parameter for saved timesheets
* Add button to delete personal expense sheet
* Add type information in expenses sheet list and form
* Allow to sort monthly timesheest in user dashboard
* Sort expenses area list
* Add class to highlight clickable rows in user dashboard
* Add button to edit personal expense sheet from user dashboard
* Show list of personal expense sheets
* Add restriction to prevent remove all the lines in a personal expense sheet
* Implement button to create a new personal expense sheet
* Add new attribute personal in ExpenseSheet
* Add new section for expenses in user dashboard
* Remove constant only used once and mark string to be internationalized
* Add currency symbol in value decimalbox
* Simplify code of BandboxSearch in ExpenseSheetCRUDController
* Fix issues in ExpenseSheetCRUDController regarding to BaseCRUDController
* Remove unused attribute in ExpenseSheetCRUDController
* Fix wrong sortDirection and width attributes in expenses sheet listings
* fixes the functions which are used to check out if the cost category and the hour cost are active in the specified work report line.
* removed the class CostWorkReportLineDTO because it is not used.
* Using OrderElement id as key in the map to avoid problems
* Add timesheet summary box
* Add other row and column with information about other work reports
* Sort tasks in my tasks area
* Hide user dashboard page from menu if current user is not bound
* Add message about monthly timesheet being saved
* Add operations column in my tasks area
* Fix problem in constraint only one work report line per day and task
* updates the sum of expenses if the task associated to the expense sheet line is changed.
* import and export the expense sheets.
* Add constraints in WorkReport entity to prevent wrong modifications of monthly timesheets
* Fix problem in work reports web services
* Fix issue in entry points renaming method
* Fix ResourceWorkedHoursDTO that was not working properly
* Bug #1452: Fix problem adding info about resource in work report lines
* Add total work column in work reports list
* Add info about resource in monthly timesheets in work reports list
* Remove unneeded set methods in WorkReportDTO
* Add possibility to create monthly timesheets from work reports list
* Use monthly timesheet page to edit work reports of this type
* Add method in WorkReportType to check if it is a monthly timesheet
* Bug #1457: Wrong value of labels CRITICAL_PATH_DURATION and CRITICAL_PATH_NUMHOURS
* Mark with bold the special rows (capacity, total and extra) in the monthly timesheet
* Change the way to calculate total extra (summing extra of each day)
* Prevent NPE in monthly timesheets are if WorkReport was not created yet
* Fix typo in Hibernate mapping of Configuration class
* Add new extra row in monthly timesheet
* Add available hours column in monthly timesheets area
* Add number of tasks column in monthly timesheets area
* Add total work column in monthly timesheets area
* Generate entity sequence codes in monthly timesheets
* Set width of bandbox search to add tasks in monthly timesheets
* Sort tasks in monthly timesheet
* Remove jqplot files from src dir
* Move jqPlot CSS and Javascript files to JAR
* Fix TypeOfWorkHoursServiceTest in MySQL
* Add "Save & continue" button in monthly timesheets
* Mark the inputs modified in the monthly timesheet
* Add previous and next buttons on monthly timesheet
* Allow to add any task in the monthly timesheet
* Code refactor moving info about first and last day to MonthlyTimesheetModel
* rpm: Add support for CentOS 6
* Show empty string instead of zero in monthly timesheet
* Use disabled textbox for capacity row in monthly timesheets
* Set a pink background for days with zero capacity in the monthly timesheet
* Fix align issues due to colspan in the first column of capacity and total rows
* Add capcity row to monthly timesheets
* Add total row to monthly timesheets
* Remove commented line
* Add button to hide/show extra filtering options
* Show summary of filtered results
* Increase number of results per page to 15
* Filter 'Work Report Lines' by type (all, direct, indirect)
* Fix TypeOfWorkHoursServiceTest due to new configuration field
* Use new TypeOfWorkHours for monthly timesheets
* Prevent to remove or disable the configured TypeOfWorkHours for monthly timesheets
* Create special bootstrap to set the new field to define the TypeOfWorkHours of monthlytimesheets.
* Add new configuration field to define the TypeOfWorkHours for monthly timesheets.
* Add total column in the monthly timesheet grid
* Fix bug: Refresh 'Global Progress' chart dinamically (no need to save project)
* Fix bug: Paths to Javascript files depend on URL context
* Fix bug: Wrong path to jqplot
* Fix bug: Return 0 if budgetAtCompletion has no elements
* Fix NPE in my tasks area when SumCharegedEffort is still null
* Bump version number to 1.2.4
* Update NEWS file for LibrePlan 1.2.4
* Update RPM package for LibrePlan 1.2.4
* Update Debian package for LibrePlan 1.2.4
* Add database script for MySQL
* Bug #1423: Remove unneeded line
* First basic implementation of monthly timesheet edition grid
* Add breadcrumb to moonthly timesheet page
* Add edit button in monthly timesheets list
* Show the list of monthly timesheets in the user dashboard
* Remove @OnConcurrentModification from MyTasksAreaModel
* Separete my tasks area .zul, controller and model to different files
* doc: Update AUTHORS file info about new Czech translator
* i18n: Add Czech language to enum and modify pom.xml to use English userguide
* i18n: Add Czech language
* Exclude inclusion of ehcache-1.1.jar
* Hide monthly timesheets work report type from the list of work report types
* Add new work report type to be used in monthly timesheets
* Bug #1448: Fix issue reattaching the work report before removing
* Bug #1450: Fix issue rounding when setting budget scale.
* Bug #1447: Remove the test checking the creation of NOT_LATER_THAN constraint when a deadline is set.
* Remove unused code
* Fix bug: Inclusion of 'GlobalProgress' html code was causing a side effect in other views
* Turn on batch-fetching for collections in several entities
* Turn on second-level cache
* Add possibility to sort the my tasks list by the different columns
* First implementation of my tasks area
* Bug #1447: Prevent the creation of NOT_LATER_THAN constraint when a deadline is set.
* doc: Update Eclipse document with 2 new sections (Maven profiles and MySQL development)
* Configure a custom URL target resolver in order to define the proper URL for bound users
* Configure a custom authentication filter
* Move code related with 'GlobalProgressChart' to separate files
* Add user dashboard page without content yet
* Rename "settings" folder for .zul files to "myaccount"
* Bug #1444: Renamed variable with a more meaningful name.
* Fix bug: Include minimized version of jquery and jplot
* Bug #1444: Save affected parent TaskElements in the end of the process.
* Bug #1445: Fix issue adding purple color in CalendarExceptionTypeColorConverter
* doc: Fix wrong path in web services documentation
* Remove unused jqplot files
* Remove included jqplot plugins that are not actually needed
* Fix bug: Work around to make possible to set colors in a PieChart
* Fix bug: 'Absolute margin with deadline' was not being refreshed even if the deadline of the project changed
* Bug #1441: Fix rendering problems on general data tab for chrome
* Added qualitative indicators to deadline ratios
* Revamped standard jqplot label styles
* Updated titles and captions in Progress chart
* Modified Project progress percentage chart colors
* Revamped appearance of cost status indicator boxes
* Revamped appearance of resources usage box
* Transformed deadline status grid into a natural language caption
* Moved tasks status grid values inside chart legend series
* Disable first name and last name in settings window if user is bound
* Add some Javadoc to AssignedEffortForResource class
* Fix Sahi tests due to removal of scenarios option
* fixes the test ReportAdvancesServiceTest
* fix the ExpenseSheetTestDAO
* Refactor code
* Bug #1428: Rename customAssignedEffortForResource to setAssignedEffortForResource
* Bug #1428: Take into account the load of the other allocations when reassigning
* Bug #1428: Fix bug
* Bug #1428: Bring all files related to IAssignedEffortForResource to one unique file
* Revert "Bug #1428: Possible fix"
* Bug #1431: Sort the EffortModifications too
* Bug #1431: Fix problem in commit 9d5e3d88dd4dacc4fc00af544a3306d4327dd674
* doc: Update development guide with the line for the favicon
* Bug #1284: Add favicon to new pages (expenses and subcontractor communications)
* doc: Improve web services documentation with the list of available services
* i18n: Update Dutch translation
* i18n: Update Italian translation
* Bug #1442: Fix regression.
* Trying to fix tests related with unbound users in UserDAOTest
* Improve UserDAOTest adding a new check for getUnboundUsers method
* Fix UserDAOTest that was failing in some cases
* Display value 'Availability ratio' in Dashboard
* Display value 'Overtime ratio' in Dashboard
* Moved constants to inner class as they were only being used inside that class
* Refactor code
* Fix problem with MySQL and description TEXT field in expense_sheet
* Update Liquibase to 2.0.5
* Fix issues with MySQL in some Liquibase changes
* Bug #1284: Added favicon in all the screens.
* Replace "Login name" for "Username"
* Add option to unbound resource from user edition
* Add warning about bound resource when removing a user
* Add link from user edition to worker edition if the user is bound to any worker
* Add info about bound resource in user edition
* Add bound resource info in the users list
* Refactor users list to use a RowRenderer
* Disable first and last name in user edition if user is bound to any resource
* Add option to remove bound user too when removing a resource
* Move logic to remove order authorizations when removing a user to UserDAO
* Add link from worker edition to user edition if the worker is bound to a user
* Change user Listbox for a BandboxSearch
* Modify worker edition UI to add the chance to bound a worker to a user
* Add assert to check that a limiting or virtual resource is not bound to any user
* Add assert to check that a worker is not bound to a user already bound with other worker
* Create basic UI to bound a user to a worker
* Add relationship between and Worker and User
* Merge branch 'master' into expenses-tracking
* Merge branch 'master' into subcontracting
* Fix bug
* Fix typo in method name
* Fix bug
* Fix bug, don't try to render Dashboard charts if the project doesn't have tasks
* fixes the method addCurrencySymbol to return zero if the parameter is null
* adds the currency symbol and set the right parameter to method reloadTotalBudget in the order edition view.
* Merge branch 'master' into subcontracting
* i18n: Fix small typo in Spanish and Galician translations.
* Bug #1284: Added favicon.
* Bug #1284: Fix NullPointerException.
* Bug #1414: Reduced width of MultipleBandbox filter search
* Bug #1421: Fixed the pagination bug when indenting nodes into containers on previous pages
* Remove 'Overall progress' tab
* adds some tests to check that the expenses calculation works properly.
* changes the method getCostOfHours to getHoursMoneyCost in order to fix the Test.
* Includes the currency format in the expenses module, and in the report of order cost per resource.
* Code refactoring
* Rename 'EarnedValueCalculator' to 'OrderEarnedValueCalculator'
* Code refactoring
* Code refactoring
* Bug #1433: Fix the problem when there are multiple levels of tasks involved.
* Bug #1433: Small code refactor of this bug fix.
* Bug #1433: Make sure that old TaskSources are deleted also in the case of parent tasks.
* Bug #1433: Make sure that old TaskSources are deleted when a task is unscheduled and re-scheduled.
* Merge branch 'master' into expenses-tracking
* Modify the Project cost report  to include a new area called Expenses, per OrderElement.
* Add method in Util to include currency symbol in a BigDecimal
* Include the cost because of expenses in the WBS imputed hours pop-up and updates the costs bar in Gantt chart with expenses.
* create or update the sumExpenses when the expenseSheet is saved.
* i18n: Update French translation
* Remove unneeded calls to I18nHelper in web services
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* Fix wrong message in deadline constraints
* i18n: Update keys.pot files
* Implement Dashboard 'Cost Status' table
* Bug #1431: Fix test because of now allocations are sorted (specific before generic)
* Bug #1431: Fix test creating a standard list instead of an unmodifiable list
* Bug #1431: Fix issue sorting allocations (first the specific ones)
* Bug #1428: Possible fix
* Bug #1430: Fixing issue checking code for each child and not for the parent node
* Bug #1430: Fix problem with error messages in OrderElementConverter
* Remove unused method in IntegrationEntityModel
* Moved code attribute from InfoComponent to new class InfoComponentWithCode to fix regression.
* Remove minor optimization
* Fix bug, upper limit and lower limit in TaskCompletationLag should be +3,-2 when there's only one task
* Revamp 'Estimation Accuracy' chart
* Fix bug, wrong deviation calculator in 'TaskCompletationLeadLag' chart
* Days interval in 'Task Completation Lead/Lag' should be Integer
* Calculate 'Absolute Margin with Deadline'
* Bug #1425: Fix problem change wrong parentheses
* Bug #1263: Fix issue reseting the value of the textbox
* Remove unused variable in TreeController.Renderer.updateBudgetFor(T)
* Bug #1388: Fix issue updating name textbox when coming back from pop-up
* Moved database changeset to the correct file.
* Recode 'Margin with Deadline' chart
* Recode 'Task Completation Lead/Lag' chart using jqplot4java
* Recode 'Task Status' chart using jqplot4java
* Recode 'Deadline Violation' chart using jqplot4java
* Add jplot4java
* Removed code attribute at template level.
* Remove code column from template tree.
* Remove code attribute from templates finder.
* Remove code attribute from OrderElementTemplate entity and from DB.
* create the sumExpenses class to represent the total money spent in expense lines for each OrderElement.
* Remove dirs 'docs' and 'examples' from jqplot
* Bug #1423: Fix bug adding root task to graph
* Bug #1424: Fix issue loading derived allocations
* Bug #1412: Avoid NPE if clockStart and clockFinish are not defined
* Recode 'Task Completation Lead/Lag' using jqPlot
* Add table with summary of the status of tasks
* Recode 'Global Progress' and 'Task Status' using jqPlot
* Add jqPlot
* Fix rounding problems with BigDecimal in reports
* Fix issue when a currency symbol includes some special chars (like ".")
* Add currency symbol in all the decimalbox representing money
* Moved method to get currency symbol to Util class
* Avoid to delete an order element with expenses
* Refactoring ExpenseSheetCRUDController to extend BaseCRUDController
* Use currency symbol instead of euro symbol in report orderCostsPerResource
* Use currency symbol instead of euro symbol in tasks tooltip
* Add combo in configuration window to choose currency
* Add new fields for currency code and symbol in configuration
* Bug #1422: Fix issue calling onRetreival to force synchronization
* change the properties with type Date to LocalDate in the ExpenseSheet and ExpenseSheetLine classes.
* Upgrade ZK to 5.0.11
* rpm: Make sure all update SQL scripts are installed
* Fix problems with Liquibase 2.0.4 and MySQL for tag modifyDataType
* add constraints and validations in the expense sheet view.
* renamed the TaskInExpenseSheetBandboxFinder to OrderElementInExpenseSheetBandboxFinder
* add the methods to set a constraint to the bandbox
* reset the model in BandboxSearch when the finder is set, in order to update the model.
* Refactoring the classes ExpenseSheet and ExpenseSheetLine
* Update Liquibase to 2.0.4
* create the test "expenseSheetTestDAO"
* change the name of the entry Work Reports to be Time Tracking.
* Configuring permissions to access and to save the expenses sheet with the role "Expenses tracking allowed".
* create expense sheet list and the edition view for each one.
* change Libreplan configuration view and add the expense sheet as a class whose code can be autogenerated.
* create ExpenseSheet and ExpenseSheetLine classes.
* doc: Fix some broken links to files in SourceForge.net
* doc: Add note about removing browser cache in NEWS file
* Corrected wrong indentation in NEWS file.
* Bug #1416: Reload resources text in parent tasks too
* Bump version number to 1.2.3
* Update NEWS file for LibrePlan 1.2.3
* Update RPM package for LibrePlan 1.2.3
* Update Debian package for LibrePlan 1.2.3
* Update database scripts for LibrePlan 1.2.3
* Bug #1417: Add UI validation to prevent empty codes
* doc: Update AUTHORS file info about new Polish translator
* i18n: Add Polish language to enum and modify pom.xml to use English userguide
* i18n: Add Polish translation
* Bug #1384: Add validation in starting date and deadline depending on position constraints
* Bug #1358: Removed unnecessary tooltip string
* Bug #1358: Added CSS max dimensions restriction to configured company logo
* Bug #1407: Run TaskComponent.updateProperties() after running the scheduling algorithm.
* Remove unused fields in Order Costs Per Resource Report
* Bug #1412: Fix problem converting to string clockStart and clockFinish
* Bug #1409: Rename RelatedWithAnyOf to RelatedWith
* Bug #1409: Fix problem replacing allocations for the ones related to the criterion
* Revert "Bug #1320: When asking a container for start constraints, return the leftmost"
* Bug #1411: Missing Spanish translation for "Project cost by resource"
* Fix Sahi test due to change in string
* i18n: Update Dutch translation
* i18n: Update French translation
* doc: Update HACKING file about the compilation options
* Add new compilation option to disable default users (user, wsreader and wswriter)
* Change the order and labels of the filtering area
* Make coherent behavior of bandbox with the rest of elements in the filtering bar
* Bug #1395: Fix issue setting width of date boxes to 100px
* Rename WorkerMultipleFiltersFinder to ResourceFilterEnumByResourceAndCriterion
* Allow to filter resources by criteria in the resource load window
* Fix error in documentation of class ResourcesMultipleFiltersFinder
* i18n: Update Italian translation
* i18n: Update Spanish and Galician translations
* enables the button "Update task end" in the subcontract pop-up, when the deadline is empty.
* i18n: Update keys.pot files
* Merge branch 'master' into subcontracting-merger-master
* Bug #1402: Invalidate the TaskComponents instead of the whole GanttPanel
* Refactor the class EndDateCommunicationToCustomer to be EndDateCommunication.
* add tests to check out end dates requested from subcontract to customer are correctly reported.
* Merge branch 'master' into project-dashboards
* change the precision of delivery date requested by the customer in the subcontract pop-up.
* if the project is regular, the tables of the delivery dates, requested by the subcontractor, and of the delivery dates, requested by the customer, wont be shown in the general tab.
* modify the subcontract pop-up to view the information of the end dates communicated by subcontractors and add a button to update the deadline of the task.
* set the new end date communications to customers as not transient object anymore.
* modify the view of communications received from subcontractors to show the end date communications correctly.
* Adaptation of the XML message to send end date communications from subcontractor to customer
* Bug #1349: Fix translation issue in choosing template pop-up
* Bug #1349: Mark to translate exception day type
* Bug #1349: Fix translation in calendar type
* Bug #1298: Mark to translate roles in user and profile edition
* Remove CutyCaptTimeout
* Bug #1406: Add UI validation for name field too
* Bug #1406: Add validation in the UI and also a try catch for possible ValidationExceptions
* Fix several issues in the new thread
* Refactor the class OrderElementWithAdvanceMeasurementsDTO to be OrderElementWithAdvanceMeasurementsOrEndDateDTO.
* modify the screen "Send to customers" to detect that there is a new end date pending to send from a subcontractor to its customer.
* modify the General Data tab of a project in order to include the table to manage the end date communications to customer.
* add to the Order class a list sorted of elements of the class EndDateCommunitationToCustomer.
* Add a thread to perform SumChargedEffort recalculations
* Improve database preconditions in Liquibase changeset
* Bug #1400: Move call to do recalculations after doTheSaving
* doc: Fix typo in INSTALL file
* Bug #1400: Fix problem recalculating SumChargedEfforts if some elements are moved in the WBS
* Avoid to delete a subcontractor progress that has been sent in subcontractor.
* Include a column with the name of the project and other with the "Delivery date, and make the list sortable by the column "communication" or "company".
* Fix the error for what the column "Communication" was not being updated.
* Add map in order to avoid repeat find when SumChargedEffort has been already found before
* Prevent NPE in OrderElement::getSumChargedEffort()
* Create SumChargedEffort while saving the work reports
* Change mapping between SumChargedEffort and OrderElement
* doc: Fix broke link in INSTALL file
* doc: Update information about how to install in Fedora and openSUSE
* Changes the "General data" tab about subcontracting module.
* doc: Add info about JAVA_OPTS configuration in INSTALL file
* make the list sortable by default by state,but using alphabetic sort, but the first tasks will be the pending tasks.
* include the hour, minute precision in the communication date received from customers.
* Fix the layout of the screen "to customers" in subcontractor module.
* Rebase the code of the .zul pages for the templates screen.
* [Bug #1234] Fix the deletion of fields in progress reporting in subcontractor module.
* [Bug #1234] Fix the deletion of fields in progress reporting in subcontractor module.
* Fix problems in Liquibase changes in MySQL
* Merge branch 'money-cost-monitoring-system'
* Merge branch 'libreplan-1.2' into money-cost-monitoring-system
* add one-to-many association from SubcontractedTaskData to SubcontractorCommunication with a cascade="delete" in order to delete SubcontractorCommunication when its associated SubcontractedTaskData is deleted.
* doc: Add info about add-apt-repository command in INSTALL file
* doc: Add info about add-apt-repository command in INSTALL file
* Bug #1387: Code refactor of the previous patches for this bug.
* Bug #1387: Fix bug when it happens in the opposite way.
* Bug #1387: Fix bug
* Bug #1387: Code refactor of the previous patches for this bug.
* Bug #1387: Fix bug when it happens in the opposite way.
* Show budget information in a read-only field inside task properties tab
* Bug #1387: Fix bug
* Change color of money cost bar to a darker one to avoid accessibility issues
* Bug #1403: Only regenerate codes if isCodeAutogenerated() is true
* Bug #1403: Only regenerate codes if isCodeAutogenerated() is true
* update the deadline of the task and of the order in the subcontract side when a new deliver date is sent.
* save the subcontract communication date if you send a subcontract communication and the previous state is Failed_Sent.
* manage a optimistic locking exception which happens due to a subcontracted task has been modified by other instance.
* set the fields of the subcontraction pop-up to read-only mode when a subcontracted task has been sent.
* Add a map in MoneyCostCalculator to cache calculated values
* Disable Money Cost Bar in company view to avoid performance issues
* Bug #1289: Added subcontractor name to tasks when showing resources is enabled
* Bug #1289: Added subcontractor name to tasks when showing resources is enabled
* set the correct class to the button delete "Delivery date".
* set the fields "Subcontracting date" and "Subcontracting communication date" in the subcontractor pop-up in read only mode.
* Remove unneeded throws in MoneyCostCalculatorTest
* Add unit tests to check MoneyCostCalculator with a different type of hours
* set the progress values, which are sent from a subcontrated task, in read only mode.
* Remove commented lines in MoneyCostCalculatorTest
* Update Copyright info in user documentation
* Update Copyright info in user documentation
* Change the value format of the last progress in sent communications from subcontractors.
* Improve sentence in "Imputed hours" tab editing a task
* Improve sentence in "Imputed hours" tab editing a task
* Add information about budget in "Imputed hours" tab
* Add unit tests to check MoneyCostCalculator with a tree of tasks
* Change name of the communication type from "Report advance" to "Progress Update".
* Show in bold (the font) the rows which are not reviewed in the list of sent communications from customer and subcontractor.
* Remove unused parameters in CutyPrint.createCSSFile
* Remove unused parameters in CutyPrint.createCSSFile
* Add option to print money cost bar
* include a refresh button belonging to the list of customer and subcontractor communications, in order to update the selected filter.
* Add a new test case to check MoneyCostCalculator when there is not relationship via cost category
* add the appropiate icon, the class and the tooltip text in the edit button, in the subcontracting module.
* change precision of the deadline to just show in format dd/mm/YYYY and according to the locale.
* Change the name of the menus for the subcontracting module.
* Merge branch 'master' into subcontracting
* Prevent possible rounding problems dividing BigDecimals
* Prevent NPE if there is not relationship between resource and type of hours via cost category
* Reload budget field in "General data" of templates
* Add field in "General data" tab to show the project budget
* Print Money Cost Bar proportinal to task size
* Prevent NPE calculating money cost for a TaskElement
* Remove method getMoneyCostBarPercentage from ITaskFundamentalProperties
* Improve tooltip message using budget, consumed money and percentage
* Using the new MoneyCostCalculator to print the new Money Cost bar
* Implement money cost calculation in a new class called MoneyCostCalculator
* Fix Money Cost Bar position in containers
* Add money cost percentage in the tooltip
* Change CSS for the money cost bar and reported hours bar
* Change icon for the new money cost bar
* Add new money cost bar at this moment using value, icon and color of reported hours
* doc: Update Fedora and openSUSE documentation for upgrade LibrePlan
* doc: Update Fedora and openSUSE documentation for upgrade LibrePlan
* Merge branch 'libreplan-1.2' into money-cost-monitoring-system
* Fix typo in "Interporlation" (extra r)
* Fix typo in "Interporlation" (extra r)
* doc: Fix date format in on version at NEWS file
* doc: Fix date format in on version at NEWS file
* Bump version number to 1.2.2
* Update NEWS file for LibrePlan 1.2.2
* Update RPM package for LibrePlan 1.2.2
* Update Debian package for LibrePlan 1.2.2
* Update database scripts for LibrePlan 1.2.2
* Bump version number to 1.2.2
* Update NEWS file for LibrePlan 1.2.2
* Update RPM package for LibrePlan 1.2.2
* Update Debian package for LibrePlan 1.2.2
* Update database scripts for LibrePlan 1.2.2
* Remove some unneeded lines in libreplan.spec
* Remove some unneeded lines in libreplan.spec
* www: Add new README files for Fedora and openSUSE in libreplan.org
* www: Add new README files for Fedora and openSUSE in libreplan.org
* Rename database scripts to create database and user
* Prepare libreplan spec file for the release 1.2.2.
* Add LibrePlan RPM spec file
* Add instructions to configure LibrePlan in openSUSE
* Add instructions to configure LibrePlan in Fedora
* Add Tomcat6 configuration file
* Add scripts to create database and user libreplan
* Add CutyCapt RPM spec file
* Rename database scripts to create database and user
* Prepare libreplan spec file for the release 1.2.2.
* Add LibrePlan RPM spec file
* Add instructions to configure LibrePlan in openSUSE
* Add instructions to configure LibrePlan in Fedora
* Add Tomcat6 configuration file
* Add scripts to create database and user libreplan
* Add CutyCapt RPM spec file
* Updated documentation about the new i18n profile
* Wrap gettext plugin inside a new profile i18n to save time while developing
* Bug #1362: Specify type of property in Templates.hbm.xml to avoid problems with MySQL
* Bug #1362: Specify type of property in Templates.hbm.xml to avoid problems with MySQL
* doc: Removed legacy project logos from documentation screenshots
* doc: Removed legacy project logos from documentation screenshots
* Updated documentation about the new i18n profile
* Wrap gettext plugin inside a new profile i18n to save time while developing
* Add no negative constraint in budget fields in edition forms
* Make bigger the description field in templates edition
* Make bigger the description field in templates edition
* Add budget field in order element template edition form
* Use budget field when creating a template from a task or vice versa
* Renamed 'Order dashboard' perspective by 'Dashboard'
* Added new perspective icon for project dashboard
* Fixed nullpointer exception when there are not elements in deviations array
* Improved layout disposition and chart fonts in order dashboard
* Add budget field in order element details form
* Add budget cell in WBS
* Add new field budget to OrderLineTemplate
* Bug #1398: Fix problem with long descriptions in templates
* Bug #1398: Fix problem with long descriptions in templates
* Bug #1397: Revert a previous commit to avoid the problem
* Bug #1397: Revert a previous commit to avoid the problem
* Add basic tests for new attribute budget
* Add new field budget to OrderLine
* Bug #1393: Fix NPE moving milestone
* Bug #1393: Fix NPE moving milestone
* Bug #1394: Fix problem because of deletedWorkReportLinesSet set was not reseted
* Bug #1394: Fix problem because of deletedWorkReportLinesSet set was not reseted
* Change URL to demo in REST services example scripts
* Change URL to demo in REST services example scripts
* Sorts the configuration units by name
* Sort workers by lastname, name
* Bug #1387: Reset the TaskSource when an OrderGroup changes to scheduling point.
* Bug #1387: Reset the TaskSource when an OrderGroup changes to scheduling point.
* Bug #1390: correct method TaskComponent.setClass to overwrite the classes instead of adding them.
* Bug #1390: correct method TaskComponent.setClass to overwrite the classes instead of adding them.
* doc: Update AUTHORS file info about new Dutch translator
* doc: Update AUTHORS file info about new Dutch translator
* i18n: Add Dutch language to enum and modify pom.xml to use English userguide
* i18n: Add Dutch translation
* i18n: Add Dutch language to enum and modify pom.xml to use English userguide
* i18n: Add Dutch translation
* Bug #1382: Fix the bug in all cases.
* Bug #1382: Fix the bug in all cases.
* Bug #1382: Synchronize deadline dates between the WBS and the Gantt views.
* Bug 1383: Fixed corner case of deadline and current day right position
* Displayed project start vertical line with independency of project deadline
* Bug #1382: Synchronize deadline dates between the WBS and the Gantt views.
* Bug 1383: Fixed corner case of deadline and current day right position
* Displayed project start vertical line with independency of project deadline
* Added vertical line in scheduling perspective to display project start date
* Bug #1344: Fix bug moving the addition of ConstraintViolationListeners to doAfterCompose instead of constructor.
* Bug #1344: Fix bug moving the addition of ConstraintViolationListeners to doAfterCompose instead of constructor.
* Remove redundant call to scheduling algorithm from TaskPropertiesController.
* Some API docs for the entering/reentering part
* Remove uneeded parameters to prevent redundant invocations to scheduling algorithm.
* Use more accurate name
* Bug #1354: Fix bug
* Remove redundant call to scheduling algorithm from TaskPropertiesController.
* Some API docs for the entering/reentering part
* Remove uneeded parameters to prevent redundant invocations to scheduling algorithm.
* Use more accurate name
* Bug #1354: Fix bug
* Added vertical line in scheduling perspective to display project start date
* Fixed vertical positioning of resources string next to containers
* Fixed focus at textbox on created leaves with the ancestor hours
* Set focus in the element with empty name
* Fix some tests due to previous change
* Set name to empty for the order element moved inside the new container
* i18n: Update Portuguese translation
* i18n: Update Italian translation
* i18n: Update Portuguese translation
* i18n: Update Italian translation
* Update French translation
* Update French translation
* Bug #1355: transform AS SOON AS POSSIBLE and AS LATE AS POSSIBLE constraints to the correct constraint based on the scheduling mode.
* Bug #1355: transform AS SOON AS POSSIBLE and AS LATE AS POSSIBLE constraints to the correct constraint based on the scheduling mode.
* Bug #1380: Don't allow ASAP constraint for tasks in projects where init date is not set.
* Bug #1355: transform NOT EARLIER THAN and NOT LATER THAN constraints to the correct constraint based on the scheduling mode.
* Bug #1355: transform NOT EARLIER THAN and NOT LATER THAN constraints to the correct constraint based on the scheduling mode.
* Bug #1380: Don't allow ASAP constraint for tasks in projects where init date is not set.
* Keep task name for the new container if it is an empty leaf
* Change leaf creation behavior when selected parent is an empty leaf
* i18n: Update Spanish and Galician translations
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* i18n: Update keys.pot files
* Bug 1302: Fix changing ResourcesPerDay scale to 4 and rounding EffortDuration to minutes
* Bug #1374: Move capacity field to calendar tab in order to be edited together
* Bug #1374: Move capacity field to calendar tab in order to be edited together
* Remove scenarios option from configuration window
* i18n: Avoid to translate language names and use the original ones
* i18n: Avoid to translate language names and use the original ones
* Bug #1355: START IN FIXED DATE constraints don't change to NOT EARLIER THAN after drag & drop.
* Bug #1355: START IN FIXED DATE constraints don't change to NOT EARLIER THAN after drag & drop.
* Bug #1281: Remove unneeded checkVersion when editing progresses
* Bug #1281: Remove unneeded checkVersion when editing progresses
* Bug #1375: Added a constraint for 24 hours and 0 minutes
* Bug #1375: Added a constraint for 24 hours and 0 minutes
* Disables the button save in change password window and remove role and profile button on edit user
* Added condition to method isLdapUser to check configuration
* Disallow to change roles and profiles in admin panel for LDAP users when roles are imported from LDAP
* Disallow managing passwords in admin users panel for LDAP users when LDAP is being used for authentication
* Disallow to change passwords to LDAP users
* Disables the button save in change password window and remove role and profile button on edit user
* Added condition to method isLdapUser to check configuration
* Disallow to change roles and profiles in admin panel for LDAP users when roles are imported from LDAP
* Disallow managing passwords in admin users panel for LDAP users when LDAP is being used for authentication
* Disallow to change passwords to LDAP users
* Mark "Group by weeks" by default in MonteCarlo as it returns better results
* Bug #1379: Disable "Go" button in MonteCarlo if there is no tasks in a project
* Mark "Group by weeks" by default in MonteCarlo as it returns better results
* Bug #1379: Disable "Go" button in MonteCarlo if there is no tasks in a project
* Bug #1348: Added effort to predefined calendar exception types
* Bug #1348: Added effort to predefined calendar exception types
* Bug #1282, #1376: Prevent exception when deleting a milestone inside a container.
* Bug #1282, #1376: Prevent exception when deleting a milestone inside a container.
* Correct a typo in an English string.
* Bug #1362: Specify type of property in hbm.xml to avoid problems with MySQL
* Bug #1362: Specify type of property in hbm.xml to avoid problems with MySQL
* Bug #1343: Only closed projects in company view perspective are displayed in grey
* Bug #1343: Made graphically less relevant component showing project state
* Bug #1345: Added project state to name string in breadcrumbs line
* Bug #1343: Styled in grey all closed/finished/cancelled projects in company view
* Bug #1370: Fixed progress bars not being properly placed inside containers
* Bug #1373: When creating new projects from template the explicitly filled start date and deadline values are used
* Bug #1369: Moved the template combobox over autogenerated code checkbox in new project popup
* Bug #1369: Improved behaviour of project creation popup when using templates
* Bug #1343: Added method isRoot() to gantt tasks.
* Bug #1343: Only closed projects in company view perspective are displayed in grey
* Bug #1343: Made graphically less relevant component showing project state
* Bug #1345: Added project state to name string in breadcrumbs line
* Bug #1343: Styled in grey all closed/finished/cancelled projects in company view
* Bug #1370: Fixed progress bars not being properly placed inside containers
* Bug #1373: When creating new projects from template the explicitly filled start date and deadline values are used
* Bug #1369: Moved the template combobox over autogenerated code checkbox in new project popup
* Bug #1369: Improved behaviour of project creation popup when using templates
* Bug #1343: Added method isRoot() to gantt tasks.
* doc: Update translators info in documentation
* doc: Update translators info in documentation
* Update AUTHORS file info about translators
* i18n: Add French language to enum and modify pom.xml to use English userguide
* i18n: Add French translation
* Update AUTHORS file info about translators
* i18n: Add French language to enum and modify pom.xml to use English userguide
* i18n: Add French translation
* Bug #1343: Prevent NullPointerException when creating a new milestone.
* Bug #1343: Prevent NullPointerException when creating a new milestone.
* Correct a typo in an English string.
* Remove unnecessary method.
* Remove redraw listeners for dependencies once these dependencies are removed.
* Prevent unnecessary redraws of dependencies when opening a container.
* Bug #1363: Remove visibility listeners corresponding to deleted tasks.
* Remove redraw listeners for dependencies once these dependencies are removed.
* Prevent unnecessary redraws of dependencies when opening a container.
* Bug #1363: Remove visibility listeners corresponding to deleted tasks.
* Bug #1343: Prevent NullPointerException when showing default filtering dates on company view.
* Bug #1343: Prevent NullPointerException when showing default filtering dates on company view.
* Bug #1368: Allows to create users with null password when LDAP is used.
* Bug #1368: Allows to create users with null password when LDAP is used.
* Removed two warnings in the build process, which can cause problems with newer versions of maven.
* Removed two warnings in the build process, which can cause problems with newer versions of maven.
* Bug #1343: Ensure that getDefaultPredicate is run in a transactional context.
* Bug #1343: Ensure that getDefaultPredicate is run in a transactional context.
* Bug #1343: Mark closed projects with a special class in the gantt.
* Bug #1343: Use getRawValue instead of getValue in date constraint checkers.
* Bug #1343: Allow showing closed projects in the company view, if they are inside the filter dates.
* Bug #1343: Show default filtering dates on company view.
* Bug #1343: Rewrite part of the code for company view initialization.
* Bug #1343: Mark closed projects with a special class in the gantt.
* Bug #1343: Use getRawValue instead of getValue in date constraint checkers.
* Bug #1343: Allow showing closed projects in the company view, if they are inside the filter dates.
* Bug #1343: Show default filtering dates on company view.
* Bug #1343: Rewrite part of the code for company view initialization.
* Bug #1353: Create Tabpanels object for load/earned value Tabbox when the screen is loaded, not in the open event.
* Bug #1353: Create Tabpanels object for load/earned value Tabbox when the screen is loaded, not in the open event.
* Bug #1359: Initialize the resources when the page is loaded, independently from the hidden/shown property of the load chart.
* Bug #1359: Initialize the resources when the page is loaded, independently from the hidden/shown property of the load chart.
* Bug #1357: Use end date minus one day as default date for earned value chart.
* Bug #1357: Use end date minus one day as default date for earned value chart.
* Bug #1351: Replaced by a label the disabled listbox to display material unit type
* Bug #1351: fixed space for elements
* Bug #1330: Setted focus on new progress measurement after pressing on *Add measure*
* Bug #1351: Replaced by a label the disabled listbox to display material unit type
* Bug #1351: fixed space for elements
* Bug #1330: Setted focus on new progress measurement after pressing on *Add measure*
* Bug #1357: Check if the date is out of the visualization area, and in that case set a new date before updating the legend.
* Bug #1357: Small code rewrite to simplify the fix of the bug
* Bug #1357: Check if the date is out of the visualization area, and in that case set a new date before updating the legend.
* Bug #1357: Small code rewrite to simplify the fix of the bug
* Update TODO file with roadmap to LibrePlan 1.3
* Update TODO file with roadmap to LibrePlan 1.3
* Bug #1366: Fix issue subtracting the value when you are removing work report lines
* Bug #1366: Fix issue subtracting the value when you are removing work report lines
* Bug #1360: Refresh work report line from database before subtracting it from order elements
* Bug #1360: Refresh work report line from database before subtracting it from order elements
* Bug #1364: Milestones are filtered now like any other task
* Bug #1364: Milestones are filtered now like any other task
* Bug #1362: Fix problem with long descriptions in projects
* Bug #1362: Fix problem with long descriptions in projects
* Bug #1352: Fix issue not saving tasks without order element
* Bug #1352: Fix issue not saving tasks without order element
* Bug #1320: When asking a container for start constraints, return the leftmost of children's start-in-fixed-date constraints.
* Revert "[Bug #1273] Reimplement coerceToString as a workaround for the bug in Decimalbox."
* Revert "[Bug #1273] Reimplement coerceToString as a workaround for the bug in Decimalbox."
* Bug #1320: When asking a container for start constraints, return the leftmost of children's start-in-fixed-date constraints.
* Upgrade ZK version to 5.0.10
* Upgrade ZK version to 5.0.10
* doc: Update information in UPDATE file
* doc: Update information in UPDATE file
* Release new version LibrePlan 1.2.2
* Release new version LibrePlan 1.2.2
* i18n: Update Portuguese translation
* i18n: Update Portuguese translation
* Bug #1335: Don't force the check of the earned value legend date box every time the gantt is reloaded.
* Bug #1335: Don't force the check of the earned value legend date box every time the gantt is reloaded.
* Improve logging of possible exceptions checking version information
* Improve logging of possible exceptions checking version information
* Bug #1342: Move initial order state to PlanningState and use it from Gantt view too
* Bug #1342: Move initial order state to PlanningState and use it from Gantt view too
* Bug #1346: Fix the cause of the bug removing TaskElement from parent if it is removed
* Bug #1346: Fix the cause of the bug removing TaskElement from parent if it is removed
* Bug #1342: Show save button in Gantt view when it is disabled
* Bug #1342: Now it's possible to mark a project like STORED
* Bug #1342: Show save button in Gantt view when it is disabled
* Bug #1342: Now it's possible to mark a project like STORED
* Bug #1335: Update the earned value chart legend every time the chart is reloaded.
* Bug #1335: Update the earned value chart legend every time the chart is reloaded.
* doc: Update LDAP configuration translations
* doc: Update LDAP configuration translations
* Remove unused code in LibrePlanReportController
* i18: Remove unneeded code to manage languages in reports
* Remove unused code in LibrePlanReportController
* i18: Remove unneeded code to manage languages in reports
* i18n: Add support to Italian language in reports
* i18n: Update Italian translation and add missing files for reports
* i18n: Update Spanish and Galician translations
* i18n: Add support to Italian language in reports
* i18n: Update Italian translation and add missing files for reports
* i18n: Update Spanish and Galician translations
* Bug #1340: Add a listener to refresh the earned value chart in AdvanceAssignmentPlanningController.
* Bug #1334: Prevent the accumulation of the values when building the BCWP chart.
* Bug #1340: Add a listener to refresh the earned value chart in AdvanceAssignmentPlanningController.
* Bug #1334: Prevent the accumulation of the values when building the BCWP chart.
* Fix typo precision is just with 1 s
* Fix typo precision is just with 1 s
* i18n: Update keys.pot files
* Fix typo in open reports string
* i18n: Update keys.pot files
* Fix typo in open reports string
* Fix typo in new version string
* Fix typo in new version string
* Add information about current version in GET requests
* Add VERSION file just with version number in a text file
* Add configuration option to allow LibrePlan developers collect usage stats
* Add configuration option to disable warning about new LibrePlan versions
* Show a warning if there is a new project version published.
* Add information about current version in GET requests
* Add VERSION file just with version number in a text file
* Add configuration option to allow LibrePlan developers collect usage stats
* Add configuration option to disable warning about new LibrePlan versions
* Show a warning if there is a new project version published.
* Bug #1336: Checked permissions to enable project creation button
* Fixed chart tooltips scroll problem on resources load window
* Bug #1336: Checked permissions to enable project creation button
* Fixed chart tooltips scroll problem on resources load window
* Bug #1338: Fix some wrong strings in timeLineRequiredMaterial report
* Bug #1338: Fix some wrong strings in timeLineRequiredMaterial report
* Bug 1295: Remove TaskElements (except milestones) with TaskSource null when saving
* Bug 1295: Remove TaskElements (except milestones) with TaskSource null when saving
* Fixed wrong criteria string format
* Bug #1337: Removed unnecesary response when redrawing earned value
* Bug #1324: Modified behaviour of west end arrow on violated dependencies
* Display timeplot graph values when pointing over the chart
* Fixed wrong criteria string format
* Bug #1337: Removed unnecesary response when redrawing earned value
* Bug #1324: Modified behaviour of west end arrow on violated dependencies
* Display timeplot graph values when pointing over the chart
* doc: Update LDAP configuration doc
* Bug #1333: Allow specify * in role matching
* doc: Update LDAP configuration doc
* Bug #1333: Allow specify * in role matching
* i18n: Add Italian language language to enum and modify pom.xml to use English userguide
* i18n: Add Giuseppe Zizza as Italian translator in AUTHORS file
* i18n: Add Italian translation
* i18n: Add Italian language language to enum and modify pom.xml to use English userguide
* i18n: Add Giuseppe Zizza as Italian translator in AUTHORS file
* i18n: Add Italian translation
* Bug #1333: Fix issue as property and search query are needed for group strategy too
* Bug #1332: Fix problem allowing to set empty values for userDn and password
* Bug #1333: Fix issue as property and search query are needed for group strategy too
* Bug #1332: Fix problem allowing to set empty values for userDn and password
* Bug #1329: Fix problem in StretchesFunctionTest due to changes in previous test
* Bug #1329: Fix issue calculating properly end date of stretches
* Bug #1329: Now tasks are not enlarged and nothing breaks just after selecting a stretches function
* Bug #1329: Update advanced allocation row after applying default stretches function
* Bug #1329: Fix problem in StretchesFunctionTest due to changes in previous test
* Bug #1329: Fix issue calculating properly end date of stretches
* Bug #1329: Now tasks are not enlarged and nothing breaks just after selecting a stretches function
* Bug #1329: Update advanced allocation row after applying default stretches function
* Merge branch 'master' into subcontracting
* Bug #1328: Fix issue calling onClose method on controller
* Bug #1328: Fix issue calling onClose method on controller
* Bug #1261: Extract method createTab with common functionality for each tab
* Bug #1261: Allows tabs to be memorized when changing perspective
* Bug #1261: Extract method createTab with common functionality for each tab
* Bug #1261: Allows tabs to be memorized when changing perspective
* Bug #1327: Fix issue changing method to do reassignments in consolidation process
* Modify GenericDayAssignment.toString to add info about consolidation
* Bug #1327: Fix issue changing method to do reassignments in consolidation process
* www: Folder and script for libreplan.org documentation
* www: Folder and script for libreplan.org documentation
* [Bug #1326] Fix issue calculating properly hours to allocate
* [Bug #1326] Fix issue calculating properly hours to allocate
* [Bug #1325] Fix issue remove criteria from configuration unit
* [Bug #1325] Fix issue remove criteria from configuration unit
* [Bug #1322] Fix bug
* Allow to include or exclude DerivedDayAssignments
* Allow reported hours bar in gantt diagram to be wider than the task.
* [Bug #1242] Don't allow progress end dates superior to the corresponding task end date.
* [Bug #1311] Replace Date objects with IntraDayDate objects in SaveCommandBuilder.
* [Bug #1311] Replace Date objects with IntraDayDate objects in SaveCommandBuilder.
* [Bug #1242] Don't allow progress end dates superior to the corresponding task end date.
* Allow reported hours bar in gantt diagram to be wider than the task.
* [Bug #1321] Reseting highlighted days in calendar to prevent issue
* [Bug #1321] Reseting highlighted days in calendar to prevent issue
* [Bug #1323] Fix lazy loading properly derived allocations
* [Bug #1323] Fix lazy loading properly derived allocations
* [Bug #1242] Return end date directly when calculating advance and percentage is 100%.
* [Bug #1242] Use IntraDayDate to draw the progress bar in leaf Tasks.
* [Bug #1242] Return end date directly when calculating advance and percentage is 100%.
* [Bug #1242] Use IntraDayDate to draw the progress bar in leaf Tasks.
* [Bug #1319] Change division method of EffortDuration
* [Bug #1304] Make sumOfAssignedEffort return the cached value only for orders.
* [Bug #1304] Reorder the code to improve the performance with TaskGroups.
* Replace attribute TaskElement.sumOfHoursAllocated with an equivalent attribute measured in EffortDuration.
* [Bug #1304] Don't use the cached value sumOfHoursAllocated when drawing the progress bars of tasks.
* Revert "[Bug #1319] Change division method of EffortDuration"
* [Bug #1309] Fix problem with reported hours bar when progress type is changed
* [Bug #1309] Fix problem with reported hours bar when progress type is changed
* [Bug #1319] Change division method of EffortDuration
* [Bug #1319] Change division method of EffortDuration
* [Bug #1309] Invalidate each TaskComponent instead of the whole TaskList when progress type is changed
* [Bug #1309] Invalidate each TaskComponent instead of the whole TaskList when progress type is changed
* Hide warning messages on bottom when user is not admin
* Hide warning messages on bottom when user is not admin
* [Bug #1288] Return null instead of zero in a just created AdvanceMeasurement
* [Bug #1288] Fix message regarding progress type precision
* [Bug #1288] Set Decimalbox scale according to progress type precision
* [Bug #1288] Return null instead of zero in a just created AdvanceMeasurement
* [Bug #1288] Fix message regarding progress type precision
* [Bug #1288] Set Decimalbox scale according to progress type precision
* [Bug #1307] Prevent changing spread progress in children when parents are consolidated
* [Bug #1307] Prevent changing spread progress in children when parents are consolidated
* [Bug #1307] Prevent add progress measurement before consolidated day in any parent
* Refactor method getSpreadIndirectAdvanceAssignmentWithSameType to be used in more places
* [Bug #1307] Prevent add progress measurement before consolidated day in any parent
* Refactor method getSpreadIndirectAdvanceAssignmentWithSameType to be used in more places
* Prevent NPE editing progress measurements
* Prevent NPE editing progress measurements
* [doc] Add warning on INSTALL file about how to create database structure
* [doc] Add warning on INSTALL file about how to create database structure
* [Bug #1308] Remove unneeded preventing create default progress in containers
* [Bug #1308] Remove unneeded preventing create default progress in containers
* [Bug #1305] Prevent adding progress measurement before last consolidated date
* Fix messages related with progress management and consolidations
* Disable some options in progress management if there is a consolidated progress
* [Bug #1305] Prevent adding progress measurement before last consolidated date
* Fix messages related with progress management and consolidations
* Disable some options in progress management if there is a consolidated progress
* [doc] Update SourceForge.net URLs due to rename to LibrePlan
* [doc] Update SourceForge.net URLs due to rename to LibrePlan
* [Bug #1316] Use correct JS selectors to work both with leaf tasks and task groups.
* [Bug #1304] Make sumOfAssignedEffort return the cached value only for orders.
* [Bug #1304] Reorder the code to improve the performance with TaskGroups.
* [Bug #1316] Use correct JS selectors to work both with leaf tasks and task groups.
* Fix NPE removed advance measurement
* Remove unused method
* Fix NPE removed advance measurement
* Remove unused method
* Replace attribute TaskElement.sumOfHoursAllocated with an equivalent attribute measured in EffortDuration.
* [Bug #1301] Fix issue creating a container in a task with dependencies
* [Bug #1301] Fix issue creating a container in a task with dependencies
* Remove duplicated method in QualityForm
* Remove duplicated method in QualityForm
* [Bug #1314] Fix problem in materials report
* [Bug #1314] Fix problem in materials report
* Skipping some tests in ScenarioModelTest because they were causing problems in Debian Wheezy
* Remove uneeded test as it was causing problems in Debian Wheezy
* Fix problems in MaterialDAOTest and MaterialServiceTest
* Fix problem with consolidation and specific assignments
* [Bug #1300] Updated intended resources per day after consolidation is added or removed
* Fix problem with consolidation and specific assignments
* [Bug #1300] Updated intended resources per day after consolidation is added or removed
* [Bug #1304] Don't use the cached value sumOfHoursAllocated when drawing the progress bars of tasks.
* [Bug #1312] Fix issue with printing in HTTPs is enabled
* [Bug #1312] Fix issue with printing in HTTPs is enabled
* [Bug #1303] Fix issue removing consolidation using IntraDayDate for task end
* [Bug #1303] Fix issue adding consolidation using IntraDayDate for task end
* [Bug #1303] Fix issue removing consolidation using IntraDayDate for task end
* [Bug #1303] Fix issue adding consolidation using IntraDayDate for task end
* Skipping some tests in ScenarioModelTest because they were causing problems in Debian Wheezy
* Remove uneeded test as it was causing problems in Debian Wheezy
* Fix problems in MaterialDAOTest and MaterialServiceTest
* [Bug #1310] Reorder the code to fix the two exceptions happening here.
* [Bug #1310] Reorder the code to fix the two exceptions happening here.
* set the relationship owner, in order to save the reference of the order, without the need of a property on the other side of relation, with the class DeadlineCustomer
* revamp the interface of the General Data tab in the Project Details perspective, in order to show the deliver dates.
* It fixes and adds constraints for adding and deleting the deliver dates correctly.
* Create new subcontractor state for sending updates of the delivering date, while it is created a new customer communication.
* Create new subcontractor communication type: UPDATE_DELIVERING_DATE
* Changes the fields in the subcontraction pop-up to read-only if subcontracted task has been sent.
* [Bug #1299] Enable scrollbars in the tasks input buffer grid.
* [Bug #1299] Enable scrollbars in the tasks input buffer grid.
* [Bug #1297] Add listeners to 'See schedule allocation' buttons every time the panel is redrawn.
* [Bug #1297] Add listeners to 'See schedule allocation' buttons every time the panel is redrawn.
* [Bug #1297] Replaced the misleading word "Filter" with "Show".
* [Bug #1297] Reset the list listenersToAdd when changing perspectives.
* [Bug #1297] Replaced the misleading word "Filter" with "Show".
* [Bug #1297] Reset the list listenersToAdd when changing perspectives.
* [Bug #1073] Fix issue introduced by me while reviewing the previous patch
* [Bug #1073] Fix issue introduced by me while reviewing the previous patch
* [Bug #1294] Revert "Update i18n files with the new translations for the replaced string."
* [Bug #1073] Add dependencies with its parent when a milestone is added.
* [Bug #1073] Add dependencies with its parent when a milestone is added.
* [Bug #1294] Update i18n files with the new translations for the replaced string.
* [Bug #1294] Replace the sentence 'Click on direct link to go to output directly' with a better alternative.
* [Bug #1294] Replace the sentence 'Click on direct link to go to output directly' with a better alternative.
* [doc] Move images to a proper folder
* [doc] Create new document about how to develop LibrePlan in Eclipse
* changes on interface and fixes several issues in the subcontract pop-up for adding new subcontractor deliver date.
* [doc] Add warning about printing issues in Debian Squeeze
* [doc] Add instructions to install Cutycapt from testing
* [doc] Add warning about printing issues in Debian Squeeze
* [doc] Add instructions to install Cutycapt from testing
* [Bug #1296] Rename Spanish and Galician .properties files as country was not needed
* [Bug #1296] Remove duplicated \*_en_US.properties files after updating \*.properties when required
* [Bug #1296] Rename Spanish and Galician .properties files as country was not needed
* [Bug #1296] Remove duplicated \*_en_US.properties files after updating \*.properties when required
* [Bug #1292] Replace Toolbarbutton object for the link to the PDF with an A object.
* [Bug #1292] Replace Toolbarbutton object for the link to the PDF with an A object.
* Remove an unnecessary check.
* [Bug #1287] Fix NPE opening a project from Gantt view
* [Bug #1287] Fix NPE opening a project from Gantt view
* [Bug #1290] Fix NPE issue launching LibrePlan
* [Bug #1290] Fix NPE issue launching LibrePlan
* [Bug #1291] Fix NPE when a task with a dependency is removed
* [Bug #1291] Fix NPE when a task with a dependency is removed
* Remove duplicated method in TaskElement
* creates a new field in the SubcontratedTaskData to store the collection of subcontrator delivering dates.
* create the interface DeliverDate and the comparator DeliverDateComparator to sort the SubcontractorDeliverDate and the DeadlineCommunications.
* [Bug #1229] Look for new resources when moving a task
* [Bug #1229] Wrap all position mofications
* Create the class SubcontractorDeliverDate
* creates a new field in the Order entity to store the delivering dates communications.
* creates a new field in the Order entity to store the delivering dates communications.
* create the class DeadlineCommunication
* [Bug #1285] Avoided NPE when dropping a treerow in the same component
* Add vertical borders in watermarks to improve differentiation between days
* Added transparency effect to hover treerow
* [Bug #1256] Removed inner padding on help tooltip
* Made more explicit links styles in headers
* Changed treerow input style for over elements
* [Bug #1285] Avoided NPE when dropping a treerow in the same component
* Add vertical borders in watermarks to improve differentiation between days
* Added transparency effect to hover treerow
* [Bug #1256] Removed inner padding on help tooltip
* Made more explicit links styles in headers
* Changed treerow input style for over elements
* [i18n] Update Portuguese translation
* [i18n] Update Portuguese translation
* Remove an unnecessary check.
* Merge branch 'master' into project-dashboards
* Depending on jfreechartengine 1.1 in order to use new charts
* Handle charts visibility in a more elegant way.
* Reset cached task status after setting new advance percentage.
* Add style to warning message.
* Create interface IDashboardModel to do dependency injection correctly, as Developers reference recommends.
* Avoid storing references to domain objects in DashboardController.
* Handle projects with no tasks in a nicer way.
* Bring time KPI "Lead/Lag in task completion" to the UI.
* Extract local variables to class static attributes.
* Preliminar business logic implementation for time KPI "Lead/Lag in task completion".
* Set a method transactional.
* Refactor. Extract some code to a private method because the same logic is gonna be called from other methods in the future.
* Fix marginWithDeadline chart.
* Fix window scrolling.
* Cache margin with deadline KPI because now it's requested by the controller twice.
* Refactor local variable name.
* Customize X axis tick font, series color and range axis bounds.
* Bring time KPI "Estimation accuracy" to the UI.
* Fix typo in static member name.
* Adjust upper bound for EA stretches.
* Add visitor to calculate hours estimation deviation for finished tasks.
* Business logic for time KPI "Estimation accuracy"
* Temporary UI for time KPI "Margin with deadline"
* Implement business logic for time KPI "Margin with deadline"
* Avoid to add subcontractor progress in a project with subcontracted tasks.
* Avoid to subcontract a task if there are subcontractor progresses incompatible with receiving progress reporting from the provider
* it removes unnecessary prints of text.
* Refactoring the code for replacing the occurences of comunication by communication.
* it changes REPORT_ADVANCE for REPORT_PROGRESS and set properly the copyright
* it removes trailing whitespaces.
* it reduces the visibility of the constructor and uses the inherited method create
* Merge branch 'master' into project-dashboards
* Add a generic method to calculate percentages to avoid duplicate code.
* Coding style and minor issues.
* i18nize user-space strings.
* Bring progress KPI "Deadline violation" to the UI.
* Cancel planning state reattaching.
* Add pie chart to represent progress KPI "Number of tasks by status".
* Add bar chart to represent progress KPI "Global progress of the project".
* Bring progress KPI "Global progress of the project" to DashboardModel.
* Add extra methods to calculate working days until date that consider limit dates more recent than the end date of the task.
* Assign EfforDuration references again to the result of the sum as they are immutable.
* Add method to retrieve theoretical progress.
* Several changes to the UI: * Use bindings to link ZUL items to controller methods. * Add Model for DashboardController * Implement methods to bring data and calculate progress KPI "Task Status" * Fetch PlanningState to get updated planning status * Add dumb Label to the view to do preliminary tests.
* Add a new empty tab to the UI.
* Initialize Map before traversing task graph.
* Enable recursion.
* Request only first-level children when traversing task graph.
* Create a new Integer when incrementing status counts as wrappers are immutable.
* Fix test. Reset task status.
* Avoid using equals to compare if task progress is zero or one, use compareTo instead.
* Add visitor to reset task statuses.
* Cache task status (only if FINISHED or IN_PROGRESS) to avoid doing unnecessary calculations (especially in nodes if type TaskGroup).
* Fix coding style. Add space in for/if statements.
* Add description to two classes.
* Rename class (Visitor -> TaskElementVisitor)
* Initialize Map before traversing task tree.
* Create a list with all the communications received from subcontractors
* add new communication type 'Report advance'
* renamed file FilterCustomerComunicationEnum to ICustomerComunicationModel
* use private visibility in constructors.
* Create the entity SubcontractorComunication, the dao SubcontractorComunicationDAO, the test SubcontractorComunicationDAOTest and add the changes of the database in a new file db.changelog-1.2.xml
* Merge branch 'master' into project-dashboards
* Refactor getter name.
* Implement bussiness logic for KPI "Deadline violation".
* Add header to newly created files.
* Add test.
* Drop unnecessary assert.
* Preliminary implementation of task status resolvers.
* Change method visibility to call it from unit tests while mocking Dependency.
* Add a helper method to test if an instance of SumChargedEffort is zero.
* Improve ComunicationType enum and include the translate method.
* Update the routing of the css with libreplan.
* Add the relationship from order entity with its customer comunications.
* Create a customer comunication when a subcontractor receives the communication of a new project to be developed.
* Add the external code when a whole order is imported as subcontrated task.
* Create a list of incoming projects accepted by customers and contracted with the company
* Create the entity CustomerComunication, the dao CustomerComunicationDAO, the test CustomerComunicationDAOTest and add the changes of the database in a new file db.changelog-1.2.xml
* Fix 2 style mini-issues
* Turn ResourceCalendar mock creator static to reuse it from another test.
* Fix copyright.
* Refactor attribute names and getters.
* Remove TODO.
* Use EffortDuration to compute time instead of using raw hours.
* Add a method to return division result as BigDecimal.
* Calculate total assigned hours instead of relaying on getSumOfHoursAllocated.
* * Use the correct method to sum all allocated hours. * Prevent division by zero.
* Implement indicator 3.3
* Use private method for dividing.
* Implement KPI 3.2 (Global progress of the project) and bring 3.1 to PlanningData.
* Implement business logic to calculate Global Progress Indicator number 3.1.


Version 1.2.4 (23 May 2012)
---------------------------

Summary
~~~~~~~

This is a new minor release of LibrePlan. It includes all the fixes done since
previous version 1.2.3 together with a new language (Czech) supported in the
application.

Some highlights:

* Fixed problem in "Hours Worked Per Resource" report with standard work
  reports.

* Solved small issue in project web service in order to allow add tasks to an
  already existent project.

* Remove scenarios option in configuration window.

* Reviewed algorithm to do generic allocations in order to avoid any problem if
  there are more than one generic allocation in the same task.

* Added favicon in all the windows.

* Improved web services documentation with the list of available services.

* Fixed problem using deadlines.

* LibrePlan is now translated into Czech language thanks to the work done by
  Zbyněk Schwarz.

* This is the first version when we are providing files for using MySQL
  database. This has been possible thanks to the upgrade to Liquibase 2.0.5.


Notes
~~~~~

If you are upgrading from any 1.1.x version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.0.sql``, ``scripts/database/upgrade_1.2.1.sql``,
``scripts/database/upgrade_1.2.2.sql`` and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.1.sql``, ``scripts/database/upgrade_1.2.2.sql``
and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.1 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.2.sql`` and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.2 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.3.sql``.

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Óscar González Fernández
* Manuel Rego Casasnovas
* Juan A. Suarez Romero
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [cs] Zbyněk Schwarz
* [es] Manuel Rego Casasnovas
* [fr] Philippe Poumaroux
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [nl] Jeroen Baten

Changes
~~~~~~~

* Add database script for MySQL
* Bug #1423: Remove unneeded line
* doc: Update AUTHORS file info about new Czech translator
* i18n: Add Czech language to enum and modify pom.xml to use English userguide
* i18n: Add Czech language
* Bug #1448: Fix issue reattaching the work report before removing
* Bug #1450: Fix issue rounding when setting budget scale.
* Bug #1447: Remove the test checking the creation of NOT_LATER_THAN constraint when a deadline is set.
* Bug #1447: Prevent the creation of NOT_LATER_THAN constraint when a deadline is set.
* doc: Update Eclipse document with 2 new sections (Maven profiles and MySQL development)
* Bug #1444: Renamed variable with a more meaningful name.
* Bug #1444: Save affected parent TaskElements in the end of the process.
* Bug #1445: Fix issue adding purple color in CalendarExceptionTypeColorConverter
* doc: Fix wrong path in web services documentation
* Add some Javadoc to AssignedEffortForResource class
* Fix Sahi tests due to removal of scenarios option
* Bug #1428: Rename customAssignedEffortForResource to setAssignedEffortForResource
* Bug #1428: Take into account the load of the other allocations when reassigning
* Bug #1428: Fix bug
* Bug #1428: Bring all files related to IAssignedEffortForResource to one unique file
* Revert "Bug #1428: Possible fix"
* Bug #1431: Sort the EffortModifications too
* Bug #1431: Fix problem in commit 9d5e3d88dd4dacc4fc00af544a3306d4327dd674
* doc: Update development guide with the line for the favicon
* doc: Improve web services documentation with the list of available services
* i18n: Update Dutch translation
* i18n: Update Italian translation
* Bug #1442: Fix regression.
* Update Liquibase to 2.0.5
* Bug #1284: Added favicon in all the screens.
* i18n: Fix small typo in Spanish and Galician translations.
* Bug #1284: Added favicon.
* Bug #1284: Fix NullPointerException.
* Bug #1414: Reduced width of MultipleBandbox filter search
* Bug #1421: Fixed the pagination bug when indenting nodes into containers on previous pages
* Bug #1433: Fix the problem when there are multiple levels of tasks involved.
* Bug #1433: Small code refactor of this bug fix.
* Bug #1433: Make sure that old TaskSources are deleted also in the case of parent tasks.
* Bug #1433: Make sure that old TaskSources are deleted when a task is unscheduled and re-scheduled.
* i18n: Update French translation
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* Fix wrong message in deadline constraints
* i18n: Update keys.pot files
* Bug #1431: Fix test because of now allocations are sorted (specific before generic)
* Bug #1431: Fix test creating a standard list instead of an unmodifiable list
* Bug #1431: Fix issue sorting allocations (first the specific ones)
* Bug #1428: Possible fix
* Bug #1430: Fixing issue checking code for each child and not for the parent node
* Bug #1430: Fix problem with error messages in OrderElementConverter
* Remove scenarios option from configuration window
* Bug #1425: Fix problem change wrong parentheses
* Bug #1263: Fix issue reseting the value of the textbox
* Bug #1388: Fix pending things that were not ready in libreplan-1.2 yet
* Remove unused variable in TreeController.Renderer.updateBudgetFor(T)
* Bug #1388: Fix issue updating name textbox when coming back from pop-up
* Bug #1423: Fix bug adding root task to graph
* Bug #1424: Fix issue loading derived allocations
* Bug #1412: Avoid NPE if clockStart and clockFinish are not defined
* Bug #1422: Fix issue calling onRetreival to force synchronization
* Upgrade ZK to 5.0.11
* rpm: Make sure all update SQL scripts are installed
* Fix problems with Liquibase 2.0.4 and MySQL for tag modifyDataType
* Update Liquibase to 2.0.4
* doc: Fix some broken links to files in SourceForge.net
* doc: Add note about removing browser cache in NEWS file
* Corrected wrong indentation in NEWS file.
* Bug #1384: Add validation in starting date and deadline depending on position constraints
* Bug #1416: Reload resources text in parent tasks too


Version 1.2.3 (19 Apr 2012)
---------------------------

Summary
~~~~~~~

A new minor version of the LibrePlan 1.2.* version family. The main changes
included in this new release are:

* Money based cost monitoring system: This is a new feature that allows users to
  monitor the project cost based on the money spent comparing it to the budget.
  Users can configure the budget for each task and, after this, LibrePlan
  calculates the cost in money already spent using the worked time tracked, the
  type of worked hours (standard, overtime,...) and the cost of each resource
  hour according to the value defined by the cost category the worker belongs.

* Polish language: LibrePlan is now translated into Polish thanks to the work
  done by Krzysztof Kamecki.

* Other minor enhancements and bugfixing:

  * Fixed database synchronization issues which appeared on changing planning
    points in the WBS. The problems arose when planning points were moved from
    children to their parents or vice versa.
  * Default users (user, wsreader and wswriter) are disabled by default.
  * Fixed resource usage grouped by criteria load analysis that was being bad
    calculated inside a project.
  * Task duration was not being refreshed properly when doing an allocation and
    you needed to apply the allocation twice to see it right.
  * START_IN_FIXED_DATE constraint caused that the project duration was bad
    calculated in company view.

Notes
~~~~~

.. WARNING::

  Remove web browser cache to avoid any problem with changes in JavaScript
  resources.

If you are upgrading from any 1.1.x version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.0.sql``, ``scripts/database/upgrade_1.2.1.sql``,
``scripts/database/upgrade_1.2.2.sql`` and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.1.sql``, ``scripts/database/upgrade_1.2.2.sql``
and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.1 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.2.sql`` and ``scripts/database/upgrade_1.2.3.sql``.

If you are upgrading from 1.2.2 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.3.sql``.

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Óscar González Fernández
* Susana Montes Pedreira
* Francisco Javier Morán Rúa
* Manuel Rego Casasnovas
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [es] Manuel Rego Casasnovas
* [fr] Philippe Poumaroux
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [nl] Jeroen Baten
* [pl] Krzysztof Kamecki

Changes
~~~~~~~

* Bug #1417: Add UI validation to prevent empty codes
* doc: Update AUTHORS file info about new Polish translator
* i18n: Add Polish language to enum and modify pom.xml to use English userguide
* i18n: Add Polish translation
* Bug #1358: Removed unnecessary tooltip string
* Bug #1358: Added CSS max dimensions restriction to configured company logo
* Fixed vertical positioning of resources string next to containers
* Bug #1407: Run TaskComponent.updateProperties() after running the scheduling algorithm.
* Remove unused fields in Order Costs Per Resource Report
* Bug #1412: Fix problem converting to string clockStart and clockFinish
* Bug #1409: Rename RelatedWithAnyOf to RelatedWith
* Bug #1409: Fix problem replacing allocations for the ones related to the criterion
* Revert "Bug #1320: When asking a container for start constraints, return the leftmost"
* Bug #1411: Missing Spanish translation for "Project cost by resource"
* Fix Sahi test due to change in string
* i18n: Update Dutch translation
* i18n: Update French translation
* doc: Update HACKING file about the compilation options
* Add new compilation option to disable default users (user, wsreader and wswriter)
* Bug #1395: Fix issue setting width of date boxes to 100px
* i18n: Update Italian translation
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* Bug #1402: Invalidate the TaskComponents instead of the whole GanttPanel
* Bug #1349: Fix translation issue in choosing template pop-up
* Bug #1349: Mark to translate exception day type
* Bug #1349: Fix translation in calendar type
* Bug #1298: Mark to translate roles in user and profile edition
* Remove CutyCaptTimeout
* Bug #1406: Add UI validation for name field too
* Bug #1406: Add validation in the UI and also a try catch for possible ValidationExceptions
* doc: Fix typo in INSTALL file
* doc: Fix broke link in INSTALL file
* doc: Update information about how to install in Fedora and openSUSE
* doc: Add info about JAVA_OPTS configuration in INSTALL file
* [Bug #1234] Fix the deletion of fields in progress reporting in subcontractor module.
* Fix problems in Liquibase changes in MySQL
* Fix compilation error in previous merge
* Merge branch 'libreplan-1.2' into money-cost-monitoring-system
* doc: Add info about add-apt-repository command in INSTALL file
* Bug #1387: Code refactor of the previous patches for this bug.
* Bug #1387: Fix bug when it happens in the opposite way.
* Show budget information in a read-only field inside task properties tab
* Bug #1387: Fix bug
* Change color of money cost bar to a darker one to avoid accessibility issues
* Bug #1403: Only regenerate codes if isCodeAutogenerated() is true
* Add a map in MoneyCostCalculator to cache calculated values
* Disable Money Cost Bar in company view to avoid performance issues
* Bug #1289: Added subcontractor name to tasks when showing resources is enabled
* Remove unneeded throws in MoneyCostCalculatorTest
* Add unit tests to check MoneyCostCalculator with a different type of hours
* Remove commented lines in MoneyCostCalculatorTest
* Update Copyright info in user documentation
* Improve sentence in "Imputed hours" tab editing a task
* Add information about budget in "Imputed hours" tab
* Add unit tests to check MoneyCostCalculator with a tree of tasks
* Remove unused parameters in CutyPrint.createCSSFile
* Add option to print money cost bar
* Add a new test case to check MoneyCostCalculator when there is not relationship via cost category
* Prevent possible rounding problems dividing BigDecimals
* Prevent NPE if there is not relationship between resource and type of hours via cost category
* Reload budget field in "General data" of templates
* Add field in "General data" tab to show the project budget
* Print Money Cost Bar proportinal to task size
* Prevent NPE calculating money cost for a TaskElement
* Remove method getMoneyCostBarPercentage from ITaskFundamentalProperties
* Improve tooltip message using budget, consumed money and percentage
* Using the new MoneyCostCalculator to print the new Money Cost bar
* Implement money cost calculation in a new class called MoneyCostCalculator
* Fix Money Cost Bar position in containers
* Add money cost percentage in the tooltip
* Change CSS for the money cost bar and reported hours bar
* Change icon for the new money cost bar
* Add new money cost bar at this moment using value, icon and color of reported hours
* doc: Update Fedora and openSUSE documentation for upgrade LibrePlan
* Merge branch 'libreplan-1.2' into money-cost-monitoring-system
* Fix typo in "Interporlation" (extra r)
* doc: Fix date format in on version at NEWS file
* Add no negative constraint in budget fields in edition forms
* Add budget field in order element template edition form
* Use budget field when creating a template from a task or vice versa
* Add budget field in order element details form
* Add budget cell in WBS
* Add new field budget to OrderLineTemplate
* Add basic tests for new attribute budget
* Add new field budget to OrderLine


Version 1.2.2 (15 Mar 2012)
---------------------------

Summary
~~~~~~~

Minor release of LibrePlan including all the maintanance work and small
enhancements done since 1.2.1.

The major developments which come with this new version are the next ones:

* We are going to provide for the first time RPM packages for the main
  GNU/Linux distributions based on this package format. This has been a
  contribution of Juan A. Suárez Romero.

* LibrePlan interface is available in two new languages thanks to our growing
  translators community:

  * French. Thanks to Stephane Ayache, Guillaume Postaire and
    Philippe Poumaroux.
  * Dutch. Thanks to Jeroen Baten.

* Two new planning features have been added:

  * The Gantt chart has been improved to include a graphic representation of the
    start date of a project through a dashed black vertical line. This is very
    useful to have always visible when a project starts.
  * If there are violated dependencies in the Gantt charts because of the higher
    precendence of the task constraints, the dependencies are painted in red.

* Some small user experience enhancements:

  * The project status is displayed in the Gantt chart next to the breadcrumb.
  * States that represent a closed project (finished, canceled, stored) are
    represented in the projects planning view (home page of the application)
    with a grey color. So, from now on the code color is the next one:

    * Light blue. For projects without any allocated task and not closed.
    * Dark blue. For project with any allocated task and not closed.
    * Grey. For closed projects.

  * To measure progress is now a bit faster. The focus is automatically set in
    the textbox of the row just created for the new progress value.

* LDAP authentication system has been improved:

  * LDAP users cannot change the password in the LibrePlan interface.
  * Managers cannot manage LDAP user roles if they are being imported from the
    configured directory.

Apart from this, the new versions come with fixes of many issues detected by
our users and that can be read in the Changes section.

Notes
~~~~~

If you are upgrading from any 1.1.x version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.0.sql``, ``scripts/database/upgrade_1.2.1.sql``
and ``scripts/database/upgrade_1.2.2.sql``.

If you are upgrading from 1.2.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.1.sql`` and
``scripts/database/upgrade_1.2.2.sql``.

If you are upgrading from 1.2.1 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.2.sql``.

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Nacho Barrientos
* Ignacio Díaz Teijido
* Lucía García Fernández
* Óscar González Fernández
* Manuel Rego Casasnovas
* Juan A. Suárez Romero
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [es] Manuel Rego Casasnovas
* [fr] Stephane Ayache, Guillaume Postaire, Philippe Poumaroux
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [nl] Jeroen Baten
* [pt] Joaquim Rocha

Changes
~~~~~~~

* Update RPM package for LibrePlan 1.2.2
* Update Debian package for LibrePlan 1.2.2
* Update database scripts for LibrePlan 1.2.2
* Remove some unneeded lines in libreplan.spec
* www: Add new README files for Fedora and openSUSE in libreplan.org
* Rename database scripts to create database and user
* Prepare libreplan spec file for the release 1.2.2.
* Add LibrePlan RPM spec file
* Add instructions to configure LibrePlan in openSUSE
* Add instructions to configure LibrePlan in Fedora
* Add Tomcat6 configuration file
* Add scripts to create database and user libreplan
* Add CutyCapt RPM spec file
* Updated documentation about the new i18n profile
* Wrap gettext plugin inside a new profile i18n to save time while developing
* Bug #1362: Specify type of property in Templates.hbm.xml to avoid problems with MySQL
* doc: Removed legacy project logos from documentation screenshots
* Make bigger the description field in templates edition
* Bug #1398: Fix problem with long descriptions in templates
* Bug #1397: Revert a previous commit to avoid the problem
* Bug #1393: Fix NPE moving milestone
* Bug #1394: Fix problem because of deletedWorkReportLinesSet set was not reseted
* Change URL to demo in REST services example scripts
* Bug #1387: Reset the TaskSource when an OrderGroup changes to scheduling point.
* Bug #1390: correct method TaskComponent.setClass to overwrite the classes instead of adding them.
* doc: Update AUTHORS file info about new Dutch translator
* i18n: Add Dutch language to enum and modify pom.xml to use English userguide
* i18n: Add Dutch translation
* Bug #1382: Fix the bug in all cases.
* Bug #1382: Synchronize deadline dates between the WBS and the Gantt views.
* Bug 1383: Fixed corner case of deadline and current day right position
* Displayed project start vertical line with independency of project deadline
* Added vertical line in scheduling perspective to display project start date
* Bug #1344: Fix bug moving the addition of ConstraintViolationListeners to doAfterCompose instead of constructor.
* Remove redundant call to scheduling algorithm from TaskPropertiesController.
* Some API docs for the entering/reentering part
* Remove uneeded parameters to prevent redundant invocations to scheduling algorithm.
* Use more accurate name
* Bug #1354: Fix bug
* i18n: Update Portuguese translation
* i18n: Update Italian translation
* Update French translation
* Bug #1355: transform AS SOON AS POSSIBLE and AS LATE AS POSSIBLE constraints to the correct constraint based on the scheduling mode.
* Bug #1355: transform NOT EARLIER THAN and NOT LATER THAN constraints to the correct constraint based on the scheduling mode.
* Bug #1380: Don't allow ASAP constraint for tasks in projects where init date is not set.
* i18n: Update Spanish and Galician translations
* i18n: Update keys.pot files
* Bug #1374: Move capacity field to calendar tab in order to be edited together
* i18n: Avoid to translate language names and use the original ones
* Bug #1355: START IN FIXED DATE constraints don't change to NOT EARLIER THAN after drag & drop.
* Bug #1281: Remove unneeded checkVersion when editing progresses
* Bug #1375: Added a constraint for 24 hours and 0 minutes
* Disables the button save in change password window and remove role and profile button on edit user
* Added condition to method isLdapUser to check configuration
* Disallow to change roles and profiles in admin panel for LDAP users when roles are imported from LDAP
* Disallow managing passwords in admin users panel for LDAP users when LDAP is being used for authentication
* Disallow to change passwords to LDAP users
* Mark "Group by weeks" by default in MonteCarlo as it returns better results
* Bug #1379: Disable "Go" button in MonteCarlo if there is no tasks in a project
* Bug #1348: Added effort to predefined calendar exception types
* Bug #1282, #1376: Prevent exception when deleting a milestone inside a container.
* Correct a typo in an English string.
* Bug #1362: Specify type of property in hbm.xml to avoid problems with MySQL
* Bug #1343: Only closed projects in company view perspective are displayed in grey
* Bug #1343: Made graphically less relevant component showing project state
* Bug #1345: Added project state to name string in breadcrumbs line
* Bug #1343: Styled in grey all closed/finished/cancelled projects in company view
* Bug #1370: Fixed progress bars not being properly placed inside containers
* Bug #1373: When creating new projects from template the explicitly filled start date and deadline values are used
* Bug #1369: Moved the template combobox over autogenerated code checkbox in new project popup
* Bug #1369: Improved behaviour of project creation popup when using templates
* Bug #1343: Added method isRoot() to gantt tasks.
* doc: Update translators info in documentation
* Update AUTHORS file info about translators
* i18n: Add French language to enum and modify pom.xml to use English userguide
* i18n: Add French translation
* Bug #1343: Prevent NullPointerException when creating a new milestone.
* Remove redraw listeners for dependencies once these dependencies are removed.
* Prevent unnecessary redraws of dependencies when opening a container.
* Bug #1363: Remove visibility listeners corresponding to deleted tasks.
* Bug #1343: Prevent NullPointerException when showing default filtering dates on company view.
* Bug #1368: Allows to create users with null password when LDAP is used.
* Removed two warnings in the build process, which can cause problems with newer versions of maven.
* Bug #1343: Ensure that getDefaultPredicate is run in a transactional context.
* Bug #1343: Mark closed projects with a special class in the gantt.
* Bug #1343: Use getRawValue instead of getValue in date constraint checkers.
* Bug #1343: Allow showing closed projects in the company view, if they are inside the filter dates.
* Bug #1343: Show default filtering dates on company view.
* Bug #1343: Rewrite part of the code for company view initialization.
* Bug #1353: Create Tabpanels object for load/earned value Tabbox when the screen is loaded, not in the open event.
* Bug #1359: Initialize the resources when the page is loaded, independently from the hidden/shown property of the load chart.
* Bug #1357: Use end date minus one day as default date for earned value chart.
* Bug #1351: Replaced by a label the disabled listbox to display material unit type
* Bug #1351: fixed space for elements
* Bug #1330: Setted focus on new progress measurement after pressing on *Add measure*
* Bug #1357: Check if the date is out of the visualization area, and in that case set a new date before updating the legend.
* Bug #1357: Small code rewrite to simplify the fix of the bug
* Update TODO file with roadmap to LibrePlan 1.3
* Bug #1366: Fix issue subtracting the value when you are removing work report lines
* Bug #1360: Refresh work report line from database before subtracting it from order elements
* Bug #1364: Milestones are filtered now like any other task
* Bug #1362: Fix problem with long descriptions in projects
* Bug #1352: Fix issue not saving tasks without order element
* Bug #1320: When asking a container for start constraints, return the leftmost of children's start-in-fixed-date constraints.
* Revert "[Bug #1273] Reimplement coerceToString as a workaround for the bug in Decimalbox."
* Upgrade ZK version to 5.0.10
* doc: Update information in UPDATE file


Version 1.2.1 (19 Jan 2012)
---------------------------

Summary
~~~~~~~

This is a minor release including all the fixes done since LibrePlan 1.2.0 was
published at the beginning of past December.

Apart from all the bugs fixed, we would like to highlight the following changes:

* LibrePlan is now fully translated into Italian thanks to the work done by
  Giuseppe Zizza.
* When a new release of LibrePlan is published, administrators will be notified
  with a small warning inside LibrePlan.
* Timeplot graph values are displayed when hovering the chart.

Notes
~~~~~

If you are upgrading from any 1.1.x version without using the Debian package,
you will need to manually execute on your database the SQL sentences from files:
``scripts/database/upgrade_1.2.0.sql`` and
``scripts/database/upgrade_1.2.1.sql``

If you are upgrading from 1.2.0 version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.1.sql``

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Jacobo Aragunde Pérez
* Nacho Barrientos
* Ignacio Díaz Teijido
* Lucía García Fernández
* Manuel Rego Casasnovas
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators in this new version:

* [es] Manuel Rego Casasnovas
* [gl] Manuel Rego Casasnovas
* [it] Giuseppe Zizza
* [pt] Joaquim Rocha

Changes
~~~~~~~

* i18n: Update Portuguese translation
* Bug #1335: Don't force the check of the earned value legend date box every time the gantt is reloaded.
* Improve logging of possible exceptions checking version information
* Bug #1342: Move initial order state to PlanningState and use it from Gantt view too
* Bug #1346: Fix the cause of the bug removing TaskElement from parent if it is removed
* Bug #1342: Show save button in Gantt view when it is disabled
* Bug #1342: Now it's possible to mark a project like STORED
* Bug #1335: Update the earned value chart legend every time the chart is reloaded.
* doc: Update LDAP configuration translations
* Remove unused code in LibrePlanReportController
* i18: Remove unneeded code to manage languages in reports
* i18n: Add support to Italian language in reports
* i18n: Update Italian translation and add missing files for reports
* i18n: Update Spanish and Galician translations
* Bug #1340: Add a listener to refresh the earned value chart in AdvanceAssignmentPlanningController.
* Bug #1334: Prevent the accumulation of the values when building the BCWP chart.
* Fix typo precision is just with 1 s
* i18n: Update keys.pot files
* Fix typo in open reports string
* Fix typo in new version string
* Add information about current version in GET requests
* Add VERSION file just with version number in a text file
* Add configuration option to allow LibrePlan developers collect usage stats
* Add configuration option to disable warning about new LibrePlan versions
* Show a warning if there is a new project version published.
* Bug #1336: Checked permissions to enable project creation button
* Fixed chart tooltips scroll problem on resources load window
* Bug #1338: Fix some wrong strings in timeLineRequiredMaterial report
* Bug 1295: Remove TaskElements (except milestones) with TaskSource null when saving
* Fixed wrong criteria string format
* Bug #1337: Removed unnecesary response when redrawing earned value
* Bug #1324: Modified behaviour of west end arrow on violated dependencies
* Display timeplot graph values when pointing over the chart
* doc: Update LDAP configuration doc
* Bug #1333: Allow specify * in role matching
* i18n: Add Italian language language to enum and modify pom.xml to use English userguide
* i18n: Add Giuseppe Zizza as Italian translator in AUTHORS file
* i18n: Add Italian translation
* Bug #1333: Fix issue as property and search query are needed for group strategy too
* Bug #1332: Fix problem allowing to set empty values for userDn and password
* Bug #1329: Fix problem in StretchesFunctionTest due to changes in previous test
* Bug #1329: Fix issue calculating properly end date of stretches
* Bug #1329: Now tasks are not enlarged and nothing breaks just after selecting a stretches function
* Bug #1329: Update advanced allocation row after applying default stretches function
* Bug #1328: Fix issue calling onClose method on controller
* Bug #1261: Extract method createTab with common functionality for each tab
* Bug #1261: Allows tabs to be memorized when changing perspective
* Bug #1327: Fix issue changing method to do reassignments in consolidation process
* www: Folder and script for libreplan.org documentation
* [Bug #1326] Fix issue calculating properly hours to allocate
* [Bug #1325] Fix issue remove criteria from configuration unit
* Allow reported hours bar in gantt diagram to be wider than the task.
* [Bug #1242] Don't allow progress end dates superior to the corresponding task end date.
* [Bug #1311] Replace Date objects with IntraDayDate objects in SaveCommandBuilder.
* [Bug #1321] Reseting highlighted days in calendar to prevent issue
* [Bug #1323] Fix lazy loading properly derived allocations
* [Bug #1242] Return end date directly when calculating advance and percentage is 100%.
* [Bug #1242] Use IntraDayDate to draw the progress bar in leaf Tasks.
* [Bug #1319] Change division method of EffortDuration
* [Bug #1304] Make sumOfAssignedEffort return the cached value only for orders.
* [Bug #1304] Reorder the code to improve the performance with TaskGroups.
* Replace attribute TaskElement.sumOfHoursAllocated with an equivalent attribute measured in EffortDuration.
* [Bug #1304] Don't use the cached value sumOfHoursAllocated when drawing the progress bars of tasks.
* Revert "[Bug #1319] Change division method of EffortDuration"
* [Bug #1309] Fix problem with reported hours bar when progress type is changed
* [Bug #1319] Change division method of EffortDuration
* [Bug #1309] Invalidate each TaskComponent instead of the whole TaskList when progress type is changed
* Hide warning messages on bottom when user is not admin
* [Bug #1288] Return null instead of zero in a just created AdvanceMeasurement
* [Bug #1288] Fix message regarding progress type precision
* [Bug #1288] Set Decimalbox scale according to progress type precision
* [Bug #1307] Prevent changing spread progress in children when parents are consolidated
* [Bug #1307] Prevent add progress measurement before consolidated day in any parent
* Refactor method getSpreadIndirectAdvanceAssignmentWithSameType to be used in more places
* Prevent NPE editing progress measurements
* [doc] Add warning on INSTALL file about how to create database structure
* [Bug #1308] Remove unneeded preventing create default progress in containers
* [Bug #1305] Prevent adding progress measurement before last consolidated date
* Fix messages related with progress management and consolidations
* Disable some options in progress management if there is a consolidated progress
* [doc] Update SourceForge.net URLs due to rename to LibrePlan
* [Bug #1316] Use correct JS selectors to work both with leaf tasks and task groups.
* Fix NPE removed advance measurement
* Remove unused method
* [Bug #1301] Fix issue creating a container in a task with dependencies
* Remove duplicated method in QualityForm
* [Bug #1314] Fix problem in materials report
* Skipping some tests in ScenarioModelTest because they were causing problems in Debian Wheezy
* Remove uneeded test as it was causing problems in Debian Wheezy
* Fix problems in MaterialDAOTest and MaterialServiceTest
* Fix problem with consolidation and specific assignments
* [Bug #1300] Updated intended resources per day after consolidation is added or removed
* [Bug #1312] Fix issue with printing in HTTPs is enabled
* [Bug #1303] Fix issue removing consolidation using IntraDayDate for task end
* [Bug #1303] Fix issue adding consolidation using IntraDayDate for task end
* [Bug #1310] Reorder the code to fix the two exceptions happening here.
* [Bug #1299] Enable scrollbars in the tasks input buffer grid.
* [Bug #1297] Add listeners to 'See schedule allocation' buttons every time the panel is redrawn.
* [Bug #1297] Replaced the misleading word "Filter" with "Show".
* [Bug #1297] Reset the list listenersToAdd when changing perspectives.
* [Bug #1073] Fix issue introduced by me while reviewing the previous patch
* [Bug #1294] Revert "Update i18n files with the new translations for the replaced string."
* [Bug #1073] Add dependencies with its parent when a milestone is added.
* [Bug #1294] Update i18n files with the new translations for the replaced string.
* [Bug #1294] Replace the sentence 'Click on direct link to go to output directly' with a better alternative.
* [doc] Add warning about printing issues in Debian Squeeze
* [doc] Add instructions to install Cutycapt from testing
* [Bug #1296] Rename Spanish and Galician .properties files as country was not needed
* [Bug #1296] Remove duplicated \*_en_US.properties files after updating \*.properties when required
* [Bug #1292] Replace Toolbarbutton object for the link to the PDF with an A object.
* Remove an unnecessary check.
* [Bug #1287] Fix NPE opening a project from Gantt view
* [Bug #1290] Fix NPE issue launching LibrePlan
* [Bug #1291] Fix NPE when a task with a dependency is removed
* [Bug #1285] Avoided NPE when dropping a treerow in the same component
* Add vertical borders in watermarks to improve differentiation between days
* Added transparency effect to hover treerow
* [Bug #1256] Removed inner padding on help tooltip
* Made more explicit links styles in headers
* Changed treerow input style for over elements
* [i18n] Update Portuguese translation


Version 1.2.0 (01 Dec 2011)
---------------------------

Summary
~~~~~~~

In the current 1.2.0 version the project has been renamed to LibrePlan and this
will be the official name for the project from now on. With the new name,
decided by the community, we are remarking that LibrePlan is a generic purpose
planning tool, suitable to many sectors.

Besides, with this new version, the project has a new image and a revamped
website that you can visit at http://www.libreplan.com/

This new major version comes with important new features apart from all the
bugfixing done since the 1.1 major release. Among the new features, these are
the most relevant ones:

* Migration to ZK 5 Community Edition.

  LibrePlan uses ZK framework as UI toolkit and in this release has been
  migrated the interface from ZK 3.6 to ZK 5.0.

  ZK 5 was incompatible with version version 3.6 and this movement have implied:

  * Reimplementing LibrePlan custom UI widgets in ZK 5.0 architecture

  * Providing an implementation in ZK 5.0 for widgets in 3.6 version which
    changed their license and which are not open source anymore.

  * Reviewing all the application windows and to do small configuration and
    formatting changes.

  This allows us to get advantage of the latest innovations in rich web
  interfaces and enjoy both the performance and architectural enhancements of
  this new ZK version.

* Resource allocation engine enhancements.

  The planner engine has been improved with three new functionalities regarding
  to allocation engine:

  * Task movements in Gantt view keeps the configured allocation function.
    Before this, if you configured a stretches function or sigmoid function in
    a resource allocation and the task was moved because a dependency or
    constraint set by the user, the allocation function applied in the new
    point was the flat function. Now, the configured stretches or sigmoid
    function is used.

  * Manual allocation automatic detection in advanced allocation. Now if the
    user changes the allocation of a task in advanced allocation window, the
    type of allocation is automatically tracked as manual.

  * Manual allocation locking support in Gantt view. So far if a task had a
    manual allocation and was moved in the Gantt, the custom allocation was
    lost and a flat function was applied. In this version, a keep manual
    allocation strategy has been implemented. It consists of disabling the
    automatic movement of those allocated tasks in the Gantt by being
    configured with a START IN FIXED DATE constraint after the manual
    allocation. If the user wants to move these tasks, he has to change in
    first place the allocation function used and, in second place, has to
    remove the constraint. In this way, the manual allocation configured is not
    lost by accident.

* Sharing state between perspectives.

  The project planning in LibrePlan is achieved by using several perspectives:

  * Project scheduling (Gantt).
  * Project details (WBS).
  * Resource load
  * Advanced allocation.

  Until this version, the user had to save before abandoning a perspective to
  avoid losing the changes on going to another. To improve this, in this
  release, the planning data has been shared among all the project perspectives
  and on saving in one of them, all the planning data is stored.

  This a more natural way of creating the project plan. Now the user can move
  freely among the project views configuring different things and, on reaching
  a desired state, he can ask to persist changes.

  Besides, with this new feature, the loss of changes by accident is prevented
  on a perspective change without saving first.

* LDAP authentication and authorization module.

  In this version the possibility of using LDAP as backend for users has been
  implemented.

  An LDAP authentication and authorization module has been developed. With
  this, it is possible to use the users configured in the company directory
  without the need to create them in LibrePlan manually.

  The main features of this module are:

  * User passwords saving in LibrePlan database after right authentication
    option. This allows the LDAP users enter the application if the LDAP is
    off-line.

  * Role matching support. It is possible to map LDAP roles to LibrePlan
    permissions. Two strategies are provided: Group strategy (all the users
    under a node) and property strategy (property of the node user with all the
    role values).

* My account area.

  It has been developed a new top level menu entry called *My account* with
  several options with a per user scope. This means that they are operations
  that concern only to the connected user which is configuring them.

  The operations included in this zone are:

  * Personal data. User can manage his contact information

  * Application preferences. User can select the language of the application
    and if if the load charts of the planning windows appear folded or
    unfolded  by default.

  * Password administration. User can change his own password.

* Functional tests.

  One of the maxims of LibrePlan is to create a project with good quality. For
  this reason in this version functional tests to the project have been
  incorporated into the project.

  Functional tests are black-box tests in which the interface of the program is
  tested. The tool which has been chosen is Sahi and it allows verifying that
  all functionalities keep working successfully.

  An initial set of Sahi tests have been developed and they will be increased
  as time passes.

* Other minor tasks implemented:

  * Internationalization. Some strings were not being translated accordingly to
    the language used under certain conditions.

  * Default login auto-completion is now configurable, you can disable it from
    the configuration window.

  * Now when editing contents, information identifiying the element being
    edited is always displayed on the page heading.

  * Work reports minute detail level support has been added. This allows users
    to specify how many hours and minutes they have devoted to each task.
    Reports have been updated accordingly to include the work time in minutes
    too.

  * LibrePlan web services have now a new method to export only one entity by
    specifying its code.

  * Russian and Portuguese translations. Apart from English, Spanish and
    Galician now LibrePlan is also available in Russian and Portuguese.

Notes
~~~~~

If you are upgrading from any 1.1.x version without using the Debian package,
you will need to manually execute on your database the SQL sentences from file:
``scripts/database/upgrade_1.2.0.sql``

Contributors
~~~~~~~~~~~~

Thanks to all the contributors to this new version:

* Cristina Alvariño
* Jacobo Aragunde Pérez
* Nacho Barrientos
* Ignacio Díaz Teijido
* Pablo Fernández de la Cigoña Nóvoa
* Óscar González Fernández
* Dmytro Melanchenko
* Susana Montes Pedreira
* Javier Morán Rúa
* Diego Pino García
* Manuel Rego Casasnovas
* Farruco Sanjurjo
* Lorenzo Tilve Álvaro

Translators
~~~~~~~~~~~

Thanks to all the translators for their hard work:

* [es] Manuel Rego Casasnovas
* [gl] Manuel Rego Casasnovas
* [pt] Helena Grosso
* [ru] Pavel Rudensky

Changes
~~~~~~~

* Release new version LibrePlan 1.2.0
* [doc] Update some links in INSTALL file
* Fixed width issue in Workreport models edition
* Fixed width issue in ffox with materials category selection
* [Bug #1279] Fix problem with Decimalbox in Spanish
* [Bug #1278] Fix NPE going to project details after Gantt without read permissions
* [Bug #1277] Generating exception days code before checking if they are null
* Add postinst script
* [doc] Fix wrong styles in LDAP documentation
* doc/en: Make direct links to reports only shown in HTML
* doc/en: Re-flow text to avoid excessive nesting in TeX output
* doc/en: Convert Avanza logo to PNG
* doctool: Support translation of underlines for sub-pages
* doctool: Sort input filenames when generating indexes
* doctool: Use spaces only for indentation
* [doc] Added LDAP configuration help in Galician.
* [doc] Added LDAP configuration help in Spanish
* [doc] Links the help of 'LibrePlan Configuration' to the help icon.
* [doc] Added LDAP configuration help in English
* Mark libreplan as Pre-Depends
* Updated Debian control files for different distributions
* Dump database
* Add a transitional package
* Remove unneeded dependencies in Debian control files
* [doc] Fix several wrong URLs in documents
* [doc] Fix several wrong URLs in documents
* Update README and Debian package info
* [Bug #1249] Calendar names sorted in several screens
* [Bug #1246] Added sorting for Customer field
* Corrected typo in authors list.
* [Bug #1276] Fix problem with START_START and END_END dependencies in critical path
* [Bug #1273] Reimplement coerceToString as a workaround for the bug in Decimalbox.
* [Bug #1274] Do decimal separator replacement in the correct way in ZK5.
* Revert "[Bug #1274] Remove LenientDecimalBox."
* [Bug #1274] Remove LenientDecimalBox.
* Update Debian package configuration files
* Generate SQL script to upgrade from version 1.1.0 to 1.2.0
* Revert "Revert "[Bug #1253] Constraint for EffortDuration in advanced allocation""
* Revert "[Bug #1253] Constraint for EffortDuration in advanced allocation"
* [i18n] Update keys.pot and Spanish and Galician translations
* Rename database to use LibrePlan in its name
* Fix typo in comments in Sahi tests
* Added vertical alignment in WBS tree buttons
* Renamed operations column in external companies grid
* [Bug #1247] Fixed issue with checkbox validation in Quality Form edition
* Added not empty constraints for WBS task names
* Added hflex property to WBS tree
* Fix possible issue if configured progress type is null
* Prevent NullPointerException filling progress type combo
* Fix OrderElementServiceTest to pass tests in MySQL
* Removed fixed height in WBS task edition popup
* Removed extra border in resources load
* [Bug #1235] Added scrollbar to perspective icons area when needed
* [Bug #1235] Changed height calculations affecting project edition scollbar
* Mark first menu entry as active when user goes to /
* Add link to first subpage in "My account" menu entry
* [Bug #1253] Constraint for EffortDuration in advanced allocation
* Fixed Sahi test data-types/exception_days_test.sah in chromium
* Removed jsession parameter in EntryPoints matching conditions
* Fix pending issues in previous commit
* Inserted empty conditions for default data
* Fix several functional tests due to problems with accept button in resource allocation pop-up
* Fix materials functional test
* Fix work reports functional test
* Fix work hours types functional test
* [Bug #1267] Add reloadBindings of material tree after saving
* [Bug #1270] Fix issue invalidating TaskComponent
* [Bug #1270] Fix problem the first time you choose a progress type
* [Bug #1269] Mark as selected the configured progress
* Added a message in print modal window to remind saving
* [Bug #1260] Fixed resizing of task information table in allocation popup
* [Bug #1268] Removed display property causing progress-types combobox issues
* Fixed Sahi test : data-types/unit_measures_test.sah
* Fixed Sahi test : scheduling/templates_test.sah
* [Bug #1272] Set progress in TaskElement when its created
* [Bug #1269] Fix tooltip text using now configured progress type
* [Bug #1269] Fix previous commit as it was not working properly
* [Bug #1269] Now configured progress is used by default in company view
* [Bug #1266] Fix issue reloading bindings of allocation grid
* Mark some strings to translate in configuration.zul
* Fix some issues pending in previous commit
* Changes on LDAP configuration screen
* [Bug #1260] Improved horizontal scaling of assigned materials
* Replaced legacy NavalPlan logo versions
* Fixed styles for selected listitem and comboitem elements in ZK5
* [Bug #1264] Checked that we are accessing a planning tab with creation button enabled
* Fixed textbox and datebox style issues in leftTasksTreeRow component
* Changed Textbox automatic width calculation in LeftTasksTree
* Removed problematic left and right listeners on TaskDetails treeRows
* [Bug #1260] Fixed width issues in Task hours group
* [Bug #1244] Only create dateboxes for treerow textboxes when needed
* Removed unnnecesary elements from DynamicDatebox component
* [Bug #1248] Added extra validators when saving order
* [Bug #1248] Project general data tab now validates if project or code exists
* Removed ambiguous arrow in mouse-selected tree row
* Fixed horizontal scaling issues in criterion requirements
* [Bug #1244] Fixed DynamicDatebox component behaviour in zk5
* [Bug #1262] Remove option to cancel progress assignments in Gantt view
* [Bug #1262] Fix wrong method names in ManageOrderElementAdvancesModel
* Disable login name and disabled checkbox in user edition for LDAP users
* Rename User::getAuthenticationMethod() to User::getAuthenticationType
* Authentication type not editable
* Changed the way in which authentication type is shown
* [Bug #719] Fix problem in WBS when you modify tasks hours with parents in previous pages
* Fix warning in the JavaScript code when trying to access a method of a null object.
* [Bug #1257] Prevent saving null TaskSource
* [Bug #1239] Updates text of tooltips when changing name or progress
* [Bug #1254] Keep zoom level in advanced allocatin perspective
* [doc] Update authors info in user guide
* Updated AUTHORS file adding Helena Grosso as Portuguese translator
* [Bug #1252] Fix issue avoiding reset TaskElement endDate
* [i18n] Added Portuguese language to enum and modified pom.xml to use English userguide
* [i18n] Add Portuguese translation
* Fix some extra ":" in Sahi tests
* Update AUTHORS file
* [i18n] Fix small typo in Spanish translation
* [i18n] Mark string to be translated
* [i18n] Mark string to be translated
* [i18n] Update Spanish and Galician translations due to last changes
* [i18] Update keys.pot due to last issues fixed in past commit
* [i18n] Fixing several issues in project strings
* [i18n] Updated Spanish and Galician translations
* [i18n] Update keys.pot files
* [Bug #1141] Remove Task.reloadResourcesTextIfChange().
* [Bug #1141] Added setter for left attribute of TaskComponent which updates the dependencies.
* [Bug #1250] Fix issue removing TaskSource of unscheduled element
* Add toString to OrderElement to make easier debug process
* [Bug #1245] Fix issue when moving task to an un-direct descendant
* [Bug #1238] Fix concurrent modification exception
* [Bug #1243] Labels of root task were not properly loaded
* Fix tests due to changes in 3bd402d5baaaee3f90c6885be1d8b041d3ae989c
* Reduced unnecesary vertical minimum height in orderElement advances
* Fixed horizontal scaling and other layout issues in WBS
* Moved draggable property from WBS rows to inner schedulling cell
* Removed unused attribute
* Applying days from start to deadline when selecting template
* Added validation when creating new project that the same name is not being used
* Increased minimum number of columns to display in timetracker
* Replaced obsolete separator between project name and project id
* Removed specific button for creating projects from templates
* Moving code that enables global buttons out from the perspectives
* Merged creation of new projects from templates into main creation window
* Fixed hibernate issues when creating new projects from templates
* [Bug #1224] Added constraints in effort textbox
* [Bug #1237] Fix problem with progress bar size
* Default work report model has all attributes at line level
* Fix wrong string in templates functional test
* Fix planning criteria functional test
* Fix functional tests related with my account menu
* Fix subcontract functional test
* Fix functional tests related to work reports
* Fix functional tests related to workers, virtual workers and machines
* Remove links to non-existent resource.css file
* Fix configuration functional test when running all tests together
* Added default data for WorkReportType
* Added default data for TypeOfWorkHours
* Added default data for Labels
* Added predefined calendar exceptions
* Configured default data for Criteria
* Change wrong string "Extra Effort" for "Overtime Effort"
* [Bug #1236] Sorting orders in some combos of reports
* Fix configuration functional test
* Fix users and profiles functional tests
* Fix work hours functional test
* Fix work report model functional test
* [Bug #1215] Store the resources tooltip hidden/shown state in the TaskRow widget and use that state when redrawing the TaskComponents.
* [Bug #1221] Shows orders sorted in project finder
* [Bug #1215] Store the labels hidden/shown state in the TaskRow widget and use that state when redrawing the TaskComponents.
* [Bug #1162] Fix issues in stretches function dedication chart
* Make simple code to remove perspective buttons
* [Bug #1228] Fix issue creating default write authorization
* Set mold paging in criteria tree to show the pager
* [Bug #1231] Fix issue avoiding call EffortDuration.sum with null values
* Skipping some tests in ScenariosBootstrapTest failing in MySQL
* Fix name of OnDay class inside ContiguousDaysLine
* [Bug #1232] Fix problem in load chart filtering all assignments taking into account order resources
* Corrected .gitignore to match the new directory names.
* Unskip tests in ScenariosBootstrapTest
* Fix BaseCalendarModelTest needed to load configuration
* Rename pending file to libreplan
* Rename column navalplan_user to libreplan_user in user_table
* Skipping some test due to rename to libreplan
* Rename NavalPlan to LibrePlan
* Removed unnecesary code for showing/hiding labels
* [Bug #1097] Fixed issue with container corners when showing labels
* [Bug #1120] Fixed appearance of labels on tasks when its parent is expanded
* Fixed issue when the splitter is resized the legend area was not aligned
* Removed access to ResourcesLoad and AdvancedAllocation from company view context menu
* [Bug #1207] Fix templates historical statistics messages
* Removed unused lines in ConfigurationController
* [Bug #1199] Fix issue setting Listitem value in renderer
* Fix small issue in INSTALL file
* [Bug #1230] Fix issue updating OrderElement name when it's changed in TaskElement
* Remove ZK repository as it's not needed anymore
* Change dependency to ZK CE
* [Bug #906] Remove some pending comments
* Upgrade ZK version to 5.0.9
* Revert "Look for new resources when moving a task"
* [Bug #1225] Fix issue in OrderDAO.loadOrdersAvoidingProxyFor with new OrderElements
* [Bug #1227] Order authorizations are saved now in SaveCommand
* Replace the component Detail from ZK EE with our free replacement in the Java code too.
* [Bug #1227] Fix problem with percentage advances not being created
* [Bug #1223] Catch ValidationException in SaveCommandBuilder
* Renamed 'Accept' for 'Apply' commands in advanced allocation
* Removed 'Up' command in resources load view of a project
* Added a 'Cancel' button to common area with modal window to ask for confirmation
* Moved 'Reassing' icon out of common area over perspectives
* Fix lazy mapping to be lazy="false".
* Fix lazy exception chaging to resources load view after moving a task in Gantt
* Remove some unneeded reattachments in ResourceAllocationModel
* [Bug #1214] Fix problem reattaching HoursGroup in allocation pop-up
* [Bug #1214] Fix issue changing Hibernate mapping for map inside CalendarData
* Replaced component master-detail with a GPL alternative based on the code of the ZK3 version.
* Use LocalDate instead of Date in LoadTimeLine.getIntervalFrom.
* Changed planner save command message
* Injected planner global commands in common area over perspectives
* Created an empty area for positioning all planner global commands
* Merge branch 'bugs'
* [Bug #1217] Fix issue avoiding reattach of criteria
* [Bug #1219] Shows labels and/or resources when the buttons are pressed after changing perspective
* Added a fixed height for resourcesload watermark
* Added listener to recalculate height on window resizing
* [Bug #820] Added minimum vertical heigth for timetracker watermark
* Avoided dual scrollbar inside resourcesload worker details table
* [Bug #820] Several changes in watermark height calculations
* Removed unnecesary scroll in login window
* Fixed issue in resourceload_row width calculations in Resources Load view
* Remove unused param in LimitingResourcesTabCreator::create
* [Bug #1216] Fix issue chaning code to check allocation resource radio
* [Bug #1216] Removed unused code as Radiogroup at that point never has elements
* [Bug #1216] Remove unused argument in method related with radio buttons in resource allocation
* [Bug #1218] Fixing issue avoiding load order from database
* [Bug #1183] Implement the replacement of decimal comma with dot in the client side.
* [Bug #1213] Use String.CASE_INSENSITIVE_ORDER comparator instead of String.compareTo() function to compare resource names.
* [Bug #1186] Show all the resources in the list on resources usage page, even the empty ones.
* [Bug #1205] Fix issue going to edition of a task in WBS directly
* [Bug #1196] Assign task to the first valid queue in case that all of them are empty.
* Fix wrong string, now using project instead of order
* Applied interface conventions in settings UI
* [Bug #1173] Application language must be user language
* [Bug #1187] Add filter by resources in TaskGroup filter in company Gantt view
* [Bug #1187] Fix issue filtering TaskGroup by criteria in company Gantt view
* [Bug #1187] Move TaskElementPredicate to a proper package
* [Bug #1187] Add filter by resources in TaskElement filter in project Gantt view
* [Bug #1187] Fix issue filtering TaskElements by criteria in project Gantt view
* Fix 2 wrong comments in SigmoidFunction
* Fixed zk5 migration issue in advanced assignment search
* Fixed regression introduced with icons positioning in WBS
* Applied interface conventions in user edition and advance consolidation
* [Bug #1200] Add dependency from ganttz in the definition of limitingresources package.
* [Bug #1206] Add event listeners for checkboxes in the legend of the earned value chart.
* Remove unused method in SigmoidFunction
* [Bug #1204] Do not round hours in Sigmoide allocation
* Fixed issue with positioning of buttons and icons to manipulate WBS
* Removed logos from application footer
* Applied interface conventions to workreport window
* Increased standard height for listbox in bandbox search component
* Applied interface conventions to WorkReport Type edition
* Improved comboboxes spacing on workreports edition
* Labels are displayed ordered in label type edition
* Applied interface conventions to workreport query
* Added textbox to set description value in template edition
* Fixed project name width issue in projects list using reduced resolutions
* Grouped visually the same entity sequences in cofiguration window
* Removed Group component dependency
* Revert "[Bug #1173] Application language must be user language"
* [doc] Add note in web services README about HTTP Basic Authentication
* Prevent moving task with manual allocation
* Disable start constraint combo and date in task properties if it has manual allocations
* Set tasks as START_IN_FIXED_DATE when manual allocation is applied
* Remove unneeded cast and rename variable
* [Bug #1209] Fix NullPointerException in WorkReportCRUDController::updateEffort
* [Bug #1208] Remove some generic catchs that was preventing to detect previous issue
* [Bug #1208] Fix issue with imputed hours view in company view
* [Bug #1208] Rename calculateLimitDate functions to prevent future confusions
* [Bug #1208] Fix problem with imputed hours in project view
* [Bug #1173] Application language must be user language
* [Bug #1202] Managed division by zero calculating template averages
* Revert "[Bug #1202] Managed division by zero calculating template averages"
* [Bug #1203] Added event on client side to send the data of the zoom change and listener on the server to store them.
* [Bug #1198] Implemented missing JS operation QueueListComponent.adjustScrollHorizontalPosition.
* [Bug #1202] Managed division by zero calculating template averages
* Look for new resources when moving a task
* Internationalize UI string.
* [Bug #1190] Limiting resources not translated properly
* [Bug #1160] Show footer in "Work and progress per project" report
* Fix problem in interpolation function it was doubling the assignments
* Fix issue in interpolation function because of new default stretches 0 and 100
* Fix size problem with new Listbox of assignment function in resource allocation pop-up
* Add ON_SELECT event to Listbox, reseting assignment function to flat
* Added flat function always as first option in resource allocation pop-up
* Change assignment function information from Label for Listbox in resource allocation pop-up
* Disable inputs in AllocationRow if there is any manual allocation
* [Bug #953] Simulated click on progress button when changing type
* [Bug #1185] Fixed combobox problems with selector for progress types
* Added more space for perspective button labels
* [Bug #1127] Fixed dependencies in limiting resources
* [Bug #1188] Fixed graphical issues in Montecarlo modal window
* Remove last deprecated methods related to EffortDuration
* Fix issues in Ubuntu with REST scripts
* Move advanced allocation command controller out of EditTaskController
* [Bug #1193] Fix bug
* [Bug #1191] Avoid NullPointerException in "Hours Worked Per Resource" report
* Revert "[Bug #1191] avoid null pointer exception"
* [Bug #1161] Fix comparison in BigDecimal in previous patch
* [Bug #1161] The value of progress is at maximum 1
* [Bug #1197] Fix bug
* Ensure retrieved OrderVersions are not proxies
* [Bug #1195] Fix bug
* Fix another functional test due to removal of "Go to advanced allocation" button
* [Bug #1194] Fix bug
* [Bug #1191] avoid null pointer exception
* Include search_resources_test.sah in all_test.suite and in all_resources_test.suite
* Fix another functional test due to removal of "Go to advanced allocation" button
* Make new dependencies to be rendered in the correct position of the DOM.
* Added synchronization for property dependencyType in LimitingDependencyComponent widget.
* Reimplemented the onclick event in QueueTask elements that existed in the ZK 3.x version.
* Reimplemented the onmouseover and onmouseout events in QueueTask elements that existed in the ZK 3.x version.
* [Bug #1193] Fix bug
* [Bug #1192] Fix bug
* [Bug #1157] Sorted input fields
* Fix functional test due to removal of "Go to advanced allocation" button
* Add "Advanced allocation" option in secondary menu of a task
* Remove "Go to advanced allocation" button in resource allocation pop-up
* Disabling fields in ResourceAllocation pop-up if any allocation is manual
* Now Monte Carlo simulation tab shares the state with the rest of the tabs
* [Bug #1178] Fixed calculation of sum hours with EffortDuration in report HoursWorkedPerResource
* [Bug #1178] Fixed calculation of sum hours on a report
* [Bug #1178] Showing EffortDuration in reports instead of BigDecimal
* [Bug #1159] Fixed end date position
* Now project's details view shares the state with gantt and resource load
* Improve toString method
* Add verstion to PlanningData
* The cause must be thrown
* ConcurrenetModificationException shouldn't happen
* Now the save command should be able to save orders
* Do the don't pose as transients outside of the transaction
* Synchronize in memory on retrieval of the PlanningState
* Allow to provide several strategies for saving TaskSources
* The tasks to save and so on are recreated each time a screen is entered.
* PlanningState is now responsible of creating the SaveCommand and the PlannerConfiguration
* Use a singleton for creating a SaveCommand
* Make TaskElementAdapter a singleton
* Written the client-side code to draw dependencies in limiting resources screen.
* Added properties idTaskOrig and idTaskEnd to LimitingDependencyComponent.
* Create widget QueueTask with an empty structure, removing wrong event listeners from it.
* [Bug #1172] Fixed search
* [Bug #1151] Fixed layout problems in report "Hours worked by resource in a month"
* Include some changes to solve a error on criteria_test.sah and include scheduling tests on all_test.suite
* Fix problems in the tests, created for some changes in work report form
* Use some functions from common_functions.sah, in planning_labels_test.sah
* [Bug #1149] Fixed ClassCastException on ResourceDAO
* Added options on label filtering
* Changed query to filter by labels
* [Bug #1177] Allows to insert EffortDuration instead of hours in webservices
* Fix issue in work reports services
* Upgrade ZK version to 5.0.8
* [Bug #1179 & #1182] Fixes UI for WorkReport edition
* Remove unused method in ResourceAllocationController
* Added new file which includes tests for all the search fields of resources
* Include some functions from virtual_worker_test.sah in common_functions.sah
* Improve commonCriteriaCreate to allow create a criterion type with different criteria names
* [Bug #1189] Added auto horizontal scroll in templates WBS to fix bug
* Fix issue with size of font in combos in advanced allocation window
* [Bug #1184] Fix issue avoiding reset verticalPage to zero
* Load required data in MaterialAssignmentDAOTest
* [Bug #1180] Change method names in order to make explicit that work with root templates
* [Bug #1180] Fix bug. checkConstraintUniqueTemplateName is only checked in root elements
* Include new test in planning_criteria_test.sah to check the filter of project planning and move some elements to common_functions.sah
* Added new test file with include some tests for project planning and assign a labels in a task
* Added new test file with include some tests for project planning and assign a criterion in a task
* [Bug #1148] Intializes pulldown for year and month properly
* [Bug #1155] Changed selected items to items for showing name of projects in report
* Change dialect to MySQL5InnoDBDialect in order to fix issues with MySQL 5.5
* Drop and restore foreign key in database due to change in to sum_charged_effort_id
* [Bug #1154] Sorts the report by order name.
* [Bug #1156] Fix bug force loading of HoursGroup entities
* [Bug #1178] Showing EffortDuration in reports instead of BigDecimal
* Include some functions of workers in common_functions.sah and use them
* Include some functions about labels in common_functions.sah and use them
* Include new test for templates in a new folder scheduling
* [Bug #1147] Fix issues in Hours Worked Per Resource report
* Small fix in HoursWorkedPerWorkerController to avoid NullPointerException
* [Bug #1146] Fixed model setting hasChangeLabels to true after a label remove
* [Bug #1175] Fix bug
* [Bug #1175] Fix bug
* Avoid unnecessary calculations
* Add method to extract the interval of a ContiguousDaysLine
* Fixed problems width problems in chrome
* Fixed issue with hidden elements in print view
* Fix intermittent test failures
* [Bug #1176] changed controller to allow a search in only one day (from 00:00:00 to 23:59:59)
* Use some commonProgress functions included in common_functions.sah in progress_test.sah
* Include a new test in calendar_test.sah and modify a function in configuration_test.sah
* Include some new tests in machines_test.sah
* Added new file all_account_test.suite which allow run all the account tests and include account tests in all_test.suite
* Added new file password_test.sah
* Added new test file settings_test.sah in a new folder account
* Fix issues in with StretchesFunction after removing date field
* Remove date attribute from Stretch
* Fix Liquibase changes in MySQL
* Merge branch 'work-reports-effort-duration'
* Include some new tests in virtual_worker_test.sah
* Include some new tests in worker_test.sah
* Include a new function on common_functions.sah
* Fix some mistakes in the resource descriptions
* Merge branch 'master' into work-reports-effort-duration
* Fix issue with logo in reports
* Fixed Reports to work with sumEffort instead of sumHours
* Merge branch 'master' into work-reports-effort-duration
* Change EffortDuration toString and toFormattedString to show always 2 digits in minutes and seconds
* Change repository URI to make it point to the new Nexus location
* Removed method getTotalChargedHours in SumChargedEffort class
* Avoid change login from settings screen
* Include some new tests in material_test.sah
* Change test to remove more old hours methods in SumChargedEffort
* Remove unused methods in SumChargedEffort
* Saves work reports with efforts instead of hours
* [Bug #1171] Prevent delete companies already in use
* Do some changes in files which include materials tests because they didn't work correctly after a bug fixing
* Add a suite for all the resources and include resources in all_test.suite
* Added tests for subcontracting
* Added tests for companies
* Added tests for work report
* Added tests for virtual workers groups
* Add tests for machines
* Add tests for workers
* Include some functions in common_functions.sah
* Updated 'Projects List' entry in functional tests
* Renamed 'Projects' with 'Projects List' in breadcrumbs menu
* [Bug #1169] Fixed regression in z-indexing which made impossible to expand containers
* [Bug #1153] Set empty code if code is not autogenerated for new calendars
* [Bug #1152] Use ConstraintChecker to show error messages before saving
* [Bug #1152] Fix error message in wrong language in calendars
* [Bug #1144] Fix now Material needs a description
* Added support for custom logos in reports
* Avoided hidden horizontal scroll effect in taskdetails
* Renamed entry 'Projects' for 'Projects List' in main menu
* [Bug #1099] Fixed several issues in print styles
* Replacing application logos with new LibrePlan image
* Fixed styles problem in apply and cancel buttons in advanced allocation
* Merged redundant logo images and removed its i18n file paths
* Removing toolbar button styles in advanced allocation
* Changed paginator size in projects and workers lists
* Change custom logo position
* [Bug #1150] Material categories tree is reloaded after adding new elements
* Fixed chromium width problem in materials window
* [Bug #1142] Fix returning false when needed in beforeDeleting method
* [Bug #1170] Fix bug
* [Bug #1137] Fix bug. Project name is now unique
* [Bug #1131] Fix adding message for user when work report is removed
* Add some functions to common_functions and improve work_hours_test.sah using common functions
* Allow to look for the set of OrderElement associated with a WorkReportElement
* [Bug #1125] Disable remove button if unit type is assigned to any material
* Fix imputed hours tab UI now using EffortDuration
* Rename attribute sumChargedHours to sumChargedEffort in OrderElement
* Change to effort SumChargedHours now called SumChargedEffort
* Remove FlatFunction class as it is not used anymore
* Uncomment configuration_test.sah in suite files
* Adds navalplan configuration tests and include some new functions in common_functions.sah
* Fix issue when changing to Flat assignment function
* Enable again resource allocation inputs for any assignment function
* Added a new suite to run all the administration tests and include the new tests on all_test.suite
* Added calendar test file
* Add material tests file
* Added Quality forms tests
* Add new folder for administration-management elements and include a new test file cost_categories.sah
* Change some functions from work_hour_test.sah for functions included in common_function.sah
* Include some functions from criteria, worker, calendar and cost category in common_functions.sah
* Using EffortDurationPicker for work reports interface
* Remove unused methods in WorkReportLine
* Change numHours to effort in WorkReportLine
* [Bug #1166] Check if advanceType is null
* Show languages sorted in settings page
* [i18n] Add Russian value to Language enum
* Increased width of selected day details grid
* Changed styles for selected day and out of current month
* Hide seconds granularity in calendar exception types
* [Bug #1132] Adding more selectable colours for calendar exception types
* Displaying color samples in Calendar Exception Type edition
* Solved z-index problems in gantt listdetails
* [i18n] Fix some pending strings in Russian translation
* [i18n] Add command in pom.xml to copy English help to Russian folder too
* [i18n] Add images folder needed for Russian translation
* [i18n] Add Russian translation
* [Bug #1123] Added condition to avoid max value equals zero
* [Bug #1123] Add test to check that maxValue is greater than zero
* Update information about assignment function in allocation row
* Rename setWithoutApply to setAssignmentFunctionWithoutApply in ResourceAllocation
* Apply assignment function if any from resource allocation pop-up
* Use already existent method with more descriptive name
* Avoid FormBinder.getCurrentRows
* Disable resource allocation inputs when assignment function is not flat
* Remove unneeded URL in data types tests suite
* Added two new suites
* [Bug #1143] A quality form cannot be deleted if it is associated to any task
* Only show assignment function if any is not flat
* Show information about assignment function in resource allocation popup
* Fix bug now it is possible to come back to flat allocation from any assignment function
* Apply assignment function if any when user modify total hours of an allocation
* Remove warning messages when user modifies assignments in advanced allocation
* Disable configuration button for assignment functions not configurable.
* Added profile_test.sah into user folder
* Include file all_data_type_test.suite which allow run all data type test
* Added work report model data type tests
* If a task has manual allocation and is moved then reset to flat
* Add function isSigmoid in IAssignmentFunctionConfiguration
* Rename assignment function name enum
* AssignmentFunction is now an abstract class.
* Add ManualFunction for advanced allocation
* [Bug #1163] Included .js file in component for scroll synchronization with timetracker
* [Bug #1163] Fixed visibility of allocation panel scroll bars
* Fixed transparency problems in stretches function configuration window
* Fixed width issues in reassignment popup
* [Bug #1165] Fix bug
* [Bug #1165] Fix bug
* Fix exception when going to the resource load directly
* Use Listbox instead of Combobox in functions column of advanced allocation window
* Reduce width for efforts column in advanced allocation
* Fix wrong message in configuration button of functions
* Create a new folder which includes user tests and include user_test.sah
* Rename None function to Flat in resource allocation
* Reattach the planning state if exists
* Fix percentage calculation
* [Bug #1164] Fix bug
* Added check Code label functions in all data type tests
* [doc] Links the help of report 'Work and progress per project' to the help icon.
* [doc] Added help for report 'Work and progress per project' in Galician
* [doc] Added help for report 'Work and progress per project' in Spanish.
* [doc] Added help for report 'Work and progress per project' in English.
* Removed unused variable in loops.
* Fix issue when moving a task with an assignment function.
* Fix typo
* Include logs in all the data type tests.
* Added new test to criteria_test.sah file and include respective functions on the common_function.sah file
* Adds work hours data type test
* [Bug #1139] Fix several tests due to new constraint in defaultPrice
* [Bug #1139] Add constraint on UI to show warning in work hours without default price
* [Bug #1139] Do not save work hour without default price
* Unify state of resource load's chart
* [doc] Links the help of report 'Total worked hours per resource in a month' to the help icon.
* [doc] Added help for report 'Total worked hours per resource in a month' in Galician
* [doc] Added help for report 'Total worked hours per resource in a month' in Spanish
* [doc] Added help for report 'Total worked hours by resource in month' in English
* [doc] Links the help of report 'Hours Worked per resource' to the help icon.
* [doc] Added help for report 'Hours worked by resource' in Galician.
* [doc] Added help for report 'Hours worked by resource' in Spanish.
* [doc] Added help for report 'Hours worked by resource' in English.
* Ignore log files
* [doc] Updated development documentation to new BaseCRUDController class
* Fixed alignment problems in gantt taskdetails cells
* Fixed dependency arrows
* Improved alignment in assignment columns
* Release new version NavalPlan 1.1.3
* [Bug #1138] Fix bug
* [Bug #1136] Fix bug
* Add class to distribute an EffortDuration considering the capacities
* Improved appearance of tasks filter
* Removed unused methods
* Modified positioning of filtering options popup
* Adds Unit measures data type test
* Delete trailing whitespaces from scripts/functional-tests/README
* Applied previous color to selected grid row
* Removed draggable properties from progress type list
* Fixed header filter widths
* Fixed styles issues in progress advance management
* Fixed chrome width problems in work hour types edition
* Fixed limiting resources planning header issue in chrome
* Fixed horizontal width fill in WorkReports list
* [Bug #1128] Fixed bug
* Adds label data type test
* Adds several changes in the README to increase in quality of the text
* Add logging category for authentication attempts
* Fix NullPointerException in highlightDaysOnCalendar method
* [Bug #1126] Fix issue in Chromium/Google Chrome
* Refactor ResourceLoadController
* Refactorize setupNameFilter
* Create fields instead of implicitly passing the data
* Group data in class
* Extract another super class for load charts
* Adds a warning about the need to configure the browser in english
* Add Exception days data type test
* Fix some probles with the method BaseCRUDController:updateWindowTitle and also add new test to comprobate the correct working of precission and Max Value.
* [i18n] Update Spanish and Galician translations
* [i18n] Update keys.pot files
* Add criteria data type test
* Change one line in progress_test.sah because it produced an error in some machines
* Add some exclusions to avoid conflicts in runtime
* Add script to start Sahi from command line
* Create a new folder to introduce all data type tests called data-types and include the first of them progress_test.sah
* Add some files needed to run sahi in <navalplan-root>/scripts/functional-tests and include some explanations in REAME file
* ConstraintChecker does not return boolean now
* Fix highlighted days on datebox widget in limiting resources
* Added popup in filters to use or not labels inheritance
* [Bug #1134] Add checks for empty username or password
* [doc] Fix some style format in functional tests documentation
* Create new directory scripts/functional-tests
* Fix typo in database username
* Fix typo: s/Ban/Bank
* [Bug #1132] Fix web service example for CalendarExceptionTypeSample
* [Bug #1132] Remove DayType enum in BaseCalendar
* [Bug #1132] Updated calendar interface to use different colors
* [Bug #1132] Using a renderer in CalendarExceptionType listing
* [Bug #1132] Modify CalendarExceptionType edition form
* [Bug #1132] Create new enum CalendarExceptionTypeColor
* [Bug #1133] Fix bug
* Fix issue in ConstraintChecker now error messages was not being shown
* [Bug #1132] Fix problem with CalendarExceptionType without name
* Fix problem in ConstraintChecker
* [Bug #1121] Fix issue adding @AssertTrue on AdvanceType entity
* [Bug #1119] DataIntegrityViolationException saving a new Process
* Fix highlighted days on calendar widget
* Use ContiguousDaysLine at OrderPlanningModel
* Implement ResourceLoadChartData using ContiguousDaysLine
* Add ContiguousDaysLine class
* Fix problem with chart colors
* Extract class for filling load charts
* Fix wrong label in "Work And Progress Per Task" report
* [Bug #1130] Fix issue changing text size and moving box
* [Bug #1107] Fix issue preventing NullPointerException
* [Bug #1113] Fix issue marking as not transient all allocations
* [Bug #1129] Fix bug setting scale for progress value
* [Bug #1111] Fix name and code of tasks in report
* Define CompanyPlanningModel using annotations
* Define OrderPlanningModel using annotations
* Remove not in effect annotations
* [Bug # 1111] Fix several issues in report: Task Scheduling Status In Project
* Show the precise efforts instead of the rounded amount in hours
* Use a valid url-pattern
* Switch to GanttDates
* [Bug #1124] Fix problem when calculating assigned duration
* Unify advanced allocation state
* Return empty list instead of throwing an exception
* Take into account the scenario
* Create mechanism for reusing common parts of the queries
* Remove unused methods
* Fix the check for checking if an allocation belongs to the order
* [Bug #1122] Fix bug
* [Bug #1117] Fix issue changing I18nHelper in business.
* Avoid workaround to not allow select rows in calendars tree.
* Upgrade ZK version to 5.0.7
* Port client enhacements of BandboxMultipleSearch to ZK5
* [Bug #1115] Fix bug and add new ProgressType for spread progress
* Change the way to update advances in parent elements.
* Not allow select rows in calendar tree.
* Don't use the returned by queries allocations belonging to the order
* Remove spurious annotation
* Reattach the order
* Provide PlanningState to ResourceLoadModel
* Move PlanningState to within PlanningStateCreator
* Reuse PlanningState
* [Bug #1088] Fixed issue in earned value chart
* Revert "[Bug #1088] fix the representation of the chart of earned value month and upper zoom"
* [Bug #1115] DataIntegrityViolationException saving a Cost Category with repeated name
* [Bug #1111] Change label in filter by project section
* [Bug #1111] Task status combo internationalized
* Modify CSS to show labels in containers when are expanded
* Improved getHumanId for worker entities.
* Adapted WorkReportTypeCRUDController in order to extend BaseCRUDController
* Adapted CriterionAdminController to extend BaseCRUDController.
* it changes the labels in the columns of the calendar list, fixes the error messages.
* Modify calendars controllers to show information about edited entity
* Use only editWindow and remove createWindow for BaseCalendarCRUDController
* Manually modified workers to show information about edited entity
* It retrieves the complete description of the type filtered object to show it in the results.
* Change the format of the matching results of a search in the gantt view and the resource allocation view.
* Change the format of the matching results of a search in the projects view.
* Change the format of the matching results of a search in the workers list and in the machine list.
* it adds the property description in the class FilterPair.
* add method in ResourceEnum to retrieve the lowercase value.
* it changes the order of the columns in the bandbox finders.
* Adapted ScenarioCRUDController to extend BaseCRUDController.
* Fixed NPE in tests due to changes in I18nHelper.
* Updated some entity types strings
* Adapted UnitTypeCRUDController in order to extend BaseCRUDController
* Adapted AdvanceTypesCRUDController in order to extend BaseCRUDController
* Revert "Adapted AdvanceTypesCRUDController in order to extend BaseCRUDController"
* Uses default browser locale when user has no language in settings
* Adapted CostCategoryCRUDController in order to extend BaseCRUDController
* Adapted QualityFormCRUDController in order to extend BaseCRUDController
* Adapted UserCRUDController in order to extend BaseCRUDController
* Adapted AdvanceTypesCRUDController in order to extend BaseCRUDController
* Adapted TypeofWorkHoursCRUDController in order to extend BaseCRUDController
* Adapted ProfileCRUDController in order to extend BaseCRUDController
* Released 1.1.2 version.
* Updated Spanish and Galician translations.
* [i18n] Update keys.pot files.
* [Bug #1108] Fixed problem if label is created by another user
* [Bug #1108] Fix bug
* [Bug #1106] Fix bug
* updates the work weeks table when the hours of a day are changed.
* Add "throws InstanceNotFoundException" to delete method and catch the exception into confirmDelete method
* [i18n] Fixed typo in Spanish translation.
* fix the layout in exceptions and work weeks table.
* fix returned values when the configuration is not loaded in BaseCalendarModel and in the EntitySequenceDao.
* Adds constraint to check the validation of the dates of the work weeks when these one are empty.
* it set a fixed width to the column headers in the exceptions table.
* On creating a work week derived from a calendar all the days are created as "Inherited", properly internationalized.
* Fill the combo to create a new work week with the first calendar in alphabetic order and changes the name columns and labels to the edition calendar view.
* It set a fixed width to the columns and changes its names.
* fix some errors when a new work week is added.
* Corrections in calendars test
* the parent from which a new work week derives, must be specified.
* the column parent is not shown if the calendar is not derived.
* shows the current parent from which it derives, in the description calendar
* shows the validation messages and constraint messages.
* it permits delete any work week except the last one
* It does not must permit overwrite another whole work week when a new work week is created.
* Corrections in calendars listing
* create default work week of non-derived calendar
* change how create and edit the work week in the calendars view.
* Refactorized PasswordUtil and used JavaScript to default password warnings in "Change Password" page.
* Add onBlur event calling to updateWindowTitle method in _editExternalCompany
* [Bug #1105] Fix bug
* Removed more controller interfaces related to entry points not being used.
* Removed ItEr75S13GenericCRUDController as it was not being used.
* Adapted ExternalCompanyCRUDController in order to extend BaseCRUDController
* Workaround for maven 3
* [doc] Updated web services documentation with info about new methods.
* Added missing @XmlRootElement annotations.
* Implementation of method which returns one entity
* Remove unused constants and lines in WorkReportLine.
* Add Eclipse m2e settings to pom.xml files
* Merge branch 'master' into ldap
* [Bug #1104] Allow remove profile if it's only used in order authorizations
* Option to delete a user pending in the interface of user list
* Catches NamingException when a role in LDAP does not exist
* Modified export script to test new get methods by code in web services.
* Returning 404 status code if entity is not found in web service method.
* Generic getter for IntegrationEntities via webservice
* Fixed issue in LabelType controller with validation of labels.
* Adapted machines controller to MachineCRUDController.
* Adapted exception days controller to BaseCRUDController.
* Minor fixes in configuration.zul.
* Merge branch 'master' into ldap
* Fixed issue with wrong password in login window (error not shown).
* New option "Change password" in tab "Settings"
* Added default implementation for cancel method in BaseCRUDController.
* Added new beforeSaving method to BaseCRUDController
* Improved info about entities in messages for user.
* Fixed issue if user sets duplicated LDAP roles for the same LibrePlan role.
* Sorted ConfigurationRolesLDAP set in order to show information to user.
* Update window title dynamically for LabelType while user is editing.
* In edition mode show entity being edited in window title.
* Created IHumanIdentifiable interface and used in LabelType.
* Using a set for ConfigurationRolesLDAP instead of a List in order to fix issue.
* Removing unnecesary loop in matching roles
* Used ConstraintChecker in BaseCRUDController.
* Marked some methods in BaseCRUDController as final as they are not intended to be overridden.
* Moved more stuff to BaseCRUDController.
* Added more generic methods to BaseCRUDController.
* Created basic BaseCRUDController and used in LabelTypeCRUDController.
* Removed unneeded lines in worker edition zuls.
* Ensure that the mouseover event for TaskComponent is executed
* Fix end of the dependency was a little above than the pointer
* [Bug #1102]
* Small fixes related with some unneeded variables.
* Refactorization of method getMatchedRoles
* Fixed issue with combo style in Firefox and Epiphany.
* Fixed issue with getMatchingRoles
* Refactorized retrieveUser method in LDAPCustomAuthenticationProvider.
* Removed unneeded usernameInserted variable.
* Changed arguments of authenticateInDatabase.
* Added more responsibilities to authenticateInDatabase method.
* Use authenticateInDatabase in more places.
* Small fixes (groupbox not closable, remove not needed space)
* Move entity Language to package org.navalplanner.business.settings.entities
* Incorporate password and mail to settings window for each user
* Moved Spring LDAP dependencies to root pom.xml.
* Refactorization. Exceptions and generics.
* Add some profilling information for loading a project
* No feedback when loading a project from URL directly
* Refactorization of method retrieveUser
* Replaced tabs for 4-spaces in more files (.css and .js).
* Replaced tabs for 4-spaces in several files.
* Lets the user authenticate when LDAP role search is not properly configured
* [Bug #1103] Fix bug
* Remove unnecessary files
* Merge branch 'master' into ldap
* Fixing issue adding autoscroll to main area in template.zul.
* Move classes and settings.zul to new package
* Incorporate new fields to the user
* Avoid duplicating visual effect on arrows
* Merge branch 'master' into ldap
* [doc] Updated installation instructions due to new "cutycapt" command.
* [Bug #1094] Made changes to fix the issue and use "cutycapt" command.
* Added Nacho and Cristina to AUTHORS file.
* Added an extra grid in settings to format the page like others.
* Refactoring options about planning charts expanded to value per user
* [Bug #1100] Fix bug
* Fixed issue adding constructor without parameters for Hibernate.
* Changed [@user_id] by [USER_ID] in replacement for search query
* Matching-roles with dynamic query.
* Added column LDAP role search query in LDAPConfiguration
* Merge branch 'migration-to-ZK5' into master
* Fix ClassCastException on EffortDurationBox
* Remove still present spurious throws clauses
* ScriptsComponent and associated clases no longer needed
* Remove no longer necessary throws clauses
* Don't pollute throws clauses
* Fixed translation issue with language combo.
* Changed language como for listbox with mold select.
* Fixed copyright header of configuration.zul file.
* Create language configuration option for user
* [doc] Fixed small things in web services basic documentation.
* Created constructor for LDAPConfiguration component and added some doc.
* [Bug #1088] fix the representation of the chart of earned value month and upper zoom levels.
* Changed roles matching renderer code
* LibrePlan - LDAP role matching
* Remove no longer needed js files
* Mixin position restorer into QueueListComponent
* Add missing function adjustTimeTrackerSize
* Extract mixin for copied and pasted code
* Rename memoize to throttle
* Remove zk-Timeplot-1.0_2 project
* Delete no longer needed js files
* Throttle the drawing of the dependencies
* Reimplement moving dependencies when moving task
* Fix the key of the response
* The elements must be queried again otherwise the offset is calculated badly
* Remove unncessary calls to redraw
* Avoid error when moving a task
* [Bug #1096] Fix bug
* [Bug #1095] Fixed issue using header parameter of CutyCapt to set Accept-Language.
* [doc] Improved info about web services.
* [doc] Updated development doc to new REST scripts.
* Tidy not mandatory for example REST scripts.
* [doc] Get rid of Ruby.
* Updated script to get REST XML schema to new variables for environments.
* Remove needed to use Ruby for REST example scripts.
* Use method in SecurityUtils to get current user
* Added method getLoggedUser() in SecurityUtils class
* [Bug #1090] Added JavaScript removed in one of the previous patches.
* [Bug #1090] Removed unused variables in UserModel.
* [Bug #1090] Avoid pass Configuration to MandatoryUser.
* [Bug #1090] Fixed hidden warnings if user is disabled.
* [Bug #971] Fixed reseting Tree model to null.
* [doc] Created UPDATE file.
* [doc] Fixed small typo in INSTALL file.
* Sometimes a double was returned
* Fix error when changing zoom
* [doc] Updated INSTALL file.
* [doc] Improved HACKING file.
* Removed HSQLDB profile as it's not working anymore since we use Liquibase.
* [Bug #1091] Fix bug
* [Bug #1091] Be more lenient if the end date is before start date
* Extract validation checks and fix exception message
* [Bug #975] Cannot apply Sigmoid function is resource allocation has consolidated days
* Now rest clients use demo deployment by default and have 2 new options --prod and --dev.
* [Bug #1084] Fix bug
* Released 1.1.1 version.
* Captured generic exceptions.
* Updated TODO file with roadmap for 1.2 version.
* Changed OpenJDK dependency in Debian package for default-jdk or default-jre.
* [i18n] Fixed wrong translation of project in some reports.
* [i18n] Fixed uppercase/lowercases incoherences.
* [Bug #1084] Fix bug
* Avoid where possible to mark a method as throws Exception
* Avoid creation of array by reflection
* Review methods that provide variables that are known to be null
* Implement missing equals or add warning to classes that have a natural ordering
* Remove unnecessary null checks
* Mark classes as static when not referencing outer class
* Avoid the use of Number constructors
* [Bug #789] Script for parsing ZUL files should look for 'ganttzk_i18n' tag too
* [Bug #789] Renamed 'i18n' prefix in ganttzk to 'ganttzk_i18n'
* Fix possible NPE
* Fix NPE if provided allocation is null
* Avoid NPE if editedValue is null
* [Bug #1086] Fix bug
* [doc] Added explanation about different scopes development documentation.
* [doc] Fixed wrong dependency in training exercises.
* [doc] Fixed small issue in reports guide.
* [doc] Fixed typo in development documentation.
* Changed test to avoid it fails if it's launched on Saturday.
* Merge branch 'master' into migration-to-ZK5-merging
* [i18n] Fixed typo in progress with all tasks.
* [i18n] Fixed typo in "criterions" using "criteria".
* [i18n] Updated Spanish and Galician translations.
* [i18n] Fixed issue in keys generator and updated keys.pot files.
* [i18n] Updated Spanish and Galician translations.
* [i18n] Fixed translation of "progress" to Galician and Spanish.
* [i18n] Marked "Choosing template" for translate.
* [i18n] Changed "order sequence" for "entity sequence"
* [i18n] Updated keys.pot files
* Fixed two translation issues.
* Fixed problem when LDAP host is reachable but LDAP is out-of-service
* Fixed bug in button Test LDAP connection
* [Bug #1083] Fix bug
* Added log messages for "Test LDAP connection" button.
* Corrections of previous patch
* [Bug #1014] add borders in the table of the report of worked hours per each resource.
* [Bug #1014]
* [Bug #1014]
* [Bug #1081] validate and action stop if something fails while saving data in progress type view.
* [Bug #1013] increase the width in filter search box.
* LDAP Authentication
* [Bug #1082] Fix bug
* Revert "Fix bug"
* improve the view of autocomplete property of the login form.
* Fixed problem with last LiquiBase change in PostgreSQL.
* [Bug #1075] Fix the some features in the autocomplete property configuration.
* Add isNavalPlanUser column to users list.
* Composite Handler LDAP-Database. Import of users from LDAP. Support of two types of users (LDAP and Database).
* Adding LDAP configuration properties to Configuration and User
* [Bug #1075] disables the autocomplete login form property if the admin password is not on default.
* [Bug #1075] adds compiling option to disable/enable the autocomplete login.
* [Bug #954] Fix bug
* Revert "[Bug #954] Handle concurrency support in Configuration window"
* The new support for parametrizing the clearing of handlers is used
* Now the clearing behaviour can be parametrized
* More aggresive discarding of sessions and desktops
* Basic LDAP Authentication added on login form.
* [Bug #1080] Fixed issue with Montecarlo method when critical path has more than 10 tasks.
* [Bug #1079] Fixed lazy exception initializing parent calendar too.
* Improve toString message
* Fix bug
* [Bug #984] Add ON_OK event on project name textbox in creation project wizard.
* [Bug #1074] check if exists indicators of the earned value for that date.
* [Bug #1076] Fixed NullPointerException going to Gantt view when project is not scheduled.
* Reduce the time that request handlers are kept around in CallbackServlet
* Fix memory leak in TemplateController
* Refactor password not changed controller code
* Allow to GC the page before the desktop is discarded
* Don't let the thread local hang forever in the threads local map
* Fix leak
* Remove unnecessary timers.
* [Bug #1022] Include filtering criteria information in the hearders of the reports.
* Reduce the live time of desktops
* Fix memory leak
* Determine the log directory dinamically
* Use asynchronous appender
* Move default log4j.properties to application
* Added new Liquibase changelog file for new developments on stable branch.
* Added dependencies
* Configured our JfreeChartEngine as the chart engine
* Deleted JFreeChartEngine class
* GPL implementation of JFreeChartEngine
* Deleted files related with JasperreportComponent
* Fixed shrinked rows on Montercarlo chart
* Fixed shrinked rows on grids at Montecarlo view
* Deleted ExtendedJasperreport
* Deleted forgotten references to ExtendedJasperreport
* Added a temporary fix for Chrome bug
* Added missing files for JasperreportComponent
* Changed report component on all report controllers
* Adapted NavalplannerReportController to make use of JasperreportComponent
* Created JasperreportComponent a free implementation of ZK Jasperreport
* Fixed bug related with the model used with a combobox
* Changed some css to fix some errors
* Merge commit 'navalplan-1.0.1' into HEAD
* Fixed invalid timetracker's width for days
* Changed timeplotz version
* Renamed TimeTrackerComponent#afterCompose method
* Made tabs to use all avaliable width
* Fixed bug regarding timetrackergap
* Fixed shrikend boxes on calendars/_edition.zul
* Fixes shrinked boxes on Work Reports
* Fixed shrinked boxes in Resources>Companies>Edit company
* Fixed shrinked boxes in Resources>WorkReports>Edit Work Report
* Fixed shrinked boxes in Resources>Machines>Edit machine
* Fixed shrinked boxes on Resources>Worker>Edit Worker
* FEA: ItEr02S03MigracionZK5  Fixed tree header visualization problems on Chrome
* Fixed bug with watermkars on secondlevel timetracker
* Changed fixedByLayout by sizedByContent
* Released 1.0.1 version.
* Updated TODO file with decisions from roadmap meeting.
* [Bug #803] Remove unused code
* [Bug #803] Change Zoom level in LimitingResources launches Validation exception
* [Bug #804] Fix bug
* [Bug #788] changes the structure and labels in resource load view.
* [Bug #808] Fix bug when a resource field in Resource search is selected for the first time, set 'Current selection' as name of that resource
* [Bug #785] fixes translations in company list.
* Added dependency with ttf-freefont on debian/control.
* [Bug #801] Fixed typo in Spanish translation.
* [Bug #785] Add improvements in companies list.
* [Bug #784] Sort results of add criterion combo in resource edition
* Reset the progressmeter to zero when the MonteCarlo chart is closed
* [Bug #800] Fix closable button in 'Calendar allocation' window
* [Bug #800] Fix closable button in 'Progress consolidation' window
* [Bug #800] Add closable button to 'Progress assignment' window
* [Bug #800] Add closable button to 'Project details' window
* Added a default "left" value for timetrackergap
* [Bug #782] Fix bug
* Fix asyncNotifications
* Fix the type parameter
* [Bug #779] Fix bug
* Avoid LazyInitializationException accessing template's labels
* [Bug #798] Refactor 'Remove Criterion' window, change it for a MessageBox window
* Changed the order Plotinfos are appended
* [Bug #795] Refactor calendar remove dialog box and change it to a MessageBox
* Deleted method used to adapt callbacks for older versions on Timelinez
* Fixed some bugs on DependencyComponent's widget-class
* Deleted native namespace
* Missing commit
* Added configuration property for using trendy molds
* Added a comment to explain why we are not using XulElement#setContext
* Replaced EventListener by XulElement#setContext
* Upgraded to ZK 5.0.5
* Changed base widget-class for DependencyComponent
* Fixed bug on TaskComponent widget
* Adding a dependency is cancelled if you click anywhere that's not a task
* ESC keypress event is now handled
* Added initial implementation for Milestone widget
* Added missing ">" symbol at task-list mold
* Fixed bug on TaskComponent widget-class
* Added if-check to avoid exception caused by Milestone
* Fixed shrinked treecol on resource load view
* Changed the given id for TimeTracker at AdvancedAllocationController
* Recoded advanceAllocation Javascript file
* Added method to retrieve the TimeTracker's real width
* Added scrolling functionallity to TimeTracker's widget-class
* Added code to make TimeTracker's widget-class singleton
* Skeletal code for TimeTracker widget-class
* Explicitly set width to solve shrinking problems with chrome
* Fixed shrinked tables on labels tab
* Added missing closing symbol
* FEA: ItEr02S03MigracionZK5  Avoid uses of Mode#goToOrderMode that cause unnecessary repaintings
* Changed valing attribute to pack
* Replaced DOM methods with jQuery
* Fixed bug that caused Duplicated ID exception
* Fixed onflicts: 	ganttzk/src/main/java/org/zkoss/ganttz/TaskComponent.java 	navalplanner-webapp/src/main/java/org/navalplanner/web/planner/company/CompanyPlanningModel.java
* Missing commit
* Revert comments that prevented the use of timeplot
* Polished version of previous commit (load chart on demand)
* First approach for loading charts on demand
* Added named parameters for TaskComponent widget-events
* Added resize capabilities to tasks
* Fixed bug regarding Widget.addAft() function
* Merge branch 'master' into migration-to-ZK5-dev
* Added missing setClass method for TaskComponent widget-class
* Deleted deprecated code
* Ganttpanel changed to scroll on zoomlevel change
* Sent timetracker's scroll_left value when planner's zoom level changes
* Added a token for referencing elements on event data
* Replaced listener on zul code with listener on widget
* Delay the construction and data retrieval for the charts below the company view until they are actually shown.
* Updated timelinez version to 2.3.1_50
* Added a scroll listener for LimitingResourcesPanel
* Overwritten getWidgetClass method for LimitingResourcesPanel
* Created LimitingResourcesPanel widget-class
* Explicitely added width attr for vbox at _orderElementTree
* Deleted useless Javascript dependencies
* Changed how the CSS class of a class is set
* Overwrote default setLeft method
* Added drag&drop functionallity
* Added missing methods for adding depencies
* Changed how we retrieve the DependencyList widget
* Added a z-index property to .box class
* Modified UnlinkedDependencyComponent and how it's inserted on the DOM
* Added singleton-like structure for DependencyList
* Added behaviour for consolidating a new depedency
* New isOverTask method
* Fixed method call at DependencyComponentBase
* Fixed typo on variable name
* Converted jQuery-Array-like to proper Array
* Test code for using UnlinkedDependencyComponent
* Done some changes to improve performance while adding dependencies
* Behaviour for UnlinkedDependencyComponent
* Added missing Math.abs(...)
* Added missing constant for DependencyComponentBase
* Moved setupArrow method to top class
* Basic implementation for UnlinkedDependencyComponent
* Created a base widget-class for dependency elements
* Added missing $supers call at overwrote method
* Fixed bug that caused and exception on empty Strings
* Adapted retrieveData to use JSONArray objects
* Added addRelatedDependency method for TaskComponent widget-class
* Added methods for drawing dependency arrows
* Added required constants for TaskComponent widget-class
* Added common.Common as a dependency for ganttz package
* Created a Common widget-class
* Overwrote appendChild method for DependencyList widget-class
* Fixed bug on TaskContainer mold
* Added a method for initializing commonly used properties at DependencyComponent widget-objecs
* Added missing mold content for DependencyList component
* Content for DependencyComponent mold
* Added $define property for DependencyComponent widget-class
* Added renderProperties method for DependencyComponent
* Added skeletal mold and widget-class for DependencyComponent
* Avoid adding Timeplot because it is failing.
* Replaced deprecated valign property
* Replaced deprecated valign property
* Added show/hideResourceTooltips methods
* Added hide/showAllTaskLabels methods
* Renamed method at TaskComponent widget-class
* Replaced deprecated LayoutRegion import
* Replaced deprecated propertie: valign
* Changed super-widget-class for TaskContainerComponent
* Mold content for TaskContainerComponent
* Skeletal mold and widget class for TaskContainerComponent
* Adapted css classes
* Added adjustResourceLoadRows for QueueListComponent
* QueueListComponent now extends XulElement
* Fixed typos
* Mold content for QueueListComponent
* Skeletal mold and widget-class for QueueListComponent
* Mold content for LimitingDependencyComponent
* Mold content for LimitingDependencyList
* Mold content for QueueComponent
* Basic widget-clasess and molds for Limitin Resources perspective
* Added adjustScrollableDimensions to Planner widget-class
* Refactored event listener from TaskList to GanttPannel
* Added method to ResourceLoadList
* Added event handler for scrolling
* Fixed bug on overwritted method
* Event handler for resize event
* Replaced Clients.evalJavascript argument
* Added recalculateTimeTrackerHeight
* Fixed typos
* Added class-methods setInstance and getIntance
* Added ResourceLoadList to WPD file for resourceload package
* Basic widget-class and mold for ResourceLoadList
* Changed inherited class for ResourceLoadList
* Mold content for ResourceLoadCompononent
* GanttPanel static-widget-instance is updated on each instantiation
* Replaced setStyle with render(...,"style",...)
* Replaced deprecated method
* Fix compiler error.
* Proper setup of zkforge dependencies
* Replaced deprecated interface import
* Basic widget-class and mold for ResourceLoadComponent
* Added widget and mold config for ResourceLoadComponent
* zk.wpd file for ganttz.resourceload package
* Fixed width for modal window
* Fixed typo on method name
* Enabled event thread
* Added <?component...?> directive
* Replaced Javascript sent to client
* Replaced jQuery by jq
* Fixed scroll handling for GanttPanel
* Added mold attribute to button component
* Added <?component...?> directive
* Added methods to TaskComponent widget-class
* Added method to TaskComponent widget-class
* Fixed bug on attribute setting
* Added method for displaying deadlines
* Set "trendy" mold for main buttons
* Added a trim method to adapt callback uri
* remove contextpath from callback uri
* Conf. maven for sources.jar
* Created widget-class for TaskComponent
* Created GanttPanel widget-class
* Added javascript dependencies for Ganttz
* Modified how Javascript dependencies are loaded
* Minor bug fixing
* Mold for TaskComponent
* Mold for TaskRow component
* Bugfix at TaskList mold
* Mold for GanttPanel comp.
* Mold for TaskList comp.
* Skeletal widgets for ganttz
* Upgraded timelinez version
* Conf. Maven for sources.jar
* Upgraded timeplot package
* Changed South import
* Added throws statements
* Added throws statement
* Added throws statement
* Removed deprecated method getRealStyle
* Upgrading TaskComponent to ZK 5
* Upgraded TimeTrackerComponent to ZK 5
* Upgrade ZK version to 5.0.3


Version 1.1.3 (12 Aug 2011)
---------------------------

New minor release with all the issues fixed in stable branch since previous
version. The most important problem solved is the save operation in Gantt view
screen for big projects using labels. Under some special conditions not all the
labels were loaded and the persistence to disk failed.

Other minor fixes included in this version:

* Task Scheduling Status In Project report has been reviewed and fixed.

* Now NavalPlan does not depend on server language for translations of strings
  in the business module.

* Resource load chart is fixed for tasks finishing in the middle of a day.

* Solved issue on Gantt saving due to transient resource allocations.

* Added some basic constraints checkers to exception days and progress type.

* New log file to register login attempts.

Changes
~~~~~~~

* [Bug #1138] Fix bug
* [Bug #1136] Fix bug
* Add class to distribute an EffortDuration considering the capacities
* Add logging category for authentication attempts
* [i18n] Update Spanish and Galician translations
* [i18n] Update keys.pot files
* [Bug #1133] Fix bug
* Fix issue in ConstraintChecker now error messages was not being shown
* [Bug #1132] Fix problem with CalendarExceptionType without name
* Fix problem in ConstraintChecker
* [Bug #1121] Fix issue adding @AssertTrue on AdvanceType entity
* [Bug #1119] DataIntegrityViolationException saving a new Process
* Fix wrong label in "Work And Progress Per Task" report
* [Bug #1130] Fix issue changing text size and moving box
* [Bug #1113] Fix issue marking as not transient all allocations
* [Bug #1129] Fix bug setting scale for progress value
* [Bug #1106] Fix bug
* [Bug #1111] Fix name and code of tasks in report
* Remove not in effect annotations
* [Bug # 1111] Fix several issues in report: Task Scheduling Status In Project
* Show the precise efforts instead of the rounded amount in hours
* Switch to GanttDates
* [Bug #1124] Fix problem when calculating assigned duration
* [Bug #1117] Fix issue changing I18nHelper in business.
* [Bug #1111] Change label in filter by project section
* [Bug #1111] Task status combo internationalized
* Modify CSS to show labels in containers when are expanded
* [Bug #1088] Fixed issue in earned value chart


Version 1.1.2 (18 Jul 2011)
---------------------------

Bugfixing release due to a critical bug causing problems in concurrent edition
of projects using shared labels.

Other minor issues fixed in this version:

* Added operation for users deletion.
* Solved issue with printing translation.
* Don't show default password warnings if user is disabled.
* Fixed issue with Sigmoid function if there is consolidated progress.

Notes
~~~~~

In this version *CutyCapt* command user has changed from ``CutyCapt`` to
``cutycapt``. Depending on your distribution you will need to perform the
following steps:

* Ubuntu Lucid (10.04) or Maverick (10.10) upgrade your CutyCapt package in
  order to use ``cutycapt_20110107-2``.
* Debian Sqeeze (6) remove ``cutycapt_20110107-1`` package and install the
  official CutyCapt package.
* Ubuntu Natty (11.04) simply install CutyCapt package if not done
  automatically.
* For manual installation simply rename ``CutyCapt`` command to ``cutycapt``.

Changes
~~~~~~~

* Updated Spanish and Galician translations.
* [i18n] Update keys.pot files.
* Fixed problem in UserModel.
* Workaround for maven 3
* Add Eclipse m2e settings to pom.xml files
* [Bug #1104] Allow remove profile if it's only used in order authorizations
* Option to delete a user pending in the interface of user list
* [Bug #1108] Fixed problem if label is created by another user
* [Bug #1108] Fix bug
* [i18n] Fixed typo in Spanish translation.
* [Bug #1094] Made changes to fix the issue and use "cutycapt" command.
* [Bug #1096] Fix bug
* [Bug #1095] Fixed issue using header parameter of CutyCapt to set
  Accept-Language.
* [Bug #1090] Added JavaScript removed in one of the previous patches.
* [Bug #1090] Removed unused variables in UserModel.
* [Bug #1090] Avoid pass Configuration to MandatoryUser.
* [Bug #1090] Fixed hidden warnings if user is disabled.
* [Bug #971] Fixed reseting Tree model to null.
* [Bug #1091] Fix bug
* [Bug #1091] Be more lenient if the end date is before start date
* Extract validation checks and fix exception message
* [Bug #975] Cannot apply Sigmoid function is resource allocation has
  consolidated days


Version 1.1.1 (07 Jun 2011)
---------------------------

First minor version for 1.1.x cycle with lots of bugfixes. The most important
ones:

* Fixed several memory leaks which will make application use less memory now.
* Improved log system configuration.
* Solved a translation issue with some strings in the Gantt view.
* Resolved some bugs moving tasks due to new dependencies.

Changes
~~~~~~~

* Updated TODO file with roadmap for 1.2 version.
* Changed OpenJDK dependency in Debian package for default-jdk or default-jre.
* [i18n] Fixed wrong translation of project in some reports.
* [i18n] Fixed uppercase/lowercases incoherences.
* [Bug #1084] Fix bug
* [Bug #789] Script for parsing ZUL files should look for 'ganttzk_i18n' tag too
* [Bug #789] Renamed 'i18n' prefix in ganttzk to 'ganttzk_i18n'
* Fix possible NPE
* Fix NPE if provided allocation is null
* [Bug #1083] Fix bug
* [Bug #1014] add borders in the table of the report of worked hours per each
  resource.
* [Bug #1014] include the name of the assigned task to each report line, in the
  report worked hours per each resource.
* [Bug #1014] return the date time at start of day to perform the grouping by
  date correctly.
* Avoid NPE if editedValue is null
* [Bug #1013] increase the width in filter search box.
* [Bug #1086] Fix bug
* Changed test to avoid it fails if it's launched on Saturday.
* [i18n] Fixed typo in progress with all tasks.
* [i18n] Fixed typo in "criterions" using "criteria".
* [i18n] Updated Spanish and Galician translations.
* [i18n] Fixed issue in keys generator and updated keys.pot files.
* [i18n] Updated Spanish and Galician translations.
* [i18n] Fixed translation of "progress" to Galician and Spanish.
* [i18n] Marked "Choosing template" for translate.
* [i18n] Changed "order sequence" for "entity sequence"
* [i18n] Updated keys.pot files
* Fixed two translation issues.
* [Bug #1082] Fix bug
* Revert "Fix bug"
* [Bug #954] Fix bug
* Revert "[Bug #954] Handle concurrency support in Configuration window"
* The new support for parametrizing the clearing of handlers is used
* Now the clearing behaviour can be parametrized
* More aggresive discarding of sessions and desktops
* [Bug #1080] Fixed issue with Montecarlo method when critical path has more
  than 10 tasks.
* [Bug #1079] Fixed lazy exception initializing parent calendar too.
* Improve toString message
* Fix bug
* [Bug #1074] check if exists indicators of the earned value for that date.
* [Bug #1076] Fixed NullPointerException going to Gantt view when project is
  not scheduled.
* Reduce the time that request handlers are kept around in CallbackServlet
* Fix memory leak in TemplateController
* Refactor password not changed controller code
* Allow to GC the page before the desktop is discarded
* Don't let the thread local hang forever in the threads local map
* Fix leak
* Remove unnecessary timers.
* Reduce the live time of desktops
* Fix memory leak
* Determine the log directory dinamically
* Use asynchronous appender
* Move default log4j.properties to application


Version 1.1.0 (19 May 2011)
---------------------------

New major version of NavalPlan. Apart from a lot of bugfixing the main features
included in this version are:

* Fixed problem with WebKit based browsers.

  Now WebKit based browsers (Chrome, Safari, Epiphany) and last version of
  Firefox are fully supported.

* Fixed issues in printing.

  Printing uses a WebKit based solution and thus failed occasionally because of
  the previous issue that is fixed now.

* Over allocation control support.

  In 1.0 version it was not possible to configure a limit of overtime for
  workers. From now on users can configure calendars with this feature. You can
  set a maximum overtime in each regular work week day or in a specific
  exception day.

  This provides a lot of flexibility in the resource allocation strategies. You
  can do allocations without generating overtime or generating a controlled
  amount of it. In this way, users can ask the application to calculate the
  soonest finish date for a task taking into account the overtime allowed.

  A last clarification about this control is that is done per task. The maximum
  allocation assigned to a worker is observed per task. This means that if you
  have two tasks in the same period assigned to the same resource, taking into
  both tasks, you can surpass the overtime constraint.

* Hierarchical criteria enforced.

  Criteria in NavalPlan are like dynamic roles that resources can satisfy and
  can be hierarchical. This means that one general criterion can include other
  more specific criteria inside. For instance, it is possible to configure the
  general criterion Engineer with two children like Electric Engineer and
  Software Engineer.

  This feature was disabled in version 1.0.4 and is provided again after some
  fixes were done.

* New generic allocation algorithm.

  Generic allocation is the assignment based on criteria. You specify the set
  of criteria which must be satisfied by the workers who are able to do the
  task and, after this, NavalPlan looks for them and selects the ones less
  loaded to be planned.

  The algorithm which selects the workers to be assigned to a task when
  generic allocation is used was improved in several points:

  * The sharing hours process allocates workers when selected up to load 100%.
  * On selecting workers for a date inside a task, the new algorithm tries to
    use the workers assigned in the previous days of the task. In this way, the
    algorithm is not based just in load but selects first the previously used
    resources if they have free hours.

* Project scheduling window new features.

  In the project scheduling window four improvements were done:

  * Positioning system on zoom changes. Now when the user changes the zoom
    level in the Gantt planning window, after the screen has been refreshed
    the scroll is moved so the user will see the same time period.
  * Calendar exception days are shown in the Gantt planning grid with a
    different background color. At day and week zoom level the project
    calendar is used to highlight the non-working days.
  * Violated dependencies are highlighted. A red color is used to draw
    dependencies that are violated.
  * Resource load chart precision was improved. Now two points per time unit
    are represented and this makes the chart more accurate in the resource load
    chart which is displayed in the bottom of Project Planning view, Project
    scheduling window, company resource usage screen and project resource usage
    screen.

* Default password change notification protocol.

  It was implemented a mechanism to control if the password for the default
  users have been changed. Default accounts which are provided are:

  * admin/admin. This user is the user with administration privileges.
  * user/user. This is a common user without administration privileges.
  * wswriter/wswriter. This user has write permissions to use then web
    services operations which require modify or insert data in NavalPlan.
  * wsreader/wsreader. This user has read permissions to the use web services
    operations which only require read permissions.

  The mechanism consists of showing warning messages in the left bottom corner
  of the screen remembering which default user accounts have still configured
  the default password.

* Minute accuracy in calendars and allocation windows.

  In 1.0 version calendars are expressed in hours the same as the amount of
  allocated time in tasks. This means that users cannot use minutes if they
  want to have a higher detail level in the plans.

  This was changed and now is possible to specify the calendars and the
  allocations in hours and minutes. In the allocation screens this is done
  with the ":" separator in the input boxes.

  A thing which is pending to be implemented is to allow estimate the WBS for
  a project in hours and minutes. In 1.1 is only allowed in hours.

* Limiting resources new insertion algorithm.

  It was implemented a new algorithm for inserting tasks in points where there
  are already tasks planned in the limiting resources. The new algorithm moves
  the tasks respecting better the order of the tasks already planned.

* Calendar interface revamp

  User interface for calendars was improved to provide an easier use.

* WBS screen

  This window was tested deeply and errors related to moving nodes in the tree
  were corrected.

Notes
~~~~~

If you are upgrading from any 1.0.x verion, you will need to manually execute on
your database the SQL sentences from file:
``/usr/share/dbconfig-common/data/navalplan/upgrade/pgsql/1.1.0``

To get database access information review ``/etc/tomcat6/context.xml`` file.

Changes
~~~~~~~

* [Bug #980] show warning in "Earned Value" tab for future dates.
* Fixed missing file for upgrading database with Debian package.
* Fixed issue upgrading Debian package and removed unneeded dependency.
* Extract common functionality into methods
* Calls to reassign wipe out the previous not consolidated assignments
* Add test for a corner case
* Use guard instead of if for all method
* Move calculation of efforts to Consolidation
* When deconsolidating it always uses the previous assingments instead of the
  newly created ones
* Remove code with no effect
* Provide EffortDurations instead of the more coarse hours
* Remove unused method
* [Bug #979] Set default width for earned value legend container
* Linked wiki from README and INSTALL files.
* Removed dependency with LaTeX as it is not needed to build the package.
* Added database upgrade scripts for version 1.1.0.
* [Bug #1070] Calculate the critical path using a topological order
* Remove tasks that are not really initial from the initial and end tasks
* Cache the calculated topological order
* [Bug #1070] Fix problem in GanttDiagramGraph
* When enforcing all the constraints using only the ones without incoming
  dependencies
* When populating the graph with dependencies don't enforce contraints yet
* [Bug #1066] Fixed bug updating calendar exception days.
* [Bug #1068] Fixed issue when a quality form advance was marked as spread.
* [Bug #1067] Fix bug
* Fix disparity between dates in task properties and allocation tab
* [i18n] Fixed issue using application in Spanish.
* [i18n] Updated Spanish and Galician translations.
* [i18n] Updating keys.pot files.
* [Bug #1006] it catchs HibernateOptimisticLockingFailureException in validator
  method of the CalendarExceptionType Entity.
* [Bug #1054] Consider task constraints
* Make DomainDependency implement IDependency
* Rename method to a more accurate name
* Avoid O(n2) algorithm
* Change allocateKeepingProportions so it uses EffortDurations instead of hours
* Refactor
* It's inefficient to use boxed objects
* Fix violation of the intended immutability of IntraDayDate
* [Bug #996] fixing bug in desconsolidation action.
* [Bug #996] Update the end date of the task correctly.
* [Bug #996] Calculate correctly the proportion of hours for each day.
* [Bug #1041] Fixed issues with default password warning messages.
* [Bug #1065] Fixed issue adding method to open a new transaction on DAO.
* Revert "[Bug #1006] it catchs HibernateOptimisticLockingFailureException in
  validator method"
* [Bug #1006] it catchs HibernateOptimisticLockingFailureException in validator
  method of the CalendarExceptionType Entity.
* [Bug #1002] it catchs HibernateOptimisticLockingFailureException in validator
  method of the orderElementTemplate Entity.
* [Bug #1008] it catchs HibernateOptimisticLockingFailureException in validator
  method of the  workReportType entity.
* [Bug #1061] it marks LimitingResourceQueueModel with @OnConcurrentModification
  to intercept an OptimisticLockingFailureException.
* [Bug #1043] Fixed issue changing constraintDate to IntraDayDate.
* Remove enforceAllRestrictions call
* Add support for receiving not notified events
* [Bug #1048] Fix bug
* [Bug #1064] Fixed more problems related with this issue.
* [Bug #1064] Fixed bug removing unneeded code to update criteria.
* Rename URLHandler to EntryPointsHandler
* Fix method name and reuse it
* Fix regression introduced in c05150b2345a4c2bebd631c690daf69aeda0f06e
* Use capture mechanism instead of building urls manually
* When use redirect avoid to use fragment
* [Bug #1001] Fix bug
* Add mechanism for handling OptimisticLockingFailureException on random objects
* Add mechanism to capture the redirection to a entry point
* Add missing metadata to entry points
* Removed PDF option in printing configuration dialog.
* [Bug #975] Set last stretch with 100% completition as read-only
* Added method isConsolidated()
* Improved pretty-print of resources for assignment
* [Bug #966] Revamped materials assignment interface
* Revamped calendars administration
* [Bug #1063] Fix NullPointerException
* [Bug #1059] Fix bug
* [Bug #1050] Fix bug
* Some refactorings
* [Bug #802] Rearranged context menu and added vertical separators
* [Bug #1056] Added default icon to Delete Milestone Command
* Fixed small regression with cell styles in advanced allocation
* [Bug #1035] Changed left margin of timeplot to fix gantt alignment issues
* [Bug #1031] Style improvements in assigned resources popup
* [Bug #1024] Fixed showing progress in print view
* [Bug #975] Refactored calculation of interpolation
* [Bug #975] Intervals defined by stretches should include consolidated stretch
* [Bug #975] Use stretchesPlusConsolidated for drawing Graph
* [Bug #975] Renamed getStretches() to getAllStretches()
* [Bug #975] Added method getStretchesPlusConsolidated()
* [Bug #975] Added method getStretchesDefinedByUser()
* Fixed possible OutOfBoundsException
* If loadedProportion is negative set it to zero
* Show only two decimals in loadProportion and amountOfWork
* Set date of new stretch starting from consolidated date if any
* Renamed variables to fit better name
* Fix misspelling
* [Bug #1021] Fix bug
* Fixed some tests due to previous commit.
* Added check for CHILDREN advance in container and fixed detected issue.
* Added test to check CHILDREN advance marked as spread by default and fixed
  issues.
* [Bug #1046] Fix bug
* [Bug #1049] Fix bug
* [Bug #1045] Fix bug
* Don't show seconds in EffortDurationBox
* [Bug #648] Add new more lenient decimal box
* [Bug #1039] Added new test to check spread advance and set them randomly if
  needed.
* [Bug #1039] Fixed broken tests due to bug solution.
* [Bug #1039] Fixed issue with spread advance when it is removed.
* [Bug #1003] it marks MachineModel with @OnConcurrentModification to intercept
  an OptimisticLockingFailureException.
* [Bug #1000] it marks OrderModel with @OnConcurrentModification to intercept an
  OptimisticLockingFailureException.
* [Bug #998] Fix layout
* [Bug #998] Fix column inherited in Calendars
* Fixed broken tests in previous commit related with CHILDREN advance behaviour.
* Changed tests and fixed issue with CHILDREN advance.
* [Bug #1029] Fix bug
* Fix precondition error in finishing criterions
* [Bug #1023] Now it can be marked as not finished
* Added more tests for criteria in WBS.
* Added more tests for advances in WBS and fixed detected issue.
* Added more tests for labels in WBS and refactorized some parts.
* Reorganized code of last tests for WBS.
* Added more tests for criterions in WBS.
* Fixed issues in advances related to previous tests.
* Added more tests for advances in WBS.
* Added more tests for labels in WBS and fixed detected issue.
* Added more test to check labels behaviour on WBS movements.
* Add test to check same criterion on WBS movement.
* [Bug #1044] it check out the subcontracted direct advance assignment exists
  before reattaching its measures.
* [Bug #1011] Fix bug
* [Bug #1040] Fixed problem added in previous commit.
* [Bug #1040] Added new checks on tests and fixed problem for orphan children
  advance types.
* Removed unused variables in test.
* [Bug #1028] Removed unnecesary save of rootTask
* [Bug #1028] Avoid bug
* [Bug #1038] Added test and fixed bug for repeated labels in WBS.
* [Bug #1037] Fixed other tests affected by changes in bugfix.
* [Bug #1037] Added test and fixed bug for advances of same type.
* Refactorized test to have AdvanceType as checking variables.
* Basic test for move OrderLine to OrderLineGroup in OrderElementTreeModel.
* [Bug #987] Pretty print ValidationExceptions as WrongValueExceptions in
  CostCategories
* [Bug #978] Update overallProgress whenever the user enters the Gantt screen
* [Bug #978] Update overallProgress when progress is assigned to a task
* [Bug #1028] Remove update when a project is saved, and do updateAndRefresh
  when the graph changes
* [Bug #1042] Fix bug
* Added methods to assert criteria and used in the whole test file.
* Basic test for move method in OrderElementTreeModel.
* [Bug #893] Fix bug updating dates
* Merge branch 'FixBug894_3'
* [Bug #1036] Fix bug removing a consolidation
* [Bug #894] Add a confirm dialog on order list to warn the user when it
  removes a subcontracted order.
* Added more checks to test unindent method.
* Rename "getDirectAdvanceAssignmentOfSubcontractedOrderElements" to
  "getDirectAdvanceAssignmentOfTypeSubcontractor"
* [Bug #894] Subcontrating relation is maintained in the root element.
* Added test to check invalidation of indirect criterion requirements.
* Extract copy and pasted code to utilities class
* [Bug #1016] [Bug #1017] [Bug #1018] [Bug #1019] [Bug #1020] Fix report
  criteria hierarchy bugs
* untilAllocating requires an EffortDuration instead of hours
* Change HoursModification to use EffortDuration
* ResourceAllocation#isSatisfied considers the non consolidated assignments
* Add intendedNonConsolidatedEffort field
* Fix formatting
* Rename originalTotalAssignment to intendedTotalAssignment and use
  EffortDuration instead of hours
* [Bug #1010] Switch from Intboxes to EffortDurationBoxes
* Return EffortDurations instead of ints in aggregating methods
* Extract method to produce a sum of EffortDurations
* Remove unused method
* Removed unused method.
* Added remove method to interface ICriterionRequirable.
* Bump timeplot version to 1.0_2_4.
* Removed unused and empty interface ICriterionRequirementHandler.
* Add test to check preservation of invalidated indirect criteria.
* Added checks for criteria in hours groups in tests.
* [Bug #1034] Fixed bug for users with "Project creation allowed" role.
* [Bug #1010] Use EffortDurationBox in AdvanceAllocation
* Make restrictions use EffortDuration instead of hours
* Add ZK component for editing EffortDurations as text
* Add getTotalEffort method
* Rename IAllocateHoursOnInterval to IAllocateEffortOnInterval
* Add method to allocate an EffortDuration instead of hours
* Updated NEWS file and release date.
* Released 1.0.6 version.
* [Bug #1028] Avoid bug
* [Bug #1030] Fixed opening read-only transaction in removeMaterialAssignment.
* [Bug #1026] Fix bug
* Inline silly method
* Extract class class responsible of hooking into chart refilling events
* [Bug #978] Remove refresh button
* Fixed test notAllowRemoveCalendarWithChildrenInOtherVersions
* [Bug #1015] Fix bug at hours worked by resource report
* [Bug #1012] Fix bug at resource load
* Refactor Criterion comparators
* Remove unnecessary invocation to show message
* [Bug #994] Show message when finishing reallocations
* Fix some type warnings related to the use of generics with GanttDiagramGraph
* Use getReassignationResourcesPerDay
* [Bug #995] Fix bug
* Fix checking of a calendar being used by resources
* [Bug #1009] Fix bug
* [Bug #907] Changed styles in advanced limiting assignment cells
* [Bug #407] Fixed positioning issues in print CSS
* [Bug #1007] Vertical height parameter added to CutyCapt
* [Bug #975] Stretch function cannot be applied if the task is already 100%
  consolidated
* [Bug #975] Respect consolidated day assignments of a Resource Allocation
  when applying a Stretch function
* Added sortByDate
* Use factory method instead of constructor
* Tell the user when the intended and the real resources per day differ
* AllocationRow tracks the current calculated value
* Fix satisfaction condition
* Add intended_resources_per_day column
* Keep the intended resources per day
* Fix test that was not testing anything
* Specify the resource calendars using capacities
* Create easier to read methods for specifying overassingment
* Not satisfying the resources per day doesn't prevent exit
* [Bug #976] Add an initial only-read stretch in advance allocation functions
* Added toString()
* Added constructor and contructor copy
* Moved member attributes to beginning of class
* Moved StretchesFunction$Type to its own file
* Changed contructor to protected
* Refactoring getAdvanceMeasurement method to
  getAdvanceMeasurementAtDateOrPrevious.
* [Bug #1005]  subcontracted task sends each progress measurement correctly.
* Added more tests for OrderElementTreeModel::indent method.
* Added basic test for OrderElementTreeModel::unindent method.
* More checks in basic test for OrderElementTreeModel::indent method.
* [Bug #999] Don't pose as transient anymore other objects hanging from Calendar
* [Bug #988] Check calendar is not being referenced by other entities
* [Bug #988] Remove former resource calendar when a new calendar is selected
  for a machine
* [Bug #988] Remove former resource calendar when a new calendar is selected
  for a worker
* [Bug #988] Create test saveAndRemoveResourceCalendar
* [Bug #1004] Fix bug LazyInitializationException of Calendars
* Added basic test for OrderElementTreeModel::indent method.
* [Bug #997] Fix error in generic allocation
* Rename method to reflect better meaning
* Add Javadoc comment explaining the semantics of IntraDayDate#getEffortDuration
* Removed added method in EntitiesTree and moved to business.
* [Bug #999] Fix bug set as dontPoseAsTransientAnymore after clicking
  SaveAndContinue
* Fixed lazy when toLeaf is called manipulating WBS.
* [Bug #952] Fix bug
* [Bug #992] Fix bug
* [Bug #993] Fix bug
* [Bug #993] Add methods to increase and decrease a IntraDayDate
* [Bug #990] When removing a profile check is not referenced by order
  authorizations
* [Bug #990] When removing a profile check is not referenced by other users
* Changed toContainer to set code to null as it was done till now.
* Added several tests that check add and remove elements on tree.
* Adding OrderElementTreeModelTest with basic tests for addElement operation.
* Added method getCriterionType in PredefinedCriterionTypes.
* Added missing MANIFEST file
* Set gradient default property to false in timeplotz
* Added MANIFEST file to timemplotz to avoid error with deploy.sh
* [Bug #992] Fix bug
* [Bug #991] When removing an hours type check is not referenced by other
  entities
* Prevent exiting by accept if goals are not satisfied
* Warn if the goals set are not satisfied when applying the allocation
* Usability improvement on calendar's work week edition
* [Bug #989] Fix bug
* [Bug #989] Support discounting the hours of several allocations
* Rename method so its meaning is easier to understand
* [Bug #986] Prevent removing a label type that is being used by and
  orderelement
* [Bug #986] Prevent removing a label that is being used by an orderelement
* [Bug #986] Set cascade to none in mapping between Labels and OrderElements
* [Bug #986] Remove unused code
* [Bug #987] Don't select hour type if there are no items in the list of hours
  type
* [Bug #987] Change cast to Listbox
* [Bug #985] Fixed changing lables mapping to all-delete-orphan.
* Add method for specifying several days together in an interval
* [Bug #941] fixing bug :  Quality forms are not duplicated now in the same
  task.
* Refactor to make easy further changes
* It can specify the intervals using IntraDayDates now
* Refactor
* Fixed NullPointerException in PageForErrorOnEvent.
* Improved design of errors and fixed NullPointerException when clicking in
  continue.
* [Bug #981] Project with long name are showed properly in "Project Costs Per
  Resource" report.
* Improved information showed when some runtime error happens.
* [Bug #983] Sigmoid function is applied without modifying the endDate of the
  task
* [Bug #974] Rename applyDefaultFunction() to applyOn()
* [Bug #974] Refactor the code that handles selection of function allocation
  options
* [Bug #974] Do standard resource allocation when user selects NONE in
  AdvanceAllocation
* Fix misspelling error
* [Bug #969] Substitute autocomplete box for select box in Work Reports. Hours
  type are now shown as a select box.
* [Bug #969] Refresh box of price per hour instead of doing a reloadBindings
  of the current row
* [Bug #969] Substitute autocomplete box for select box in Cost Categories.
  Hours type are now shown as a select box.
* Added method findItemByValue() to ComponentsFinder
* [Bug #911] Use method easier to understand
* [Bug #911] Declare the truly required type in Resource
* [Bug #911] Fix bug
* Revert "[Bug #911] Disabling at interface criterion hierarchy while bug is
  not fixed."
* [Bug #982] Fix bug ClassCastException on editing a Work Report
* [Bug #941] Check out if a quality form is not assigned twice to the same
  order element.
* Take into consideration that some days could not have day assignments
* Field must be renamed so ZK injects it
* [Bug #873] Show warning before add an empty label.
* Remove findSatisfyingAllCriterionsAtSomePoint from IResourceDAO
* Use IResourcesSearcher for searching resources associated with criterions
* Move and rename ResourceSearchModel to ResourcesSearcher
* The no longer valid allocations are marked as unsatisfied
* Fix variable name
* Remove wrong assert
* Use ResourcesSearchModel instead of IResourceDAO
* Remove unused field
* Remove unused method using IResourceDAO query by criteria capabilities
* Remove unused method
* Remove already present method in Resource
* Add simple README file to zk-Timeplot
* Bundle all needed files in timeplot.js
* Remove some generated artifacts from repository
* Fix infinite loops
* Use deploy script to install locally the new timeplotz-modified dependency
* Adding to repository original Timeplot-1.0_2
* [Bug #] Fix division by zero in Gantt
* Released 1.0.5 version.
* [i18n] Updated Spanish and Galician translations
* [i18n] Updated .pot files
* [Bug #876] Remove tree operation options (up, down, etc) for every entry in
  the tree of tasks in Template view
* [Bug #876] Refactor interface for handling operations in a tree (up, down,
  indent, unindent, etc)
* [Bug #877] ValidationException saving several templates at one time with the
  same name
* Revert "[Bug #876] Template tasks tree is not coherent with project details
  task tree"
* [Bug #876] Template tasks tree is not coherent with project details task tree
* [Bug #876] Swap columns 'Hours' and 'Must start after' in Template view to
  match view in Orders
* [Bug #931] Added helper class for printing a ValidatonExcepton as a
  WrongValidationException, showing up next to a widget
* [Bug #931] Rename method
* [Bug #931] Reuse invalidValue() method from ValidationException
* [Bug #961] Call to dontPoseAsTransientObjectAnymore for each transient object.
* [Bug #931] Added explicit check hour cost in 'Cost category' don't overlap
  for the same type of hours
* [Bug #965] Change field 'Date' to 'Receipt date' in OrderElement's Material
  tab
* [Bug #967] Transient instance editing task in WBS after set criterion and save
* [Bug #955] Perspectives column displays scroll when vertical space is not
  enough
* [Bug #962] Cancel action in 'Progress assignment' window leave the Task as
  it was before editing
* [Bug #930] Impossible to delete work hours type
* [Bug #942] Detect it can't fullfil the request duration beforehand
* [Bug #964] Impossible to delete materials not assigned to an order element
* [Bug #963] Fixed setting Comboitem value.
* [Bug #962] It is necessary the reattachment of the orderelement in order to
  get the consolidate values.
* [Bug #947] When closing OrderElement window, if there are errors in advance
  tab, show this tab with the error
* [Bug #947] check if not exists any indirect or direct advance when it
  changes advance type.
* Pull up asHoursModification
* [Bug #943] Fix bug
* [Bug #943] Avoid to reassign allocations with zero resources per day
* The tasks inside a task group are not being validated
* [Bug #943] Change error handling
* Fix OrderElementServiceTest
* [Bug #948] Updated legend colours
* [Bug #948] Improved graphs colour coherence
* [Bug #962] Cancel action in "Progress assignment" window leave the Task as
  it was before editing
* [Bug #962] Refactor fillVariables
* [Bug #962] Refactor addNewLineAdvanceAssignment
* [Bug #962] Avoid nulls in merging process
* [Bug #932] Check there are not overlapped category assignments, and if there
  are mark failing criterio assignment in form
* [Bug #937] Add event ON_OK in bandboxSearch in the correponding controllers.
* [Bug #960] When closing OrderElement window, if there are errors in other
  tabs, go to the tab with the error
* [Bug #958] Fix NullPointerException in progress assignment pop-up
* [Bug #946] Fixed LazyInitializationException in MonteCarloTab
* [Bug #883] delete the pop-up with "Project saved" when you create a new
  project
* [Bug #957] Fixed bug object references an unsaved transient instance
* [Bug #951] Change example URL in 'Edit company' window
* [Bug #954] Handle concurrency support in Configuration window
* [Bug #952] Show list of resources in 'Resource load view' in order
* [Bug #950] Planning mode combo filled several times
* [Bug #949] Translate values of ProgressType in Global Company View
* [Bug #949] Translate values of ProgressType in listbox of
  Configuration->'Show Progress'
* [Bug #946] Fix LazyInitializationException
* [Bug #942] Fixed tests that try to store a calendar with zero hours.
* [Bug #942] Modified creation of new calendars to use 8 hours for workable
  days by default.
* [Bug #942] Added constraint to avoid storing calendars with zero hours.
* [Bug #942] Added test to avoid store a calendar with zero hours.
* [Bug #942] Using configurationBootstrap in BaseCalendarDAOTest in order to
  run the test isolated.
* [Bug #942] Logging a warning when it's not calendar capacity in 5 years to
  calculate end date for a task.
* [Bug #943] Skipped test while issue is not finally closed.
* [Bug #885] Added single-click edition support to CRUD grids
* [Bug #885] Changed grid double-click grid listeners for edition to
  single-click
* [Bug #885] Changed appearance of single-click editable grids to be underlined
* Added missing @Override annotations and removed unneccesary casts
* Removed unneccesary parameter on LimitesResourcesPanel
* Added rounded corners to clicked icons in toolbar
* [Bug #943] Temporal fix, LOG error instead of launching exception
* [Bug #906] Fixed bug avoiding exception and using task dates if aggregate is
  empty.
* Fix typos in training guide (spanish version).
* Released 1.0.4 version.
* [i18n] Updated translation files and current translation.
* [Bug #880] Fixed bandboxSearch component widths for QualityForms finders
* [Bug #880] Fixed templateFinder listbox width
* [Bug #939] Fixed adding the same method also in TaskQualityFormItem.
* [i18n] Marked string to translate.
* [Bug #880] Fixed default width property
* [doc] Fixed several issues on training exercises.
* [Bug #883] Just after creating a project save it and go to edit mode.
* [Bug #789] Fixed enum not translated in quality forms edition.
* [Bug #939] Fixed adding new method to return a string for the position.
* [Bug #927] change standard way to manage this exception
* [Bug #874] it does not reallocate resources if resource per day has 0 hours.
* [Bug #889] Fixed changing styles of progress bars
* [Bug #789] Fixed string not market for translation "Save & New work report".
* [Bug #914] reset the class of the icon of expanded tree.
* [Bug #936] Fixed issue setting scale to 2 decimal figure, before converting
  in String.
* [Bug #935] query grouped by date
* [doc] Fixed small issues on training exercises.
* [Bug #934] Fixed problem with calendar creating a new resource.
* [i18n] Fixed wrong translation in Spanish string.
* [i18n] Small fixes in wrong strings.
* [i18n] Updated translations.
* [i18n] Updated .pot files.
* [Bug #933] Fixed translations in report "Work and progress per task".
* [doc] Removed auto-generated file in user documentation.
* [doc] Fixed wrong message on index of user documentation.
* [doc] Translated changes by Loren in commit b1b5b4 to other languages.
* [doc] Remove unneeded sentence (marked to remove) in introduction section of
  help.
* [doc] Fixed encoding in some files. Now all files have UTF-8.
* [Bug #875] Fixed subcontract service to create project also in Gantt view.
* [doc] Small changes to help main page
* [Bug #928] Add a button in the template list to allow deleting templates.
* [Bug #927] Improve the imputed hours tab in the order element view.
* [Bug #926] Add constraint to enable the report progress option in a quality
  form
* [Bug #925] Improve the report "cost by resource" with some changes:
* [Bug #921] Fixed problem avoiding possibility to save URL with white spaces.
* [Bug #921] Showing a proper error message if there are problems creating HTTP
  connection.
* [Bug #891] Fixed checking if a task is subcontracted and was already sent to
  subcontract.
* [doc] Increased font size on CSS to 0.8em.
* [Bug #909] Fixed critical path when dependencies have priority.
* [Bug #880] Fixed increasing templateFinder component widths
* [Bug #917] Fixed bug changing visibility of PlanningData default constructor
  to public.
* [doc] Small fix in use case development guide
* [doc] General revision of development guide.
* Removed jfreechart-igalia dependency as it was not needed anymore.
* Moved minutes and second labels in EffortDuration Picker to tooltipText
  attribute
* [Bug #901] Fixed showing limiting resources queues ordered alphabetically
* Added parameter to hide seconds from EffortDurationPicker component
* [Bug #915] Fixed correcting corner case in AdvancedAllocation horizontal
  paginator
* [doc] Added web services section to use case development guide.
* [doc] Added testing section in use case development guide.
* [Bug #898] Add column criterion type (worker or machine) in criterion types
  list.
* [Bug #924] Call method useSchedulingDataFor for each order.
* NavalPlan exercises for formation course in Spanish in reStructuredText.
* [doc] Added more sections to use case development guide.
* [Bug #884] Show progress measurements list in reverse order.
* [Bug #884] Sort progress measurements list in reverse order.
* [Bug #854] fix the labels in workingProgressPerTask report
* [Bug #923] control if the save command is initialized.
* [Bug #908] Total amount of allocated hours miscalculated in lineal
  interpolation (Streches function)
* [Bug #862] Changed generic button styling
* [Bug #862] Added custom images with button borders
* [Bug #858] Fixed container width properties in advanced allocation grid
* [Bug #918] Fixed issue reattaching and force loading of calendar.
* [Bug #920] clear old data in progress chart, before adding new data.
* [Bug #912] You cannot delete an derived exception.
* [Bug #916] Fixed issue checking if list of constraints is empty and not only
  if it is null.
* [Bug #913] NullPointerException exception changing between perspectives
* [Bug #911] Disabling at interface criterion hierarchy while bug is not fixed.
* [i18n] Updated translations.
* [Bug #910] Change string 'Type resource assignation' to 'Resource allocation
  type'
* NavalPlan formation manual in spanish in reStructuredText.
* [doc] Added information about interface in use case development guide.
* [Bug #892] Rename labels in 'Overall progress'
* [doc] Use case development guide.
* [Bug #903] Fix regression: creating a gap out of a LocalDate caused tasks
  may overlap due to loose of information about hour
* Some improvements in code
* If a LongOperation is executed inside another it's executed directly
* [Bug #903] Fix regression wrong calculation of gaps in limiting resources
* [Bug #890] Fixed marking to generate the entries in keys.pot.
* [Bug #871] Fixed problem calculating initial date.
* [Bug #871] Added more tests checking more constraints.
* [Bug #903] Limiting resource allocation window does not respect activation
  periods for the resources
* [Bug #871] Added more tests to confirm that the bug is fixed.
* [Bug #871] Fixed problem in critical path when dependencies are violated by
  constraints.
* [Bug #871] Detected problem with critical path calculator added test to
  explain it.
* [Bug #890] call to method of internationalization from the component.
* [Bug #888] show the chart of all advance assignments by default.
* [doc] Small fixes in reports guide to be ready to publish it.
* [Bug #887] Fix bug
* [Bug #887] Fix bug
* [Bug #887] Fix bug
* [doc] Added sections about how to filter report and send parameters to Jasper.
* [Bug #896] Fix bug
* [Bug #897] Fix bug
* [Bug #895] Fix bug
* Show summary of validation errors
* [Bug #892] Rename labels in 'Overall progress'
* Rename property 'default.passwords.control' to 'default.passwordsControl'
* [Bug #903] Limiting resource allocation window does not respect activation
  periods for the resources
* [Bug #900] Error doing generic allocation
* [Bug #841] Automatic generic assignment limiting resources raises nullpointer
  exception
* [Bug #899] Show tag GENERIC_MACHINES
* [doc] Adapted guide to last changes on reports.
* [reports] Changed order for project in report strings.
* [Bug #611] [reports] Fixed font issues in the rest of reports.
* [Bug #611] [reports] Set font styles in hoursWorkedPerWorkerReport.jrxml.
* [Bug #611] [reports] Review font definition in
  hoursWorkedPerWorkerReport.jrxml.
* Training material update
* [doc] Added info about model and extract database data to report guide.
* [doc] First version of basic tutorial to create a new report on NavalPlan.
* Added comment in main pom.xml to mark the point where bundle folders for
  reports are specified.
* Give more priority to the resources already picked
* Keep using the same AssignmentsAllocator
* No need to make it abstract
* Considerer the biggest assignment done in the last day
* Removed some unneeded test code in .zul files related with reports.
* Removed pencil files with interface prototypes.
* Removed unneeded dump from Git repository.
* First tries to assign all possible hours without using overtime
* Refactor GenericResourceAllocationTest
* Change method to use EffortDuration
* Not need to specify explictly a null calendar
* Rename classes
* Rename local variables so it's easier to understand
* Remove unused methods
* Add getCapacityWithOvertime to ICalendar
* Add min and max methods to Capacity
* Rename method so it's consistent
* Add method to multiply a Capacity
* Move method closer to call site
* Remove warnings from file
* [Bug #789] Fixed several translation issues lately detected.
* [Bug #867] Fix bug
* [Bug #867] Fix bug
* [Bug #867] Fix bug
* [Bug #847] Fix bug
* [Bug #881] Using always the same string for autogenerated codes.
* [Bug #861] Fix bug
* [Bug #879] Using inherited instead of indirect for criteria and labels.
* [Bug #878] Changed menu order in order to have coherency between menu and
  perspectives.
* [Bug #856] Failure when you close with the exit button 'X' in edition window
  of the order elements
* [Bug #855] Fix bug
* Apply validation annotation to the field
* [Bug #871] The problem is due to receiving an empty list of tasks
* [Bug #865] Fix bug
* [Bug #870] NavalPlan appears in English if browser is configured with es-es
* Fix bug create or edit resource should start on first tab
* [Bug #864] S-curve changes the end date of a task
* [Bug #868] It does not update the row of the order element correctly
* [Bug #866] Repeated project code message after saving an already saved project
* [Bug #863] Error changing strategy without resource assignment
* [Bug #859] Fix bug
* [Bug #859] Fix bug
* [Bug #844] Fix bug
* [Bug #857] Fix bug
* [Bug #845] Fix bug
* [Bug #884] fix the filtering to show only the current satisfied criteria.
* Added constraint to detect issue before saving wrong data.
* Add some columns to work report lines report
* Released 1.0.3 version.
* [Bug #848] Removed repeated method in OrderLineGroup.
* [Bug #851] fix bug
* Refactor
* [Bug #853] Fix bug
* [Bug #852] Fixed bug adding children advance if some indirect advance exists.
* [Bug #850] fix bug
* Added warning log message when fake advance is NULL in
  ManageOrderElementAdvancesModel.
* Added warning log message when fake advance is NULL in
  AdvanceAssignmentPlanningModel.
* [Bug #849] set the init date in with current date by default in cost category
  view
* [Bug #849] fix the behaviour of the dates in the cost category view
* [Bug #848] Fix bug
* Removed extra padding appearing next to floating calendars
* [Bug #814] Fix bug
* Avoid having Orders as proxies
* Move loadOrderAvoidingProxyFor to OrderModel
* [Bug #843] Fix bug
* Fixed nullpointer exception on work week date picker
* [Bug #842] Fix bug
* Avoid exception if effortAssigned is zero
* Fixed positioning issues on add assignment button
* Fixed bug with project deadline mark
* Revamped toolbar buttons
* Dotted line representing current day aligned to day middle
* Added save-and-continue action to calendar edition window
* Removed unused code
* Change the way it is represented the load chart at zoom level of week
* Change the way it is represented the load chart
* Change the way it is represented the load chart
* Remove unused field
* Use EffortDuration instead of hours to increase precision
* [Bug #840] Fix bug
* Expose methods to allow to get the EffortDuration elapsed in an interval of
  IntraDayDates
* [Bug #838] Fix bug
* Add method to allow to allocate hours outside the task's bounds
* Extract superclass and generalize the use of interval
* Rename onInterval method
* Rename methods
* Add documentation for method
* [Bug #838] Fix bug
* [Bug #837] Fix bug
* Replace "principal" by main
* Add related specific allocations second level
* Add new query for searching for specific allocations interfering with a
  criterion
* Pull up method and do it more generic
* Refactor genericAllocationsByCriterion
* Adapt LoadPeriodGeneratorOnCriterion so it can work with specific allocations
* Avoid executing methods twice
* Avoid repeating query
* Fixed visibility problems of disabled inputs on Chrome
* [Bug #805] Fix bug
* [Bug #819] Report internazionalization problem in webkit browsers
* [Bug #836] Fixed using English as default language
* fix the code style in db.changelog-1.0.xml and improve the comments in
  Configuration class.
* Make the change password link bolder
* Apply footer-messages-area to more top level elements
* Improve warning message
* [Bug #830] Remove unnecessary code
* [Bug #830] Changing in perspective does not reload data from database
* Removed enable_critical_chain_support from LiquiBase configuration.
* adds compiling option to disable the warning changing default password.
* Introduce the changes of data base in db.changelog-1.0.xml.
* Introduce warning for other predefined users about its default password.
* Add information in user list about if the user is administrator or not.
* Remove the warning of the need to change admin password.
* Introduce warning to change admin password
* [Bug #833] Fix bug
* [Bug #833] Disable advance search button
* [Bug #835] Fix bug
* Some fix on Debian package folder.
* [Bug #828] Recreate dependencies on chaning horizontal scroll in limiting
* On changing zoom in limiting resources dependencies are not redrawn
* Small fix in a message that uses "fiscal code" instead of ID.
* [Bug #824] Fixed issue checking worker ID prior to criterions.
* Avoid memory leak
* [Bug #825] Fix bug
* [Bug #829] Don't let user change the type of a dependency between a Limiting
  Task and any other type of Task
* [Bug #829] Fix bug initialize destination and origin tasks of dependencies if
  they were not initialized yet
* [Bug #826] Fix bug Validating Exception on allocating a resource to a limiting
  task the first time
* [Bug 827] Fix bug do appropriative allocation if required based on days
  constraints
* [Bug #826] For limiting tasks, update size of the task if the user changes the
  numbers of hours allocated of the resource allocated in that task
* [Bug #821] Fix bug
* Not allow to allocate on intervals beyond a task's bounds
* The cells after the end of the task are not editable
* Extract method in order to encapsulate the rule in one place
* Eliminate code repetition
* Fixing the condition for isBeforeTaskStartDate
* [Bug #816] Fix bug
* Show saving message after the changes have really been done
* Use Resource.getCaptionFor() for showing list of resources selected
* Select the first radio item of AssignmentType (not necessary
  GENERIC_ALL_WORKERS)
* Refactored constraintForResourcesPerDay and constraintForHoursInput
* [Bug #807] Revamped cell styles in advanced assignment
* Graphical issues in taskdetails component
* Fix javascript error when resizing ganttpanel and chart is not shown
* Changed image path for non-workable day shade
* [Bug #661] Fix bug for weeklevel
* Added shade image for non workable days on zoomlevel week
* Released 1.0.2 version.
* Fixed problem in Debian package install with some missing SQL sentences.
* [Bug #810] Fix bug
* Minor fix
* [Bug #817] Fix bug
* Added horizontal line under timetracker header
* [Bug #818] Exception launched on clicking 'Resource search' type of allocation
  (generic, specific) radio button
* Fix bug in 'Worker search' screen, close button was not working for limiting
  resources
* Fixed invalid timetracker's width for days
* Fixed problem with last LiquiBase change in MySQL.
* [Bug #815] Fix bug
* Add possibility of specifying new invalid dates
* Extract method
* Make canWork rely on Capacity data
* Use Capacity in BaseCalendar
* Make consufing method private
* Updated NavalPlan license headers to 2011.
* Refactored components allocation_search and allocation_search_component
* Added Resource.getCaptionFor()
* Replaced method IResourceSearchModel.byLimiting(boolean) with .byResourceType
  (ResourceType).
* Replaced the limitingResource attribute in Resource entity with resourceType.
* Small enhancement in CutyCapt command.
* Fixed problem with CSS not being used in printing.
* When creating the default calendar make weekend days not overassignable
* Ensure selected date is always not null
* [Bug #765] Fixes the position of the scroll bar in the resources load screen.
* [Bug #765] Fixes the position of the scroll bar in the limiting planning.
* [Bug #765] Fixes the position of the scroll bar in the gantt.
* [Bug #813] it corrects redirects in the editing virtual workers screen
* [Bug #812] changes Map for SortedMap in PredefinedDatabaseSnapshots
* Add Capacity Picker for edition of CalendarData
* Make CalendarData return Capacity instead of EffortDuration
* Refactor test
* Replace switch by method
* Remove parameter
* Fix parameter name
* CalendarData is updated receiving Capacity objects
* Remove unchecked casting warnings
* Remove unused method
* Remove use of java.util.Date for Calendar related entities
* Rename method to reflect better meaning
* Use CapacityPicker to edit the capacity of a CalendarException
* Allow CapacityPicker to work without bindings
* Make the methods for creating and adding exceptions receive Capacity
* Show extra effort column for CalendarException
* Move methods for getting efforts representations
* [Bug #796] Fixed. The problem is that the AdvanceMeasurement was still marked
  as transient.
* [Bug #811] Translate pending messages including string 'nif/NIF'
* [Bug #812] sorts the combo of multiple search in allocation pop-up.
* [Bug #797] checks out if obligatory data are introduced to create a new
  project.
* [Bug #797] checks out that deadline is greater than start date in project
  popup window.
* When doing an appropriative allocation, unschedule only enough elements for
  making room for the new position
* Remove reloadElementInQueue()
* When end is null (last gap) add 10 years from now
* Fix bug unschedule since date, if an element is already placed at that date
  unschedule it too
* Refactor code for allocating previously unscheduled elements
* Change behaviour for appropriative and non-appropriative allocations
* Schedule several unassigned queue elements at once
* Check if an automatic allocation should be appropriative or non-appropriative
* Calculate the latestEndDate where a limiting resource task could be allocated
* Use name for method more consistent with field name
* Use Capacity instead of EffortDuration for CalendarData
* Now CalendarException uses a Capacity instead of the field duration
* Change label to reflect better meaning
* [Bug #803] Refactor LimitingResourcesPanel
* [Bug #804] Fix bug
* [Bug #803] Fix bug dependencies were not being drawn again on changing zoom
  level in LimitingResources
* [Bug #809] Fixed marking to translate missing label.
* Added warning message to login screen for unsupported browsers
* Changed image for limiting resources tasks deadlines
* After moving tasks its violated depedendences are displayed with different
  style
* Refactored dependencies implementation to support different appearance based
  on CSS class
* Released 1.0.1 version.
* Updated TODO file with decisions from roadmap meeting.
* [Bug #803] Remove unused code
* [Bug #803] Change Zoom level in LimitingResources launches Validation
  exception
* [Bug #808] Fix bug when a resource field in Resource search is selected for
  the first time, set 'Current selection' as name of that resource
* [Bug #788] changes the structure and labels in resource load view.
* A new LiquiBase issue with MySQL again.
* Now fixing a issue added in previous commit 8d5c9dfe issue with PostgreSQL.
* Fixed LiquiBase issue with MySQL.
* Allow to edit the extra hours for CalendarExceptionType
* Create picker for Capacity
* Now CalendarExceptionType uses a Capacity instead of the field duration and
  notAssignable
* Added dependency with ttf-freefont on debian/control.
* [Bug #801] Fixed typo in Spanish translation.
* Substitute panel for groupbox
* Isolate 'Allocation Configuration' group box into a component, and embed it
  into 'Resource allocation'
* Isolate 'Task Information' group box into a component, and embed it into
  'Resource allocation' and 'Limiting Resource allocation'
* Create folder taskpanels and move all task panels related with 'Edit Task' to
  it
* [Bug #785] it has fixed translations of the changes in the company list.
* [Bug #785] Add improvements in companies list.
* [Bug #784] Sort results of add criterion combo in resource edition
* Reset the progressmeter to zero when the MonteCarlo chart is closed
* Fix the type parameter
* [Bug #800] Fix closable button in 'Calendar allocation' window
* [Bug #800] Fix closable button in 'Progress consolidation' window
* [Bug #800] Add closable button to 'Progress assignment' window
* [Bug #800] Add closable button to 'Project details' window
* [Bug #782] Fix bug
* Fix asyncNotifications
* [Bug #779] Fix bug
* Avoid LazyInitializationException accessing template's labels
* [Bug #798] Refactor 'Remove Criterion' window, change it for a MessageBox
  window
* [Bug #795] Refactor calendar remove dialog box and change it to a MessageBox


Version 1.0.6 (27 Apr 2011)
---------------------------

New minor version in stable branch due to several important bugs detected during
1.1 stabilization. The most important ones:

* Fix problems when tasks with zero hours are in critical path.
* Improve feedback to user when some error happens (add
  exception stacktrace).
* Solved issues removing entities related with others.
* Fixed sorting problems in resources load chart.
* Avoid creation of duplicate quality forms in tasks.

Changes
~~~~~~~

* [Bug #1030] Fixed opening read-only transaction in removeMaterialAssignment.
* [Bug #1026] Fix bug
* Inline silly method
* [Bug #952] Fix bug
* [Bug #992] Fix bug
* [Bug #990] When removing a profile check is not referenced by order
  authorizations
* [Bug #990] When removing a profile check is not referenced by other users
* [Bug #992] Fix bug
* [Bug #991] When removing an hours type check is not referenced by other
  entities
* [Bug #986] Prevent removing a label type that is being used by and
  orderelement
* [Bug #986] Prevent removing a label that is being used by an orderelement
* [Bug #986] Set cascade to none in mapping between Labels and OrderElements
* [Bug #986] Remove unused code
* [Bug #985] Fixed changing lables mapping to all-delete-orphan.
* [Bug #941] fixing bug :  Quality forms are not duplicated now in the same
  task.
* Fixed NullPointerException in PageForErrorOnEvent.
* Improved design of errors and fixed NullPointerException when clicking in
  continue.
* Improved information showed when some runtime error happens.
* [Bug #873] Show warning before add an empty label.
* [Bug #] Fix division by zero in Gantt


Version 1.0.5 (08 Apr 2011)
---------------------------

Last bugfixing version of NavalPlan for 1.0.x cycle, like in previous releases
it includes all the fixes done in the stable branch since 1.0.4. Among them we
would like to highlight:

* Resolved critical issue related to zero hours in resource allocations.
* Solved an important bug with regard to an infinite loop with zero hours in
  calendars.
* Fixed more issues manipulating tasks tree (WBS).

Changes
~~~~~~~

* [i18n] Updated Spanish and Galician translations
* [i18n] Updated .pot files
* [Bug #876] Refactor interface for handling operations in a tree (up, down,
  indent, unindent, etc)
* [Bug #877] ValidationException saving several templates at one time with the
  same name
* [Bug #876] Swap columns 'Hours' and 'Must start after' in Template view to
  match view in Orders
* [Bug #931] Added helper class for printing a ValidatonExcepton as a
  WrongValidationException, showing up next to a widget
* [Bug #931] Rename method
* [Bug #931] Reuse invalidValue() method from ValidationException
* [Bug #961] Call to dontPoseAsTransientObjectAnymore for each transient object.
* [Bug #931] Added explicit check hour cost in 'Cost category' don't overlap for
  the same type of hours
* [Bug #965] Change field 'Date' to 'Receipt date' in OrderElement's Material
  tab
* [Bug #967] Transient instance editing task in WBS after set criterion and save
* [Bug #955] Perspectives column displays scroll when vertical space is not
  enough
* [Bug #962] Cancel action in 'Progress assignment' window leave the Task as it
  was before editing
* [Bug #930] Impossible to delete work hours type
* [Bug #942] Detect it can't fullfil the request duration beforehand
* Fix CalendarData empty condition
* [Bug #964] Impossible to delete materials not assigned to an order element
* [Bug #963] Fixed setting Comboitem value.
* [Bug #962] It is necessary the reattachment of the orderelement in order to
  get the consolidate values.
* [Bug #947] When closing OrderElement window, if there are errors in advance
  tab, show this tab with the error
* [Bug #947] check if not exists any indirect or direct advance when it changes
  advance type.
* Pull up asHoursModification
* [Bug #943] Fix bug
* [Bug #943] Avoid to reassign allocations with zero resources per day
* The tasks inside a task group are not being validated
* [Bug #943] Change error handling
* Fix OrderElementServiceTest
* [Bug #948] Updated legend colours
* [Bug #948] Improved graphs colour coherence
* [Bug #962] Cancel action in "Progress assignment" window leave the Task as it
  was before editing
* [Bug #962] Refactor fillVariables
* [Bug #962] Refactor addNewLineAdvanceAssignment
* [Bug #962] Avoid nulls in merging process
* [Bug #932] Check there are not overlapped category assignments, and if there
  are mark failing criterio assignment in form
* [Bug #937] Add event ON_OK in bandboxSearch in the correponding controllers.
* [Bug #960] When closing OrderElement window, if there are errors in other
  tabs, go to the tab with the error
* [Bug #958] Fix NullPointerException in progress assignment pop-up
* [Bug #946] Fixed LazyInitializationException in MonteCarloTab
* [Bug #883] delete the pop-up with "Project saved" when you create a new
  project
* [Bug #957] Fixed bug object references an unsaved transient instance
* [Bug #951] Change example URL in 'Edit company' window
* [Bug #954] Handle concurrency support in Configuration window
* [Bug #952] Show list of resources in 'Resource load view' in order
* [Bug #950] Planning mode combo filled several times
* [Bug #949] Translate values of ProgressType in Global Company View
* [Bug #949] Translate values of ProgressType in listbox of Configuration->'Show
  Progress'
* [Bug #946] Fix LazyInitializationException
* [Bug #942] Logging a warning when it's not calendar capacity in 5 years to
  calculate end date for a task.
* [Bug #943] Skipped test while issue is not finally closed.
* [Bug #943] Temporal fix, LOG error instead of launching exception
* [Bug #906] Fixed bug avoiding exception and using task dates if aggregate is
  empty.
* Fix typos in training guide (spanish version).


Version 1.0.4 (17 Mar 2011)
---------------------------

A new bugfixing version of NavalPlan, including a lot of bugs fixed during this
month.

* Resolved some stability problems appearing under certain circumstances.
* Managed previously unhandled corner cases, that were raising uncaught
  exceptions.
* Fixed several issues manipulating tasks tree (WBS).
* Solved problems for users with read-only permissions.
* Minor improvements on usability.

Changes
~~~~~~~

* [i18n] Updated translation files and current translation.
* [Bug #880] Fixed bandboxSearch component widths for QualityForms finders
* [Bug #880] Fixed templateFinder listbox width
* [Bug #939] Fixed adding the same method also in TaskQualityFormItem.
* [i18n] Marked string to translate.
* [Bug #880] Fixed default width property
* [doc] Fixed several issues on training exercises.
* [Bug #883] Just after creating a project save it and go to edit mode.
* [Bug #789] Fixed enum not translated in quality forms edition.
* [Bug #939] Fixed adding new method to return a string for the position.
* [Bug #927] change standard way to manage this exception
* [Bug #874] it does not reallocate resources if resource per day has 0 hours.
* [Bug #914] reset the class of the icon of expanded tree.
* [Bug #889] Fixed changing styles of progress bars
* [Bug #789] Fixed string not market for translation "Save & New work report".
* [Bug #936] Fixed issue setting scale to 2 decimal figure, before converting in
  String.
* [Bug #935] query grouped by date
* [doc] Fixed small issues on training exercises.
* [Bug #934] Fixed problem with calendar creating a new resource.
* [i18n] Fixed wrong translation in Spanish string.
* [i18n] Small fixes in wrong strings.
* [i18n] Updated translations.
* [i18n] Updated .pot files.
* [Bug #933] Fixed translations in report "Work and progress per task".
* [doc] Removed auto-generated file in user documentation.
* [Bug #875] Fixed subcontract service to create project also in Gantt view.
* [Bug #928] Add a button in the template list to allow deleting templates.
* [Bug #927] Improve the imputed hours tab in the order element view.
* [Bug #926] Add constraint to enable the report progress option in a quality
  form
* [Bug #925] Improve the report "cost by resource" with some changes:
* [Bug #921] Fixed problem avoiding possibility to save URL with white spaces.
* [Bug #921] Showing a proper error message if there are problems creating HTTP
  connection.
* [Bug #891] Fixed checking if a task is subcontracted and was already sent to
  subcontract.
* [Bug #909] Fixed critical path when dependencies have priority.
* [Bug #880] Fixed increasing templateFinder component widths
* [Bug #917] Fixed bug changing visibility of PlanningData default constructor
  to public.
* Moved minutes and second labels in EffortDuration Picker to tooltipText
  attribute
* [Bug #901] Fixed showing limiting resources queues ordered alphabetically
* Added parameter to hide seconds from EffortDurationPicker component
* [Bug #915] Fixed correcting corner case in AdvancedAllocation horizontal
  paginator
* [Bug #898] Add column criterion type (worker or machine) in criterion types
  list.
* [Bug #924] Call method useSchedulingDataFor for each order.
* NavalPlan exercises for formation course in Spanish in reStructuredText.
* [Bug #884] Show progress measurements list in reverse order.
* [Bug #884] Sort progress measurements list in reverse order.
* [Bug #854] fix the labels in workingProgressPerTask report
* [Bug #923] control if the save command is initialized.
* [Bug #908] Total amount of allocated hours miscalculated in lineal
  interpolation (Streches function)
* [Bug #862] Changed generic button styling
* [Bug #862] Added custom images with button borders
* [Bug #858] Fixed container width properties in advanced allocation grid
* [Bug #918] Fixed issue reattaching and force loading of calendar.
* [Bug #920] clear old data in progress chart, before adding new data.
* [Bug #919] You cannot delete an derived exception.
* [Bug #916] Fixed issue checking if list of constraints is empty and not only
  if it is null.
* [Bug #913] NullPointerException exception changing between perspectives
* [Bug #911] Disabling at interface criterion hierarchy while bug is not fixed.
* [i18n] Updated translations.
* [Bug #910] Change string 'Type resource assignation' to 'Resource allocation
  type'
* NavalPlan formation manual in spanish in reStructuredText.
* [Bug #892] Rename labels in 'Overall progress'
* [Bug #903] Fix regression: creating a gap out of a LocalDate caused tasks may
  overlap due to loose of information about hour
* If a LongOperation is executed inside another it's executed directly
* [Bug #903] Fix regression wrong calculation of gaps in limiting resources
* [Bug #890] Fixed marking to generate the entries in keys.pot.
* [Bug #871] Fixed problem calculating initial date.
* [Bug #871] Added more tests checking more constraints.
* [Bug #903] Limiting resource allocation window does not respect activation
  periods for the resources
* [Bug #890] call to method of internationalization from the component.
* [Bug #871] Added more tests to confirm that the bug is fixed.
* [Bug #871] Fixed problem in critical path when dependencies are violated by
  constraints.
* [Bug #871] Detected problem with critical path calculator added test to
  explain it.
* [Bug #888] show the chart of all advance assignments by default.
* [Bug #887] Fix bug
* [Bug #887] Fix bug
* [Bug #887] Fix bug
* [Bug #896] Fix bug
* Remove warning
* [Bug #897] Fix bug
* [Bug #895] Fix bug
* Show summary of validation errors
* [Bug #892] Rename labels in 'Overall progress'
* [Bug #903] Limiting resource allocation window does not respect activation
  periods for the resources
* [Bug #900] Error doing generic allocation
* [Bug #841] Automatic generic assignment limiting resources raises nullpointer
  exception
* [Bug #899] Show tag GENERIC_MACHINES
* [reports] Changed order for project in report strings.
* [Bug #611] [reports] Fixed font issues in the rest of reports.
* [Bug #611] [reports] Set font styles in hoursWorkedPerWorkerReport.jrxml.
* [Bug #611] [reports] Review font definition in
  hoursWorkedPerWorkerReport.jrxml.
* Training material update
* [Bug #789] Fixed several translation issues lately detected.
* [Bug #867] Fix bug
* [Bug #867] Fix bug
* [Bug #867] Fix bug
* [Bug #867] Fix bug
* [Bug #847] Fix bug
* [Bug #881] Using always the same string for autogenerated codes.
* [Bug #861] Fix bug
* Rename method to make it clearer
* [Bug #879] Using inherited instead of indirect for criteria and labels.
* [Bug #878] Changed menu order in order to have coherency between menu and
  perspectives.
* [Bug #856] Failure when you close with the exit button 'X' in edition window
  of the order elements
* [Bug #855] Fix bug
* Apply validation annotation to the field
* [Bug #871] The problem is due to receiving an empty list of tasks
* [Bug #870] NavalPlan appears in English if browser is configured with es-es
* Fix bug create or edit resource should start on first tab
* [Bug #864] S-curve changes the end date of a task
* [Bug #868] It does not update the row of the order element correctly
* [Bug #866] Repeated project code message after saving an already saved project
* [Bug #863] Error changing strategy without resource assignment
* [Bug #865] Fix bug
* [Bug #859] Fix bug
* [Bug #859] Fix bug
* [Bug #844] Fix bug
* [Bug #857] Fix bug
* [Bug #845] Fix bug
* [Bug #884] fix the filtering to show only the current satisfied criteria.


Version 1.0.3 (16 Feb 2011)
---------------------------

This version include all fixed done since previous release. In the following
list the most important ones are highlighted.

* Advanced allocation screen. Disable start/end date changes in
  advance allocation window. Graph dependencies are not travelled in this window
  yet, so as temporary fix, it will not be allowed to change the start or the
  end date of a task in this screen.
* Limiting resources. Several fixes on limiting resources planning window.
  Moreover fixed Gantt view dependencies using limiting resources tasks.
* Allocation window. Improvements to avoid misunderstandings using the
  interface.

Changes
~~~~~~~

* [Bug #848] Removed repeated method in OrderLineGroup.
* [Bug #851] fix bug
* [Bug #852] Fixed bug adding children advance if some indirect advance exists.
* Refactor
* [Bug #853] Fix bug
* [Bug #850] fix bug
* Added warning log message when fake advance is NULL in
  ManageOrderElementAdvancesModel.
* Added warning log message when fake advance is NULL in
  AdvanceAssignmentPlanningModel.
* [Bug #849] set the init date in with current date by default in cost category
  view
* [Bug #849] fix the behaviour of the dates in the cost category view
* [Bug #848] Fix bug
* Removed extra padding appearing next to floating calendars
* [Bug #814] Fix bug
* [Bug #843] Fix bug
* Fixed nullpointer exception on work week date picker
* [Bug #842] Fix bug
* Avoid exception if effortAssigned is zero
* Remove unused field
* Use EffortDuration instead of hours to increase precision
* [Bug #840] Fix bug
* Expose methods to allow to get the EffortDuration elapsed in an interval of
  IntraDayDates
* Fixed visibility problems of disabled inputs on Chrome
* [Bug #805] Fix bug
* [Bug #838] Fix bug
* Add method to allow to allocate hours outside the task's bounds
* Extract superclass and generalize the use of interval
* Rename onInterval method
* Rename methods
* Add documentation for method
* [Bug #838] Fix bug
* Replace "principal" by main
* Add related specific allocations second level
* Add new query for searching for specific allocations interfering with a
  criterion
* Pull up method and do it more generic
* Refactor genericAllocationsByCriterion
* Adapt LoadPeriodGeneratorOnCriterion so it can work with specific allocations
* Avoid executing methods twice
* Avoid repeating query
* [Bug #837] Fix bug
* [Bug #819] Report internazionalization problem in webkit browsers
* [Bug #836] Fixed using English as default language
* [Bug #830] Remove unnecessary code
* [Bug #830] Changing in perspective does not reload data from database
* [Bug #833] Fix bug
* [Bug #833] Disable advance search button
* [Bug #835] Fix bug
* Some fix on Debian package folder.
* [Bug #828] Recreate dependencies on chaning horizontal scroll in limiting
* On changing zoom in limiting resources dependencies are not redrawn
* Small fix in a message that uses "fiscal code" instead of ID.
* [Bug #824] Fixed issue checking worker ID prior to criterions.
* Avoid memory leak
* [Bug #825] Fix bug
* [Bug #829] Don't let user change the type of a dependency between a Limiting
  Task and any other type of Task
* [Bug #826] Fix bug Validating Exception on allocating a resource to a limiting
  task the first time
* [Bug #826] For limiting tasks, update size of the task if the user changes the
  numbers of hours allocated of the resource allocated in that task
* [Bug #821] Fix bug
* Not allow to allocate on intervals beyond a task's bounds
* The cells after the end of the task are not editable
* Extract method in order to encapsulate the rule in one place
* Eliminate code repetition
* Fixing the condition for isBeforeTaskStartDate
* [Bug #816] Fix bug
* Show saving message after the changes have really been done
* [Bug #807] Revamped cell styles in advanced assignment
* Graphical issues in taskdetails component
* Fix javascript error when resizing ganttpanel and chart is not shown
* [Bug #817] Fix bug
* Added horizontal line under timetracker header


Version 1.0.2 (21 Jan 2011)
---------------------------

Bugfixing release due to a problem with database installation that makes Debian
package not work if it was newly installed in a system. Apart from fixing this
important bug, this version also includes other fixes done since 1.0.1 release.

Notes
~~~~~

If you are upgrading from any 1.0.x verion, you will need to manually execute on
your database the SQL sentences from file:
``/usr/share/dbconfig-common/data/navalplan/upgrade/pgsql/1.0.0``

To get database access information review ``/etc/tomcat6/context.xml`` file.

Changes
~~~~~~~

* Fixed problem in Debian package install with some missing SQL sentences.
* Fix bug in 'Worker search' screen, close button was not working for limiting
  resources
* [Bug #815] Fix bug
* Small enhancement in CutyCapt command.
* Fixed problem with CSS not being used in printing.
* [Bug #813] it corrects redirects in the editing virtual workers screen
* [Bug #812] changes Map for SortedMap in PredefinedDatabaseSnapshots
* [Bug #812] sorts the combo of multiple search in allocation pop-up.
* [Bug #796] Fixed. The problem is that the AdvanceMeasurement was still marked
  as transient.
* [Bug #811] Translate pending messages including string 'nif/NIF'
* [Bug #797] checks out if obligatory data are introduced to create a new
  project.
* [Bug #797] checks out that deadline is greater than start date in project
  popup window.
* [Bug #803] Refactor LimitingResourcesPanel
* [Bug #803] Fix bug dependencies were not being drawn again on changing zoom
  level in LimitingResources
* [Bug #809] Fixed marking to translate missing label.
* Fixed wrong e-mail on debian/changelog.

Version 1.0.1 (14 Jan 2011)
---------------------------

Bugfixing release due to a missing dependency on Debian packages that make
reports do not work on 1.0.0. Apart from fixing the Debian package other fixes
done during last week are also included in this release.

Changes
~~~~~~~

* Updated TODO file with decisions from roadmap meeting.
* [Bug #803] Remove unused code
* [Bug #803] Change Zoom level in LimitingResources launches Validation
  exception
* [Bug #804] Fix bug
* [Bug #788] changes the structure and labels in resource load view.
* [Bug #808] Fix bug when a resource field in Resource search is selected for
  the first time, set 'Current selection' as name of that resource
* [Bug #785] fixes translations in company list.
* Added dependency with ttf-freefont on debian/control.
* [Bug #801] Fixed typo in Spanish translation.
* [Bug #785] Add improvements in companies list.
* [Bug #784] Sort results of add criterion combo in resource edition
* Reset the progressmeter to zero when the MonteCarlo chart is closed
* [Bug #800] Fix closable button in 'Calendar allocation' window
* [Bug #800] Fix closable button in 'Progress consolidation' window
* [Bug #800] Add closable button to 'Progress assignment' window
* [Bug #800] Add closable button to 'Project details' window
* [Bug #782] Fix bug
* Fix asyncNotifications
* Fix the type parameter
* [Bug #779] Fix bug
* Avoid LazyInitializationException accessing template's labels
* [Bug #798] Refactor 'Remove Criterion' window, change it for a MessageBox
  window
* [Bug #795] Refactor calendar remove dialog box and change it to a MessageBox


Version 1.0.0 (07 Jan 2011)
---------------------------

This is the first stable release of *NavalPlan*, a free software web
application for project management.

Main features
~~~~~~~~~~~~~

* Multiproject management. It offers a global vision of the company managing
  several projects sharing resources.
* Group resource allocations: dynamic groups based on criteria.
* Flexible calendars.
* Configurable Gantt chart from Work Breakdown Structure (WBS).
* Resource Breakdown Structure (RBS) chart.
* Overload resource allocation control.
* Earned Value Management (EVM).
* Cost analysis based on work reports.
* Integration with other *NavalPlan* instances and third-parties.
* Other functionalities: materials, quality forms, project templates, planning
  scenarios, multiple task progress measurement ...

Latest features
~~~~~~~~~~~~~~~

* Backwards planning with new restrictions (AS_LATE_AS_POSSIBLE and
  FINISH_NOT_LATER_THAN).
* Two direction resource allocation schemes. Forwards and backwards strategies
  supported depending on Gantt conditions.
* Time unit inferior to hours in calendars. Minutes and seconds are allowed.
* Experimental support for limiting resources.
* Monte Carlo technique for project duration simulation. Duration probability
  functions are calculated based on likelihood estimations.
* Sigmoid advance resource allocation function approaching what happens in real
  world.
* Monitorization of project progress based on critical path progresses.
* Human readable codes generation for data.
* Configurable display of advances and cost in planning tasks.
* Database migration support. Automatic management of database refactorings
  keeping former data.
