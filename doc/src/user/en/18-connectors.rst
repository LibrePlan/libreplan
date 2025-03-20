Connectors
##########

.. contents::

Connectors are *LibrePlan* client applications that can be used to communicate with (web) servers to retrieve data, process it, and store it. Currently, there are three connectors: the JIRA connector, the Tim Enterprise connector, and the E-mail connector.

Configuration
=============

Connectors must be configured properly before they can be used. They can be configured from the "Main Settings" screen under the "Connectors" tab.

The connector screen includes:

*   **Pull-down list:** A list of available connectors.
*   **Properties edit screen:** A property edit form for the selected connector.
*   **Test connection button:** A button to test the connection with the connector.

Select the connector you want to configure from the pull-down list of connectors. A property editor form for the selected connector will be displayed. In the property editor form, you can change the property values as needed and test your configurations using the "Test connection" button.

.. NOTE::

   The properties are configured with default values. The most important property is "Activated." By default, it is set to "N." This indicates that the connector will not be used unless you change the value to "Y" and save the changes.

JIRA Connector
==============

JIRA is an issue and project tracking system.

The JIRA connector is an application that can be used to request JIRA web server data for JIRA issues and process the response. The request is based on JIRA labels. In JIRA, labels can be used to categorize issues. The request is structured as follows: retrieve all issues that are categorized by this label name.

The connector receives the response, which in this case is the issues, and converts them to *LibrePlan* "Order elements" and "Time sheets."

The *JIRA connector* must be configured properly before it can be used.

Configuration
-------------

From the "Main Settings" screen, choose the "Connectors" tab. In the connectors screen, select the JIRA connector from the pull-down list. A property editor screen will then be displayed.

In this screen, you can configure the following property values:

*   **Activated:** Y/N, indicating whether you want to use the JIRA connector. The default is "N."
*   **Server URL:** The absolute path to the JIRA web server.
*   **User name and password:** The user credentials for authorization.
*   **JIRA labels: comma-separated list of labels or URL:** You can either enter the label URL or a comma-separated list of labels.
*   **Hours type:** The type of work hours. The default is "Default."

.. NOTE::

   **JIRA labels:** Currently, the JIRA web server does not support providing a list of all available labels. As a workaround, we have developed a simple PHP script that performs a simple SQL query in the JIRA database to fetch all distinct labels. You can either use this PHP script as the "JIRA labels URL" or enter the labels you want as comma-separated text in the "JIRA labels" field.

Finally, click the "Test connection" button to test if you can connect to the JIRA web server and that your configurations are correct.

Synchronization
---------------

From the project window, under "General data," you can start synchronizing order elements with JIRA issues.

Click the "Sync with JIRA" button to start the synchronization.

*   If this is the first time, a pop-up window (with an auto-completed list of labels) will be displayed. In this window, you can select a label to synchronize with and click the "Start sync" button to begin the synchronization process, or click the "Cancel" button to cancel it.

*   If a label is already synchronized, the last synchronized date and the label will be displayed in the JIRA screen. In this case, no pop-up window to select a label will be displayed. Instead, the synchronization process will start directly for that displayed (already synchronized) label.

.. NOTE::

   The relationship between "Order" and "label" is one-to-one. Only one label can be synchronized with one "Order."

.. NOTE::

   Upon successful (re)synchronization, the information will be written to the database, and the JIRA screen will be updated with the last synchronized date and label.

(Re)synchronization is performed in two phases:

*   **Phase 1:** Synchronizing order elements, including progress assignment and measurements.
*   **Phase 2:** Synchronizing time sheets.

.. NOTE::

   If Phase 1 fails, Phase 2 will not be performed, and no information will be written to the database.

.. NOTE::

   The success or failure information will be displayed in a pop-up window.

Upon successful completion of synchronization, the result will be displayed in the "Work Breakdown Structure (WBS tasks)" tab of the "Project details" screen. In this UI, there are two changes from the standard WBS:

*   The "Total task hours" column is unmodifiable (read-only) because the synchronization is one-way. Task hours can only be updated in the JIRA web server.
*   The "Code" column displays the JIRA issue keys, and they are also hyperlinks to the JIRA issues. Click the desired key if you want to go to the document for that key (JIRA issue).

Scheduling
----------

Re-synchronization of JIRA issues can also be performed through the scheduler. Go to the "Job scheduling" screen. In that screen, you can configure a JIRA job to perform synchronization. The job searches for the last synchronized labels in the database and re-synchronizes them accordingly. See also the Scheduler Manual.

Tim Enterprise Connector
========================

Tim Enterprise is a Dutch product from Aenova. It is a web-based application for the administration of time spent on projects and tasks.

The Tim connector is an application that can be used to communicate with the Tim Enterprise server to:

*   Export all hours spent by a worker (user) on a project that could be registered in Tim Enterprise.
*   Import all rosters of the worker (user) to plan the resource effectively.

The *Tim connector* must be configured properly before it can be used.

Configuration
-------------

From the "Main Settings" screen, choose the "Connectors" tab. In the connectors screen, select the Tim connector from the pull-down list. A property editor screen will then be displayed.

In this screen, you can configure the following property values:

*   **Activated:** Y/N, indicating whether you want to use the Tim connector. The default is "N."
*   **Server URL:** The absolute path to the Tim Enterprise server.
*   **User name and password:** The user credentials for authorization.
*   **Number of days timesheet to Tim:** The number of days back you want to export the time sheets.
*   **Number of days roster from Tim:** The number of days forward you want to import the rosters.
*   **Productivity factor:** Effective working hours in percentage. The default is "100%."
*   **Department IDs to import roster:** Comma-separated department IDs.

