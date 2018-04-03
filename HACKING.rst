Hacking
=======

This is a guide about how to start hacking on *LibrePlan* project.
If you want more information about *LibrePlan* development you should visit the wiki
available at: http://wiki.libreplan.org/.

.. contents::


Compilation requirements
------------------------

* *Git* - Version Control System

  Needed to clone source code repository

* *Maven 3* - Java software project management and comprehension tool

  Needed to build and compile the project

* *JDK 8* - Java Development Kit

  Project depends on Java 8 and JDK is needed in order to compile it

* *PostgreSQL* - Object-relational SQL database

  Database server

* *MySQL* - Relational SQL database

  Alternative database server

* *Python Docutils* - Utilities for the documentation of Python modules

  Used to generate HTMLs help files from RST files (reStructuredText)

* *Make* - An utility for Directing compilation

  Needed to compile the help

* *gettext* - GNU Internationalization utilities

  Used for i18n support in the project

* *CutyCapt* - Utility to capture WebKit's rendering of a web page

  Required for printing


LibrePlan compilation
---------------------

Debian/Ubuntu
~~~~~~~~~~~~~

* Install requirements::

    # apt-get install git-core maven openjdk-8-jdk postgresql postgresql-client python-docutils make gettext cutycapt

* Connect to database::

    # su postgres -c psql

* Use SQL sentences::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplandev TO libreplan;
    GRANT ALL PRIVILEGES ON DATABASE libreplandevtest TO libreplan;

* Download source code::

    $ git clone git://github.com/LibrePlan/libreplan.git

* Compile project::

    $ cd libreplan/
    $ mvn clean install

* Launch application::

    $ cd libreplan-webapp/
    $ mvn jetty:run

* Go to http://localhost:8080/

Fedora
~~~~~~

* Install requirements::

    # yum install git maven java-1.8.0-openjdk-devel postgresql postgresql-server python-docutils make gettext gnu-free-fonts-compat

.. WARNING:: Use the following command in Fedora 16 or below::

               # yum install git maven java-1.8.0-openjdk postgresql postgresql-server python-docutils make gettext gnu-free-fonts-compat

* Start database service::

    # su - postgres -c "PGDATA=/var/lib/pgsql/data initdb"
    # systemctl start postgresql.service

.. WARNING:: Use the following commands in Fedora 16 or below::

               # service postgresql initdb
               # service postgresql start

* Connect to database::

    # su postgres -c psql

* Use SQL sentences::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplandev TO libreplan;
    GRANT ALL PRIVILEGES ON DATABASE libreplandevtest TO libreplan;

* Set ``postgres`` user password::

    ALTER USER postgres WITH PASSWORD 'postgres';

.. WARNING:: These steps are only for Fedora 16 and below:

               * Edit ``/var/lib/pgsql/data/pg_hba.conf`` and replace ``ident`` by ``md5``

               * Reload database configuration::

                 # service postgresql reload

* Download source code::

    $ git clone git://github.com/LibrePlan/libreplan.git

* Compile project::

    $ cd libreplan/
    $ mvn clean install

* Launch application::

    $ cd libreplan-webapp/
    $ mvn jetty:run

* Go to http://localhost:8080/

openSUSE
~~~~~~~~

* Install requirements::

    # zypper install git-core java-1_8_0-openjdk-devel postgresql-server postgresql docutils make gettext-tools

* Install Maven::

    # cd /opt/
    # wget http://www.apache.org/dist//maven/binaries/apache-maven-3.0.5-bin.tar.gz
    # tar -xzvf apache-maven-3.0.5-bin.tar.gz

  Edit ``/etc/bash.bashrc.local`` and add the following lines::

    export M2_HOME=/opt/apache-maven-3.0.5
    export MVN=$M2_HOME/bin
    export PATH=$MVN:$PATH

* Start database service::

    # /etc/init.d/postgresql start

* Connect to database::

    # su postgres -c psql

* Use SQL sentences::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplandev TO libreplan;
    GRANT ALL PRIVILEGES ON DATABASE libreplandevtest TO libreplan;

* Set ``postgres`` user password::

    ALTER USER postgres WITH PASSWORD 'postgres';

* Edit ``/var/lib/pgsql/data/pg_hba.conf`` and replace ``ident`` by ``md5``

* Restart database service::

    # /etc/init.d/postgresql restart

* Download source code::

    $ git clone git://github.com/LibrePlan/libreplan.git

* Compile project::

    $ cd libreplan/
    $ mvn clean install

* Launch application::

    $ cd libreplan-webapp/
    $ mvn jetty:run

* Go to http://localhost:8080/


Microsoft Windows
~~~~~~~~~~~~~~~~~

* Download and install latest Java Development Kit 8uXX (JDK8uXX)::

    # http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

