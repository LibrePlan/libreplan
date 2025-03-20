Scheduler
#########

.. contents::

The scheduler is designed to schedule jobs dynamically. It is developed using the *Spring Framework Quartz scheduler*.

To use this scheduler effectively, the jobs (Quartz jobs) that should be scheduled must be created first. Then, these jobs can be added to the database, as all jobs to be scheduled are stored in the database.

When the scheduler starts, it reads the jobs to be scheduled or unscheduled from the database and schedules or removes them accordingly. Afterward, jobs can be added, updated, or removed dynamically using the ``Job scheduling`` user interface.

.. NOTE::
   The scheduler starts when the LibrePlan web application starts and stops when the application stops.

.. NOTE::
   This scheduler supports only ``cron expressions`` to schedule jobs.

The criteria that the scheduler uses to schedule or remove jobs when it starts are as follows:

For all jobs:

* Schedule

  * Job has a *Connector*, and the *Connector* is activated, and the job is allowed to be scheduled.
  * Job has no *Connector* and is allowed to be scheduled.

* Remove

  * Job has a *Connector*, and the *Connector* is not activated.
  * Job has a *Connector*, and the *Connector* is activated, but the job is not allowed to be scheduled.
  * Job has no *Connector* and is not allowed to be scheduled.

.. NOTE::
   Jobs cannot be rescheduled or unscheduled if they are currently running.

Job Scheduling List View
========================

The ``Job scheduling list`` view allows users to:

*   Add a new job.
*   Edit an existing job.
*   Remove a job.
*   Start a process manually.

Add or Edit Job
===============

From the ``Job scheduling list`` view, click:

*   ``Create`` to add a new job, or
*   ``Edit`` to modify the selected job.

Both actions will open a create/edit ``job form``. The ``form`` displays the following properties:

*   Fields:

    *   **Job group:** The name of the job group.
    *   **Job name:** The name of the job.
    *   **Cron expression:** A read-only field with an ``Edit`` button to open the ``cron expression`` input window.
    *   **Job class name:** A ``pull-down list`` to select the job (an existing job).
    *   **Connector:** A ``pull-down list`` to select a connector. This is not mandatory.
    *   **Schedule:** A checkbox to indicate whether to schedule this job.

*   Buttons:

    *   **Save:** To save or update a job in both the database and the scheduler. The user is then returned to the ``Job scheduling list view``.
    *   **Save and continue:** The same as "Save," but the user is not returned to the ``Job scheduling list view``.
    *   **Cancel:** Nothing is saved, and the user is returned to the ``Job scheduling list view``.

*   And a hint section about cron expression syntax.

Cron Expression Pop-up
----------------------

To enter the ``cron expression`` correctly, a ``cron expression`` pop-up form is used. In this form, you can enter the desired ``cron expression``. See also the hint about the ``cron expression``. If you enter an invalid ``cron expression``, you will be notified immediately.

Remove Job
==========

Click the ``Remove`` button to delete the job from both the database and the scheduler. The success or failure of this action will be displayed.

Start Job Manually
==================

As an alternative to waiting for the job to run as scheduled, you can click this button to start the process directly. Afterward, the success or failure information will be displayed in a ``pop-up window``.
