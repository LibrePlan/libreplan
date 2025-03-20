Hours Worked by Resource Report
###############################

.. contents::

Purpose
=======

This report extracts a list of tasks and the time resources have dedicated to them within a specified period. Several filters allow users to refine the query to obtain only the desired information and exclude extraneous data.

Input Parameters and Filters
============================

* **Dates**.
    * *Type*: Optional.
    * *Two date fields*:
        * *Start Date:* This is the earliest date for work reports to be included. Work reports with dates earlier than the *Start Date* are excluded. If this parameter is left blank, work reports are not filtered by *Start Date*.
        * *End Date:* This is the latest date for work reports to be included. Work reports with dates later than the *End Date* are excluded. If this parameter is left blank, work reports are not filtered by *End Date*.

*   **Filter by Workers:**
    *   *Type:* Optional.
    *   *How it works:* You can select one or more workers to restrict the work reports to the time tracked by those specific workers. To add a worker as a filter, search for them in the selector and click the *Add* button. If this filter is left empty, work reports are retrieved regardless of the worker.

*   **Filter by Labels:**
    *   *Type:* Optional.
    *   *How it works:* You can add one or more labels to use as filters by searching for them in the selector and clicking the *Add* button. These labels are used to select the tasks to be included in the results when calculating the hours dedicated to them. This filter can be applied to timesheets, tasks, both, or neither.

*   **Filter by Criteria:**
    *   *Type:* Optional.
    *   *How it works:* You can select one or more criteria by searching for them in the selector and then clicking the *Add* button. These criteria are used to select the resources that satisfy at least one of them. The report will show all the time dedicated by the resources that meet one of the selected criteria.

Output
======

Heading
-------

The report heading displays the filters that were configured and applied to the current report.

Footer
------

The date on which the report was generated is listed in the footer.

Body
----

The report body consists of several groups of information.

*   The first level of aggregation is by resource. All the time dedicated by a resource is shown together below the header. Each resource is identified by:

    *   *Worker:* Surname, First Name.
    *   *Machine:* Name.

    A summary line shows the total number of hours worked by the resource.

*   The second level of grouping is by *date*. All the reports from a specific resource on the same date are shown together.

    A summary line shows the total number of hours worked by the resource on that date.

*   The final level lists the work reports for the worker on that day. The information displayed for each work report line is:

    *   *Task Code:* The code of the task to which the tracked hours are attributed.
    *   *Task Name:* The name of the task to which the tracked hours are attributed.
    *   *Starting Time:* This is optional. It is the time at which the resource began working on the task.
    *   *Ending Time:* This is optional. It is the time at which the resource finished working on the task on the specified date.
    *   *Text Fields:* This is optional. If the work report line has text fields, the filled values are shown here. The format is: <Name of the text field>:<Value>
    *   *Labels:* This depends on whether the work report model has a label field in its definition. If there are multiple labels, they are shown in the same column. The format is: <Name of the label type>:<Value of the label>
