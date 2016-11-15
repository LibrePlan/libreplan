Scheduler
#########

.. contents::

The scheduler is designed to schedule Jobs dynamically. It is developed with the help of *Spring framework Quartz scheduler*.

In order to use this scheduler effectively the Jobs(Quartz jobs) that should be scheduled must be created first. Then these 
jobs could be added to the database as all jobs to be scheduled is stored in the database. 

When the scheduler first starts, it reads the jobs to be scheduled/unscheduled from the database and schedule/remove 
them accordingly. Afterwards jobs can be added/updated or removed dynamically using ``Job scheduling`` user interface.

.. NOTE::
   The scheduler starts when the Libreplan web application starts and stops when the application stops.

.. NOTE::
   this scheduler supports only ``cron expressions`` to schedule the jobs.

The criteria that the scheduler uses to schedule/remove the jobs when it first starts:
For all jobs:

* Schedule

  * Job has a *Connector* and the *Connector* is activated and a Job is allowed to be scheduled
  * Job has no *Connector* and is allowed to be scheduled

* Remove

  * Job has a *Connector* and the *Connector* is not activated
  * Job has a *Connector* and the *Connector* is activated but Job is not allowed to be scheduled
  * Job has no *Connector* and is not allowed to be scheduled   

.. NOTE::
   Jobs can not be re-scheduled/unscheduled if they are currently running
   
Job scheduling list view
========================
The ``job scheduling list`` view allows users to

* add a new Job
* edit an existing Job
* remove a Job
* start a process manually

Add or Edit Job
===============
From the ``job scheduling list`` view, click

* ``Create`` button to add a new Job or 
* ``Edit`` button to modify the chosen Job.

Both actions will lead you to a create/edit ``job form``. The ``form`` displayed the following properties:

* Fields:

  * Job group: name of the job group
  * Job name: name of the job
  * Cron expression: read only field and an ``Edit`` button to open ``cron expression`` input window
  * Job class name: ``pull-down list`` to select your Job(an existing job)
  * Connector: ``pull-down list`` to select a connector. This is not mandatory
  * Schedule: check box whether you want to schedule this job or not

* Buttons:

  * Save: to save/update a Job both in database and in the scheduler. The user is then back to the ``Job scheduling list view``
  * Save and continue: the same as save above, only user is not back to the ``Job scheduling list view``
  * Cancel: nothing saved and user is back to ``Job scheduling list view`` 

* And a hint about cron expression syntax 

Cron expression pop-up
---------------------- 
In order to enter the ``cron expression`` correctly a ``cron expression`` pop-up form is used. In this form you can enter
the desired ``cron expression``. See also the hint about the ``cron expression``. In case you enter a wrong ``cron expression``, 
you will be directly notified that the ``cron expression`` you entered is illegal.   

Remove Job
==========
Click the button ``Remove`` to delete the job both from the database and the scheduler. The success/failure info of this action
will be shown. 

Start Job Manually
==================
As an alternative to wait until the Job is run as scheduled by the scheduler, you can click this button to start the 
process directly. Afterwards the success/failure info will be shown in ``pop-up window``.