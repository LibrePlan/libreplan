Rapport des besoins en matériaux à une date donnée 
##################################################

.. contents::

Objet
=====

Ce rapport vous permet de voir la liste des matériaux dont les projets auront besoins pendant une certaine période de temps.

Paramètres d'entrée et filtres
==============================

Il y a plusieurs paramètres obligatoires. Ce sont :

   * **Date de début**. C'est la date de début de la période objet du rapport. Par défaut, c'est la date du jour.

   * **Date de fin**. C'est la date de fin de la période objet du rapport. Par défaut, c'est 1 mois après la date actuelle. 

   * **État**. C'est une liste qui permet de restreindre le rapport aux matériaux dans un état de disponibilité donné : ``REÇU``, ``EN ATTENTE``, ``COMMANDÉ``, ``EN TRAITEMENT``, ``ANNULÉ`` ou  ``Tous`` qui est la valeur par défaut.

Quant aux champs facultatifs, ce sont :

   * **Filtrer par projet**. Ce filtre permet de choisir le ou les projets pour lesquels on veut limiter les données du rapport à extraire. Si aucun projet n'est ajouté au filtre, le rapport est affiché pour tous les projets dans la base de données. Il existe une liste déroulante pour trouver le projet désiré. Ils sont ajoutés au filtre en appuyant au bouton *Ajouter*.

   * **Filtrer par catégories ou matériaux**. Ce filtre permet de restreindre le rapport à un ou plusieurs matériaux ou catégories de matériaux. Pour sélectionner plusieurs éléments, utiliser la touche CTRL.

Sortie
======

Le format de sortie est le suivant :

En-tête
-------

Dans l'en-tête de rapport les champs suivants sont affichés :

   * **Date de début**. La date de début de filtrage.
   * **Date de fin**. La date de fin de filtrage.
   * **État**. État des matériaux pour lequel le rapport a été limité.

Pied de page
------------

La date à laquelle le rapport a été généré est affichée.

Corps
-----
 
Le corps contient une liste de tableaux, un par jour pour lequel un ou plusieurs matériaux sont nécessaires. Ce tableau comporte les lignes suivantes :

   * **Matériau**. Le matériau concerné.
   * **Projet - tâche**. Le projet (nom et code) et la tâche concernée.
   * **Disponibilité**. Disponibilité du matériau.
   * **Unité**. Nombre ou quantité nécessaire.
   * **Prix unitaire**. Prix unitaire du matériau.
   * **Prix**. Prix du nombre ou de la quantité de matériau.
   * **État**. État de la commande de matériau.

Sous chaque tableau, on trouve le prix total des matériaux.
