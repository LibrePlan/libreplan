Install
=======

This is a guide about how to install *LibrePlan* project in your system. If you
want to upgrade your *LibrePlan* version see ``UPDATE`` file. If you want to
know how to compile it manually see ``HACKING`` file.

.. contents::


LibrePlan automatic installation
--------------------------------

Ubuntu PPAs
~~~~~~~~~~~

There are Ubuntu PPAs for different versions (Lucid, Maverick and Natty), you
can find more info in the following URL:
https://launchpad.net/~libreplan/+archive/ppa

Instructions::

  $ sudo add-apt-repository ppa:libreplan/ppa
  $ sudo apt-get update
  $ sudo apt-get install libreplan

.. TIP::

  If you do not have ``add-apt-repository`` command, you will need to install
  ``python-software-properties`` package before running the previous commands.
  You can do it with the following line::

    sudo apt-get install python-software-properties

.. WARNING::

  If you have memory problems review the section `Fix memory errors`_.

Debian packages
~~~~~~~~~~~~~~~

There are Debian packages for Squeeze (i386 and amd64), you can download them
from: http://sourceforge.net/projects/libreplan/files/LibrePlan/

Instructions:

* Download the package::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0-1_amd64.deb

* Install package::

    # dpkg -i libreplan_1.3.0-1_amd64.deb

* Install dependencies::

    # apt-get install -f

.. WARNING::

  If you have problems with printing support review the section `Fix
  printing in Debian Squeeze`_.

.. WARNING::

  If you have memory problems review the section `Fix memory errors`_.

Fedora and openSUSE OBS (openSUSE Build Service)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Instructions depending on the distribution:

* Fedora 16::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/Fedora_16/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

* Fedora 15::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/Fedora_15/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

* openSUSE 12.1::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_12.1/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

* openSUSE 11.4::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_11.4/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

.. WARNING::

  If you have memory problems review the section `Fix memory errors`_.

RPM Packages
~~~~~~~~~~~~

There are several LibrePlan RPM packages available in the following URL:
http://download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/

.. WARNING::

  If you have memory problems review the section `Fix memory errors`_.


LibrePlan manual installation
-----------------------------

Debian/Ubuntu
~~~~~~~~~~~~~

* Install requirements::

    # apt-get install openjdk-6-jre postgresql postgresql-client tomcat6 libpg-java cutycapt xvfb

* Connect to database::

    # su postgres -c psql

* Use SQL sentences to create database::

    CREATE DATABASE libreplan;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Download database installation script::

    $ wget -O install.sql http://downloads.sourceforge.net/project/libreplan/LibrePlan/install_1.3.0.sql

* Create database structure::

    $ psql -h localhost -U libreplan -W libreplan < install.sql

  .. WARNING::

    It is very important to execute the previous command specifiying
    ``libreplan`` user (as you can see in the ``-U`` option). Otherwise your
    LibrePlan installation is not going to start properly and you could find in
    your log files something like that::

      JDBCExceptionReporter  - ERROR: permission denied for relation entity_sequence

* Download ``.war`` file from SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Create a new file ``/etc/tomcat6/Catalina/localhost/libreplan.xml`` (file
  name has to match with ``.war`` name) with database configuration for
  Tomcat 6::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="libreplan" password="libreplan"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost/libreplan" />
    </Context>

* Add a new Tomcat 6 policy file ``/etc/tomcat6/policy.d/51libreplan.policy``
  with the following content::

    grant codeBase "file:/var/lib/tomcat6/webapps/libreplan/-" {
       permission java.security.AllPermission;
    };
    grant codeBase "file:/var/lib/tomcat6/webapps/libreplan.war" {
       permission java.security.AllPermission;
    };

* Add next lines to Tomcat 6 policy file
  ``/etc/tomcat6/policy.d/03catalina.policy`` file::

    grant codeBase "file:${catalina.home}/bin/tomcat-juli.jar" {
      ...
      // begin:libreplan
      permission java.io.FilePermission "${catalina.base}${file.separator}webapps${file.separator}libreplan${file.separator}WEB-INF${file.separator}classes${file.separator}logging.properties", "read";
      // end:libreplan
      ...
    };

* Add link to Java JDBC driver for PostgreSQL in Tomcat6 libraries directory::

    # ln -s /usr/share/java/postgresql-jdbc3.jar /usr/share/tomcat6/lib/

* Copy war to Tomcat 6 web applications directory::

    # cp libreplan.war /var/lib/tomcat6/webapps/

* Restart Tomcat 6::

    # /etc/init.d/tomcat6 restart

* Go to http://localhost:8080/libreplan/

.. WARNING::

  If you have problems with printing support review the last section `Fix
  printing in Debian Squeeze`_.


openSUSE
~~~~~~~~

* Install requirements::

    # zypper install java-1_6_0-openjdk postgresql-server postgresql tomcat6 xorg-x11-server

