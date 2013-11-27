Connecteurs
###########

.. contents::

Les connecteurs sont des applications clientes de LibrePlan qui peuvent être utilisées pour communiquer avec des serveurs (web) pour obtenir des données, les traiter et les enregistrer. Actuellement, il existe deux connecteurs, le connecteur JIRA et le connecteur Tim Enterprise.

Configuration
=============
Les connecteurs doivent être configurés de manière appropriée avant d'être utilisés. Ils peuvent être configurés à partir de l'écran de configuration ``Réglages principaux``, onglet ``Connecteurs``.

L'écran des connecteurs comporte :

* ``une liste déroulante`` : une liste des connecteurs disponibles
* ``un écran de modification des propriétés`` : un formulaire de modification des propriétés pour le connecteur choisi
* ``un bouton de test de la connexion`` : pour tester la connexion avec les connecteurs
 
Choisir le connecteur que vous voulez configurer à partir de la ``liste déroulante`` des connecteurs. Un ``formulaire de modification des propriétés`` pour le connecteur choisi va apparaître. Dans ce ``formulaire de modification des propriétés`` vous pouvez modifier les valeurs des propriétés que vous souhaitez et tester votre configuration en utilisant le bouton ``Tester la connexion``. 

.. NOTE::
   Les propriétés sont configurées avec des valeurs par défaut. La plus importante est la propriété ``Activé``. Par défaut, elle est positionné à ``N``. Ceci indique que le connecteur ne sera pas utilisé à moins de changer sa valeur à ``Y`` et d'enregistrer les modifications.

Connecteur JIRA 
===============

JIRA est un système de suivi d'incidents et de projets. 

Le connecteur JIRA est une application qui peut être utilisée pour récupérer depuis le serveur web JIRA des incidents et traiter les réponses.
La requête est basée sur les ``étiquettes JIRA`` (``JIRA labels``). Dans JIRA, les étiquettes peuvent être utilisées pour catégoriser les incidents.
Aussi, la requête est aussi simple que : obtenir tous les incidents qui sont catégorisés par ce ``nom-d-etiquette``.

Le connecteur obtient la réponse, dans ce cas les incidents puis les convertit en ``tâches`` et ``relevés des heures de travail`` de LibrePlan.

Le *connecteur JIRA* doit être configuré correctement avant de pouvoir être utilisé.

Configuration
-------------

À partir de ``l'écran de configuration principal``, choisir l'onglet ``Connecteurs``.
Dans l'écran des connecteurs, choisir le connecteur JIRA dans la ``liste déroulante``. Un ``écran de modification des propriétés`` est alors affiché.

Dans cet écran, vous pouvez configurer les valeurs des propriétés suivantes :

* ``Activé``: Y/N, selon que vous voulez utiliser le connecteur JIRA ou pas. La valeur par défaut est ``N``.
* ``URL du serveur``: le chemin absolu vers le serveur web JIRA.
* ``Nom d'utilisateur`` et ``Mot de passe``: les informations de connexion de l'utilisateur pour l'autorisation
* ``Étiquettes JIRA : liste séparée par des virgules des étiquettes ou des URL``: Soit vous entrez l'URL de l'étiquette ou des étiquettes séparées par des virgules.
* ``Type d'heures``: type des heures de travail. La valeur par défaut est ``Default``  

.. NOTE::
   Étiquettes JIRA : actuellement, le serveur web JIRA ne gère pas la fourniture d'une liste de toutes les étiquettes disponibles. Comme contournement, nous avons développé un simple script PHP qui effectue une requête SQL simple dans la base de données pour récupérer toutes les différentes étiquettes. Vous pouvez soit utiliser ce script comme ``URL des étiquettes JIRA`` soit saisir les étiquettes que vous souhaitez séparées par des virgules dans le champ des étiquettes JIRA.

Enfin, cliquer sur le bouton  ``Tester la connexion`` pour tester si vous arrivez à vous connecter au serveur web JIRA et que votre configuration est correcte.

Synchronisation
---------------
À partir de la vue ``Détails du projet``, onglet ``Données générales``, vous pouvez lancer la synchronisation des tâches avec les incidents JIRA.

Cliquer sur le bouton ``Synchroniser avec JIRA`` pour lancer la synchronisation.

* Si c'est la première fois, une fenêtre ``surgissante`` s'affiche (avec la liste auto-complétée des étiquettes). Dans cette fenêtre, vous pouvez choisir une ``étiquette`` avec laquelle effectuer la synchronisation puis cliquer sur le bouton ``Démarrer la synchronisation`` pour lancer le processus de synchronisation, ou cliquer sur le bouton ``Annuler`` pour l'annuler.

