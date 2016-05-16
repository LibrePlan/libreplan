Installation
============

Ceci est un guide d'installation de *LibrePlan*. Si vous voulez mettre à jour votre version de *LibrePlan*, voir le fichier ``UPDATE_fr``. Si vous voulez savoir comment le compiler manuellement, voir le fichier ``HACKING``.

.. contents::


Installation automatisée de LibrePlan
-------------------------------------

Images Docker
~~~~~~~~~~~~~

La façon la plus facile d'obtenir rapidement une instance fonctionnelle de LibrePlan consiste à utiliser des images Docker de LibrePlan.
Outre l'image classique LibrePlan/Postgresql, vous trouverez également une image utilisant MySQL ou MariaDB.

Voir https://hub.docker.com/r/libreplan/libreplan/ pour obtenir davantage d'informations et les instructions nécessaires à leur utilisation.

PPAs Ubuntu 
~~~~~~~~~~~

Il existe des PPAs Ubuntu pour plusieurs versions (Precise, Trusty, Utopic et Vivid); vous pourrez trouver davantage d'informations à l'URL suivante :
https://launchpad.net/~libreplan/+archive/ppa

Instructions::

  $ sudo add-apt-repository ppa:libreplan/ppa
  $ sudo apt-get update
  $ sudo apt-get install libreplan

.. TIP::

  Si nous n'avez pas la commande ``add-apt-repository``, vous devrez installer le paquet ``software-properties-common`` avant de lancer les commandes précédentes.
  Vous pouvez le faire avec la ligne suivante :

    sudo apt-get install software-properties-common

.. WARNING::

  Si vous rencontrez des problèmes de mémoire, voir la section `Corriger les erreurs de mémoire`_.

Paquets Debian
~~~~~~~~~~~~~~

Il existe des paquets Debian pour Wheezy et Jessie (i386 et amd64), vous pouvez les télécharger sur : http://sourceforge.net/projects/libreplan/files/LibrePlan/

Instructions:

* Télécharger le paquet::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.4.1-1_amd64.deb

* Installer le paquet::

    # dpkg -i libreplan_1.4.1-1_amd64.deb

* Installer les dépendances::

    # apt-get install -f

.. WARNING::

  Si vous rencontrez des problèmes avec la gestion de l'impression, voir la section `Corriger l'impression avec Debian Squeeze`_.

.. WARNING::

  Si vous rencontrez des problèmes de mémoire, voir la section `Corriger les erreurs de mémoire`_.

Fedora, CentOS et openSUSE OBS (openSUSE Build Service)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Instructions dépendants de la distribution :

* Fedora 23::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/Fedora_23/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

  Suivre ensuite les instructions présentes dans /usr/share/doc/libreplan-1.4.1/README.Fedora.

* Fedora 22::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/Fedora_22/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

  Suivre ensuite les instructions présentes dans /usr/share/doc/libreplan-1.4.1/README.Fedora.

* CentOS 7::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/CentOS_7/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

* CentOS 6::

    # cd /etc/yum.repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/CentOS_CentOS-6/home:jsuarezr:LibrePlan.repo
    # yum install libreplan

* openSUSE Leap_42.1::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_Leap_42.1/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

* openSUSE Factory::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_Factory/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

* openSUSE 13.2::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_13.2/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

* openSUSE 13.1::

    # cd /etc/zypp/repos.d
    # wget download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/openSUSE_13.1/home:jsuarezr:LibrePlan.repo
    # zypper ref
    # zypper install libreplan

.. WARNING::

  Si vous rencontrez des problèmes de mémoire, voir la section `Corriger les erreurs de mémoire`_.

Paquets RPM
~~~~~~~~~~~

Il existe plusieurs paquets RPM disponibles à l'URL suivante :
http://download.opensuse.org/repositories/home:/jsuarezr:/LibrePlan/

Suivre les instructions du fichier README correspondant pour achever l'installation.

.. WARNING::

  Si vous rencontrez des problèmes de mémoire, voir la section `Corriger les erreurs de mémoire`_.


Installation manuelle de LibrePlan
----------------------------------

Debian/Ubuntu
~~~~~~~~~~~~~

* Installer les pré-requis::

    # apt-get install openjdk-7-jre postgresql postgresql-client tomcat7 libpg-java cutycapt xvfb

* se connecter à la base de données::

    # su postgres -c psql

* Utiliser les séquences SQL suivantes pour créer la base de données::

    CREATE DATABASE libreplan;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Télécharger le script d'installation de la base de données::

    $ wget -O install.sql http://downloads.sourceforge.net/project/libreplan/LibrePlan/install_1.4.0.sql

  .. WARNING::

    Le fichier 1.4.1.sql est spécifique pour une installation avec MySQL.

* Créer la structure de la base de données::

    $ psql -h localhost -U libreplan -W libreplan < install.sql

  .. WARNING::

    Il est très important d'exécuter la commande précédente en tant que l'utilisateur ``libreplan`` (c'est ce à quoi sert l'option ``-U``). Sinon votre installation de LibrePlan ne pourra pas démarrer correctement et vous pourrez trouver dans vos fichiers logs quelque chose comme ce qui suit::

      JDBCExceptionReporter  - ERROR: permission denied for relation entity_sequence

* Télécharger le fichier ``.war`` sur SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.4.1.war

* Créer un nouveau fichier ``/etc/tomcat6/Catalina/localhost/libreplan.xml`` (le nom du fichier doit correspondre au nom avec ``.war``) avec la configuration de la base de données pour Tomcat 6 ou 7::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="libreplan" password="libreplan"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost/libreplan" />
    </Context>

* Ajouter un nouveau fichier policy pour Tomcat 6 ``/etc/tomcat6/policy.d/51libreplan.policy``
  avec le contenu suivant::

    grant codeBase "file:/var/lib/tomcat6/webapps/libreplan/-" {
       permission java.security.AllPermission;
    };
    grant codeBase "file:/var/lib/tomcat6/webapps/libreplan.war" {
       permission java.security.AllPermission;
    };

* Ajouter les lignes suivantes au fichier poilicy pour Tomcat 6 ``/etc/tomcat6/policy.d/03catalina.policy`` ::

    grant codeBase "file:${catalina.home}/bin/tomcat-juli.jar" {
      ...
      // begin:libreplan
      permission java.io.FilePermission "${catalina.base}${file.separator}webapps${file.separator}libreplan${file.separator}WEB-INF${file.separator}classes${file.separator}logging.properties", "read";
      // end:libreplan
      ...
    };

* Alternativement, pour Tomcat 7, ajouter les ligne des deux points précédent à ``tomcat/conf/catalina.policy``.

* Ajouter un lien vers le pilote Java JDBC pour PostgreSQL dans le répertoire des bibliothèques de Tomcat 6 ou 7::

    # ln -s /usr/share/java/postgresql-jdbc4.jar /usr/share/tomcat7/lib/

* Copier le war dans le répertoire des applications web de Tomcat 6 ou 7::

    # cp libreplan.war /var/lib/tomcat7/webapps/

* Relancer Tomcat::

    # /etc/init.d/tomcat7 restart

* Aller à l'adresse http://localhost:8080/libreplan/

.. WARNING::

  Si vous rencontrez des problèmes avec la gestion de l'impression, voir la section `Corriger l'impression avec Debian Squeeze`_.


