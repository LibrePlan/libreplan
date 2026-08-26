Hacking
=======

This is a guide about how to start hacking on *LibrePlan* project.
If you want more information about *LibrePlan* development you should visit the wiki
available at: https://wiki.libreplan.dev/.

.. contents::


Compilation requirements
------------------------

* *Git* - Version Control System

  Needed to clone source code repository

* *Maven 3* - Java software project management and comprehension tool

  You do **not** need to install this yourself anymore. The repository ships
  a Maven Wrapper (``mvnw`` / ``mvnw.cmd``) pinned to a known-good Maven
  version, which downloads and uses that exact version automatically the
  first time you run it. Everywhere this guide shows an ``mvn`` command, run
  ``./mvnw`` (or ``mvnw.cmd`` on Windows) from the same directory instead. A
  separately installed system Maven still works if you prefer it, but is no
  longer required.

* *JDK 25* - Java Development Kit

  The project builds and runs on JDK 25 (bytecode currently targeted at release 21 —
  ``maven.compiler.release`` in the root ``pom.xml`` — see the comment there and
  ``doc/technical/jdk25-migration/`` for why; the JVM itself is genuinely JDK 25 end to end).

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

Note on ``jetty:run`` and the local dev server
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The app now targets the ``jakarta.servlet`` (Jakarta EE 9+) namespace instead of ``javax.servlet``,
so the ``jetty-maven-plugin`` used by ``jetty:run`` was bumped from the 9.4.x line (javax-only) to
**11.0.24**, the matching Jakarta EE 9 generation. This is transparent for everyday use — nothing
below changes — but if you're used to older LibrePlan docs mentioning Tomcat/Jetty 9, that no
longer applies to local ``jetty:run`` testing.

Setup database using docker compose
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can setup the Postgres database using docker compose.

Run `docker compose up` and you will have Postgres exposed on port 5432.

Docker compose is configured to run `init.sql` which creates the test database.
Aditionally, it uses the `log_statement=all` with Postgres to show all SQL statements
being processed, which is very convenient during development.

If you want to reset the database content, run `docker compose down --volumes` which
also deletes the volumes.

Debian/Ubuntu
~~~~~~~~~~~~~

* Install requirements::

    # apt-get install git-core openjdk-25-jdk postgresql postgresql-client python3-docutils make gettext cutycapt wkhtmltopdf

* Set default OpenJDK version (required for Ubuntu 24.04 and newer)::

    # update-java-alternatives -s java-1.25.0-openjdk-amd64

* Connect to database::

    # su postgres -c psql

* Use SQL sentences::

    CREATE USER libreplan WITH PASSWORD 'libreplan';
    CREATE DATABASE libreplandev owner libreplan;
    CREATE DATABASE libreplandevtest owner libreplan;

* Download source code::

    $ git clone https://github.com/LibrePlan/libreplan.git

* Compile project::

    $ cd libreplan/
    $ ./mvnw clean install

* Launch application::

    $ cd libreplan-webapp/
    $ ../mvnw jetty:run

* Alternatively start application as systemd service::

    $ nano /lib/systemd/system/libreplan.service
    [Unit]
    Description=libreplan
    After=network.target
    Wants=network.target

    [Service]
    Type=simple
    WorkingDirectory=/root/libreplan/libreplan-webapp
    ExecStart=/root/libreplan/mvnw jetty:run
    Restart=always

    [Install]
    WantedBy=multi-user.target

    $ systemctl daemon-reload
    $ systemctl enable libreplan.service
    $ systemctl start libreplan.service

* Go to http://localhost:8080/

Fedora (needs update)
~~~~~~

* Install requirements::

    # yum install git java-25-openjdk-devel postgresql postgresql-server python3-docutils make gettext gnu-free-fonts-compat wkhtmltopdf

* Start database service::

    # su - postgres -c "PGDATA=/var/lib/pgsql/data initdb"
    # systemctl start postgresql.service

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

* Download source code::

    $ git clone git://github.com/LibrePlan/libreplan.git

* Compile project::

    $ cd libreplan/
    $ ./mvnw clean install

* Launch application::

    $ cd libreplan-webapp/
    $ ../mvnw jetty:run

* Go to http://localhost:8080/

openSUSE (needs update)
~~~~~~~~

* Install requirements::

    # zypper install git-core java-25-openjdk-devel postgresql-server postgresql docutils make gettext-tools wkhtmltopdf

  A separate Maven install is no longer needed — the project's ``./mvnw``
  wrapper downloads a known-good Maven version on first use.

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
    $ ./mvnw clean install

* Launch application::

    $ cd libreplan-webapp/
    $ ../mvnw jetty:run

