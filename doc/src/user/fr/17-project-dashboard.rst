Tableau de bord projet
######################

.. contents::

Le tableau de bord projet est une perspective *LibrePlan* pour un projet donné qui contient une série d' **indicateurs clefs de performance** ou **KPI (Key Performance Indicators)** qui aident à connaître ses performances en fonction :

   * de comment le travail avance
   * de combien il coûte
   * de l'état des ressources affectées
   * des contraintes de temps.

Indicateurs de performance d'avancement
=======================================

Il existe deux indicateurs calculés : le pourcentage d'avancement projet et l'état des tâches.

Pourcentage d'avancement projet
-------------------------------

Il s'agit d'un graphique où l'avancement global d'un projet est calculé et comparé aux valeurs attendues d'avancement que le projet devrait avoir selon le diagramme de *Gantt*.

L'avancement est représenté avec deux barres :

   * *Avancement actuel*. C'est l'avancement tel qu'il est réellement au moment où la mesure est faite.
   * *Avancement attendu*. C'est l'avancement que le projet devrait avoir en ce moment selon la planification qui a été réalisée.

L'avancement global du projet est estimé de différentes façons car il n'y a pas qu'une seule bonne méthode pour le faire :

   * **Avancement étendu**. C'est le type d'avancement positionné comme avancement étendu au niveau projet. Dans ce cas, il n'y a aucun moyen de calculer une valeur attendue et seule la barre de la valeur actuelle est affichée.
   * **Par heures de toutes les tâches**. L'avancement de toutes les tâches du projet est moyenné pour calculer la valeur globale. C'est une moyenne pondérée prenant en compte le nombre d'heures affecté à chaque tâche.
   * **Par heures du chemin critique**. L'avancement des tâches appartenant à un quelconque chemin critique du projet est moyenné pour obtenir la valeur globale. On réalise une moyenne pondérée prenant en compte les heures totales allouées à chacune des tâches impliquées.
   * **Par durée des chemins critiques**. L'avancement des tâches appartenant à un quelconque chemin critique est moyenné pour obtenir une moyenne pondérée mais cette fois en prenant en compte la durée de chaque tâche impliquée au lieu des heures allouées.

État des tâches
---------------

Un diagramme camembert montrant le pourcentage des tâches du projet étant dans différents états. Les états définis sont les suivants :

   * **Finies**. Ce sont les tâches achevées, détectées par une valeur d'avancement mesurée de 100%.
   * **En cours**. Ce sont les tâches qui sont en cours. Elles ont une valeur d'avancement différente de 0% et de 100% et ayant du temps de travail enregistré.
   * **Prêtes à démarrer**. Elles ont un avancement de 0%, n'ont pas de temps de travail enregistré et toutes les tâches avec lesquelles elles ont une dépendance *FINI_POUR_DÉMARRER* sont finies et toutes les tâches avec lesquelles elles ont une dépendance *DÉMARRÉE_POUR_DÉMARRER* sont *finies* ou *en cours*.
   * **Bloquées**. Ce sont les tâches avec un avancement à 0%, sans temps de travail enregistré et avec des tâches dont elles dépendent ni dans l'état *en cours* ni *prête à démarrer*.

Indicateurs de coût
===================

