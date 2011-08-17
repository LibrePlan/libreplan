Hours Worked By Resource Report
###############################

.. contents::

Purpose
=======

This report allows to extract a list of the tasks and time devoted to them by workers in a period of time. There are several filters which allow users to adjust the query to get just the wanted information and avoiding extra data.

Input paratemers and filters
============================

   * **Dates**.
      * *Type*: Optional.
      * *Two date fields*:
         * *Start Date.* This is the minimum date of the work reports which are wanted. Work reports with an earlier date than the *start date* are ignored. If this parameter is not filled, there is not inferior filtering date.
         * *End Date.* This is the maximum date of the work reports which will be included in the results. Work reports with a later date than the *end date* will be skipped. If this parameter is not filled, there is not upper limit of the work reports filtered.

   * **Filter by workers**
      * *Type*: Optional.
      * *How it works*: You can select one worker to restrict the work reports to the time tracking of that particular worker. If you leave it empty, the work reports are retrieved independently of the worker.

   * **Filter by labels**
      * *Type:* Optional.
      * *How it works*: You can add one or several labels by searching them in the selector and by pressing the *Add* button to be used as filter. They are used to select the tasks that will be included in the results to compute the hours devoted in them.

   * **Filter by criteria**
      * *Type:* Optional.
      * *How it works:* You can select one or several criteria by searching them in the selector and, then, by clicking on the *Add* button. These criteria are used to select the resources that satisfy at least one of them. The report will show all the time devoted by the resources satisfying one of the criteria of the filtering.

Output
======

Heading
-------

In the heading of the report is informed about the filters that were configured and that were applied in the current report extract.

Foot page
---------
The date in which the repot was got is showed.

Body
----

The body of the report consists of several groups of information.

* There is a first aggretation of information per resource. All the time devoted by a resource is showed together below the header. Each resource it is identified by:

   * *Worker*: Surname, Firstname
   * *Machine*: Name.

It is showed a summing-up line with the total number of hours worked by the resource.

* There is a second grouping level consisting of the *date*. All the reports coming from a concrete resource at the same date are showed together.

There is a summing-up line with the total number of hours worked by the resource.

* There is a last level at which there are listed the work reports belonging to the same day for the worker. The information which is displayed for each work report line is:

   * *Task code* the tracked hours impute.
   * *Task name* the tracked hours impute.
   * *Starting time*. It is not mandatory. It is the starting time at which the resource began doing the work tracked in the task.
   * *Ending time*. It is not mandatory. It is the ending time until which the resource works in the task at the date specified.
   * *Textfields*. It is optional. If the work report line has text fields the filled values, they are showed here. The format is: <Name of the text field>:<Value>
   * *Labels*. It depends on if the work report model has a label field in its definition. If there are several labels they are showed in the same column. The format is: <Name of the label type>:<Value of the label>
