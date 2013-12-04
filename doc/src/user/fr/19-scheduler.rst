Ordonnanceur
############

.. contents::

L'ordonnanceur est conçu pour programmer des tâches système dynamiquement. Il a été implémenté avec l'aide de *l'ordonnanceur Quartz du framework Spring*.

De façon à utiliser cet ordonnanceur efficacement, les tâches système (jobs Quartz) qui doivent être programmées doivent être créées au préalable. Ensuite, ces tâches système pourront être ajoutées à la base de données puisque toutes les tâches système à programmer sont enregistrées en base de données.

Quand l'ordonnanceur démarre la première fois, il lit les tâches système à programmer/déprogrammer dans la base de données et les programme/supprime en conséquence. Après cela, des tâches système peuvent être ajoutées/mises à jour ou supprimées dynamiquement en utilisant l'interface utilisateur ``Ordonnancement de tâches système``.

.. NOTE::
   L'ordonnanceur démarre quand l'application web LibrePlan démarre et s'arrête quand l'application s'arrête.

.. NOTE::
   Cet ordonnanceur ne gère que les ``expressions cron`` pour programmer les tâches système.

Les critères que l'ordonnanceur utilise pour programmer/supprimer des tâches système quand il démarre la première fois :

Pour toutes les tâches système :

* Programmer
  
  * La tâche système possède un *Connecteur* et le *Connecteur* est activé et une tâche système est autorisée à être programmée
  * La tâche système n'a pas de *Connecteur* et est autorisée à être programmée

* Supprimer

  * La tâche système a un *Connecteur* et le *Connecteur* n'est pas activé
  * La tâche système a un *Connecteur* et le *Connecteur* est activé mais la tâche système n'est pas autorisée à être programmé
  * La tâche système n'a pas de *Connecteur* et n'est pas autorisée à être programmée 

.. NOTE::
   Les tâches système ne peuvent pas être reprogrammées/déprogrammées si elles sont en cours d'exécution
   
Vue de la liste de programmation des tâches systèmes
====================================================
La vue ``liste de programmation des tâches système`` permet aux utilisateurs de :

* ajouter une nouvelle tâche système
* modifier une tâche système existante
* supprimer une tâche système
* démarrer manuellement un processus

Ajouter ou modifier une tâche système
=====================================
À partir de la vue ``liste de programmation des tâches système``, cliquer sur :

* le bouton ``Créer`` pour ajouter une nouvelle tâche système, ou
* le bouton ``Modifier`` pour modifier la tâche système choisie.

Ces deux actions vous conduiront à un ``formulaire création/modification de tâches système``. Le ``formulaire`` affiche les propriétés suivantes :

* Champs :

  * Groupe de tâches système : le nom du groupe de tâches système
  * Nom de tâche système : le nom de la tâche système
  * Expression cron : champ en lecture seule avec un bouton ``Modifier`` pour ouvrir une fenêtre de saisie d'une ``expression cron``
  * Nom de classe de la tâche système : ``liste déroulante`` pour choisir un connecteur. Ce n'est pas obligatoire.
  * Programmation: case à cocher pour indiquer si vous voulez programmer la tâche système ou non

* Boutons :

  * Enregistrer : pour enregistrer/mettre à jour une tâche système à la fois en base de données et dans l'ordonnanceur. L'utilisateur est alors ramené à la ``vue de la liste de programmation des tâches système``
  * Enregistrer et continuer : la même chose que ci-dessus à la différence que l'utilisateur n'est pas ramené à la ``vue de la liste de programmation des tâches système``.
  * Annuler : ne rien enregistrer et l'utilisateur est ramené à la ``vue de la liste de programmation des tâches système``

* Et une aide concernant la syntaxe des expressions cron


Fenêtre surgissante des expressions cron
----------------------------------------
De façon à saisir correctement l' ``expression cron``, un formulaire surgissant d'``expression cron`` est utilisé. Dans ce formulaire, vous pouvez saisir l'``expression cron`` souhaitée. Voir également l'astuce concernant l'``expression cron``. Dans le cas où vous saisiriez une ``expression cron`` incorrecte, vous seriez immédiatement averti que l'``expression cron`` saisie n'est pas conforme.

Supprimer une tâche système
===========================
Cliquer sur le bouton ``Supprimer`` pour supprimer la tâche système à la fois dans la base de données et dans l'ordonnanceur. L'information de réussite/d'échec de cette action sera affichée.

Démarrer la tâche système manuellement
======================================
Au lieu d'attendre jusqu'à ce que la tâche système soit exécutée comme planifié par l'ordonnanceur, vous pouvez cliquer ce bouton pour lancer le processus directement. A l'issue, l'information de réussite/d'échec sera affichée dans une ``fenêtre surgissante``.

