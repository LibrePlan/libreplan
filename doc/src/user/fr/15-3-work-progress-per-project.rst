Rapport de travail et d'avancement par projet
#############################################

.. contents::

Objet
=====

Ce rapport vous permet de voir l'état global des projets en tenant compte de deux variables : l'avancement et le coût.

L'état courant de l'avancement du projet est analysé en le comparant avec la prévision selon la planification et selon le travail consacré.

Sont également affichés plusieurs taux liés aux coûts des projets en comparant les performances actuelles avec les performances théoriques.

Paramètres d'entrée et filtres
==============================

Il y a plusieurs paramètres obligatoires. Ce sont :

   * **Date de référence**. C'est la date à prendre comme référence pour faire la comparaison avec l'état planifié anticipé du projet à cette date avec la performance réelle du projet à cette même date. *La valeur par défaut pour ce champ est la date actuelle*.

   * **Type d'avancement**. C'est le type d'avancement qui est souhaité pour être utilisé pour mesurer les progrès des projets. Dans l'application un projet peut être mesuré simultanément avec différents types d'avancement et celui sélectionné par le composant déroulant par l'utilisateur est celui utilisé pour calculer les données du rapport. La valeur par défaut pour le *type d'avancement* est *étendu* qui est un type d'avancement spécial consistant à utiliser la façon préférée de mesurer l'avancement configuré dans chaque élément de la structure hiérarchique arborescente.

Quant aux champs facultatifs, ce sont :

   * **Date de début**. C'est la date minimum de démarrage des projets à inclure dans le rapport. Il est facultatif. Si aucune date de début n'est renseignée, il n'y a pas de date minimum pour les projets.

   * **Date de fin**. C'est la date maximum de fin des projets à inclure dans le rapport. Tous les projets qui finissent après la *Date de fin* seront exclus.

   * **Filtre par projet**. Ce filtre permet de choisir les projets pour lesquels l'utilisateur veut limiter les données du rapport à extraire. Si aucun projet n'est ajouté au filtre, le rapport est affiché pour tous les projets dans la base de données. Il existe une liste déroulante pour trouver le projet désiré. Ils sont ajoutés au filtre en appuyant au bouton *Ajouter*.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Type d'avancement**. Le type d'avancement utilisé pour le rapport.
   * **Projets**. Les projets pour lequel le rapport a été obtenu. Il affichera *Tous* quand ce filtre n'est pas utilisé.
   * **Date de début**. La date de début de filtrage. Ne sera pas affichée si ce filtre n'a pas été renseigné.
   * **Date de fin**. La date de fin de filtrage. Ne sera pas affichée si ce filtre n'a pas été renseigné.
   * **Date de référence**. La date de référence d'entrée obligatoire choisie pour extraire le rapport.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient une liste de tableaux relatifs à chacun des projets qui ont été retenus par les filtres d'entrée.

Les filtres fonctionnent en ajoutant des conditions à l'exception des filtres de date (*Date de début*, *Date de fin*) pris ensemble et du *filtre par projets*. Dans ce cas, si l'un ou les deux filtres date sont renseignés et qu'en même temps le *filtre par projets* est renseigné avec une liste de projets, c'est ce dernier filtre qui a la priorité. Ceci signifie que les projets qui sont inclus dans le rapport sont ceux fournis par le *filtre par projets* sans tenir compte des filtres de date.

Une autre chose importante est que les avancements présents dans le rapport généré sont calculés en fraction d'unité. Leurs valeurs seront donc comprises entre 0 et 1.

Pour chaque projet retenu pour faire partie du rapport généré, les informations suivantes sont affichées :

   * *Le nom du projet*.
   * *Total des heures*. 

      Le total des heures du projet est affiché en ajoutant les heures de chaque tâche. Deux types de totaux d'heures sont affichés :

      * *Estimées (TE)*. Cette quantité est l'addition de toutes les heures dans la structure hiérarchique arborescente. C'est le nombre total d'heures estimé nécessaire pour achever le projet.
      * *Planifiées (TP)*. Dans *LibrePlan*, il est possible d'avoir deux valeurs différentes. Le nombre estimé à l'avance des heures nécessaires pour réaliser une tâche et les heures planifiées qui sont les heures affectées dans la planification pour réaliser la tâche. Les heures planifiées peuvent être égales, inférieures ou supérieures aux heures estimées et sont déterminées dans une phase ultérieure, l'opération d'affectation. Ainsi, le total des heures planifiées d'un projet est l'addition de toutes les heures affectées aux tâches.

   * *Avancement*. 

      Trois mesures liées à l'avancement au moment de la date de référence et en fonction du type d'avancement indiqué dans le filtre sont affichées :

      * *Mesuré (AM) ou Mesured Progress (PM)*. C'est l'avancement global établi en additionnant toutes les mesures d'avancement ayant une date inférieure à la *date de référence* indiquée. En parallèle, toutes les tâches sont prises en compte et l'addition est pondérée par le nombre d'heures de toutes ces tâches.
      * *Imputé (AI) ou Progress Imputed (PI)*. C'est l'avancement calculé en considérant que le travail avancera sur le même rythme que celui des heures consacrées pour la tâche. Si X heures sur Y d'une tâche ont été faites, on considère que l'avancement global imputé est de X/Y.
      * *Planifié (AP) ou Planned Progress (PP)*. C'est l'avancement global du projet conformément à la planification théorique à la date de référence. Si les choses se passent exactement comme planifié, l'avancement mesuré doit être le même que celui planifié.

   * *Heures à ce jour*. 

      Il existe deux champs qui affichent le nombre d'heures jusqu'à la date de référence selon deux points de vue :

      * *Planifiées (HP)*. Ce nombre est l'addition des heures affectées à toutes les tâches du projet qui ont une date inférieure ou égale à la *date de référence*.
      * *Réelles (HR)*. Ce nombre est l'addition des heures rapportées dans les relevés d'heures de travail de toutes les tâches du projet ayant une date inférieure ou égale à la *date de référence*.

   * *Différence*. 

      Sous ce titre il y a plusieurs métriques liées au coûts :

      * *Coût*. C'est la différence en heures entre le nombre d'heures dépensées en prenant en compte l'avancement mesuré et les heures consacrées jusqu'à la *date de référence*. La formule est *AM*TP - HR* (*PM*TP - HR*).
      * *Planifiée*. C'est la différence entre les heures dépensées selon la mesure globale du projet global et le nombre d'heures planifiées jusqu'à la *date de référence*. Cela mesure l'avance ou le retard en temps.
      * *Ratio de coût*. Il est calculé en divisant *AM* / *AI* (*PM* / *PI*). S'il est supérieur à 1, cela signifie que le projet est bénéficiaire à ce point et s'il est inférieur à 1, cela signifie que le projet perd de l'argent.
      * *Ratio planifié*. Il est calculé en divisant *AM* / *AP* (*PM* / *PP*). S'il est supérieur à 1, cela signifie que le projet est en avance et s'il est inférieur à 1, que le projet est en retard
