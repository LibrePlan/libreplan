Rapport de travail et d'avancement par tâches
#############################################

.. contents::

Objet
=====

Ce rapport vous permet de voir l'état des tâches d'un projet en tenant compte de deux variables : l'avancement et le coût.

L'état courant de l'avancement des tâches est analysé en le comparant avec la prévision selon la planification et selon le travail consacré.

Sont également affichés plusieurs taux liés aux coûts des tâches en comparant les performances actuelles avec les performances théoriques.

Paramètres d'entrée et filtres
==============================

Il y a plusieurs paramètres obligatoires. Ce sont :

   * **Date de référence**. C'est la date à prendre comme référence pour faire la comparaison avec l'état planifié anticipé de la tâche à cette date avec la performance réelle de cette tâche à cette même date. *La valeur par défaut pour ce champ est la date actuelle*.

   * **Filtrer par projet**. C'est le projet pour les tâches duquel on veut générer le rapport.

Quant aux champs facultatifs, ce sont :

   * **Filtrer par critères**. Permet de ne retenir que les tâches disposant du ou des critères indiqués. Choisir un critère et cliquer sur le bouton *Ajouter*.

   * **Filtrer par étiquettes**. Permet de ne retenir que les tâches disposant de la ou des étiquettes indiquées. Choisir une étiquette et cliquer sur le bouton *Ajouter*.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Projet**. Le projet pour lequel le rapport a été obtenu. 
   * **Date de référence**. La date de référence d'entrée obligatoire choisie pour extraire le rapport.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient une liste de tableaux relatifs à chacune des tâches du projet qui ont été retenues par les filtres d'entrée.

Une autre chose importante est que les avancements présents dans le rapport généré sont calculés en fraction d'unité. Leurs valeurs seront donc comprises entre 0 et 1.

Pour chaque tâche retenue pour faire partie du rapport généré, les informations suivantes sont affichées :

   * *Le nom de la tâche*.

   * *Total des heures*. 

      Le total des heures de la tâche est affiché. Deux types de totaux d'heures sont affichés :

      * *Estimées (TE)*. Cette quantité est l'addition de toutes les heures dans la structure hiérarchique arborescente. C'est le nombre total d'heures estimé nécessaire pour achever la tâche.
      * *Planifiées (TP)*. Dans *LibrePlan*, il est possible d'avoir deux valeurs différentes. Le nombre estimé à l'avance des heures nécessaires pour réaliser une tâche et les heures planifiées qui sont les heures affectées dans la planification pour réaliser la tâche. Les heures planifiées peuvent être égales, inférieures ou supérieures aux heures estimées et sont déterminées dans une phase ultérieure, l'opération d'affectation. Ainsi, le total des heures planifiées d'un projet est l'addition de toutes les heures affectées aux tâches.

   * *Avancement*. 

      Trois mesures liées à l'avancement au moment de la date de référence et en fonction du type d'avancement indiqué dans le filtre sont affichées :

      * *Mesuré (AM) ou Mesured Progress (PM)*. C'est l'avancement global établi en additionnant toutes les mesures d'avancement ayant une date inférieure à la *date de référence* indiquée.
      * *Imputé (AI) ou Progress Imputed (PI)*. C'est l'avancement calculé en considérant que le travail avancera sur le même rythme que celui des heures consacrées pour la tâche. Si X heures sur Y d'une tâche ont été faites, on considère que l'avancement global imputé est de X/Y.
      * *Planifié (AP) ou Planned Progress (PP)*. C'est l'avancement global de la tâche conformément à la planification théorique à la date de référence. Si les choses se passent exactement comme planifié, l'avancement mesuré doit être le même que celui planifié.

   * *Heures à ce jour*. 

      Il existe deux champs qui affichent le nombre d'heures jusqu'à la date de référence selon deux points de vue :

      * *Planifiées (HP)*. Ce nombre est l'addition des heures affectées à la tâche.
      * *Réelles (HR)*. Ce nombre est l'addition des heures rapportées dans les relevés d'heures de travail pour cette tâche ayant une date inférieure ou égale à la *date de référence*.

   * *Différence*. 

      Sous ce titre il y a plusieurs métriques liées au coûts :

      * *Coût*. C'est la différence en heures entre le nombre d'heures dépensées en prenant en compte l'avancement mesuré et les heures consacrées jusqu'à la *date de référence*. La formule est *AM*TP - HR* (*PM*TP - HR*).
      * *Planifiée*. C'est la différence entre les heures dépensées et le nombre d'heures planifiées jusqu'à la *date de référence*. Cela mesure l'avance ou le retard en temps.
      * *Ratio de coût*. Il est calculé en divisant *AM* / *AI* (*PM* / *PI*). S'il est supérieur à 1, cela signifie que la tâche est bénéficiaire à ce point et s'il est inférieur à 1, cela signifie que la tâche perd de l'argent.
      * *Ratio planifié*. Il est calculé en divisant *AM* / *AP* (*PM* / *PP*). S'il est supérieur à 1, cela signifie que la tâche est en avance et s'il est inférieur à 1, que la tâche est en retard
