Project dashboard
#################

.. contents::

The project dashboard is a *LibrePlan* perspective which contains a set of **KPI (Key Performance Indicators)**
which help to know how is performing a project according to:

   * how the work is progressing 
   * how much is costing
   * the status of the resources allocated
   * the time constraints

Progress performance indicators
===============================

There are two indicators calculated: project progress percentage and task
status.

Project progress percentage
---------------------------

It is a chart where the global progress of a project is calculated and compared to
the expected value of progress the project should have according to the *Gantt*.

The progress is represented with two bars:

   * *Current progress*. It is the progress existing at the moment according to
     the measurements done.
   * *Expected progress*. It is the progress the project should have at the
     moment according to the planning created.

The project global progress is estimated in several different ways as there
is not a unique right method to do it:

   * **Spreading progress**. It is the progress type set as spreading progress
     at project level. In this case, there is not way to calculate an expected
     value and only the current bar is displayed.
   * **By all tasks hours**. The progress of all project tasks is averaged to
     calculate the global value. It is a weighted average taking into account
     the number of hours allocated at each task.
   * **By critical path hours**.  The progress of the tasks belonging to any of
     the critical paths of the project is averaged to obtain the global value.
     It is done a weighted average taking into account the total allocated hours
     of each of the involved tasks.
   * **By critical path duration**. The progress of the tasks belonging to any of
     the critical paths is averaged by doing a weighted average but this time taking
     into account the duration of each involved task instead of the assigned
     hours.

Task status
-----------

A pie chart showing the percentage of the project tasks being at different
states. The defined states are the next ones:

   * **Finished**. They are the completed tasks, detected by a 100% progress value measured.
   * **In progress**. They are the tasks which are underway. They have a
     progress value distinct from 0% and 100% or some worked time tracked.
   * **Ready to start**. They have a progress of 0%, do not have time tracked, all their *FINISH_TO_START* dependent tasks are *finished* 
     and all their *START_TO_START* dependent tasks are *finished* or *in progress*.
   * **Blocked**. These are the tasks with a 0% of progress, without time
     tracked  and with previous dependent tasks neither *in progress* not in *ready to start* state.

Cost indicators
===============

There are several *Earned Value Management* cost indicators calculated:

   * **CV (Cost Variance)**. Difference between the *Earned Value curve* and the
     *Actual Cost curve* at the moment. Positive values indicate benefit and
     negative ones loss.
   * **ACWP (Actual Cost Work Performed)**. It is the total number of hours tracked in the project at the
     moment.
   * **CPI (Cost Performance Index)**. It is the *Earned Value / Actual
     Cost* ratio.

     * > 100 is good, means to be under budget.
     * = 100 is also good, means the cost is right on plan.
     * < 100 is bad, means that the cost of completing the work is higher than
       planned.

   * **ETC (Estimate To Complete)**. It is the time that is pending to devote
      to the project to finish it.
   * **BAC (Budget At Completion)**. It is the total amount of work allocated
      in the project plan.
   * **EAC (Estimate At Completion)**. It is the manager projection of the total
      cost at project completion time according to the *CPI*.
   *  **VAC (Variance At Completion)**. It is the difference between the *BAC*
      and the *ETC*. 

      * < 0 is over budget.
      * > 0 is under budget.

Resources
=========

To analyze the project from resources' point of view two ratios and a histogram
are provided.

Estimation deviation on completed task histogram
------------------------------------------------

It is calculated the deviation between the number of hours allocated to the
project tasks and the eventual number of hours dedicated to them.

The deviation is calculated in percentage for all the finished tasks and the
calculated deviations are represented in a histogram. In the vertical axis the number of tasks which are in an 
interval of deviation is shown. Six deviation intervals are dynamically calculated.

Overtime ratio
--------------

It sums up the overload of the resources that are allocated in the project tasks.
It is calculated according to the formula: **overtime ratio = overload / (load +
overload)**.

   *  = 0 is good, meaning that the resources are not overloaded.
   *  > 0 is bad, meaning that the resources are overloaded.

Availability ratio
-------------------

It sums up the capacity that is free in the resources currently allocated to the
project. Therefore it is a measurement of the resource availability to receive more allocations without
being overloaded. It is calculated as: **availability ratio = (1 - load/capacity)*100**

   * Possible values are between 0% (fully assigned) and 100 (not assigned).

Time
====

They are included two charts: a histogram for the time deviation in the finish
time of project tasks and a pie chart for the deadline violations.

Task completion lead or lag
---------------------------

It is calculated the difference in days between the planned end time for the project
tasks and their actual end time. The planned completion date is got from the
*Gantt* and the actual finish date is got from the last time tracked at the task.

The lag or lead in task completion is represented in a histogram. In the
vertical axis the number of tasks with a lead/lag day difference value
corresponding to the abscissa days interval is represented. Six
dynamic task completion deviation intervals are calculated.

   * Negative values mean finishing ahead of time.
   * Positive values mean finishing with a delay.

Deadline violations
-------------------

On one hand it is calculated the margin with the project deadline, if set. On the other hand a pie chart with
the percentage of tasks hitting their deadline is painted. Three types of values
are included in the chart:

   * Percentage of task without deadline configured.
   * Percentage of ended tasks with an actual end date later than their
     deadline. The actual end date is got from the last time tracked in the
     task.
   * Percentage of ended tasks with an actual end date sooner than their

