Rapport d'état de planification des tâches d'un projet
######################################################

.. contents::

Objet
=====

Ce rapport vous permet de voir l'état d'avancement et de prévision des tâches d'un projet.

Paramètres d'entrée et filtres
==============================

Le projet est le seul paramètre obligatoire. Il existe d'autres paramètres facultatifs :

   * **Filtre par état des tâches**. ``Tous`` par défaut. Il est possible de se limiter à un état particulier parmi les états suivants: ``Bloquée``, ``Finie``, ``En cours``, ``En attente`` ou ``Prête à démarrer``.

   * **Montrer les dépendances**. Cocher cette case pour afficher les dépendances des tâches dans le rapport.

   * **Filtrer par étiquettes**. Permet de ne retenir que les tâches disposant de la ou des étiquettes indiquées. Choisir une étiquette et cliquer sur le bouton *Ajouter*.

   * **Filtrer par critères**. Permet de ne retenir que les tâches disposant du ou des critères indiqués. Choisir un critère et cliquer sur le bouton *Ajouter*.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Projet**. Le projet pour lequel le rapport a été obtenu. 
   * **État**. L'état des tâches objet du rapport. ``Toutes`` par défaut.
   * **Étiquettes**. Les étiquettes choisies s'il y en a.
   * **Critères**. Les critères choisis s'il y en a.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient une liste de tableaux relatifs à chacune des tâches du projet qui ont été retenues par les filtres d'entrée.

Sont d'abord affichés le nom et le code de la tâche puis un tableau comportant les colonnes suivantes :

   * *Date de début*. Comporte à son tour deux sous-colonnes :

      * *Estimée* : la date de début estimée
      * *Premier relevé* : date du premier relevé d'heures de travail

   * *Date de fin*. Comporte à son tour trois sous-colonnes :

      * *Estimée* : la date de fin estimée
      * *Dernier relevé* : date du dernier relevé d'heures de travail
      * *Date limite* : date limite pour terminer la tâche

   * *Avancement* : le pourcentage d'avancement de la tâche
   * *État* : l'état de la tâche parmi ceux mentionnés plus hauts
   * *Date limite* : date d'échéance de la tâche

Si la case *Montrer les dépendances* a été cochée, un second tableau suit le premier. Il comporte une ligne par dépendance comprenant les colonnes suivantes :

   * *Nom* : nom de la tâche dont dépend la tâche courante
   * *Code* : le code de cette tâche
   * *Type* : le type de la dépendance
   * *Avancement* : avancement de la tâche, sous forme fractionnaire (de 0 à 1).
