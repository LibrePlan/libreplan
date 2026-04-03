LDAP-konfiguration
##################

.. contents::

Den här skärmen låter dig upprätta en anslutning med LDAP för att delegera
autentisering och/eller auktorisering.

Den är uppdelad i fyra olika områden som förklaras nedan:

Aktivering
==========

Det här området används för att ange egenskaper som bestämmer hur *LibrePlan* använder
LDAP.

Om fältet *Aktivera LDAP-autentisering* är markerat kommer *LibrePlan* att fråga
LDAP varje gång en användare försöker logga in på programmet.

Fältet *Använd LDAP-roller* markerat innebär att en mappning mellan LDAP-roller och
LibrePlan-roller upprättas. Följaktligen kommer behörigheterna för en användare i
LibrePlan att bero på de roller som användaren har i LDAP.

Konfiguration
=============

Det här avsnittet innehåller parametervärdena för åtkomst till LDAP. *Base*, *UserDN* och
*Password* är parametrar som används för att ansluta till LDAP och söka efter användare. Därför
måste den angivna användaren ha behörighet att utföra denna operation i LDAP. Längst
ned i det här avsnittet finns en knapp för att kontrollera om en LDAP-anslutning är
möjlig med de givna parametrarna. Det är tillrådligt att testa anslutningen innan
konfigurationen fortsätts.

.. NOTE::

   Om din LDAP är konfigurerad för anonym autentisering kan du
   lämna attributen *UserDN* och *Password* tomma.

.. TIP::

   Gällande konfiguration av *Active Directory (AD)* måste fältet *Base* vara den
   exakta platsen där den bundna användaren finns i AD.

   Exempel: ``ou=organizational_unit,dc=example,dc=org``

Autentisering
=============

Här kan du konfigurera egenskapen i LDAP-noder där det angivna användarnamnet
ska hittas. Egenskapen *UserId* måste fyllas i med namnet på
egenskapen där användarnamnet lagras i LDAP.

Kryssrutan *Spara lösenord i databasen*, när den är markerad, innebär att
lösenordet också lagras i LibrePlan-databasen. På så sätt, om LDAP är
offline eller inte nåbar, kan LDAP-användare autentisera sig mot LibrePlan-
databasen. Om den inte är markerad kan LDAP-användare bara autentiseras mot
LDAP.

Auktorisering
=============

Det här avsnittet låter dig definiera en strategi för att matcha LDAP-roller med
LibrePlan-roller. Det första valet är vilken strategi som ska användas, beroende på
LDAP-implementeringen.

Gruppstrategi
-------------

När den här strategin används anger det att LDAP har en rollgruppsstrategi.
Det innebär att användare i LDAP är noder som direkt befinner sig under en gren som
representerar gruppen.

Följande exempel representerar en giltig LDAP-struktur för att använda gruppstrategin.

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

I det här fallet kommer varje grupp att ha ett attribut, till exempel kallat ``member``,
med listan över användare som tillhör gruppen:

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

Konfigurationen för det här fallet är följande:

* Rollsökningsstrategi: ``Group strategy``
* Gruppsökväg: ``ou=groups``
* Rollegenskap: ``member``
* Rollsökningsfråga: ``uid=[USER_ID],ou=people,dc=example,dc=org``

Och, till exempel, om du vill matcha vissa roller:

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

Egenskapsstrategi
-----------------

När en administratör bestämmer sig för att använda den här strategin anger det att varje användare
är en LDAP-nod, och inom noden finns en egenskap som representerar
gruppen/grupperna för användaren. I det här fallet kräver konfigurationen inte
parametern *Group path*.

Följande exempel representerar en giltig LDAP-struktur för att använda egenskapsstrategin.

* LDAP-struktur::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Med attribut**

I det här fallet kommer varje användare att ha ett attribut, till exempel kallat ``group``,
med namnet på den grupp de tillhör:

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

   Den här strategin har en begränsning: varje användare kan tillhöra bara en grupp.

Konfigurationen för det här fallet är följande:

* Rollsökningsstrategi: ``Property strategy``
* Gruppsökväg:
* Rollegenskap: ``group``
* Rollsökningsfråga: ``[USER_ID]``

Och, till exempel, om du vill matcha vissa roller:

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

**Genom användaridentifierare**

Du kan till och med använda ett alternativt tillvägagångssätt för att direkt tilldela LibrePlan-roller till användare
utan att ha ett attribut i varje LDAP-användare.

I det här fallet anger du vilka användare som har de olika LibrePlan-rollerna
via ``uid``.

Konfigurationen för det här fallet är följande:

* Rollsökningsstrategi: ``Property strategy``
* Gruppsökväg:
* Rollegenskap: ``uid``
* Rollsökningsfråga: ``[USER_ID]``

Och, till exempel, om du vill matcha vissa roller:

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

Rollmatchning
-------------

Längst ned i det här avsnittet finns en tabell med alla LibrePlan-roller
och ett textfält bredvid var och en. Detta är för att matcha roller. Till exempel,
om en administratör bestämmer att rollen *Administration* i LibrePlan matchar
rollerna *admin* och *administrators* i LDAP, bör textfältet innehålla:
"``admin;administrators``". Tecknet för att dela roller är "``;``".

.. NOTE::

   Om du vill ange att alla användare eller alla grupper har en behörighet kan du
   använda en asterisk (``*``) som jokertecken för att referera till dem. Om du till exempel
   vill att alla ska ha rollen *Project creation allowed* konfigurerar du rollmatchningen så här:

   * Project creation allowed: ``*``
