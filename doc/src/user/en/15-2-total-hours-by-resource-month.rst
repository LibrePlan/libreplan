Total Hours Worked by Resource in a Month Report
################################################

.. contents::

Purpose
=======

This report provides the total number of hours worked by each resource in a given month. This information can be useful for determining worker overtime or, depending on the organization, the number of hours for which each resource should be compensated.

The application tracks work reports for both workers and machines. For machines, the report sums up the number of hours they were in operation during the month.

Input Parameters and Filters
============================

To generate this report, users must specify the year and month for which they want to retrieve the total number of hours worked by each resource.

Output
======

The output format is as follows:

Header
------

The report header displays:

   *   The *year* to which the data in the report pertains.
   *   The *month* to which the data in the report pertains.

Footer
------

The footer displays the date on which the report was generated.

Body
----

The data section of the report consists of a single table with two columns:

   *   One column labeled **Name** for the name of the resource.
   *   One column labeled **Hours** with the total number of hours worked by the resource in that row.

There is a final row that aggregates the total number of hours worked by all resources during the specified *month* and *year*.