openSUSE
~~~~~~~~

* Installer les pré-requis::

    # zypper install java-1_7_0-openjdk postgresql-server postgresql tomcat7 xorg-x11-server

* Installation manuelle du pilote JDBC::

    # cd /usr/share/java/
    # wget http://jdbc.postgresql.org/download/postgresql-9.2-1004.jdbc41.jar
    # mv postgresql-9.2-1004.jdbc41.jar postgresql-jdbc4.jar

* Suivre les instructions du fichier ``HACKING`` pour compiler et installer CutyCapt

* Lancer le service base de données::

    # /etc/init.d/postgresql start

* Se connecter à la base de données::

    # su postgres -c psql

* Utiliser la séquence SQL suivante pour créer la base de données::

    CREATE DATABASE libreplan;
    CREATE USER libreplan WITH PASSWORD 'libreplan';
    GRANT ALL PRIVILEGES ON DATABASE libreplan TO libreplan;

* Configurer le mot de passe de l'utilisateur ``postgres``::

    ALTER USER postgres WITH PASSWORD 'postgres';

* Ouvrir ``/var/lib/pgsql/data/pg_hba.conf`` et remplacer ``ident`` par ``md5``

* Relancer le service de base de données::

    # /etc/init.d/postgresql restart

* Télécharger le script d'installation de la base de données::

    $ wget -O install.sql http://downloads.sourceforge.net/project/libreplan/LibrePlan/install_1.4.0.sql

* Créer la structure de la base de données::

    $ psql -h localhost -U libreplan -W libreplan < install.sql

  .. WARNING::

    Il est très important d'exécuter la commande précédente en tant que l'utilisateur ``libreplan`` (c'est ce à quoi sert l'option ``-U``). Sinon votre installation de LibrePlan ne pourra pas démarrer correctement et vous pourrez trouver dans vos fichiers logs quelque chose comme ce qui suit::

      JDBCExceptionReporter  - ERROR: permission denied for relation entity_sequence

* Télécharger le fichier ``.war`` sur SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.4.1.war