* JDBC Driver manual installation::

    # cd /usr/share/java/
    # wget http://jdbc.postgresql.org/download/postgresql-9.0-801.jdbc3.jar
    # mv postgresql-9.0-801.jdbc3.jar postgresql-jdbc3.jar

* Follow instructions at ``HACKING`` file to compile and install CutyCapt

* Start database service::

    # /etc/init.d/postgresql start

* Connect to database::

    # su postgres -c psql

* SQL sentences to create database::

    CREATE DATABASE libreplan;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Set ``postgres`` user password::

    ALTER USER postgres WITH PASSWORD 'postgres';

* Edit ``/var/lib/pgsql/data/pg_hba.conf`` and replace ``ident`` by ``md5``

* Restart database service::

    # /etc/init.d/postgresql restart

* Download database installation script::

    $ wget -O install.sql http://downloads.sourceforge.net/project/libreplan/LibrePlan/install_1.3.0.sql

* Create database structure::

    $ psql -h localhost -U libreplan -W libreplan < install.sql

  .. WARNING::

    It is very important to execute the previous command specifiying
    ``libreplan`` user (as you can see in the ``-U`` option). Otherwise your
    LibrePlan installation is not going to start properly and you could find in
    your log files something like that::

      JDBCExceptionReporter  - ERROR: permission denied for relation entity_sequence

* Download ``.war`` file from SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Create a new file ``/etc/tomcat6/Catalina/localhost/libreplan.xml`` (file
  name has to match with ``.war`` name) with database configuration for
  Tomcat 6::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="libreplan" password="libreplan"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost/libreplan" />
    </Context>

* Add link to Java JDBC driver for PostgreSQL in Tomcat6 libraries directory::

    # ln -s /usr/share/java/postgresql-jdbc3.jar /usr/share/tomcat6/lib/

* Copy war to Tomcat 6 web applications directory::

    # cp libreplan.war /srv/tomcat6/webapps/

* Restart Tomcat 6::

    # /etc/init.d/tomcat6 restart

* Go to http://localhost:8080/libreplan/


Logs
----

Since *LibrePlan 1.1.1* log system is configured automatically creating a new
folder under ``/var/log/tomcat6/`` with ``.war`` name. For example:
``/var/log/tomcat6/libreplan/``.

Inside this new directory there will be two files (``libreplan.log`` and
``libreplan-error.log``) that will be rotated every day.

Configure log directory
~~~~~~~~~~~~~~~~~~~~~~~

Anyway if you want to set manually LibrePlan log path you will have to
configure ``JAVA_OPTS`` variable in your server. This variable is configured in
different files depending on the distribution:

* Debian or Ubuntu: ``/etc/default/tomcat6``
* Fedora or openSUSE: ``/etc/tomcat6/tomcat6.conf``

Where you will need to add the next line::

  # Configure LibrePlan log directory
  JAVA_OPTS="${JAVA_OPTS} -Dlibreplan-log-directory=/my/path/to/libreplan/log/"

.. WARNING::

  You have to be sure that the user running Tomcat (usually ``tomcat6``) has
  permissions to write in the specified directory.


Fix printing in Debian Squeeze
------------------------------

Since LibrePlan 1.2 printing support is not working properly in Debian Squeeze.
To fix this issue, basically, you have to get a newer version of CutyCapt and
WebKit dependencies from Debian testing.

Instructions:

* Make sure stable remains the default distribution to pull packages from::

  # echo 'APT::Default-Release "stable";' >> /etc/apt/apt.conf

* Add a new repository to make testing packages available to ``apt-get``::

  # echo "deb http://ftp.debian.org/debian testing main" >> /etc/apt/sources.list

* Refresh package index::

  # apt-get update

* Fetch and install ``cutycapt`` (and its dependencies) from testing::

  # apt-get -t testing install cutycapt


Fix memory errors
-----------------

With the default parameters of Tomcat in the different distributions you could
have problems with Java memory.

After a while using LibrePlan you could see that some windows do not work and
the log shows a ``java.lang.OutOfMemoryError`` exception.

This exception could be caused because of two different issues:

* Heap space::

    java.lang.OutOfMemoryError: Java heap space

* PermGemp space (Permanent Generation, reflective data for the JVM)::

    java.lang.OutOfMemoryError: PermGen space

In order to avoid this problem you need to configure properly ``JAVA_OPTS``
variable in your server. This is configured in different files depending on the
distribution:

* Debian or Ubuntu: ``/etc/default/tomcat6``
* Fedora or openSUSE: ``/etc/tomcat6/tomcat6.conf``

The next lines show a possible configuration to fix the memory errors (the exact
values depends on the server features)::

  JAVA_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
  JAVA_OPTS="${JAVA_OPTS} -server -Djava.awt.headless=true"

Where the different parameters have the following meaning:

* ``-Xms``: Initial size of the Java heap
* ``-Xmx``: Maximum size of the Java heap
* ``-XX:PermSize``: Initial size of PermGen
* ``-XX:MaxPermSize``: Maximum size of PermGen

.. NOTE::

   Take into account that size of PermGen is additional to heap size.
