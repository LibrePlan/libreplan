Project Dashboard
#################

.. contents::

The project dashboard is a *LibrePlan* perspective that contains a set of **KPIs (Key Performance Indicators)** to help assess a project's performance in terms of:

   *   Work progress
   *   Cost
   *   Status of allocated resources
   *   Time constraints

Progress Performance Indicators
===============================

Two indicators are calculated: project progress percentage and task status.

Project Progress Percentage
---------------------------

This chart displays the overall progress of a project, comparing it to the expected progress based on the *Gantt* chart.

Progress is represented by two bars:

   *   *Current Progress:* The current progress based on the measurements taken.
   *   *Expected Progress:* The progress the project should have achieved at this point, according to the project plan.

To view the actual measured value for each bar, hover the mouse cursor over the bar.

The overall project progress is estimated using several different methods, as there is no single, universally correct approach:

   *   **Spreading Progress:** This is the progress type set as the spreading progress at the project level. In this case, there is no way to calculate an expected value, and only the current bar is displayed.
   *   **By All Task Hours:** The progress of all project tasks is averaged to calculate the overall value. This is a weighted average that considers the number of hours allocated to each task.
   *   **By Critical Path Hours:** The progress of tasks belonging to any of the project's critical paths is averaged to obtain the overall value. This is a weighted average that considers the total allocated hours for each involved task.
   *   **By Critical Path Duration:** The progress of tasks belonging to any of the critical paths is averaged using a weighted average, but this time considering the duration of each involved task instead of the assigned hours.

Task Status
-----------

A pie chart shows the percentage of project tasks in different states. The defined states are:

   *   **Finished:** Completed tasks, identified by a 100% progress value.
   *   **In Progress:** Tasks that are currently underway. These tasks have a progress value other than 0% or 100%, or some work time has been tracked.
   *   **Ready to Start:** Tasks with 0% progress, no tracked time, all their *FINISH_TO_START* dependent tasks are *finished*, and all their *START_TO_START* dependent tasks are *finished* or *in progress*.
   *   **Blocked:** Tasks with 0% progress, no tracked time, and with previous dependent tasks that are neither *in progress* nor in the *ready to start* state.

Cost Indicators
===============

Several *Earned Value Management* cost indicators are calculated:

   *   **CV (Cost Variance):** The difference between the *Earned Value curve* and the *Actual Cost curve* at the current moment. Positive values indicate a benefit, and negative values indicate a loss.
   *   **ACWP (Actual Cost of Work Performed):** The total number of hours tracked in the project at the current moment.
   *   **CPI (Cost Performance Index):** The *Earned Value / Actual Cost* ratio.

        *   > 100 is favorable, indicating that the project is under budget.
        *   = 100 is also favorable, indicating that the cost is right on plan.
        *   < 100 is unfavorable, indicating that the cost of completing the work is higher than planned.
   *   **ETC (Estimate To Complete):** The time remaining to complete the project.
   *   **BAC (Budget At Completion):** The total amount of work allocated in the project plan.
   *   **EAC (Estimate At Completion):** The manager's projection of the total cost at project completion, based on the *CPI*.
   *   **VAC (Variance At Completion):** The difference between the *BAC* and the *EAC*.

        *   < 0 indicates that the project is over budget.
        *   > 0 indicates that the project is under budget.

Resources
=========

To analyze the project from the resources' point of view, two ratios and a histogram are provided.

Estimation Deviation on Completed Task Histogram
------------------------------------------------

This histogram calculates the deviation between the number of hours allocated to the project tasks and the actual number of hours dedicated to them.

The deviation is calculated as a percentage for all finished tasks, and the calculated deviations are represented in a histogram. The vertical axis shows the number of tasks within each deviation interval. Six deviation intervals are dynamically calculated.

Overtime Ratio
--------------

This ratio summarizes the overload of resources allocated to the project tasks. It is calculated using the formula: **overtime ratio = overload / (load + overload)**.

   *   = 0 is favorable, indicating that the resources are not overloaded.
   *   > 0 is unfavorable, indicating that the resources are overloaded.

Availability Ratio
------------------

This ratio summarizes the free capacity of the resources currently allocated to the project. Therefore, it measures the resources' availability to receive more allocations without being overloaded. It is calculated as: **availability ratio = (1 - load/capacity) * 100**

   *   Possible values are between 0% (fully assigned) and 100% (not assigned).

Time
====

Two charts are included: a histogram for the time deviation in the finish time of project tasks and a pie chart for deadline violations.

Task Completion Lead or Lag
---------------------------

This calculation determines the difference in days between the planned end time for project tasks and their actual end time. The planned completion date is taken from the *Gantt* chart, and the actual finish date is taken from the last time tracked for the task.

The lag or lead in task completion is represented in a histogram. The vertical axis shows the number of tasks with a lead/lag day difference value corresponding to the abscissa days interval. Six dynamic task completion deviation intervals are calculated.

   *   Negative values mean finishing ahead of schedule.
   *   Positive values mean finishing behind schedule.

Deadline Violations
-------------------

This section calculates the margin with the project deadline, if set. Additionally, a pie chart shows the percentage of tasks meeting their deadline. Three types of values are included in the chart:

   *   Percentage of tasks without a deadline configured.
   *   Percentage of ended tasks with an actual end date later than their deadline. The actual end date is taken from the last time tracked for the task.
   *   Percentage of ended tasks with an actual end date earlier than their deadline.
