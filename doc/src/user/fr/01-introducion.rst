Introduction
#############

.. contents::

L'objet de ce document est de décrire les fonctionnalités de LibrePlan et de fournir à l'utilisateur les informations nécessaires pour configurer et utiliser l'application.

LibrePlan est une application web Open Source destinée à la planification de projets. Son objet premier consiste à fournir une solution complète pour la gestion des projets d'une compagnie.
Pour tout renseignement plus spécifique dont vous pourriez avoir besoin sur ce logiciel, merci de contacter l'équipe de développement sur http://www.libreplan.com/contact/


.. figure:: images/company_view.png
   :scale: 50

   Vue de la compagnie

.. NOTE::

   NdT : Dans la traduction de ce guide, on utilisera le terme de ``compagnie`` pour désigner aussi bien une entreprise qu'un organisme ayant à gérer des projets. De la même façon, on parlera de ``projet`` alors qu'on trouve dans la documentation en anglais alternativement les termes de ``projet`` et de ``commande``, ce dernier terme ayant une connotation commerciale pas toujours applicable aux organismes tels que les administrations par exemple. C'est pour cela également que ce manuel utilisera systématiquement le terme de ``tâche`` alors qu'on trouve à certains endroits de la documentation anglaise ``ligne de commande``. Pour résumer, nous nous intéressons à une ``compagnie``, qu'il s'agisse d'un organisme ou d'une entreprise, planifiant des ``projets`` qui peuvent être des commandes commerciales provenant de clients, mais pas obligatoirement, eux-mêmes découpés en ``tâches`` qui peuvent correspondre à des lignes de commandes pour les entreprises.

Vue de la compagnie et perspectives
===================================

