Rapport des coûts de projets
############################

.. contents::

Objet
=====

Ce rapport vous permet de voir le coût d'un ou plusieurs projets, qu'il s'agisse des coûts horaires des ressources ou des dépenses effectuées.

Paramètres d'entrée et filtres
==============================

Il y a plusieurs paramètres facultatifs disponibles. Ce sont :

   * **Filtrer par projet**. C'est le ou les projets pour les tâches duquel ou desquels on veut générer le rapport. Si aucun projet n'est renseigné, les coûts de tous les projets seront successivement indiqués.

   * **Dates**.
      * *Travail effectué depuis*. C'est la date minimum des rapports de travail qui sont demandés. Les rapports avec des dates antérieures à cette date de début sont ignorés. Si le paramètre n'est pas renseigné, il n'y a pas de date de filtrage d'antériorité.
      * *Travail effectué jusqu'au*. C'est la date maximum des rapports de travail qui seront inclus dans les résultats. Les rapports de travail avec une date postérieure à cette date ne seront pas pris en compte. Si le paramètre n'est pas renseigné, il n'y a pas de limite supérieure de filtrage des rapports de travail.

   * **Filtrer par critères**. Permet de ne retenir que les tâches disposant du ou des critères indiqués. Choisir un critère et cliquer sur le bouton *Ajouter*.

   * **Filtrer par étiquettes**. Permet de ne retenir que les tâches disposant de la ou des étiquettes indiquées. Choisir une étiquette et cliquer sur le bouton *Ajouter*.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Les dates de début et/ou de fin** si elles ont été renseignées.
   * **Projet**. Les projets pour lesquels le rapport est généré. Si aucun n'est indiqué, *Tous les projets* sera indiqué.
   * **Étiquettes**. Les étiquettes si certaines ont été indiquées.
   * **Critères**. Les critères si certains ont été indiqués.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient une liste de sections et de sous-sections.
On trouve d'abord une section par projet indiquant le nom et le code du projet. Dans chacune, on trouve des sous-sections concernant chacune des tâches du projet ayant fait l'objet de relevés d'heures de travail. Figurent le nom et le code de la tâche.
Enfin, dans chacune de ces sous-sections, on trouve un tableau par ressource indiquant:

   * les noms et prénoms des employés ou le nom des machines.
   * un tableau comportant les colonnes suivantes :

      * *Date*. La date du relevé des heures de travail
      * *Type d'heures*. Le type d'heures concerné (par exemple: par défaut, heures supplémentaires ...).
      * *Salaire*. Le prix horaire de ce type d'heures.
      * *Heures*. Le nombre d'heures réalisées.
      * *Coûts*. Le coût de ces heures obtenu en multipliant les deux colonnes précédentes.

Sous chaque tableau on trouve le total des heures et des coûts de la ressource concernée.

On trouve ensuite un tableau avec pour chaque relevé de dépenses les colonnes suivantes :

   * *Date*. La date de la dépense.
   * *Catégorie*. La catégorie de la dépense.
   * *Ressource*. La ressource à l'origine de la dépense.
   * *Coûts*. Les coûts de la dépense.

Sous ce tableau, on trouve le total des dépenses pour la tâche.

En fin de chaque section, on trouve le total des heures et des coûts de toutes les ressources ayant travaillé sur la tâche concernée.

En fin de chaque section de projet, on trouve le *Total projet* comportant :

   * Le *temps passé*. Somme des heures effectuées pour l'ensemble des tâches par toutes les ressources impliquées
   * L'*argent dépensé* qui présente successivement :

      * le *coût des heures*
      * le *coût des dépenses*
      * le *total* des 2 coûts précédents
