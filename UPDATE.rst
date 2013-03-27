Update
======

This is a guide about how to upgrade *LibrePlan* when a new version is released.
If you want to know how to install the application ``INSTALL`` file.

.. contents::


LibrePlan automatic update
--------------------------

Ubuntu PPAs
~~~~~~~~~~~

Instructions::

  $ sudo apt-get update
  $ sudo apt-get install libreplan


Debian packages
~~~~~~~~~~~~~~~

Instructions:

* Download the new package::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0-1_amd64.deb

* Install package::

    # dpkg -i libreplan_1.3.0-1_amd64.deb

* Install new dependencies if needed::

    # apt-get install -f


LibrePlan manual update
-----------------------

.. WARNING::

    It is recommended to create a backup of the database, just in case any
    problem appear during the upgrade.

.. IMPORTANT::

    If you are upgrading between version a.b.c to version x.y.z, you need to
    execute all the upgrade scripts starting from the next one to a.b.c in
    order.

    For example, if you are upgrading from LibrePlan 1.2.1 to 1.3.0 you will
    have to upgrade your database using the scripts: ``upgrade_1.2.2.sql``,
    ``upgrade_1.2.3.sql`` and ``upgrade_1.3.0.sql``.

Debian/Ubuntu
~~~~~~~~~~~~~

* Stop Tomcat::

    # /etc/init.d/tomcat6 stop

* Download database upgrade scripts from previous version. For example, if you
  are upgrading from *LibrePlan 1.2.4* to *LibrePlan 1.3.0* you should download
  ``upgrade_1.3.0.sql``::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/upgrade_1.3.0.sql

* Upgrade database::

    $ psql -h localhost -U libreplan -W libreplan < upgrade_1.3.0.sql

* Download ``.war`` file of new version from SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Backup current deployed application::

    # mv /var/lib/tomcat6/webapps/libreplan/ /tmp/

* Copy war to Tomcat 6 web applications directory::

    # cp libreplan.war /var/lib/tomcat6/webapps/

* Start Tomcat 6::

    # /etc/init.d/tomcat6 start

* Go to http://localhost:8080/libreplan/


openSUSE
~~~~~~~~

* Stop Tomcat::

    # /etc/init.d/tomcat6 stop

* Download database upgrade scripts from previous version. For example, if you
  are upgrading from *LibrePlan 1.2.4* to *LibrePlan 1.3.0* you should download
  ``upgrade_1.3.0.sql``::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/upgrade_1.3.0.sql

* Upgrade database::

    $ psql -h localhost -U libreplan -W libreplan < upgrade_1.3.0.sql

* Download ``.war`` file of new version from SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Backup current deployed application::

    # mv /srv/tomcat6/webapps/libreplan/ /tmp/

* Copy war to Tomcat 6 web applications directory::

    # cp libreplan.war /srv/tomcat6/webapps/

* Start Tomcat 6::

    # /etc/init.d/tomcat6 start

* Go to http://localhost:8080/libreplan/
