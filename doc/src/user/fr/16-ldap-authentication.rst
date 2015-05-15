Configuration LDAP
##################

.. contents::

Cet écran permet d'établir une connexion avec un annuaire LDAP pour lui déléguer l'authentification et/ou l'identification.
Il est accessible via le menu *Configuration*, sous-menu *Réglages principaux*, onglet *Configuration LDAP*.

Il est divisé en quatre sections différentes comme expliqué ci-dessous :

Activation
==========

Cette section est utilisée pour régler les propriétés concernant la façon dont *LibrePlan* utilise le LDAP.

Si la case *Activer l'authentification LDAP* est cochée, *LibrePlan* ira interroger le LDAP à chaque fois qu'un utilisateur essaiera de se connecter dans l'application.

La case *Utiliser les rôles LDAP*, quand elle est cochée, indique qu'un lien entre les rôles LDAP et les rôles LibrePlan est établi de telle sorte que les permissions d'un utilisateur dans LibrePlan dépendront des rôles que possède cet utilisateur dans LDAP.

Configuration
=============

Cette section contient les valeurs des paramètres pour accéder à LDAP. D'abord le nom ou l'adresse IP de l'*hôte* fournissant le service LDAP puis le *Port* à utiliser. *Base*, *UserDN* et *Mot de passe* sont les paramètres utilisés pour se connecter au LDAP et chercher des utilisateurs, donc il faut utiliser un compte ayant la permission de faire cette opération dans LDAP. Dans la partie basse de cette section, il y a un bouton pour vérifier que la connexion LDAP est possible avec les paramètres indiqués. C'est une bonne idée de l'essayer avant de continuer la configuration.

.. NOTE::

   Si votre LDAP est configuré pour fonctionner avec l'authentification anonyme, vous n'êtes pas tenu de renseigner les attributs *UserDN* et *Mot de passe*.

.. TIP::

   Concernant la configuration *Active Directory (AD)*, le champ *Base* doit correspondre exactement à l'emplacement où se trouve l'utilisateur lié dans l'AD.

   Exemple : ``ou=organizational_unit,dc=exemple,dc=org``

Authentification
================

Ici peut être configurée la propriété du LDAP où l'on doit trouver le nom d'utilisateur (given username). La propriété *UserID* doit être renseignée avec le nom de la propriété dans laquelle le nom d'utilisateur est enregistré dans le LDAP.

La case à cocher *Sauvegarder les mots de passe en base de données* quand elle est cochée signifie que le mot de passe sera également enregistré dans la base de données de LibrePlan. De cette façon, si le LDAP est déconnecté ou injoignable, les utilisateurs LDAP pourront quand même s'authentifier en utilisant la base de données de LibrePlan. Si la case n'est pas cochée, les utilisateurs LDAP ne pourront être authentifiés que via le LDAP.

Permissions
===========

Cette section permet de définir une stratégie pour faire correspondre les rôles LDAP avec les rôles LibrePlan. De fait, le premier choix à faire est celui de la stratégie à utiliser en fonction de l'implémentation LDAP.

Stratégie de groupe
-------------------

Quand cette stratégie est utilisée, cela signifie que le LDAP possède une stratégie groupe-rôle. Cela signifie que les utilisateurs dans LDAP sont des éléments appartenant directement à une branche qui représente le groupe.

L'exemple suivant représente une structure valide pour utiliser la stratégie de groupe.

* structure LDAP ::

   dc=exemple,dc=org
   |- ou=groupes
      |- cn=admins
      |- cn=itpeople
      |- cn=employés
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=employés1
      |- uid=employés2
      |- uid=employés3

Dans ce cas, chaque groupe aura un attribut, appelé par exemple ``membres`` avec la liste des utilisateurs appartenant au groupe :

* ``cn=admins``:

  * ``membres: uid=admin1,ou=people,dc=exemple,dc=org``
  * ``membres: uid=it1,ou=people,dc=exemple,dc=org``