Finally, click the "Test connection" button to test if you can connect to the Tim Enterprise server and that your configurations are correct.

Export
------

From the project window, under "General data," you can start exporting time sheets to the Tim Enterprise server.

Enter the "Tim product code" and click the "Export to Tim" button to start the export.

The Tim connector adds the following fields along with the product code:

*   The worker/user's full name.
*   The date the worker worked on a task.
*   The effort, or hours worked on the task.
*   An option indicating whether Tim Enterprise should update the registration or insert a new one.

The Tim Enterprise response contains only a list of record IDs (integers). This makes it difficult to determine what went wrong, as the response list contains only numbers not related to the request fields. The export request (registration in Tim) is assumed to have succeeded if all the list entries do not contain "0" values. Otherwise, the export request has failed for those entries that contain "0" values. Therefore, you cannot see which request failed, as the list entries only contain the value "0." The only way to determine this is to examine the log file on the Tim Enterprise server.

.. NOTE::

   Upon successful exporting, the information will be written to the database, and the Tim screen will be updated with the last exported date and product code.

.. NOTE::

   The success or failure information will be displayed in a pop-up window.

Scheduling Export
-----------------

The export process can also be performed through the scheduler. Go to the "Job Scheduling" screen. In that screen, you can configure a Tim Export job. The job searches for the last exported time sheets in the database and re-exports them accordingly. See also the Scheduler manual.

Import
------

Importing rosters only works with the help of the scheduler. There is no user interface designed for this, as no input is needed from the user. Go to the "Job scheduling" screen and configure a Tim Import job. The job loops through all departments configured in the connector properties and imports all rosters for each department. See also the Scheduler Manual.

For import, the Tim connector adds the following fields in the request:

*   **Period:** The period (date from - date to) for which you want to import the roster. This can be provided as a filter criterion.
*   **Department:** The department for which you want to import the roster. Departments are configurable.
*   The fields you are interested in (like Person info, RosterCategory, etc.) that the Tim server should include in its response.

The import response contains the following fields, which are sufficient to manage the exception days in *LibrePlan*:

*   **Person info:** Name and network name.
*   **Department:** The department the worker is working in.
*   **Roster category:** Information on the presence/absence (Aanwzig/afwezig) of the worker and the reason (*LibrePlan* exception type) in case the worker is absent.
*   **Date:** The date the worker is present/absent.
*   **Time:** The start time of presence/absence, for example, 08:00.
*   **Duration:** The number of hours that the worker is present/absent.

By converting the import response to *LibrePlan*'s "Exception day," the following translations are taken into account:

*   If the roster category contains the name "Vakantie," it will be translated to "RESOURCE HOLIDAY."
*   The roster category "Feestdag" will be translated to "BANK HOLIDAY."
*   All the rest, like "Jus uren," "PLB uren," etc., should be added to the "Calendar Exception Days" manually.

Moreover, in the import response, the roster is divided into two or three parts per day: for example, roster-morning, roster-afternoon, and roster-evening. However, *LibrePlan* allows only one "Exception type" per day. The Tim connector is then responsible for merging these parts as one exception type. That is, the roster category with the highest duration is assumed to be a valid exception type, but the total duration is the sum of all durations of these category parts.

Contrary to *LibrePlan*, in Tim Enterprise, the total duration in case the worker is on holiday means the worker is not available for that total duration. However, in *LibrePlan*, if the worker is on holiday, the total duration should be zero. The Tim connector also handles this translation.

E-mail Connector
================

E-mail is a method of exchanging digital messages from an author to one or more recipients.

The E-mail connector can be used to set Simple Mail Transfer Protocol (SMTP) server connection properties.

The *E-mail connector* must be configured properly before it can be used.

Configuration
-------------

From the "Main Settings" screen, choose the "Connectors" tab. In the connectors screen, select the E-mail connector from the pull-down list. A property editor screen will then be displayed.

In this screen, you can configure the following property values:

*   **Activated:** Y/N, indicating whether you want to use the E-mail connector. The default is "N."
*   **Protocol:** The type of SMTP protocol.
*   **Host:** The absolute path to the SMTP server.
*   **Port:** The port of the SMTP server.
*   **From address:** The e-mail address of the message sender.
*   **Username:** The username for the SMTP server.
*   **Password:** The password for the SMTP server.

Finally, click the "Test connection" button to test if you can connect to the SMTP server and that your configurations are correct.

Edit E-mail Template
--------------------

From the project window, under "Configuration" and then "Edit E-mail Templates," you can modify the e-mail templates for messages.

You can choose:

*   **Template language:**
*   **Template type:**
*   **E-mail subject:**
*   **Template contents:**

You need to specify the language because the web application will send e-mails to users in the language they have chosen in their preferences. You need to choose the template type. The type is the user role, meaning that this e-mail will be sent only to users who are in the selected role (type). You need to set the e-mail subject. The subject is a brief summary of the topic of the message. You need to set the e-mail contents. This is any information that you want to send to the user. There are also some keywords that you may use in the message; the web application will parse them and set a new value instead of the keyword.

Scheduling E-mails
------------------

Sending e-mails can only be performed through the scheduler. Go to "Configuration," then the "Job Scheduling" screen. In that screen, you can configure an e-mail sending job. The job takes a list of e-mail notifications, gathers data, and sends it to the user's e-mail. See also the Scheduler manual.

.. NOTE::

   The success or failure information will be displayed in a pop-up window.