* Download and install latest Gettext runtime::

    # https://mlocati.github.io/articles/gettext-iconv-windows.html

* Add Gettext_installed_directory\bin (for example ``C:\Program Files\gettext-iconv\bin``) to ``Path`` variable

* Download and install latest PostgreSQL database::

    # http://www.enterprisedb.com/products-services-training/pgdownload#windows

* Download and install Apache Tomcat 8::

    # http://tomcat.apache.org/download-80.cgi
    # Note: in JDK folder there is JRE folder

* Set up JDBC41 PostgreSQL Driver::

    # Download latest driver: https://jdbc.postgresql.org/download.html
    # Copy downloaded *.jar file to JRE location: (e.g. C:\Program Files\Java\jre8\lib\ext)
    # Copy downloaded *.jar file to JAVA_HOME location: (e.g. C:\Program Files\Java\jdk1.8.0_111\jre\lib\ext)
    # Put downloaded *.jar file to Tomcat lib location: (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.0\lib)

* Create database::

    CREATE DATABASE libreplan;

* Use SQL sentences::

    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;
* Download and install Git

    # https://git-scm.com/download/win

* Download Maven

    # https://maven.apache.org/download.cgi

.. WARNING::

    Check if latest Maven version is compatible with your JDK

* Connect to database::

    # Go to PostgreSQL bin folder and command window from here
    # psql -U postgres

* Use SQL sentences::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;

    CREATE USER libreplan WITH PASSWORD 'libreplan';

    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Create an Environment Variable JAVA_HOME

    # You need to set it to your JDK installed directory

* Configure Apache Tomcat Server

* Go to (e.g. C:/Program Files/Apache Software Foundation/Tomcat 8.0/conf/Catalina/localhost/)
  and create there libreplan.xml file with this lines of code::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="libreplan" password="libreplan"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost/libreplan" />
    </Context>

=======

* Download source code::

    # Open GitBash
    # git clone https://github.com/LibrePlan/libreplan.git

* Set JAVA_HOME environment variable::

    # You need to set it to your JDK installed directory (e.g. C:\Program Files\Java\jdk1.8.0_111)

* Add path of unpacked distributions bin directory of Maven to 'Path' environment variable

    # (e.g. C:/Program Files/apache-maven-3.3.3/bin)

* Compile project::

    # cd libreplan
    # mvn clean install

* Launch application::

    * Get *.war file from project folder (e.g ../libreplan/libreplan-webapp/target/libreplan-webapp.war)
    * Rename it to libreplan.war
    * Put your libreplan.war file to Apache Tomcat webapps folder (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.0\webapps\)
    * Start Apache Tomcat server

    # Possible location: C:\Program Files\Apache Software Foundation\Tomcat 8.0\bin\Tomcat8.exe

* Go to http://localhost:8080/


CutyCapt compilation
--------------------

Like *CutyCapt* is not packaged for all distributions here are the instructions.

Ubuntu/Debian
~~~~~~~~~~~~~

* Install requirements::

    # apt-get install subversion libqt4-dev libqtwebkit-dev qt4-qmake g++ make

  In Ubuntu Lucid 10.04 remove ``libqtwebkit-dev`` package.

  * Install::

    # apt-get install cutycapt

If it doesn't work. Try to do it:

* Download source code::

    $ svn co https://cutycapt.svn.sourceforge.net/svnroot/cutycapt cutycapt

* Compile::

    $ cd CutyCapt
    $ qmake CutyCapt.pro
    $ make

* Install::

    # cp CutyCapt /user/bin/cutycapt

Fedora
~~~~~~

* Install requirements::

    # yum install subversion qt-devel qt-webkit-devel gcc-c++ make

* Download source code::

    $ svn co https://cutycapt.svn.sourceforge.net/svnroot/cutycapt cutycapt

* Compile::

    $ cd cutycapt/CutyCapt
    $ qmake-qt4 CutyCapt.pro
    $ make

* Install::

    # cp CutyCapt /user/bin/cutycapt

openSUSE
~~~~~~~~

* Install requirements::

    # zypper install subversion libqt4-devel libQtWebKit-devel gcc-c++ make

* Download source code::

    $ svn co https://cutycapt.svn.sourceforge.net/svnroot/cutycapt cutycapt

* Compile::

    $ cd cutycapt/CutyCapt
    $ qmake-qt4 CutyCapt.pro
    $ make

* Install::

    # cp CutyCapt /user/bin/cutycapt

LibrePlan documentation generation
----------------------------------

In the doc/src folder you'll find several types of documentation
available: technical documentation, user manual, some training documentation and training exercises.
This documentation is available in several languages.

The supported outputs are HTML and PDF.

Debian/Ubuntu
~~~~~~~~~~~~~

* Install requirements if generating HTML::

    # apt-get install make python-docutils

