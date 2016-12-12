======================================================
How To Start Development With JetBrains Intellij IDEA
======================================================

.. sectnum::

:Author: Perebykivskyi Vova
:Contact: vova@libreplan-enterprise.com
:Date: 09/09/2015
:Copyright:
      Some rights reserved. This document is distributed under the Creative
      Commons Attribution-ShareAlike 3.0 licence, available in
      http://creativecommons.org/licenses/by-sa/3.0/.
:Abstract:
      Quick guide to start the development of LibrePlan_ using JetBrains Intellij IDEA_ IDE.
  As this is the most common way of work among LibrePlan developers team.

.. contents:: Table of Contents

Download LibrePlan source code
------------------------------

* You need to download LibrePlan source code to start hacking on it. You have two options:

a) Clone Git repository (recommended)::

    git clone git://github.com/LibrePlan/libreplan.git

b) Download last version source code::

    http://sourceforge.net/projects/libreplan/files/LibrePlan/libreplan_*.tar.gz

* Download latest libreplan_*.war file for your database (PostgreSQL or MySQL)

    http://sourceforge.net/projects/libreplan/files/LibrePlan/

You should review ``HACKING`` file to check that you have installed all the
requirements.

Import LibrePlan project
------------------------
* Run Intellij IDEA

* Select Import Project

* Select directory with source code of Libreplan

    # e.g. C:/Users/PC-User/IdeaProjects/libreplan

* Select *Import project from external model* > *Maven* and click *Next*

* Then leave all as default

* Then select profiles: dev and for PostgreSQL users - postgresql / Mysql users - mysql

* Then leave all by default

* Then choose your JDK(SDK), 1.8 strongly preferred

* Then define project name or leave default name

* Make "mvn clean install" in command line


Configure project to run
------------------------

* Go to *Run* > *Edit Configurations...*

* Create a new *Maven Build* called *MavenConfig*

* Change the following values:

  * Working directory: Choose ``libreplan-webapp`` folder in your workspace
  * Command line: ``jetty:stop jetty:run``
  * Profiles (optional): ``-userguide -reports -i18n``
    (to disable userguide,reports and i18n profiles to save compilation time
    as they are not mandatory to run LibrePlan)
  * Mark the following checkboxes (recommended):

* Resolve Workspace artifacts

* In "Before launch" section choose "+" and add Build Artifact (war file)

* Click *Run* and application will be available at
  http://localhost:8080/libreplan-webapp/

Develop LibrePlan in Intellij IDEA using MySQL
----------------------------------------

* This tutorial works properly with PostgreSQL, but if you want to develop
  LibrePlan using MySQL you have to do 2 small changes:

  * In section `Configure project to run`_ you have to set the *Profiles* to:
    ``dev mysql -userguide -reports -i18n``

* Remember that the three last profiles that are being disabled is just to save
  compilation time and not mandatory. However, to develop using MySQL you have
  to set at least the first two: ``dev`` and ``mysql``.


.. _LibrePlan: http://www.libreplan.com/
