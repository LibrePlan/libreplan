Rapport des heures estimées/planifiées par tâche
################################################

.. contents::

Objet
=====

Ce rapport vous permet de voir un récapitulatif des heures estimées et planifiées pour les tâches d'un projet.

Paramètres d'entrée et filtres
==============================

Il faut qu'au moins le paramètre suivant soit renseigné :

   * **Filtrer par projet**. C'est le projet pour les tâches duquel on veut générer le rapport. 

Autre paramètre obligatoire :

   * **Date de référence**. C'est la date de référence qui servira pour afficher les heures estimées et planifiées en cours de projet. Par défaut, il s'agira de la date du jour.

Il est possible de restreindre le rapport en utilisant les paramètres facultatifs suivants :

   * **Filtrer par étiquettes**. Permet de ne retenir que les tâches disposant de la ou des étiquettes indiquées. Choisir une étiquette et cliquer sur le bouton *Ajouter*.

   * **Filtrer par critères**. Permet de ne retenir que les tâches disposant du ou des critères indiqués. Choisir un critère et cliquer sur le bouton *Ajouter*.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Projet**. Le projet pour lequel le rapport a été obtenu s'il a été indiqué.
   * **Date de référence**. La date de référence utilisée pour le rapport.
   * **Critères**. Les critères restreignant les tâches si certains ont été indiqués.
   * **Étiquettes**. Les étiquettes restreignant les tâches si certaines ont été indiquées.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient un tableau contenant les colonnes suivantes:

   * **Nom de tâche**
   * **Heures totales**, les heures totales de la tâche détaillant :
      * **Estimées** : total des heures prévues
      * **Planifiées** : total des heures déjà planifiées
   * **Heures à ce jour**, les heures comptabilisées jusqu'à la date de référence détaillant :
      * **Estimées** : total des heures prévues
      * **Planifiées** : total des heures déjà planifiées