* Créer un nouveau fichier ``/etc/tomcat6/Catalina/localhost/libreplan.xml`` (le nom du fichier doit correspondre avec le nom ``.war``) avec la configuration de la base de données pour Tomcat 6 ou 7::

    <?xml version="1.0" encoding="UTF-8"?>

    <Context antiJARLocking="true" path="">
        <Resource name="jdbc/libreplan-ds" auth="Container"
            type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="libreplan" password="libreplan"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost/libreplan" />
    </Context>

* Ajouter un lien vers le pilote Java JDBC pour PostgreSQL dans le répertoire des bibliothèques Tomcat 6 ou 7::

    # ln -s /usr/share/java/postgresql-jdbc4.jar /usr/share/tomcat7/lib/

* Copier le war dans le répertoire des applications web de Tomcat 6 ou 7::

    # cp libreplan.war /srv/tomcat7/webapps/

* Relancer Tomcat 6 ou 7::

    # /etc/init.d/tomcat7 restart

* Aller à l'adresse http://localhost:8080/libreplan/


Journaux
--------

Depuis *LibrePlan 1.1.1*, le système de fichiers journaux est configuré pour créer automatiquement un nouveau répertoire sous ``/var/log/tomcat6/`` avec le nom ``.war``. Par exemple:
``/var/log/tomcat6/libreplan/``.

Dans ce nouveau répertoire on trouvera deux fichiers (``libreplan.log`` et  ``libreplan-error.log``) qui feront l'objet d'une rotation quotidienne.

Configurer le répertoire des fichiers journaux
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Cependant, si vous voulez configurer manuellement le chemin d'accès aux journaux de LibrePlan, vous devrez configurer la variable ``JAVA_OPTS`` dans votre serveur. Cette variable est configurée dans différents fichiers selon la distribution :

* Debian ou Ubuntu: ``/etc/default/tomcat6``
* Fedora ou openSUSE: ``/etc/tomcat6/tomcat6.conf``

Dans ce fichier, vous devrez ajouter la ligne suivante::

  # Configurer le répertoire des journaux de LibrePlan
  JAVA_OPTS="${JAVA_OPTS} -Dlibreplan-log-directory=/mon/chemin/vers/les/logs/libreplan/"

.. WARNING::

  Vous devez vous assurer que l'utilisateur qui exécute Tomcat (habituellement ``tomcat6`` ou ``tomcat7`` ou ``tomcat``) possède le droit en écriture sur le répertoire indiqué.


Corriger l'impression avec Debian Squeeze
-----------------------------------------

Depuis LibrePlan 1.2, la prise en charge de l'impression ne fonctionne pas correctement avec Debian Squeeze.
Pour corriger ce problème, vous devez mettre en place une nouvelle version de CutyCapt et des dépendances WebKit provenant de Debian testing.

Instructions:

* Assurez-vous que stable reste la distribution par défaut pour la récupération des paquets::

  # echo 'APT::Default-Release "stable";' >> /etc/apt/apt.conf

* Ajouter un nouveau dépôt pour rendre les paquets testing disponibles pour ``apt-get``::

  # echo "deb http://ftp.debian.org/debian testing main" >> /etc/apt/sources.list

* Mettez à jour l'index des paquets::

  # apt-get update

* Récupérez et installez ``cutycapt`` (et ses dépendances) depuis::

  # apt-get -t testing install cutycapt


Corriger les erreurs de mémoire
-------------------------------

Avec les paramètres par défaut de Tomcat dans différentes distributions, vous pourriez avec des problèmes avec la mémoire Java.

Après quelques temps d'utilisation de LibrePlan, vous pourriez voir que certains écrans ne fonctionnent pas et que les journaux afficher une exception ``java.lang.OutOfMemoryError``.

Cette exception pourrait être provoquée par deux problèmes différents :

* Heap space::

    java.lang.OutOfMemoryError: Java heap space

* PermGemp space (Génération permanente, données réflexives pour la JVM)::

    java.lang.OutOfMemoryError: PermGen space

De façon à éviter ce problème, vous devrez configure de manière appropriée la variable ``JAVA_OPTS`` de votre serveur. Ceci se fait dans différents fichiers selon la distribution :

* Debian ou Ubuntu: ``/etc/default/tomcat6``
* Fedora ou openSUSE: ``/etc/tomcat6/tomcat6.conf``

Les lignes suivantes présentent à une configuration possible pour corriger les erreurs de mémoire (les valeurs exactes dépendent des caractéristiques du serveur)::

  JAVA_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
  JAVA_OPTS="${JAVA_OPTS} -server -Djava.awt.headless=true"

où les différents paramètres ont la signification suivante :

* ``-Xms``: Taille initiale du tas (heap) Java
* ``-Xmx``: Taille maximale du tas Java
* ``-XX:PermSize``: Taille initiale du PermGen
* ``-XX:MaxPermSize``: Taille maximale du PermGen

.. NOTE::

   Tenez compte du fait que la taille de PermGen s'ajoute à celle du tas.
