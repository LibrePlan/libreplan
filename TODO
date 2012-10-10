TODO
====

Version 1.4 - 2013w01
---------------------

Dates
~~~~~

* *IRC coordination meeting*: 2012w41
* *Feature freeze*: 2012w50
* *Release date*: 2013w01

Features
~~~~~~~~

* **Top priority**

  * Add a *filter by project status* in the projects screens.

  * Add a *quicksearch to find projects* easier in the projects screens.

  * *Right mouse menu register in the left menu of the project planning perspective*: It would be very useful and we have received the feedback of some users that it would be great to have this feature. Also the double-click over the project or task name.

  * *Protect the upper menu from abandoning the planning of a project without saving*: This feature was not completed in release 1.2 but had been decided to do so in `analysis mail <https://sourceforge.net/mailarchive/message.php?msg_id=27691109>`_.

    It's also important review the behavior of advanced allocation window, we should prevent people change perspective without clicking "Apply" or even ask if they want to "Apply" if the click directly on "Save".

  * Improve *documentation*

* **Medium priority**

  * *Import projects* from Microsfot Project and Planner.

  * Review possible *performance* issues in order to look for some improvements.

  * Add a *new project status* called ``pre-sales``.

  * *JIRA* integration

  * *Show the load of the resources to be assigned in allocation pop-up*: This feature was in the roadmap for release 1.2 but there was not enough time to to it, so it is pending.

    It consists of being able to query and to represent graphically the load of the resource allocation. This will consiste of a load chart in the period which affects the task duration.

    It would be interesting to be able to view the load of each resource allocation before being done and after it:

     * *Before being done*: In the advanced search pop-up you could see a small chart for each resource or for the set of criteria being selected to the allocation.

     * *After being done*: In the allocation table a link to open a pop-up with a chart which shows the load of the resource would be included.


Future & wish list
------------------

