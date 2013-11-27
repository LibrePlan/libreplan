Rapport des heures travaillées par ressources
#############################################

.. contents::

Objet
=====

Ce rapport permet d'extraire la liste des tâches et le temps qui leur a été consacré par les ressources sur une période de temps. Il existe plusieurs filtres qui permettent d'ajuster la requête pour obtenir les informations souhaitées et éviter les données superflues.

Paramètres en entrée et filtres
===============================

   * **Dates**.
      * *Date de début.* C'est la date minimum des rapports de travail qui sont demandés. Les rapports avec des dates antérieures à la *date de début* sont ignorés. Si le paramètre n'est pas renseigné, il n'y a pas de date de filtrage d'antériorité.
      * *Date de fin.* C'est la date maximum des rapports de travail qui seront inclus dans les résultats. Les rapports de travail avec une date postérieure à la *date de fin* seront sautés. Si le paramètre n'est pas renseigné, il n'y a pas de limite supérieure de filtrage des rapports de travail.

   * **Filtrer par ressources**
      * Vous pouvez choisir un ou plusieurs employés (ou machines) pour restreindre les rapports de travail au suivi des heures de travail de ces employés et/ou machines. Si vous laissez ce filtre vide, les rapports de travail sont récupérés indépendemment des employés et des machines.

   * **Filtrer par étiquettes**
      * Vous pouvez ajouter une ou plusieurs étiquettes en les cherchant dans le sélecteur et en appuyant sur le bouton *Ajouter*. Il faut choisir si on ne retient que les relevés des heures de travail ayant ces étiquettes, ou que les tâches, ou les relevés et les tâches associés ayant en commun ces étiquettes ou n'importe lequel des deux (valeur par défaut).

   * **Filtrer par critères**
      * Vous pouvez choisir un ou plusieurs critères en les cherchant dans le sélecteur et, ensuite, en cliquant sur le bouton *Ajouter*. Ces critères sont utilisés pour choisir les ressources qui satisfont au moins l'un d'eux. Le rapport affichera le cumul du temps consacré par les ressources satisfaisant l'un des critères du filtrage.

   * **Format de sortie**
      * Peut être HTML, ODT ou PDF.

Résultats
=========

En-tête
-------

Dans l'en-tête du rapport, les filtres appliqués lors de l'extraction des données utilisées pour le rapport sont rappelés.

Pied de page
------------

La date à laquelle le rapport a été généré est indiquée.

Corps
-----

Le corps du rapport comporte plusieurs groupes d'informations.

* On trouve d'abord une première agrégation des informations par ressource et par jour, pour chaque tâche. Chaque ressource est identifiée par :

   * *Employé* : Nom, Prénom
   * *Machine* : Nom.

Suit une ligne récapitulant le nombre total d'heures travaillées par jour par la ressource.

* Il y a un deuxième niveau de regroupement selon la *date*. Tous les relevés d'heures de travail provenant d'une ressource concrète à la même date sont regroupés ensembles.

Il y a une ligne de récapitulation avec le nombre total d'heures travaillées par ressource sur l'ensemble de la période.

* il y a un dernier niveau où sont listés les relevés des heures de travail appartenant au même jour pour l'employé. Les informations qui sont affichées dans les colonnes de chaque ligne du rapport sont :

   * *Code/nom de la tâche* l'imputation des heures comptabilisées.
   * *Heure de début* Elle n'est pas obligatoire. C'est l'heure de début de travail pour la ressource sur la tâche considérée.
   * *Heure de fin*. Elle n'est pas obligatoire. C'est l'heure de fin de travail pour la ressource sur la tâche considérée.
   * *Champs texte*. Il est facultatif. Si les lignes de rapport de travail ont des champs textes renseignés, ils sont affichés ici. Le format est : <nom du champ texte>:<valeur>
   * *Étiquettes*. Cette colonne est présente si le relevé d'heures travaillées possède un champ étiquette dans sa définition. S'il y a plusieurs étiquettes, elles sont affichées dans la même colonne. Le format est <nom du type d'étiquette>:<valeur de l'étiquette>.
