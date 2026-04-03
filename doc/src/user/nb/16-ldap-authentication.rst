LDAP-konfigurasjon
##################

.. contents::

Denne skjermen lar deg etablere en forbindelse med LDAP for å delegere
autentisering og/eller autorisasjon.

Den er delt inn i fire forskjellige områder, som forklares nedenfor:

Aktivering
==========

Dette området brukes til å angi egenskapene som bestemmer hvordan *LibrePlan* bruker
LDAP.

Hvis feltet *Aktiver LDAP-autentisering* er merket av, vil *LibrePlan* spørre
LDAP hver gang en bruker forsøker å logge inn på applikasjonen.

Feltet *Bruk LDAP-roller* merket av betyr at det etableres en kartlegging mellom LDAP-roller og
LibrePlan-roller. Følgelig vil tillatelsene for en bruker i
LibrePlan avhenge av rollene brukeren har i LDAP.

Konfigurasjon
=============

Denne seksjonen inneholder parameterverdiene for tilgang til LDAP. *Base*, *UserDN* og
*Password* er parametere som brukes til å koble til LDAP og søke etter brukere. Derfor
må den angitte brukeren ha tillatelse til å utføre denne operasjonen i LDAP. Nederst
i denne seksjonen er det en knapp for å kontrollere om en LDAP-tilkobling er
mulig med de gitte parameterne. Det anbefales å teste tilkoblingen før
du fortsetter konfigurasjonen.

.. NOTE::

   Hvis LDAP er konfigurert til å arbeide med anonym autentisering, kan du
   la *UserDN*- og *Password*-attributtene stå tomme.

.. TIP::

   Angående konfigurasjon av *Active Directory (AD)*, må *Base*-feltet være den
   nøyaktige plasseringen der den bundne brukeren befinner seg i AD.

   Eksempel: ``ou=organizational_unit,dc=example,dc=org``

Autentisering
=============

Her kan du konfigurere egenskapen i LDAP-noder der det gitte brukernavnet
skal finnes. Egenskapen *UserId* må fylles ut med navnet på
egenskapen der brukernavnet er lagret i LDAP.

Avmerkingsboksen *Lagre passord i database*, når den er merket av, betyr at
passordet også lagres i LibrePlan-databasen. På denne måten, hvis LDAP er
frakoblet eller utilgjengelig, kan LDAP-brukere autentisere seg mot LibrePlan-databasen.
Hvis den ikke er merket av, kan LDAP-brukere kun autentiseres mot LDAP.

Autorisasjon
============

Denne seksjonen lar deg definere en strategi for å matche LDAP-roller med
LibrePlan-roller. Det første valget er strategien som skal brukes, avhengig av
LDAP-implementasjonen.

Gruppestrategi
--------------

Når denne strategien brukes, indikerer det at LDAP har en rolle-gruppestrategi.
Dette betyr at brukere i LDAP er noder som er direkte under en gren som
representerer gruppen.

Det neste eksempelet representerer en gyldig LDAP-struktur for bruk av gruppestrategien.

* LDAP-struktur::

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

I dette tilfellet vil hver gruppe ha et attributt, for eksempel kalt ``member``,
med listen over brukere som tilhører gruppen:

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

Konfigurasjonen for dette tilfellet er som følger:

* Rollesøkstrategi: ``Group strategy``
* Gruppebane: ``ou=groups``
* Rollegenskap: ``member``
* Rollesøkspørring: ``uid=[USER_ID],ou=people,dc=example,dc=org``

Og for eksempel, hvis du ønsker å matche noen roller:

* Administrasjon: ``cn=admins;cn=itpeople``
* Webtjenestelese: ``cn=itpeople``
* Webtjenesteskrive: ``cn=itpeople``
* Alle prosjekter lesetilgang: ``cn=admins``
* Alle prosjekter redigeringstilgang: ``cn=admins``
* Prosjektopprettelse tillatt: ``cn=workers``

Egenskapsstrategi
-----------------

Når en administrator bestemmer seg for å bruke denne strategien, indikerer det at hver bruker
er en LDAP-node, og innenfor noden eksisterer det en egenskap som representerer
gruppen(e) for brukeren. I dette tilfellet krever ikke konfigurasjonen parameteren *Gruppebane*.

Det neste eksempelet representerer en gyldig LDAP-struktur for bruk av egenskapsstrategien.

* LDAP-struktur::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Med attributt**

I dette tilfellet vil hver bruker ha et attributt, for eksempel kalt ``group``,
med navnet på gruppen de tilhører:

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

   Denne strategien har en begrensning: hver bruker kan kun tilhøre én gruppe.

Konfigurasjonen for dette tilfellet er som følger:

* Rollesøkstrategi: ``Property strategy``
* Gruppebane:
* Rollegenskap: ``group``
* Rollesøkspørring: ``[USER_ID]``

Og for eksempel, hvis du ønsker å matche noen roller:

* Administrasjon: ``admins;itpeople``
* Webtjenestelese: ``itpeople``
* Webtjenesteskrive: ``itpeople``
* Alle prosjekter lesetilgang: ``admins``
* Alle prosjekter redigeringstilgang: ``admins``
* Prosjektopprettelse tillatt: ``workers``

**Etter brukeridentifikator**

Du kan også bruke en omvei for å angi LibrePlan-roller direkte til brukere
uten å ha et attributt i hver LDAP-bruker.

I dette tilfellet angir du hvilke brukere som har de ulike LibrePlan-rollene
ved hjelp av ``uid``.

Konfigurasjonen for dette tilfellet er som følger:

* Rollesøkstrategi: ``Property strategy``
* Gruppebane:
* Rollegenskap: ``uid``
* Rollesøkspørring: ``[USER_ID]``

Og for eksempel, hvis du ønsker å matche noen roller:

* Administrasjon: ``admin1;it1``
* Webtjenestelese: ``it1;it2``
* Webtjenesteskrive: ``it1;it2``
* Alle prosjekter lesetilgang: ``admin1``
* Alle prosjekter redigeringstilgang: ``admin1``
* Prosjektopprettelse tillatt: ``worker1;worker2;worker3``

Rollesamsvar
------------

Nederst i denne seksjonen er det en tabell med alle LibrePlan-rollene
og et tekstfelt ved siden av hver av dem. Dette er for rollematching. For eksempel,
hvis en administrator bestemmer at *Administrasjon*-rollen i LibrePlan samsvarer med
*admin*- og *administrators*-rollene i LDAP, bør tekstfeltet inneholde:
"``admin;administrators``". Tegnet for å skille roller er "``;``".

.. NOTE::

   Hvis du vil angi at alle brukere eller alle grupper har én tillatelse, kan
   du bruke en stjerne (``*``) som jokertegn for å referere til dem. For eksempel, hvis
   du vil at alle skal ha rollen *Prosjektopprettelse tillatt*, konfigurerer du rollesamsvaret
   som følger:

   * Prosjektopprettelse tillatt: ``*``