Comme on peut le voir dans la vue de la compagnie qui est l'écran principal de l'application (cf. capture d'écran ci-dessus), les utilisateurs peuvent accéder à la liste des projets planifiés, ce qui leur donne une vision globale des projets gérés et de l'utilisation des ressources de la compagnie. On peut changer de perspective en cliquant sur les icônes de la colonne de gauche :

* La perspective *Planification des projets* sur laquelle le programme s'ouvre et qui combine deux points de vue :

   * Vue des projets et du temps consommé : en colonne de gauche, le nom de chaque projet suivi de ses dates de début et de fin. En face, à droite, les diagrammes de Gantt correspondant dans lesquels on retrouve ces dates ainsi qu'une comparaison entre le pourcentage d'avancement et le temps qui a été effectivement dédié à chaque projet. Ces informations donnent une image claire de ce que la compagnie fait à un moment donné.

   * Graphiques : ce point de vue comporte à son tour deux onglets :

      * Charge : graphique montrant l'utilisation faite par la compagnie de ses ressources. En utilisant les informations relatives à l'affectation des ressources aux projets, il en présente une synthèse. La ligne noire indique la capacité de ressources disponible chaque jour; le vert indique que l'affectation des ressources est inférieure à 100%, l'orange qu'elle est supérieure à 100%. À noter qu'il est possible que l'affectation soit inférieure à la capacité de ressources disponibles tout en étant en sur-affectation, ce qui signifie que certaines ressources sont sur-affectées alors que, globalement, on est en sous-affectation. La légende est rappelée en partie gauche.

      * Valeur gagnée : ce graphique s'affiche quand on clique sur l'onglet correspondant à gauche de la zone des graphiques. Il contient une courbe des indicateurs et métriques sélectionnés en partie gauche à l'aide des cases à cocher à la date choisie (date du jour par défaut).

.. figure:: images/company_view_earned_value.png
   :scale: 50

   Graphique de la valeur gagnée 

* La perspective *Liste des projets* : accessible en cliquant sur l'icône de même nom à gauche, elle présente la liste des projets assorties d'informations complémentaires : dates de début et de fin, client, budget total, heures dépensées et état. Les icônes en fin de ligne permettent de réaliser les opérations suivantes : modifier le projet, supprimer le projet, voir la planification détaillée de ce projet et créer un modèle de projet à partir de ce projet.

* La perspective *Charge des ressources* : accessible en cliquant sur l'icône de même nom à gauche, elle affiche la liste des ressources de la compagnies (employés et machines) suivi de leur charge respective jour par jour en fonction de leurs affectations à des tâches spécifiques ou du fait d'une affectation générique quand les ressources satisfont la liste des critères correspondants. En-dessous, on retrouve le graphique de charge des ressources vu précédemment dans lequel on trouve un code couleur supplémentaire : jaune, quand les ressources sont affectées à exactement 100%.

* La perspective *Calendrier des ressources limitantes* : accessible en cliquant sur l'icône de même nom à gauche, elle affiche la liste des ressources limitantes détenues par la compagnie et leur charge jour par jour.

On peut également accéder à ces perspectives en utilisant les sous-menus de même nom du menu "Vue de la compagnie".


.. figure:: images/resources_global.png
   :scale: 50

   Vue d'ensemble des ressources

.. figure:: images/order_list.png
   :scale: 50

   Structure de Découpage du Projet

La vue d'ensemble de la compagnie commentée précédemment est très semblable à la vue de planification d'un projet donné. On peut accéder à un projet de plusieurs façons :

* En faisant un clic droit sur le diagramme de Gantt du projet concerné puis en choisissant l'option de menu *Planification*
* En accédant à la liste des projets et en cliquant sur l'icône des diagrammes de Gantt.
* En créant un nouveau projet (icône avec un plus en haut à gauche) puis, une fois les informations saisies, en cliquant sur ``Valider``. 

Les perspectives suivantes sont disponibles pour un projet :

* Planification du projet: perspective dans laquelle l'utilisateur peut visualiser la planification des tâches, les dépendances, les jalons, etc. Voir la section *Planification* pour davantage d'informations.
* Détails du projet : perspective dans laquelle un utilisateur peut modifier les détails d'un projet. Voir la section *Projets* pour davantage d'informations.
* Charge des ressources : perspective dans laquelle l'utilisateur peut vérifier la charge des ressources affectées au projet. Le code couleur est le même que celui utilisé pour la vue d'ensemble de la compagnie : vert pour une charge inférieure à 100%, jaune pour une charge égale à 100% et rouge pour une charge supérieure à 100%. La charge peut provenir d'une tâche ou d'une liste de critères (affectation générique).
* Affectation avancée : perspective dans laquelle l'utilisateur peut affecter des ressources au moyen d'options avancées : choix du nombre d'heures par jour ou utilisation de fonctions d'affectation. Voir la section *Affectation de ressources* pour davantage d'informations.

On accède à ces perspectives à l'aide des icônes de même nom à gauche de l'écran.

Qu'est-ce qui rend LibrePlan utile ?
====================================

LibrePlan a été développé pour être un outil de planification générique. Il s'appuie sur une série de principes déterminés grâce à l'analyse des problèmes rencontrés en planifiant des projets de l'industrie et pour lesquels les outils de planification existant n'apportaient pas de solution. Une autre motivation était d'offrir une alternative libre et complètement web aux outils de planification propriétaires existants.

Les principes les plus importants utilisé par LibrePlan sont :

* Vue d'ensemble multi-projets de la compagnie : LibrePlan est un programme qui a été développé spécifiquement pour fournir aux utilisateurs des informations sur les projets qui sont menés dans une compagnie, c'est pourquoi c'est un programme multi-projets. Il a été décidé que l'accent ne serait pas mis sur chaque projet individuellement. Toutefois, il existe de nombreuses vues spécifiques dont certaines ne concerne qu'un projet à la fois.
* Gestion des vues : la vue d'ensemble de la compagnie ou la vue multi-projets amène les vues concernant les informations qui y sont stockées. Par exemple, la vue d'ensemble de la compagnie permet aux utilisateurs de voir les projets et de comparer leur état, de voir la charge globale des ressources de la compagnie et de conduire les projets. Les utilisateurs peuvent également voir la planification, la charge des ressources, la vue d'affectation avancée des ressources et la vue de modification des projets depuis la vue projet.
* Critères : les critères sont des entités systèmes qui permettent de différencier les ressources (employés et machines) et les tâches. Les tâches imposent des critères que les ressources doivent remplir. C'est l'un des aspects les plus importants du programme car les critères sont à la base des affectations génériques et solutionnent l'un des problèmes les plus importants du secteur pour une compagnie : l'affectation de ressources et l'obtention d'estimations de charge sur le long terme.
* Ressources : il en existe deux différentes sortes, les employés et les machines. L'affectation des employés aux tâches permet de planifier, de surveiller et de contrôler la charge de la compagnie. Les machines sont des ressources que l'on affecte de la même façon et qui répondent aux mêmes objectifs, à la nuance près qu'elles dépendent des employés qui les mettent en oeuvre.
* Affectation de ressources : l'un des points clef du programme est d'avoir la possibilité d'effectuer deux sortes de désignation : spécifique ou générique. L'affectation générique repose sur des critères qui sont nécessaires pour réaliser une tâche et qui doivent être remplis par des ressources qui sont capables de les réaliser. Voici un exemple pour comprendre l'affectation générique : John Smith est un soudeur. Pour réaliser une tâche qui nécessite un soudeur, on pourra désigner John Smith mais on peut aussi laisser "LibrePlan" réaliser une affectation générique. Dans ce cas, il cherchera parmi tous les employés de la compagnie dotés du critère "soudeur" et disponibles, sans se soucier de savoir si c'est John Smith qui a été affecté à la tâche.
* Contrôle de la charge de la compagnie : le programme offre la possibilité de contrôler facilement la charge des ressources de la compagnie. Ce contrôle est effectué au moyen et au long terme puisque les projets actuels et les futurs projets peuvent être gérés par le programme. "LibrePlan" possède des graphiques qui illustrent cette utilisation des ressources.
* Étiquettes : ce sont des éléments qui sont utilisés pour qualifier les tâches du projet. Grâce à ces étiquettes, l'utilisateur peut regrouper les tâches par domaine pour pouvoir ensuite les contrôler domaine par domaine ou les retrouver plus facilement avec un filtre.
  
* Filtres : comme on peut étiqueter ou attribuer des critères aux tâches et aux ressources, les filtres de critères ou d'étiquettes peuvent être utilisés. Ceci est très utile pour vérifier des informations catégorisées ou pour obtenir des rapports particuliers basés sur des critères ou des étiquettes.
* Calendriers : les calendriers déterminent les heures de production disponibles pour les différentes ressources. L'utilisateur peut créer des calendriers généraux pour la compagnie ou entrer des caractéristiques pour des calendriers plus spécifiques, ce qui signifie que des calendriers peuvent être réalisés sur mesure pour des tâches ou des ressources particulières.
* Projets et tâches : le travail demandé par les clients est traité par le programme comme un projet qui est organisé en tâches. Le projet et ses tâches sont attachés à une structure hiérarchique multi-niveaux. Cet arbre (SDP pour Structure de Découpage du Projet / WBS pour Work Breakdown Structure) est la base du travail de planification.
* Avancement : le programment peut gérer plusieurs types d'avancement. Un projet peut être évalué avec un pourcentage qui indique son avancement, mais également en unités, selon le budget alloué, etc. Décider quel type d'avancement utiliser pour évaluer cet avancement de façon macroscopique est de la responsabilité de la personne qui gère la planification.
* Tâches : les tâches sont les éléments de planification du programme. Elles sont utilisées pour programmer le travail qui doit être réalisé. Les caractéristiques les plus importantes des tâches sont qu'elles ont des dépendances entre elles et peuvent nécessiter que des critères soient remplis pour pouvoir y affecter des ressources.
* Rapports de travail : ce sont les rapports des employés de la compagnie qui indiquent les heures travaillées mais également les tâches affectées aux heures durant lesquelles un employé a travaillé. Avec ces informations, le système peut calculer combien d'heures cela prendra pour finir une tâche et confronter ce nombre d'heures aux heures qui avaient été budgétisées. L'avancement peut être confronté au nombre d'heures déjà consommées.

En plus de ces fonctions, il existe d'autres fonctionnalités qui différencient LibrePlan des autres programmes du même type :

* Intégration avec les ERP : le programme peut importer directement depuis l'ERP de la compagnie les informations des projets, des employés, des comptes-rendus d'heures réalisées et des critères spécifiques.
* Gestion de version : le programme peut gérer différentes versions de la planification tout en permettant aux utilisateurs d'accéder aux informations de chacune d'elles.
* Gestion de l'historique : le programme ne supprime pas d'informations, il les rend simplement invalides de telle sorte que les utilisateurs peuvent retrouver ces informations plus anciennes en utilisant les filtres de dates.

Conventions d'utilisation
==========================

Information sur les formulaires
---------------------------------
Avant de décrire les différentes fonctions associées aux modules les plus importants, nous devons donner une explication générale sur la façon de naviguer et sur les formulaires.

Essentiellement, il existe 3 types de formulaires : 

* Formulaires avec un bouton *Entrée*. Ces formulaires font partie d'un ensemble plus grand et les modifications qui sont faites sont simplement mises en mémoire. Les changements ne sont appliqués qu'une fois que l'utilisateur enregistre explicitement tous les détails de l'écran auquel appartient le formulaire.
* Formulaires avec les boutons *Enregistrer* et *Annuler*. Ces formulaires permettent la réalisation de 2 opérations distinctes. La première consiste à enregistrer les informations saisies ou modifiées puis à fermer la fenêtre actuelle tandis que la seconde se limite à fermer cette fenêtre sans enregistrer les modifications.
* Formulaires avec les boutons *Enregistrer et continuer*, *Enregistrer* et *Annuler*. Ces formulaires permettent la réalisation de 3 opérations. La première enregistre et poursuit dans le formulaire actuel. La deuxième enregistre et ferme le formulaire. Enfin, la troisième ferme la fenêtre du formulaire sans enregistrer les modifications.

Icônes standards et boutons
---------------------------

* Modifier : en général, modifier un élément peut être réalisé en cliquant sur un icône représentant un crayon sur un bloc note.
* Indenter à gauche : en général, ces opérations sont nécessaires pour des éléments d'un arbre qui doivent être déplacés dans des sous-niveaux. Cette opération peut être réalisée en cliquant sur l'icône représentant une flèche verte dirigée vers la droite.
* Indenter à droite : en général, ces opérations sont nécessaires pour des éléments d'un arbre qui doivent être déplacés d'un niveau inférieur vers des niveaux supérieurs. Cette opération peut être réalisée en cliquant sur l'icône représentant une flèche verte dirigée vers la gauche.
* Supprimer : les utilisateurs peuvent supprimer des informations en cliquant sur l'icône corbeille à papier.
* Chercher : la loupe est l'icône qui indique que le texte entré à sa gauche est destiné à chercher des éléments.

Onglets
--------
On trouve des formulaires de modification et d'administration qui sont représentés sous forme d'onglets. Cette méthode est utilisée pour organiser les informations d'une façon exhaustive en différentes sections qui peuvent être atteintes en cliquant sur le nom des différents onglets, les autres conservant leur état. Dans tous les cas, les options d'enregistrement et d'annulation affectent tous les sous-formulaires des différents onglets.

Raccourcis et aide contextuelle
---------------------------------------

Des informations complémentaires sont disponibles pour certains éléments. Elles apparaissent quand l'élément en question est survolé par le curseur de la souris pendant une seconde.
Les actions que l'utilisateur peut réaliser dans le programme sont indiquées sur les onglets boutons et dans les textes d'aide les concernant, les options du menu de navigation et les options des menus contextuels qui s'ouvrent quand on effectue un clic droit dans la zone de planification (diagrammes de Gantt).
Il existe également des raccourcis pour les principales opérations disponibles en double-cliquant sur les éléments des listes. Le curseur et la touche entrée peuvent également être utilisés, notamment dans les formulaires.