* Si l'étiquette est déjà synchronisée, la ``date de dernière synchronisation`` et l' ``étiquette`` sont affichées dans l'écran JIRA. Dans ce cas, aucune fenêtre ``surgissante`` de sélection des étiquettes ne sera affichée. À la place, le processus de synchronisation va démarrer directement pour l'étiquette (déjà synchronisée) affichée.

.. NOTE::
   La relation entre ``projet`` et ``étiquette`` est de un pour un. Une Seule ``étiquette`` est autorisée à se synchroniser avec un ``Projet``.

.. NOTE::
   Pour une (re)synchronisation réussie, les informations seront écrites en base de données et l'écran JIRA sera mis à jour avec les dernières ``dates`` et ``étiquettes`` synchronisées.

Le processus de (re)synchronisation est réalisé en deux phases :

* phase-1: Synchronisation des tâches dont les affectations d'avancement et les mesures.
* phase-2: Synchronisation des relevés des heures de travail. 

.. NOTE::
  
   Si la phase-1 échoue, la phase-2 ne sera pas réalisée et aucune information ne sera écrite en base de données. 

.. NOTE::
   Les informations de réussite ou d'échec seront affichées dans la fenêtre surgissante.

Après l'achèvement avec succès d'une synchronisation, le résultat sera affiché dans l'onglet ``SDP (Tâches)`` de l'écran ``Détails du projet``. Dans cette IHM, il y a deux modifications par rapport à la structure hiérarchique arborescente habituelle des tâches (``SDP``) :   

* La colonne ``total des heures de la tâche`` n'est pas modifiable (lecture seule) du fait que la synchronisation ne fonctionne que dans un sens.  Les heures des tâches ne peuvent être mises à jour que depuis le serveur web JIRA.
* La colonne ``Code`` affiche les ``clefs des incidents JIRA`` qui sont en même temps des ``liens hypertexte`` vers ces incidents JIRA. Cliquer sur la clef souhaitée si vous voulez accéder au document de cette clef (incident JIRA).

Programmation
-------------
La re-synchronisation des incidents JIRA peut également se faire via l'ordonnanceur. Allez à l'écran ``Ordonnancement des tâches``. Dans cet écran, vous pouvez configurer une ``tâche (job)`` JIRA pour faire la synchronisation. La ``tâche`` recherche les dernières ``étiquettes`` synchronisées dans la base de données et les resynchronise en conséquence. Voir également la section relative à l'ordonnanceur.

Connecteur Tim Enterprise
=========================
Tim Enterprise est un projet hollandais de Aénova. C'est une application web pour la gestion du temps passé sur les projets et les tâches.

Le connecteur TIM est une application qui peut être utilisée pour communiquer avec le serveur Tim Enterprise pour :

* exporter toutes les heures passées par l'employé (utilisateur) sur un projet qui peut être enregistré dans Tim Enterprise.
* importer toutes les contraintes de l'employé (utilisateur) de façon à planifier l'utilisation des ressources efficacement. 
  
Le *connecteur Tim* doit être configuré correctement avant d'être utilisé. 

Configuration
-------------

Depuis l'écran de configuration des ``Réglages principaux``, choisir l'onglet ``Connecteurs``.
Dans l'écran des connecteurs, choisir le connecteur Tim dans la liste ``déroulante``. Un ``écran de modification des propriétés`` est maintenant affiché.

Dans cet écran, vous pouvez configurer les valeurs des propriétés suivantes :

* ``Activé``: Y/N, selon que vous voulez utiliser le connecteur Tim ou non. La valeur par défaut est ``N``.
* ``URL du serveur``: le chemin d'accès absolu au serveur Tim Enterprise
* ``Nom d'utilisateur`` et ``mot de passe`` : les informations de l'utilisateur pour se connecter
* ``Nombre de relevés journaliers des heures de travail pour Tim``: le nombre de jours écoulés pour lesquels vous voulez exporter les relevés des heures de travail
* ``Nombre de listes de jours provenant de Tim``: le nombre de jours à venir pour lesquels vous voulez importer les feuilles de service
* ``Facteur de productivité`` : Heures de travail efficaces en pourcentage. La valeur par défaut est ``100%``
* ``Liste des ID de département à importer``: identifiants des départements séparés par des virgules.

Enfin, cliquez sur le bouton ``Tester la connexion`` pour vérifier que vous pouvez vous connecter au serveur Tim Enterprise et que votre configuration est correcte.
 
Export
------
Depuis la fenêtre projet, onglet ``Données générales``, vous pouvez lancer l'exportation des relevés des heures de travail vers le serveur Tim Enterprise.

Saisir le ``code produit Tim`` et cliquer sur le bouton ``Exporter vers Tim`` pour lancer l'export.

