Mise à jour
===========

Ceci est un guide relatif à la façon de mettre à jour *LibrePlan* quand une nouvelle version est mise à disposition.
si vous voulez savoir comment installer l'application, voir le fichier ``INSTALL_fr``.

.. contents::


Mise à jour automatique de LibrePlan
------------------------------------

PPAs Ubuntu
~~~~~~~~~~~

Instructions::

  $ sudo apt-get update
  $ sudo apt-get install libreplan


Paquets Debian
~~~~~~~~~~~~~~

Instructions:

* Télécharger le nouveau paquet::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.x.y-1_amd64.deb

* Installer le paquet::

    # dpkg -i libreplan_1.x.y-1_amd64.deb

* Installer les nouvelles dépendances si nécessaire::

    # apt-get install -f


Mise à jour manuelle de LibrePlan
---------------------------------

.. WARNING::

    Il est recommandé de créer une sauvegarde de la base de donnée, juste au cas où un problème se produirait lors de la mise à jour.

.. IMPORTANT::

    Si vous faîtes une mise à jour d'une version a.b.c vers une version x.y.z, vous devez exécuter tous les scripts de mise à jour dans l'ordre depuis celui qui suit a.b.c.

    Par exemple, si vous faîtes une mise à jour de LibrePlan 1.2.1 en version 1.3.0, vous mettrez à jour votre base de données en utilisant les scripts :``upgrade_1.2.2.sql``, ``upgrade_1.2.3.sql`` et ``upgrade_1.3.0.sql``.

Debian/Ubuntu
~~~~~~~~~~~~~

* Arrêtez Tomcat::

    # /etc/init.d/tomcat6 stop

* Téléchargez les scripts de mise à jour de la base de données depuis la version précédente. Par exemple, si vous faîtes une mise à jour de *LibrePlan 1.2.4* à  *LibrePlan 1.3.0*, vous devrez télécharger ``upgrade_1.3.0.sql``::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/upgrade_1.3.0.sql

* Mettez à jour la base de données::

    $ psql -h localhost -U libreplan -W libreplan < upgrade_1.3.0.sql

* Téléchargez le fichier ``.war`` de la nouvelle version sur SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Faîtes une sauvegarde de l'application actuellement déployée::

    # mv /var/lib/tomcat6/webapps/libreplan/ /tmp/

* Copiez le war dans le répertoire des applications web de Tomcat 6 ou 7::

    # cp libreplan.war /var/lib/tomcat6/webapps/

* Démarrez Tomcat::

    # /etc/init.d/tomcat6 start

* Allez à l'adresse http://localhost:8080/libreplan/


openSUSE
~~~~~~~~

* Arrêtez Tomcat::

    # /etc/init.d/tomcat6 stop

* Téléchargez les scripts de mise à jour de la base de données depuis la version précédente. Par exemple, si vous faîtes une mise à jour de *LibrePlan 1.2.4* à  *LibrePlan 1.3.0*, vous devrez télécharger ``upgrade_1.3.0.sql``::

    $ wget http://downloads.sourceforge.net/project/libreplan/LibrePlan/upgrade_1.3.0.sql

* Mettez à jour la base de données::

    $ psql -h localhost -U libreplan -W libreplan < upgrade_1.3.0.sql

* Téléchargez le fichier ``.war`` de la nouvelle version sur SourceForge.net::

    $ wget -O libreplan.war http://downloads.sourceforge.net/project/libreplan/LibrePlan/libreplan_1.3.0.war

* Faîtes une sauvegarde de l'application actuellement déployée::

    # mv /srv/tomcat6/webapps/libreplan/ /tmp/

* Copiez le war dans le répertoire des applications web de Tomcat 6 ou 7::

    # cp libreplan.war /srv/tomcat6/webapps/

* Démarrez Tomcat::

    # /etc/init.d/tomcat6 start

* Allez à l'adresse http://localhost:8080/libreplan/