* ``cn=itpeople``:

  * ``membres: uid=it1,ou=people,dc=exemple,dc=org``
  * ``membres: uid=it2,ou=people,dc=exemple,dc=org``

* ``cn=employés``:

  * ``membres: uid=employés1,ou=people,dc=exemple,dc=org``
  * ``membres: uid=employés2,ou=people,dc=exemple,dc=org``
  * ``membres: uid=employés3,ou=people,dc=exemple,dc=org``

La configuration pour ce cas est la suivante :

* Stratégie de recherche du rôle : ``Stratégie de groupe``
* Chemin du groupe : ``ou=groupes``
* Propriété du rôle : ``membres``
* Requête de recherche de rôle : ``uid=[USER_ID],ou=people,dc=exemple,dc=org``

Et, par exemple, si vous voulez faire correspondre certains rôles :

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=employés``

Stratégie de la propriété
-------------------------

Quand l'administrateur décide d'utiliser cette stratégie, cela signifie que chaque utilisateur est un noeud LDAP et que dans ce noeud existe une propriété qui représente le(s) groupe(s) de l'utilisateur. Dans ce cas, la configuration n'a pas besoin du paramètre *Chemin du groupe* :

L'exemple suivant représente une structure LDAP valide à utiliser avec la stratégie de propriété d'utilisateur.

* Structure LDAP::

   dc=exemple,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=employés1
      |- uid=employés2
      |- uid=employés3

**Avec attribut**

Dans ce cas, chaque utilisateur aura un attribut, appelé par exemple ``groupe`` avec le nom du groupe auquel il appartient :

* ``uid=admin1``:

  * ``groupe: admins``

* ``uid=it1``:

  * ``groupe: itpeople``

* ``uid=it2``:

  * ``groupe: itpeople``

* ``uid=employé1``:

  * ``groupe: employés``

* ``uid=employé2``:

  * ``groupe: employés``

* ``uid=employé3``:

  * ``groupe: employés``


.. WARNING::

   Cette stratégie impose une restriction: chaque utilisateur ne peut appartenir qu'à un seul groupe.

La configuration dans ce cas est la suivante :

* Stratégie de recherche du rôle : ``Stratégie de la propriété``
* Chemin du groupe : 
* Propriété du rôle : ``groupe``
* Requête de recherche de rôle : ``[USER_ID]``

Et par exemple si vous voulez faire correspondre certains rôles :

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``employés``

**Par identifiant utilisateur**

Il existe également un moyen d'affecter des rôles LibrePlan directement aux utilisateurs sans avoir d'attribut pour chaque utilisateur LDAP.

Dans ce cas, vous indiquerez quels utilisateurs ont différents rôles LibrePlan via les ``uid``.

La configuration dans ce cas est la suivante :

* Stratégie de recherche du rôle : ``Stratégie de la propriété``
* Chemin du groupe : 
* Propriété du rôle : ``uid``
* Requête de recherche de rôle : ``[USER_ID]``

Et par exemple si vous voulez faire correspondre certains rôles :

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``employé1;employé2;employé3``

Correspondance des rôles
------------------------

En bas de cette section, il y a une table avec tous les rôles LibrePlan et un champ texte à côté de chacun. C'est pour la correspondance des rôles. Par exemple, si l'administrateur décide que le rôle LibrePlan *Administration* correspond aux rôles LDAP *admin* et *administrators*, dans le champ texte doit apparaître :
"``admin;administrators``". Le caractère pour séparer les rôles est "``;``".

.. NOTE::

   Si vous voulez indiquer que tous les utilisateurs ou tous les groupes ont une permission, vous pouvez utiliser une astérisque (``*``) comme joker pour y faire référence. Par exemple, si vous voulez que tout le monde ait le rôle *Project creation allowed*, vous configurerez la correspondance de rôle comme suit :

   * Project creation allowed: ``*``