Le connecteur Tim ajoute les champs suivants avec le code produit :

* le nom complet de l'employé/utilisateur
* la date à laquelle l'employé a travaillé sur une tâche
* l'effort, les heures passées sur la tâche
* et une option indiquant si Tim Enterprise doit mettre à jour l'enregistrement ou en insérer un nouveau

La *réponse* de Tim Enterprise ne contient qu'une liste d' ``identifiants d'enregistrement (entiers)``. Du coup, il est difficile de voir ce qui n'a pas fonctionné car la liste de réponse ne contient que des nombres qui ne sont pas reliés aux champs de la requête.
La requête d' ``export`` a échoué pour les entrées qui contiennent des valeurs ``0``. Aussi vous ne pouvez pas voir ici quelle requête a échoué car les entrées de la liste ne contiennent que la valeur ``0``. La seule façon de comprendre est de regarder le fichier journal du serveur Tim Enterprise.

.. NOTE::
   Pour un export réussi, les informations seront écrites dans la base de données et l'écran Tim sera mis à jour avec les dernières ``date`` - ``code produit`` exportés.

.. NOTE::
   Les information de réussite ou d'échec seront affichées dans une fenêtre surgissante.

Export programmé
----------------
Le processus d'export peut également se faire via l'ordonnanceur. Aller à l'écran ``Ordonnanceur de tâches``. Dans cet écran, vous pouvez configurer une ``tâche système`` d'export vers Tim. La ``tâche système`` recherche les derniers relevés des heures de travail exportés dans la base de données et les réexporte en conséquence. Voir également la section relative à l'ordonnanceur.

Import
------
L'import des listes de service ne fonctionne qu'avec l'aide de l'*ordonnanceur*. Il n'y a pas d'interface utilisateur dédiée étant donné qu'aucune saisie de l'utilisateur n'est nécessaire. Aller à l'écran ``Ordonnancement des tâches`` et configurer la ``tâche`` d'import Tim. La ``tâche`` boucle sur tous les départements configurés dans la ``propriété des connecteurs`` et importe toutes les feuilles de service de chaque département. Voir également la section relative à l'ordonnanceur.

Pour l'import, le connecteur Tim ajoute les champs suivants dans la *requête* :

* Période : la période (date à partir du - date jusqu'au) pour laquelle vous voulez importer les feuilles de service. Ceci peut être fourni sous forme de critères de filtrage.
* Département : pour quel département vous voulez importer la feuille de services. Les départements sont configurables.
* Les champs qui vous intéressent (telles que informations personnelles, catégorie de feuilles de service, etc.) et que le serveur Tim doit inclure dans sa réponse.

La *réponse* d'import contient les champs suivants qui sont suffisants pour gérer les ``jours exceptionnels`` dans LibrePlan :

* Information personnelles : nom et nom du réseau
* Département : le département dans lequel l'employé travaille
* Catégorie de feuilles de service : information sur la présence/l'absence de l'employé et le motif (type d'exception LibrePlan) dans le cas où l'employé est absent
* Date: la date à laquelle l'employé est présent/absent
* Temps : l'heure de début d'absence/présence, par exemple 08:00
* Durée : nombre d'heures pendant lesquelles l'employé est présent / absent
  
En convertissant la *réponse* d'import en ``jours exceptionnels`` de LibrePlan, les transformations suivantes sont prises en compte : 

* Si la catégorie de feuilles de service contient le nom ``Vakantie`` elle sera traduite en ``RESOURCE VACANCY`` (VACANCES DES RESSOURCES).
* La catégorie ``Feestdag`` sera traduite en ``BANK HOLIDAY`` (jour férié)
* Toutes les autres comme ``Jus uren``, ``PLB uren``, etc. doivent être ajoutées manuellement aux ``jours exceptionnels du calendrier``.
   
Après la *réponse* d'import, la feuille de service est divisée en deux ou trois parties par jour : par exemple, roster-morning, roster-afternoon et roster-evening. Mais LibrePlan n'autorise qu'un seul ``type d'exception`` par jour. Le connecteur Tim est alors responsable de fusionner ces parties en un seul ``type d'exception``. Cela signifie que la catégorie de feuilles de service avec la ``durée`` la plus longue est supposée être un ``type d'exception`` valide mais la durée totale est la somme de toutes les durées de ces parties de catégorie.

Contrairement à LibrePlan, dans Tim Enterprise, la ``durée totale`` dans le cas où l'employé est en vacances signifie que l'employé n'est pas disponible pour cette ``durée totale``. Dans LibrePlan au contraire, si l'employé est en vacances, la durée totale sera ``Zéro``. Le connecteur Tim prend également en charge de cette transformation.
 