* Install requirements if generating PDF::

    # apt-get install make python-docutils texlive-latex-base texlive-latex-recommended texlive-latex-extra textlive-fonts-recommended

* Go to the directory where the documentation you want to generate is.
  For example, if you want to generate the user manual in English::

   # cd doc/src/user/en

* Generate HTML::

    # make html

* Generate PDF::

    # make pdf

* Generate both formats::

    # make

Compilation profiles
--------------------

There are different compilation profiles in *LibrePlan*. Check ``<profiles>``
section in root ``pom.xml`` to see the different profiles (there are also some
profiles defined in ``pom.xml`` of business and webapp modules).

* *dev* - Development environment (default)

  It uses databases ``libreplandev`` and ``libreplandevtest``.

* *prod* - Production environment

  Unlike *dev* it uses database ``libreplanprod`` and `libreplanprodtest``.

  It is needed to use it in combination with *postgresql* or *mysql* profiles.

  This is usually used while testing the stable branch in the repository. This
  allows developers to easily manage 2 different databases one for last
  development in master branch and another for bugfixing over stable branch.

* *postgresql* - PostgreSQL database (default)

  It uses PostgreSQL database server getting database names from *dev* or *prod*
  profiles.

* *mysql* - MySQL database

  It uses MySQL database server getting database names from *dev* or *prod*
  profiles.

* *reports* - JasperReports (default)

  If it is active *LibrePlan* reports are compiled.

  It is useful to disable this profile to save compilation time during
  development.

* *userguide* - User documentation (default)

  If it is active *LibrePlan* help is compiled and HTML files are generated.

  User documentation is written in *reStructuredText* and it is generated
  automatically thanks to this profile.

  Like for *reports*, it is useful deactivate this profile during development
  to save compilation time.

* *liquibase-update* - Liquibase update (default)

  If it is active Liquibase changes are applied in the database.

* *liquibase-updatesql* - Liquibase update SQL

  If it is active it is generated a file with SQL sentences for Liquibase
  changes needed to apply on database.

  This is used to generate upgrade files in releases.

* *i18n* - Internationalization (default)

  It uses gettext to process language files in order to be used in *LibrePlan*.

  Like for *reports* and *userguide*, it is useful deactivate this profile
  during development to save compilation time.

How to use profiles
~~~~~~~~~~~~~~~~~~~

Profiles active by default are used always if not deactivated. In order to
activate or deactivate a profile you should use parameter ``-P`` for Maven
command. For example:

* Deactivate *reports*, *userguide* and *i18n* to save compilation time::

    mvn -P-reports,-userguide,-i18n clean install

* Use production environment::

    mvn -Pprod,postgresql clean install


Compilation options
-------------------

In LibrePlan there are two custom Maven properties, which allow you to configure
some small bits in the project.

* *default.passwordsControl* - Warning about default passwords (``true`` by
  default)

  If this option is enabled, a warning is show in LibrePlan footer to
  application administrators in order to change the default password (which
  matches with user login) for the users created by default: admin, user,
  wsreader and wswriter.

* *default.exampleUsersDisabled* - Disable default users (``true`` by default)

  If true, example default users such as user, wsreader and wswriter are
  disabled. This is a good option for production environments.

  This option is set to ``false`` if you are using the development profile (the
  default one).

How to set compilation options
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Maven properties have a default value, but you can change it using the parameter
``-D`` for Maven command to set the value of each option you want to modify. For
example:

* Set *default.passwordsControl* to ``false``::

    mvn -Ddefault.passwordsControl=false clean install

* Set *default.passwordsControl* and *default.exampleUsersDisabled* to false::

    mvn -Ddefault.passwordsControl=false -Ddefault.exampleUsersDisabled=false clean install

* Set *default.emailSendingEnabled* to false::

    mvn -Ddefault.emailSendingEnabled=false clean install

Tests
-----

*LibrePlan* has a lot of JUnit test that by default are passed when you compile
the project with Maven. You can use ``-DskipTests`` to avoid tests are passed
always. Anyway, you should check that tests are not broken before sending or
pushing a patch.

::

  mvn -DskipTests clean install


MySQL
-----
Strongly preferred to use 5.6+ version

For MySQL users here are specific instructions.

* SQL sentences to create database::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;
    CREATE USER 'libreplan'@'localhost' IDENTIFIED BY 'libreplan';
    GRANT ALL PRIVILEGES ON libreplandev.* TO 'libreplan'@'localhost' WITH GRANT OPTION;
    GRANT ALL PRIVILEGES ON libreplandevtest.* TO 'libreplan'@'localhost' WITH GRANT OPTION;

* Compile project::

    $ mvn -Pdev,mysql clean install

* Launch application::

    $ cd libreplan-webapp/
    $ mvn -Pdev,mysql jetty:run

* Go to http://localhost:8080/libreplan-webapp/
