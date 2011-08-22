Work and progress per project report
####################################

.. contents::

Purpose
=======

This report lets you know find out which is the overall status of the projects taking into account two variables: the progress and the cost.

It is analyzed the current status of project progress comparing it with the forseen one according to planning and according to the work devoted.

It is also showed several ratios related to the project cost comparing current performance with the theorical one.

Input parameters and filters
============================

There are several mandatory parameters. They are:

   * **Reference date**. It is the date which is wanted to have as reference to make the comparison with the planned foreseen status of the project at that date with the real performance of the project at that date according. *The default value for this field is the current date*.

   * **Progres type**. It is the progress type it is wanted to be used to measure the projects progress. In the application a project can be measured simultaneously with different progress types, and the one selected with the pulldown component by the user is the one used for calculating the report data. The default value for the *progress type* is *spread*, which is a special progress type consisting of using the preferred way of measuring the progress configured in each WBS element.

With regard to the optional fields, they are the following:

   * **Starting date**. It is the minimum starting date of the projects to be included in the report. It is optional. If no starting time is filled, there is not minimum date for the projects.

   * **Ending date**. It is the maximum ending date of the projects to be included in the report. All the projects which finish after the *Ending date* will be excluded.

   * **Filter by projects**. This filter allows to select the set of projects to which the user wants to limit the report data to be extracted. If no projects are added to the filter, the report is shown for all the projects in the database. There is a searchable pulldown to find out the wanted project. They are added to the filter by pushing the *Add* button.

Output
======

The format of the output is the following:

Heading
-------

In the report header the following fields are showed:

   * **Starting date**. The filtering starting date field. It is not showed if the report is not filtered by this field.
   * **Ending date**. The filtering ending date field. It is not showed if the report is not filtered by this field.
   * **Progress type**. The progress type used for the report.
   * **Projects**. It informs about the filtered projects for which the report is being got. It will show the string *All* when the report is got for all the projects satisfying the rest of filters.
   * **Reference date**. The mandatory input reference date selected to extract the report.

Foot page
---------

It is showed the date in which the report has been got.

Body
----

The body are consists of a list of projects which has been selected as result of the input filters.

Filters work adding conditions except the set formed by the date filters (*Starting date*, *Ending date*) and the *filter by projects*. In this case, if one or the two date filters are filled and the *filter by projects* has a list of selected projects at the same this, this last filter is the one that commands the filter. This means that the projects that are included in the report are the provided by the *filter by projects* independently of the date filters.

Another important thing are that the progress in the report are calculated giving as a fraction of unity. They are between 0 and 1.

For each project selected to be included in the report output, the following information is showed:

   * *The project name*.
   * *Total hours*. The total hours of the project are showing by adding the hours of each of task. Two total hours types are showed:
      * *Estimated (TE)*. This quantity is the addition of all the hours in the WBS of the project. They are the total number of hours in which a project is estimated to be completed.
      * *Planned (TP)*. In *LibrePlan* it is possible to have two different quantities. The estimated number of hours of a task, which is the number of hours that in advance are needed to do the task, and the planned hours, which are the hours allocated in the plan to do the task. The planned hours can be equal, less or more than the estimated hours and are decided in a later phase, the assignment operation. So, the total planned hours of a project is the addition of all the allocated hours of its tasks
   * *Progress*. Three measurements related to the global progress of the type specified in the progress input filter for each project at the reference date are showed:
      * *Measured (PM)*. It is the the global progress considering the progress measurements with a date less than the *reference date* in the input parameters of the report. Besides, all the task are taken into account and the addition is weighted but the number of hours of each task.
      * *Imputed (PI)*. This is the progress considering that the work goes on at the same pace as the hours devoted in a task. If X hours out of Y hours of a task are done, it is considered that the global imputed progress is X/Y.
      * *Planned (PP)*.This is the global progress of the project according to the theorical planning at the reference date. If things hapened exactly as planned the measured progress should be the same as the planned progress.
   * *Hours up to date*.There are two field that show the number of hours until the reference date from two points of view:
      * *Planned (HP)*. This number is the addition of the hours allocated in any task of the project with a date less or equal to the *reference date*.
      * *Real (HR)*. This number is the addition of the hours reported in the work reports to any of the tasks of the project with a date less or equal to the *reference date*.
   * *Difference*. Under this title there are several meters related to the cost:
      * *Cost*. It is the difference in hours between the number of hours spent taking into account the progress measured and the hours devoted until the the reference date. Formula is: *PM*TP - HR*.
      * *Planned*. It is the difference between the hours spent according the global project measured and the number planned till the *reference date*. It measures the advantage or delay in time. Formula es: *PM*TP - HR*.
      * *Cost ratio*. It is calculated dividing the *PM* / *PI*. If it is greater than 1, it means that the project is with benefits at this point and if it es less than 1, it means that the project is losing money.
      * *Planned ratio*. It is calculated dividing the *PM* / *PP*. If it is greater than 1, it means that the project is ahead of time and it is less than 1 that the project is with delay.Work and progress per project report
