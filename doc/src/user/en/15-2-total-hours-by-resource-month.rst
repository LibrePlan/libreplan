Total hours worked by resource in a month report
################################################

.. contents::

Purpose
=======

This reports allows to get the total number of worked hours by the resources in a month. This can be useful to know the overtime a worker did or, depending of the organization, the quantity of hours that have to be paid to each resource.

The application allows to track work reports for the workers and for the machines. According to this, the report in the case of machines sums up the number of hours they were functioning in a month.

Input parameters and filters
============================

The report needs the user specify the year and month to get the total number of hours that the resources worked.

Output
======

The format of the output is the following:

Header
------

In the header of the report it is showed:

   * The *year* to which the data in the report belong.
   * The *month* to which the data in the report belong.

Foot page
---------

The date in which the report was asked to be obtained.

Body
----

The data area of the report consists of just one section at which a table with two columns is shown:

   * One column called **Name** for the name of the resource.
   * One column called **Hours** with the addition of all the hours devoted by the resource the row is of.

There is a final line aggregating the total of the hours devoted by any resource in the *month*, *year* the report is about.
