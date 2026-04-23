LDAP-configuratie
#################

.. contents::

Met dit scherm kunt u een verbinding met LDAP instellen om authenticatie en/of autorisatie te delegeren.

Het is verdeeld in vier verschillende gebieden, die hieronder worden uitgelegd:

Activering
==========

Dit gebied wordt gebruikt om de eigenschappen in te stellen die bepalen hoe *LibrePlan* LDAP gebruikt.

Als het veld *LDAP-authenticatie inschakelen* is aangevinkt, zal *LibrePlan* LDAP raadplegen telkens wanneer een gebruiker probeert in te loggen op de applicatie.

Het aangevinkte veld *LDAP-rollen gebruiken* betekent dat er een koppeling wordt gemaakt tussen LDAP-rollen en LibrePlan-rollen. Daardoor zijn de rechten van een gebruiker in LibrePlan afhankelijk van de rollen die de gebruiker heeft in LDAP.

Configuratie
============

Deze sectie bevat de parameterwaarden voor toegang tot LDAP. *Base*, *UserDN* en *Wachtwoord* zijn parameters die worden gebruikt om verbinding te maken met LDAP en naar gebruikers te zoeken. Daarom moet de opgegeven gebruiker toestemming hebben om deze bewerking uit te voeren in LDAP. Onderaan deze sectie is een knop om te controleren of een LDAP-verbinding mogelijk is met de opgegeven parameters. Het is raadzaam de verbinding te testen voordat u doorgaat met de configuratie.

.. NOTE::

   Als uw LDAP is geconfigureerd om te werken met anonieme authenticatie, kunt u de attributen *UserDN* en *Wachtwoord* leeg laten.

.. TIP::

   Met betrekking tot de configuratie van *Active Directory (AD)* moet het veld *Base* de exacte locatie zijn waar de gebonden gebruiker zich bevindt in AD.

   Voorbeeld: ``ou=organizational_unit,dc=example,dc=org``

Authenticatie
=============

Hier kunt u de eigenschap in LDAP-knooppunten configureren waar de opgegeven gebruikersnaam moet worden gevonden. De eigenschap *UserId* moet worden ingevuld met de naam van de eigenschap waar de gebruikersnaam is opgeslagen in LDAP.

Het selectievakje *Wachtwoorden opslaan in database*, wanneer aangevinkt, betekent dat het wachtwoord ook wordt opgeslagen in de LibrePlan-database. Op deze manier kunnen LDAP-gebruikers, als LDAP offline of onbereikbaar is, authenticeren tegen de LibrePlan-database. Als het niet is aangevinkt, kunnen LDAP-gebruikers alleen worden geverifieerd tegen LDAP.

Autorisatie
===========

In deze sectie kunt u een strategie definiëren voor het koppelen van LDAP-rollen aan LibrePlan-rollen. De eerste keuze is de strategie die moet worden gebruikt, afhankelijk van de LDAP-implementatie.

Groepsstrategie
---------------

Wanneer deze strategie wordt gebruikt, geeft dit aan dat LDAP een rolgroepstrategie heeft. Dit betekent dat gebruikers in LDAP knooppunten zijn die direct onder een tak vallen die de groep vertegenwoordigt.

Het volgende voorbeeld vertegenwoordigt een geldige LDAP-structuur voor het gebruik van de groepsstrategie.

* LDAP-structuur::

   dc=example,dc=org
   |- ou=groups
      |- cn=admins
      |- cn=itpeople
      |- cn=workers
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

In dit geval heeft elke groep een attribuut, bijvoorbeeld genaamd ``member``, met de lijst van gebruikers die tot de groep behoren:

* ``cn=admins``:

  * ``member: uid=admin1,ou=people,dc=example,dc=org``
  * ``member: uid=it1,ou=people,dc=example,dc=org``

* ``cn=itpeople``:

  * ``member: uid=it1,ou=people,dc=example,dc=org``
  * ``member: uid=it2,ou=people,dc=example,dc=org``

* ``cn=workers``:

  * ``member: uid=worker1,ou=people,dc=example,dc=org``
  * ``member: uid=worker2,ou=people,dc=example,dc=org``
  * ``member: uid=worker3,ou=people,dc=example,dc=org``

De configuratie voor dit geval is als volgt:

