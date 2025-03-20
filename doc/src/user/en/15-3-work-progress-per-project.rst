Work and Progress per Project Report
####################################

.. contents::

Purpose
=======

This report provides an overview of the status of projects, considering both progress and cost.

It analyzes the current progress of each project, comparing it with the planned progress and the work completed.

The report also displays several ratios related to project cost, comparing current performance with the planned performance.

Input Parameters and Filters
============================

There are several mandatory parameters:

   *   **Reference Date:** This is the date used as a reference point for comparing the planned status of the project with its actual performance. *The default value for this field is the current date*.

   *   **Progress Type:** This is the progress type used to measure project progress. The application allows a project to be measured simultaneously with different progress types. The type selected by the user in the dropdown menu is used for calculating the report data. The default value for the *progress type* is *spread*, which is a special progress type that uses the preferred method of measuring progress configured for each WBS element.

The optional parameters are:

   *   **Starting Date:** This is the earliest start date for projects to be included in the report. If this field is left blank, there is no minimum start date for the projects.

   *   **Ending Date:** This is the latest end date for projects to be included in the report. All projects that finish after the *Ending Date* will be excluded.

   *   **Filter by Projects:** This filter allows users to select the specific projects to be included in the report. If no projects are added to the filter, the report will include all projects in the database. A searchable dropdown menu is provided to find the desired project. Projects are added to the filter by clicking the *Add* button.

Output
======

The output format is as follows:

Heading
-------

The report header displays the following fields:

   *   **Starting Date:** The filtering start date. This is not displayed if the report is not filtered by this field.
   *   **Ending Date:** The filtering end date. This is not displayed if the report is not filtered by this field.
   *   **Progress Type:** The progress type used for the report.
   *   **Projects:** This indicates the filtered projects for which the report is generated. It will show the string *All* when the report includes all projects that satisfy the other filters.
   *   **Reference Date:** The mandatory input reference date selected for the report.

Footer
------

The footer displays the date on which the report was generated.

Body
----

The body of the report consists of a list of projects selected based on the input filters.

Filters work by adding conditions, except for the set formed by the date filters (*Starting Date*, *Ending Date*) and the *Filter by Projects*. In this case, if one or both date filters are filled and the *Filter by Projects* has a list of selected projects, the latter filter takes precedence. This means that the projects included in the report are those provided by the *Filter by Projects*, regardless of the date filters.

It's important to note that progress in the report is calculated as a fraction of unity, ranging between 0 and 1.

For each project selected for inclusion in the report output, the following information is displayed:

   * *Project Name*.
   * *Total Hours*. The total hours for the project are shown by adding the hours for each task. Two types of total hours are shown:
      *   *Estimated (TE)*. This is the sum of all the estimated hours in the project's WBS. It represents the total number of hours estimated to complete the project.
      *   *Planned (TP)*. In *LibrePlan*, it's possible to have two different quantities: the estimated number of hours for a task (the number of hours initially estimated to complete the task) and the planned hours (the hours allocated in the plan to complete the task). The planned hours can be equal to, less than, or greater than the estimated hours and are determined in a later phase, the assignment operation. Therefore, the total planned hours for a project are the sum of all the allocated hours for its tasks.
   * *Progress*. Three measurements related to the overall progress of the type specified in the progress input filter for each project at the reference date are shown:
      *   *Measured (PM)*. This is the overall progress considering the progress measurements with a date earlier than the *Reference Date* in the input parameters of the report. All tasks are taken into account, and the sum is weighted by the number of hours for each task.
      *   *Imputed (PI)*. This is the progress assuming that work continues at the same pace as the hours completed for a task. If X hours out of Y hours for a task are completed, the overall imputed progress is considered to be X/Y.
      *   *Planned (PP)*. This is the overall progress of the project according to the planned schedule at the reference date. If everything happened exactly as planned, the measured progress should be the same as the planned progress.
   * *Hours up to Date*. There are two fields that show the number of hours up to the reference date from two perspectives:
      *   *Planned (HP)*. This number is the sum of the hours allocated to any task in the project with a date less than or equal to the *Reference Date*.
      *   *Actual (HR)*. This number is the sum of the hours reported in the work reports for any of the tasks in the project with a date less than or equal to the *Reference Date*.
   * *Difference*. Under this heading, there are several metrics related to cost:
      *   *Cost*. This is the difference in hours between the number of hours spent, considering the measured progress, and the hours completed up to the reference date. The formula is: *PM*TP - HR*.
      *   *Planned*. This is the difference between the hours spent according to the overall measured project progress and the number planned up to the *Reference Date*. It measures the advantage or delay in time. The formula is: *PM*TP - HP*.
      *   *Cost Ratio*. This is calculated by dividing *PM* / *PI*. If it is greater than 1, it means that the project is profitable at this point. If it is less than 1, it means that the project is losing money.
      *   *Planned Ratio*. This is calculated by dividing *PM* / *PP*. If it is greater than 1, it means that the project is ahead of schedule. If it is less than 1, it means that the project is behind schedule.
