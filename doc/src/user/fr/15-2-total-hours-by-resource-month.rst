Rapport du total des heures travaillées par ressources et par mois
##################################################################

.. contents::

Objet
=====

Ce rapport permet d'obtenir le nombre total d'heures travaillées par les ressources dans un mois. Ceci peut être utile pour connaître les dépassements de temps d'un employé, ou, selon la compagnie, la quantité d'heures à payer pour chaque ressource.

L'application permet de suivre les relevés d'heures travaillées des employés et des machines. En conséquence, le rapport dans le cas des machines fait la somme du nombre des heures durant lesquelles elles ont fonctionné durant le mois.

Paramètres d'entrée et filtres
==============================

Il faut indiquer l'année et le mois pour obtenir le nombre total d'heure travaillées par les ressources.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête du rapport, sont affichés :

   * L' *année* à laquelle se rapportent les données du rapport.
   * Le *mois* auquel se rapportent les données du rapport.

Pied de page
------------

La date pour laquelle le rapport a été demandé.

Corps
-----

La zone des données du rapport comporte juste une section dans laquelle une table à 2 colonnes est affichée :

   * une colonne appelée *Nom* pour le nom de la ressource.
   * une colonne appelée *Heures* avec la somme de toutes les heures travaillées par cette ressource.

Il y a une ligne finale agrégeant le total des heures consacrées par toutes les ressources dans le mois de l'année pour lequel le rapport a été fait.