Il existe plusieurs indicateurs de coûts de *gestion de la valeur acquise* qui sont calculés :

   * **VC (Variance du coût) ou CV (Cost Variance)**. Différence entre la *courbe de la valeur acquise* et la *courbe du coût actuel* à un instant donné. Les valeurs positives indiquent un bénéfice et les négatives une perte.
   * **CRTR (Coût réel du travail réalisé) ou ACWP (Actual Cost Work Performed)**. C'est le nombre total d'heures suivies dans le projet à un instant donné.
   * **ICP (Indice de Coût Performance) ou CPI (Cost Performance Index)**. C'est le rapport *Valeur acquise / Coût actuel*.

     * > 100 est bon, signifie être sous le budget.
     * = 100 est également bon, signifie que le coût est exactement conforme au coût planifié.
     * < 100 est mauvais, signifie que le coût pour achever le travail est plus élevé que planifié.

   * **TEA (Temps estimé d'achèvement) ou ETC (Estimate To Complete)**. C'est le temps qu'il reste à consacrer au projet pour le terminer.
   * **BAC (Budget à l'achèvement / Budget At Completion)**. C'est le volume total de travail alloué dans le plan projet.
   * **EAA (Estimation à l'achèvement) ou EAC (Estimate At Completion)**. C'est la projection du gestionnaire du coût total au moment de l'achèvement du projet selon l'*ICP*.
   *  **VAA (Variance à l'achèvement) ou VAC (Variance At Completion)**. C'est la différence entre le *BAC* et le *TEA*.

      * < 0 est inférieur au budget.
      * > 0 est supérieur au budget.

Ressources
==========

Pour analyser le projet du point de vue des ressources, deux taux et un histogramme sont fournis.

Histogramme de dérive d'estimation sur les tâches achevées
----------------------------------------------------------

Il s'agit de la dérive calculée entre le nombre d'heures allouées aux tâches du projet et du nombre éventuel d'heures qui lui ont été consacrées.

La dérive est calculée en pourcentage pour toutes les tâches terminées et les dérives calculées sont représentées dans un histogramme. Sur l'axe vertical, le nombre de tâches qui se trouvent dans un intervalle de dérive est affiché. Six intervalles de dérivations sont calculés dynamiquement.

Taux de dépassement de temps
----------------------------

Il résume la surcharge des ressources qui sont affectées aux tâches du projet.
Il est calculé selon la formule : **taux de dépassement = surcharge / (charge + surcharge)**.

   *  = 0 est bon, signifie que les ressources ne sont pas surchargées.
   *  > 0 est mauvais, signifie que les ressources sont surchargées.

Taux de disponibilité
---------------------

Il résume la capacité encore libre des ressources actuellement affectées au projet. C'est donc une mesure de la disponibilité des ressources pour recevoir d'autres affectations sans être surchargées.
Il est calculé par : **taux de disponibilité = (1 - charge / capacité)*100**.

   * Les valeurs possibles sont entre 0% (totalement affectées) et 100% (non affectées).

Temps
=====

Sont inclus deux graphiques : un histogramme pour la dérive en temps dans le temps d'achèvement des tâches du projet et un diagramme camembert pour les violations de date d'échéance.

Avance ou retard d'achèvement des tâches
----------------------------------------

Est calculée la différence en jours entre la date planifiée d'achèvement des tâches du projet et leur date de fin réelle. La date d'achèvement planifiée est tirée du *Gantt* et la date de fin réelle du dernier temps suivi pour la tâche.

Le retard ou l'avance dans l'achèvement des tâches est représenté dans un histogramme. Sur l'axe vertical le nombre de tâches avec une différence de jours d'avance ou de retard correspondant aux intervalles de jours représentés en abscisse. Six intervalles dynamiques de dérive d'achèvement de tâches sont calculés.

   * Des valeurs négatives indiquent un achèvement en avance.
   * Des valeurs positives indiquent un achèvement en retard.

Violation des échéances
-----------------------

D'un coté est calculé l'écart avec l'échéance du projet, si elle est renseignée. De l'autre, un diagramme camembert avec le pourcentage de tâches atteignant l'échéance est dessiné. Trois types de valeurs sont inclus dans le diagramme :


   * Pourcentage des tâches sans échéance indiquée.
   * pourcentage des tâches complétées avec une date de fin réelle plus tardive que leur échéance. La date de fin réelle est obtenu à partir du dernier relevé de temps pour la tâche.
   * Pourcentage des tâches achevées avec une date de fin réelle anticipée par rapport à l'échéance.

