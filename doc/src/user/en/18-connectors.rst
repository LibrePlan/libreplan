Connectors
##########

.. contents::

Connectors are Libreplan client applications that could be used to communicate with (web) servers to get 
data, process and store them. At this moment there are three connectors, JIRA connector, Tim Enterprise Connector and E-mail Connector.

Configuration
=============
Connectors should be configured properly before being used. They can be configured from configuration's ``Main Setting`` 
screen, the tab ``Connectors``. 

The connector screen comprises:

* ``Pull-down list``: a list of available connectors
* ``Properties edit screen``: a property ``edit form`` for the selected connector
* ``Test connection button``: to test the connection with the connectors
  
Select the connector you would like to configure from the ``pull-down`` list of connectors. A ``property editor form``
for the selected connector will be shown. In the ``property editor form`` you can change the values of the properties 
to your likes and test your configurations using the ``Test connection`` button. 

.. NOTE::
   The properties are configured with default values. The most important one is the property ``Activated``. By
   default it is set to ``N``. This indicates that the connector would not be used unless you change the value to ``Y``
   and save the changes.

JIRA connector
==============
 
JIRA is an issue and project tracking system.

JIRA connector is an application that can be used to request JIRA web server for JIRA issues and process the response.
The request is based on ``JIRA labels``. In JIRA, labels can be used to categorize issues. 
So the request is simple like this: get all issues that are categorized by this ``label-name``.

The connector gets the response, in this case the issues and convert them to Libreplan's ``Order elements`` and ``Time sheets``. 

The *JIRA connector* should be configured properly before being used. 

Configuration
-------------

From the configuration's ``Main Setting screen`` choose the tab ``Connectors``.
In the connectors screen select the JIRA connector from the ``pull-down`` list. A ``property editor screen`` 
is displayed now.

In this screen you can configure the following property values:

* ``Activated``: Y/N, whether you want to use the JIRA connector or not. Default is ``N``.
* ``Server URL``: the absolute path to the JIRA web server.
* ``user name and password``: the user credentials for authorization  
* ``JIRA labels: comma-separated list of labels or URL``: Either you enter the label URL or a comma separated labels.
* ``Hours type``: type of work hours. Default is ``Default``  

.. NOTE::
   JIRA labels: At this moment JIRA web server doesn't support to provide a list of all available labels. As work around 
   we have being developed a simple PHP script that does a simple SQL query in JIRA database to fetch all distinct labels. 
   You can either use this PHP script as ``JIRA labels url`` or enter the labels you want as comma separated texts 
   in the field of JIRA labels. 

Finally click the ``Test connection`` button to test if you are able to connect to JIRA web server and that 
your configurations are right.

Synchronization
---------------
From the project window ``General data`` you are able to start synchronizing order elements with JIRA issues. 

Click the button ``Sync with JIRA`` to start the synchronization. 

* If it is for the first time a ``pop-up`` window(with auto completed list of labels) will be shown. 
  In this window you can select a ``label`` to sync with and click the button ``Start sync`` to start the 
  synchronization process, or click the ``Cancel`` button to cancel it.

* If label is already synchronized, the ``last synchronized date`` and the ``label`` are displayed in the JIRA screen.
  In this case no ``pop-up`` window to select label will be shown. Instead synchronization process will be started directly 
  for that displayed(already synchronized) label.

.. NOTE::
   The relation between ``Order`` and ``label`` is one-to-one. Only one ``label`` is allowed to sync with one ``Order`` 

.. NOTE::
   On the successful (re)synchronization, the information would be written to the database and the JIRA screen would 
   be updated with the last synchronized -``date`` and  -``label``. 


(Re)synchronization process is performed in two phases:

* phase-1: Synchronizing order elements including progress assignment and measurements.
* phase-2: Synchronizing time sheets. 

.. NOTE::
   if phase-1 fails, phase-2 will not be performed and no information will be written to the database.

.. NOTE::
   The success or failure information would be displayed in pop-up window.
   
On successful completing of synchronization, the result would be displayed in ``Work Breakdown Structure(WBS tasks)`` tab of the
``Project details`` screen. In this UI there are two changes from the standard ``WBS``:

* The ``Total task hours`` column is unmodifiable (read-only) as the synchronization is one way. Task hours can be only updated in 
  in JIRA web server
* The ``Code`` column shows the ``JIRA issue keys`` and as the same time they are ``Hyperlinks`` to the JIRA issues. Click the 
  desired key if you want to go to the document of that key(JIRA issue)   

Scheduling
----------
Re-synchronization of JIRA issues can also take place through the scheduler. Go to ``Job scheduling`` screen.
In that screen you can configure a JIRA ``job`` to do synchronization. The ``job`` searches for last synchronized 
``labels`` in the database and re-synchronize them accordingly. see also the scheduler Manual. 

Tim Enterprise connector
========================
Tim Enterprise is a Dutch product from Aenova. It is a web based application for the administration
of time spent on projects and tasks.

Tim connector is an application that can be used to communicate with Tim Enterprise server to:

* export all hours spent by worker(user) on a project that could be registered in Tim Enterprise.
* import all rosters of the worker(user) in order to plan the resource effectively. 
   
The *Tim connector* should be configured properly before being used. 

Configuration
-------------

From the configuration's ``Main Setting`` screen choose the tab ``Connectors``.
In the connectors screen select the Tim connector from the ``pull-down`` list. A ``property editor screen`` 
is displayed now.

In this screen you can configure the following property values:

* ``Activated``: Y/N, whether you want to use the Tim connector or not. Default is ``N``.
* ``Server URL``: the absolute path to the Tim Enterprise server.
* ``user name and password``: the user credentials for authorization  
* ``Number of days timesheet to Tim``: how many days back you want to export the times heets
* ``Number of days roster from Tim``: how many days forward you want to import the rosters  
* ``Productivity factor``: Effective working hours in percentage. Default is ``100%``
* ``Department IDs to import roster``: comma separated department IDs.