* Go to http://localhost:8080/


Microsoft Windows
~~~~~~~~~~~~~~~~~

* Download and install latest Java Development Kit 25 (JDK 25)::

    # https://jdk.java.net/25/

* Download and install latest Gettext runtime::

    # https://mlocati.github.io/articles/gettext-iconv-windows.html

* Add Gettext_installed_directory\bin (for example ``C:\Program Files\gettext-iconv\bin``) to ``Path`` variable

* Download and install latest PostgreSQL database::

    # http://www.enterprisedb.com/products-services-training/pgdownload#windows

* Download and install Apache Tomcat 10 or 11::

    # https://tomcat.apache.org/download-10.cgi
    # Note: Tomcat 9.x will NOT work — the app now targets the jakarta.servlet (Jakarta EE 9+)
    # namespace, which only Tomcat 10+ implements. In JDK folder there is JRE folder.

* Set up JDBC41 PostgreSQL Driver::

    # Download latest driver: https://jdbc.postgresql.org/download
    # Copy downloaded *.jar file to JRE location: (e.g. C:\Program Files\Java\jre25\lib\ext)
    # Copy downloaded *.jar file to JAVA_HOME location: (e.g. C:\Program Files\Java\jdk-25\lib\ext)
    # Put downloaded *.jar file to Tomcat lib location: (e.g. C:\Program Files\Apache Software Foundation\Tomcat 10.1\lib)

* Create database::

    CREATE DATABASE libreplan;

* Use SQL sentences::

    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Download and install Git

    # https://git-scm.com/download/win

  A separate Maven install is not needed — the project's ``mvnw.cmd``
  wrapper downloads a known-good Maven version on first use.

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

* Go to (e.g. C:/Program Files/Apache Software Foundation/Tomcat 9.0/conf/Catalina/localhost/)
  and create there libreplan.xml file with this lines of code::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="jakarta.sql.DataSource"
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

    # You need to set it to your JDK installed directory (e.g. C:\Program Files\Java\jdk-25)

* Compile project::

    # cd libreplan
    # mvnw.cmd clean install

* Launch application::

    * Get *.war file from project folder (e.g ../libreplan/libreplan-webapp/target/libreplan-webapp.war)
    * Rename it to libreplan.war
    * Put your libreplan.war file to Apache Tomcat webapps folder (e.g. C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\)
    * Start Apache Tomcat server

    # Possible location: C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\Tomcat9.exe

* Go to http://localhost:8080/


LibrePlan documentation generation
----------------------------------

In the doc/src folder you'll find several types of documentation
available: technical documentation, user manual, some training documentation and training exercises.
This documentation is available in several languages.

The supported outputs are HTML and PDF.

Debian/Ubuntu
~~~~~~~~~~~~~

* Install requirements if generating HTML::

    # apt-get install make python3-docutils

* Install requirements if generating PDF::

    # apt-get install make python3-docutils texlive-latex-base texlive-latex-recommended texlive-latex-extra textlive-fonts-recommended

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

    ./mvnw -P-reports,-userguide,-i18n clean install

* Use production environment::

    ./mvnw -Pprod,postgresql clean install


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

    ./mvnw -Ddefault.passwordsControl=false clean install

* Set *default.passwordsControl* and *default.exampleUsersDisabled* to false::

    ./mvnw -Ddefault.passwordsControl=false -Ddefault.exampleUsersDisabled=false clean install

* Set *default.emailSendingEnabled* to false::

    ./mvnw -Ddefault.emailSendingEnabled=false clean install

Tests
-----

*LibrePlan* has a lot of JUnit test that by default are passed when you compile
the project with Maven. You can use ``-DskipTests`` to avoid tests are passed
always. Anyway, you should check that tests are not broken before sending or
pushing a patch.

::

  ./mvnw -DskipTests clean install


MySQL (Deprecated)
-----

For MySQL users here are specific instructions.

* SQL sentences to create database::

    CREATE DATABASE libreplandev;
    CREATE DATABASE libreplandevtest;
    CREATE USER 'libreplan'@'localhost' IDENTIFIED BY 'libreplan';
    GRANT ALL PRIVILEGES ON libreplandev.* TO 'libreplan'@'localhost' WITH GRANT OPTION;
    GRANT ALL PRIVILEGES ON libreplandevtest.* TO 'libreplan'@'localhost' WITH GRANT OPTION;

* Compile project::

    $ ./mvnw -Pdev,mysql clean install

* Launch application::

    $ cd libreplan-webapp/
    $ ../mvnw -Pdev,mysql jetty:run

* Go to http://localhost:8080/libreplan-webapp/