* **Scheduling module**

  * Save KPIs in database in order to wrap up the project status.

  * Add a pipeline page to show projects grouped by status.

  * *Allow to introduce minutes in WBS screen*: This is the last planning information which is not possible to insert in minutes. So, this task consists of allowing it.

    Very close to this feature and maybe included in it would be to allow to specify quantities in a text format like: 8d7h

  * *Improve allocation strategy calculate number of hours and calculate resources per day*: Currently is a bit complex to use this allocation strategies on using the workable days. This happens because you do not have control over the end date but over the workable days. This task will consists of translating from start/end date to workable days taking into account the task calendar.

  * *Flaw when changing start/end date of project, or when changing the START_NOT_SOONER_THAN constraint from WBS*: If  you change the start date of a project already planned this start date  affects the start date of the tasks planned with constraint  AS_SOON_AS_POSSIBLE. The same happens with the end date of a project and  the constraint AS_LATER_AS_POSSIBLE.

    This  fact provokes that if you change the start date of a project and you  visit the gantt window, the Gantt window replans the project to enforce  the constraints. This provokes that the user sees data that is not the  state of things stored in the database. However, the user is not  informed of this circumstance.

    The same happens with the end date if there are tasks with AS_LATER_AS_POSSIBLE.

    There  is another weird behavior with START_NOT_SOONER_THAN constraint of a  task if it is introduced in the WBS. If the task is still not planned  then the associated task in the Gantt is configured with a  START_NOT_SOONER_THAN constraint. If it has allocations then the  constraint is not added. Reflect about this.

    There  is another problem when you manipulate the WBS and there are already  tasks planned with dependencies or allocations. Currently when certain  movements are done you lose the dependencies and the allocations but the  user is not even informed or asked. It should be provided a way to avoid this undesirable situation.

  * *Establishing dependencies from pop-up (form based interface)*: Now the only way to set up dependencies between activities is by dragging the arrow from the origin task and by releasing it in the destination task.

    This feature would consist of implementing a text based interface to managing dependencies. Two major advantages:

    * When tasks are very distant to establish them is difficult and time consuming.
    * This is a step to allow to split up big Gantt charts in several pages.

  * *Dependencies with lag*: This is a feature asked by the users (community). Instead affecting immediately the start date or the end date of the origin task of the dependency, this behavior would let configure an amount of time to add.

  * *Templates with planning information*

  * *Labels with colors*: Labels with colors in order to distinguish them easily in the Gantt chart.

  * *Fixing and improving Monte Carlo simulation*: A mail was sent time ago about `how to fix and do a better Monte Carlo simulation <https://sourceforge.net/mailarchive/message.php?msg_id=27666797>`_.

  * *Fix consolidation model*: An analysis mail was sent explaining `how to fix and improve the consolidation model <https://sourceforge.net/mailarchive/message.php?msg_id=28565283>`_.

  * *Complete the configuration unit mechanism for machines*: Several analysis stories not started have been written for this:

    * `AnA17S01ConfigurationUnitInterfaceCorrections <http://wiki.libreplan.org/twiki/bin/view/LibrePlan/AnA17S01ConfigurationUnitInterfaceCorrections>`_
    * `AnA17S02TakingIntoAccountDerivedDayAssigments <http://wiki.libreplan.org/twiki/bin/view/LibrePlan/AnA17S02TakingIntoAccountDerivedDayAssigments>`_
    * `AnA17S03EnforceDerivedDayAssignmentsWithAllocations <http://wiki.libreplan.org/twiki/bin/view/LibrePlan/AnA17S03EnforceDerivedDayAssignmentsWithAllocations>`_

  * *Allow to reassign just one task*

  * *Allow to modify task start and end dates in advanced allocation*: Now in advanced allocation you cannot change the start or the end date of a task by filling hours before the beginning or after the end respectively. This is not allowed because the Gantt graph and then reallocation engine has not been plugged into that screen.

  * *Limiting resources enforced*: Several things are pending to have a complete solution of limiting resources:

    * Avoid dependencies going from regular tasks to limiting tasks.

    * Avoid dependencies going from limiting tasks to regular tasks.

    * Change interface of limiting resource to allow several things:

      * Filter by time. It is needed.
      * Allow to set tasks as finished (progresses). View it and mark it.
      * By default filter: Filter the tasks that have not been finished.  Establish a vertical line catching only tasks in the middle or not  started one.

  * *Printing*: It is needed to keep on improving printing. Results are not enough satisfactory:

    * Specify layout to make it faster and aspect more accurate for printed version o a schedule.

    * Review if we could avoid fake xserver stuff

  * *New load chart scheme to view and analyze the load*: Now the resource load screen is in two dimensions en a color:

    * Dimensions: Resource, Time

    * Color: Load

    A better system would be to represent the load with a new axis. A row  per resource. Inside each row two dimensions: vertical axis for load and  horizontal axis for time. In this way, you get more accuracy in the representation of the load of each resource through time.

    Current system is besides inaccurate because it is based on the average  load a task. Therefore a better scheme based on the load per day.

  * *Visualization of task states in Gantt*: Show the state of the tasks in the planning. We can know if a task has  begun, it is waiting, ready, finished. Have an option to show this.

  * *Improve allocation model*:

    * Allow to specify a different strategy per allocation row.

    * Allow to specify an allocation function per allocation row:

      * Flat (by default).

      * S-curve.

      * Stretched flat function.

    * Allow to change the start date, end date of each allocation row  (task start-end calculated dynamically taking this into account).

* **Usability  module**

  * Make WBS screen more user-friendly with more keyboard shortcuts and some other thins like columns for dependencies and so on.

  * *User experience improvements*: Improve user experience and aspect of some pages like the report ones (capturing input data ones). The idea is making the interaction with LibrePlan smoother, putting the focus on the newly created cells, easing the work just with the keyboard (the "name: input : Add button" system, that allows creating several elements without needing to point-n-click on things is an example).

  * *Icons*: Improve icons and ask for them (possible integration with icons of the web).

  * *Upper menu revamp*: We have received the feedback that the upper menu is not understood many times correctly because it has the style of tabs.

  * *Menu breadcrumbs*: Proposed to  be removed or at least being made consistent.

  * *Improve error detection at perspective changes*: Improve the detection of errors and prevent to do a perspective change on planning a project.

  * *Default viewing information for Gantt projects*: This task would consist of including configuration parameters per project or globally to configure the viewing options in the Gantt set on by default. So, instead of having to activate them on entering the project each time, it will be enabled as wished by default by the user.