Finally click the ``Test connection`` button to test if you are able to connect to
Tim Enterprise server and that your configurations are right.
 
Export
------
From the project window ``General data`` you are able to start exporting time sheets to Tim Enterprise server. 

Enter the ``Tim product code`` and click the button ``Exprot to Tim`` to start the Export. 

The Tim connector adds the following fields along with the product code:

* The Worker/user full name
* The Date worked on a task by worker
* The Effort, hours worked on task
* and an option whether Tim Enterprise should update the registration or inserts a new one 

The Tim Enterprise *response* contains only a list of ``record-IDs(integers)``. This is the difficult part to see what is 
go wrong as the response list contains only numbers not related to the request fields. 
The *Export* request(registration in Tim) assumed to be succeeded if all the list entries doesn't contain ``0`` values. Otherwise
the *Export* request is failed for those entries which contains ``0`` values.  So, you can't see here which
request is failed as the list entries contains only the value ``0``. The only way to figure out this is to look at the log file 
in Tim Enterprise server.

.. NOTE::
   On the successful exporting, the information would be written to the database and the Tim screen would be updated 
   with last exported -``date`` -``product code``. 

.. NOTE::
   The success or failure information would be displayed in pop-up window.

Scheduling export
------------------
Export process can also take place through the scheduler. Go to ``Job Scheduling`` screen.
In that screen you can configure a Tim Export ``job``. The ``job`` searches for last exported 
time sheets in the database and re-export them accordingly. See also the Scheduler manual.

Import
------
Importing of rosters works only with the help of the *Scheduler*. There is no user-interface designed for as 
no input is needed from the user. 
Go to ``Job scheduling`` screen and configure Tim Import ``job``. The ``job`` loops through all departments
configured in *connectors property* and import all rosters for each department. See also the scheduler Manual.

For import, the Tim connector adds the following fields in the *request*: 

* Period: The period(date from - date to) you want import the roster. This can be provided as a filter criteria
* Department: For which department you want to import the roster. Departments are configurable.
* The fields you are interested in(like Person info, RosterCategory etc) that the Tim server should include in its response. 

The import *response* contains the following fields, which is enough to manage the ``exception days`` in Libreplan:

* Person info: name and network name
* Department: The department the worker working in
* Roster category: Information on the presence/absence(Aanwzig/afwezig) of the worker and the reason(Libreplan exception type)
  in case that the worker is absent
* Date: The Date worker is present/absent
* Time: The Start time of present/absent, for example 08:00
* duration: Number of hours that the worker is present/absent
   
By converting the import *response* to Libreplan's ``Exception day`` the following translations takes into account:

* If the roster category contains the name ``Vakantie`` it would be translated to ``RESOURCE HOLIDAY``
* The Roster category, ``Feestdag`` would be translated to ``BANK HOLIDAY`` 
* All the rest like ``Jus uren``, ``PLB uren`` etc should be added to the ``Calendar Exception Days`` manually
    
Moreover the import *response*, the roster is divided into two or three parts per day: For example roster-morning, 
roster-afternoon and roster-evening. But Libreplan allows only one ``Exception type`` per day. The Tim connector is then 
responsible for merging these parts as one ``exception type``. That is, the roster category with the highest ``duration`` is 
assumed to be a valid ``Exception type`` but the total duration is the sum of all durations of these category parts.

Contrary to the Libreplan, in Tim Enterprise, the ``total duration`` in case that the worker is on holiday means the worker is 
not available for that ``total duration``. But in Libreplan if the worker is on holiday the total duration should be ``Zero``. 
The Tim connector also takes care of this translation.

E-mail connector
================
 
E-mail is a method of exchanging digital messages from an author to one or more recipients.

E-mail connector can be used to set Simple Main Transfer Protocol (SMTP) server connection properties.

The *E-mail connector* should be configured properly before being used.

Configuration
-------------

From the configuration's ``Main Settings`` screen choose the tab ``Connectors``.
In the connectors screen select the E-mail connector from the ``pull-down`` list. A ``property editor screen``
is displayed now.

In this screen you can configure the following property values:

* ``Activated``: Y/N, whether you want to use the E-mail connector or not. Default is ``N``.
* ``Protocol``: type of SMTP protocol.
* ``Host``: the absolute path to SMTP server.
* ``Port``: port of SMTP server.
* ``From address``: e-mail address of messages sender.
* ``Username``: username for SMTP server.
* ``Password``: password for SMTP server.

Finally click the ``Test connection`` button to test if you are able to connect to
SMTP server and that your configurations are right.

Edit E-mail template
--------------------

From the project window ``Configuration`` and then ``Edit E-mail Templates`` you are able to modify E-mail templates of
messages.

You are able to choose:

* Template language
* Template type
* E-mail subject
* Template contents

You need to specify language because web application will send e-mail to user in language that user have chosen in
preferences.
You need to choose template type, type is user role, it means that this e-mail will be send only to users who are in\
selected role (type).
You need to set e-mail subject. Subject - a brief summary of the topic of the message.
You need to set e-mail contents. Any information that you want to send to user. Also there are some keywords that you
may use in message; web application will parse it and set a new value instead of keyword.

Scheduling e-mails
------------------

Sending e-mails process can take place only through the scheduler. Go to ``Configuration`` then ``Job Scheduling``
screen.
In that screen you can configure a e-mail sending ``job``. The ``job`` is taking a list of e-mail notifications,
gathering data and sending it to user`s e-mail. See also the Scheduler manual.


.. NOTE::
   The success or failure information would be displayed in pop-up window.
