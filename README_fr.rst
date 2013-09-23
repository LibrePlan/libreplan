LibrePlan
=========


Description
-----------

*LibrePlan* est une application web et un logiciel libre pour gérer, surperviser et contrôler des projets.

*LibrePlan* est un outil collaboratif pour planifier, surveiller et contrôler de projets. Il possède une interface web riche qui fournit une expérience utilisateur similaire à celle d'une application de type client lourd. Tous les membres de l'équipe peuvent prendre part à la planification et ceci permet d'avoir une planification en temps réel.

Il a été conçu en pensant au cas où de multiples projets et ressources interagissent pour le cadre de l'ensemble des activités d'une compagnie. De plus, il permet la communication avec d'autres outils de la compagnie en fournissant une large palette de web services pour importer et exporter des données.


Fonctionnalités
~~~~~~~~~~~~~~~

* Gestion des ressources :

  * Deux types de ressources : machines et employés.
  * Calendriers flexibles facilement réutilisables.
  * Configuration des ressources basées sur les aptitudes et les rôles.

* Planification :

  * Moyen simple de configurer et d'estimer le travail à planifier. Ceci se fait via des structures hiérarchiques arborescentes dites Structures de Découpage du Projet (SDP) ou Work Breakdown Structures (WBS) [1]_.
  * Différents niveaux de détails lors de la planification.
  * Deux façons d'affecter des ressources :

    * Affectation spécifique : individus concrets.
    * Affectation générique assistée : basée sur des compétences et des rôles dont sont dotées les ressources.

  * Modèles pour réutiliser le travail. Les projets peuvent être créés en se basant sur des modèles évitant les tâches répétitives, ce qui permet de gagner du temps. 

  * Réaffectation automatique de ressources de façon à minimiser les surcharges (heures supplémentaires).

  * Affectation avancée. Configuration manuelle des heures de travail journalières sur une tâche quand l'affectation automatique ne correspond pas à vos besoins.

  * Méthode Monte Carlo. Simulation statistique pour calculer la possibilité de finir un projet dans une plage de dates donnée.

* Controle et surveillance des projets :

  * Analyse globale de la compagnie.

  * Gestion de la valeur acquise [2]_. La méthode de gestion de projet pour mesurer objectivement l'avancement et la performance d'un projet.

  * Gestion de la qualité de la planification. Il est possible de contrôler la qualité des tâches à réaliser via des formulaires.

  * Analyse du coût des projets.

  * Suivi des heures : les heures travaillées sont affectées à chacune des tâches pour tracer les coûts pendant la planification.

  * Mesure d'avancement via différents types d'unités.

* Sous-traitance

  LibrePlan fournit une prise en charge des compagnies qui font appel à de la sous-traitance :

  * Les tâches des projets peuvent être externalisée et envoyées à l'instance LibrePlan du fournisseur.

  * vous pouvez obtenir des notifications d'avancement des sous-traitants pour savoir comment les tâches sous-traitées évoluent.

* Autres fonctionnalités :

  * Matériaux. Vous pouvez gérer les matériaux dont les tâches planifiées ont besoin pour être réalisées.

  * Système utilisateurs complet avec des permissions, une authentification LDAP, etc.


Pré-requis
----------

* *JRE 6 ou 7* - Java Runtime Environment

  Le projet repose sur Java 6 et une JRE est nécessaire pour qu'il puisse s'exécuter

* *PostgreSQL* - Base de données objet-relationnelle SQL

  Un serveur de base de données est nécessaire. Vous pouvez utiliser *PostgreSQL* ou *MySQL* à votre convenance.

* *Tomcat 6 ou 7* - Moteur de Servlets et JSP

  Serveur pour déployer l'application. Vous pouvez utiliser *Jetty* à la place.

* *JDBC Driver* - Pilotes de base de données Java (JDBC driver) pour PostgreSQL

  Pour connecter l'application avec la base de données *PostgreSQL* dans *Tomcat*

* *CutyCapt* - Utilitaire pour faire une capture d'écran du rendu WebKit d'une page web

  Nécessaire pour l'impression

* *Xvfb* - 'faux' serveur X Virtual Framebuffer

  Utilisé par CutyCapt pour l'impression

Voir le fichier ``INSTALL_fr`` pour les instructions d'installation.

Voir le fichier ``HACKING`` pour les pré-requis de compilation et les instructions.


Mise à disposition
------------------

La toute dernière version de ce projet est toujours disponible dans le dépôt Git 
https://github.com/Igalia/libreplan.

Les versions livrées sont disponibles sur
http://sourceforge.net/projects/libreplan/files/.


Page web
--------

Vous pourrez trouver plus d'informations concernant *LibrePlan* sur
http://www.libreplan.com/.

Pour les informations relatives au développement de *LibrePlan*, vous pouvez aller sur le wiki sur
http://wiki.libreplan.org/.


Signaler des bugs
-----------------

Merci d'utiliser le gestionnaire de bugs pour signaler les bugs sur http://bugs.libreplan.org/.


Licence
-------

*LibrePlan* est fourni sous les termes de la licence GNU Affero General Public
version 3 [3]_.

Lire le fichier ``COPYING`` pour les détails.


Auteurs
-------

Ce projet a été initialement sponsorisé par la *Fundación para o Fomento da Calidade
Industrial e o Desenvolvemento Tecnolóxico de Galicia* [4]_.

Voir le fichier ``AUTHORS`` pour plus de détails concernant les auteurs.



.. [1] http://en.wikipedia.org/wiki/Work_Breakdown_Structure
.. [2] http://en.wikipedia.org/wiki/Earned_Value_Management
.. [3] http://www.fsf.org/licensing/licenses/agpl.html
.. [4] http://www.fundacioncalidade.org/