* **Architectural tasks**

  * *Save and back button created just in one place*: Currently save and back buttons that appear while you're editing a project are created from 2 perspectives: Gantt and WBS. This makes us to remove the options to go from general Gantt directly to resources load or advanced allocation perspectives. This should be modified and these buttons should be created just in one place allowing us to enable again that features.

  * *Performance enhancements*:

    * Performance enhancements in Gantt window.
    * Performance enhancements in resource load window.
    * Task to think how to manage data with time.

  * *Allow workers to ask for holidays*: Allow workers -if the profile is created- to ask for holidays. Each worker has a calendar assigned, and he/she could ask for the vacation days. Then, a reviewer -project manager- could confirm or deny that vacation days.

  * Include the groups feature:

    * A group or department will be composed by a list of resources.
    * Add filters to show only projects/resources belonging to a group.

  * *Refactor entity model for templates*: This was part of release 1.2 but was not started yet. There are some internal issues in the implementation of templates.

* **Other**

  * Add CSV import/export for projects.

  * Print Gantt chart in PDF format.

  * *Android application*: Develop an Android application to allow work report adding as a first approach to make LibrePlan work from smartphones.

    It would use the LibrePlan webservices to add the work reports. Of course this should be improved and it would make many more things in the future, but as a first approach seems to be ok.

  * Add export/import operations for different format files from other projects like OpenProj, MicrosoftProject, ...

  * Base line in projects.

  * Resource leveling. Intraproject approach.

  * Hierarchical projects.

  * Resource load view performance improvement.

  * Dependency type START-TO-FINISH.

  * Periodic allocation schemes.

  * Earned value improvements.

  * Critical chain project management critical chain paradigm in project planning.

  * Interface to resolve allocation conflicts.

  * Increase support of intraday operations in allocations regarding planning dates.

  * Calendars with intraday timetable.

  * Quick start wizard.

  * Integration services scripts directly with Java.

  * Review dependencies with libraries and upgrade to newer versions.

  * Historic information management.

  * Cash-flow analysis.

  * New outsourcing capabilities.

  * Scenario system enhancements.

  * More reports. Improve them with charts.

  * Improve advanced allocation with decomposition of load of generic allocations

  * Inclusion of portfolio management operations

  * Paginate the Gantt diagram configuring the number of task per page.

  * Import/export data from CSV format.

  * New KPIs and implementation of global (multi-project) KPI.

  * Customization (e.g. recently opened projects in menu).

  * Application theming. Possibility to customize the styles.

  * Database snapshot with example data.

  * Unit tests for integration services using truly HTTP requests and XML files.

  * Auto-deployable executable. Application is downloaded and  installed with JNLP (Java Web Start) and on starting launches an  embedded servlet container in which it is executed. It will include a  pure Java database like HSQLDB too.

  * Notification system.

  * Explorer and Opera support.

  * Experimental rendering with canvas for Gantt and resource load.

  * Interruptible tasks.

  * Configurable reports.

  * Implement risk management

  * Document management in projects.

  * LibrePlan Control Center product. New application to manage several LibrePlan deployments in a large

  * Segmentation of projects per portfolio.

  * Automatic allocation of set of projects of a unit by several criteria. Tabu search algorithm.

  * Workflow processes implementation or BPM. For instance, needed to review tracked hours to be incorporated to the projects.

  * Mobile applications to introduce some data like work report (track time).

  * New UI whole redisign. New app theme.

  * Budgeting module. Benefit analysis.

  * Operations management support. There are tasks or departments inside a company which are not projects. They are regular tasks which are done periodically (vacations, incidences,...).

  * Integration or development of ticket systems associated to tasks.