* Rolzoekstrategie: ``Group strategy``
* Groepspad: ``ou=groups``
* Roleigenschap: ``member``
* Rolzoekquery: ``uid=[USER_ID],ou=people,dc=example,dc=org``

En als u bijvoorbeeld sommige rollen wilt koppelen:

* Beheer: ``cn=admins;cn=itpeople``
* Webservice-lezer: ``cn=itpeople``
* Webservice-schrijver: ``cn=itpeople``
* Lezen van alle projecten toegestaan: ``cn=admins``
* Bewerken van alle projecten toegestaan: ``cn=admins``
* Aanmaken van projecten toegestaan: ``cn=workers``

Eigenschapsstrategie
--------------------

Wanneer een beheerder besluit deze strategie te gebruiken, geeft dit aan dat elke gebruiker een LDAP-knooppunt is, en binnen het knooppunt bestaat een eigenschap die de groep(en) voor de gebruiker vertegenwoordigt. In dit geval vereist de configuratie de parameter *Groepspad* niet.

Het volgende voorbeeld vertegenwoordigt een geldige LDAP-structuur voor het gebruik van de eigenschapsstrategie.

* LDAP-structuur::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Met attribuut**

In dit geval heeft elke gebruiker een attribuut, bijvoorbeeld genaamd ``group``, met de naam van de groep waartoe hij behoort:

* ``uid=admin1``:

  * ``group: admins``

* ``uid=it1``:

  * ``group: itpeople``

* ``uid=it2``:

  * ``group: itpeople``

* ``uid=worker1``:

  * ``group: workers``

* ``uid=worker2``:

  * ``group: workers``

* ``uid=worker3``:

  * ``group: workers``


.. WARNING::

   Deze strategie heeft een beperking: elke gebruiker kan slechts tot één groep behoren.

De configuratie voor dit geval is als volgt:

* Rolzoekstrategie: ``Property strategy``
* Groepspad:
* Roleigenschap: ``group``
* Rolzoekquery: ``[USER_ID]``

En als u bijvoorbeeld sommige rollen wilt koppelen:

* Beheer: ``admins;itpeople``
* Webservice-lezer: ``itpeople``
* Webservice-schrijver: ``itpeople``
* Lezen van alle projecten toegestaan: ``admins``
* Bewerken van alle projecten toegestaan: ``admins``
* Aanmaken van projecten toegestaan: ``workers``

**Op gebruikersidentificatie**

U kunt zelfs een tijdelijke oplossing gebruiken om LibrePlan-rollen rechtstreeks aan gebruikers toe te wijzen zonder een attribuut in elke LDAP-gebruiker te hebben.

In dit geval geeft u aan welke gebruikers de verschillende LibrePlan-rollen hebben op basis van ``uid``.

De configuratie voor dit geval is als volgt:

* Rolzoekstrategie: ``Property strategy``
* Groepspad:
* Roleigenschap: ``uid``
* Rolzoekquery: ``[USER_ID]``

En als u bijvoorbeeld sommige rollen wilt koppelen:

* Beheer: ``admin1;it1``
* Webservice-lezer: ``it1;it2``
* Webservice-schrijver: ``it1;it2``
* Lezen van alle projecten toegestaan: ``admin1``
* Bewerken van alle projecten toegestaan: ``admin1``
* Aanmaken van projecten toegestaan: ``worker1;worker2;worker3``

Rolkoppeling
------------

Onderaan deze sectie is er een tabel met alle LibrePlan-rollen en een tekstveld naast elk. Dit is voor het koppelen van rollen. Als een beheerder bijvoorbeeld besluit dat de LibrePlan-rol *Beheer* overeenkomt met de rollen *admin* en *administrators* van LDAP, moet het tekstveld het volgende bevatten: "``admin;administrators``". Het scheidingsteken voor rollen is "``;``".

.. NOTE::

   Als u wilt opgeven dat alle gebruikers of alle groepen één toestemming hebben, kunt u een asterisk (``*``) gebruiken als jokerteken om naar hen te verwijzen. Als u bijvoorbeeld wilt dat iedereen de rol *Aanmaken van projecten toegestaan* heeft, configureert u de rolkoppeling als volgt:

   * Aanmaken van projecten toegestaan: ``*``
