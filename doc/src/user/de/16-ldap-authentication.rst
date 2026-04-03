LDAP-Konfiguration
##################

.. contents::

Dieser Bildschirm ermöglicht es Ihnen, eine Verbindung mit LDAP herzustellen, um
die Authentifizierung und/oder Autorisierung zu delegieren.

Er ist in vier verschiedene Bereiche unterteilt, die im Folgenden erläutert werden:

Aktivierung
===========

Dieser Bereich wird verwendet, um die Eigenschaften festzulegen, die bestimmen, wie *LibrePlan* LDAP
verwendet.

Wenn das Feld *LDAP-Authentifizierung aktivieren* aktiviert ist, wird *LibrePlan* LDAP
bei jedem Anmeldeversuch eines Benutzers bei der Anwendung abfragen.

Das aktivierte Feld *LDAP-Rollen verwenden* bedeutet, dass eine Zuordnung zwischen LDAP-Rollen und
LibrePlan-Rollen hergestellt wird. Daher hängen die Berechtigungen eines Benutzers in
LibrePlan von den Rollen ab, die der Benutzer in LDAP hat.

Konfiguration
=============

Dieser Abschnitt enthält die Parameterwerte für den Zugriff auf LDAP. *Base*, *UserDN* und
*Password* sind Parameter, die zur Verbindung mit LDAP und zur Suche nach Benutzern verwendet werden. Daher
muss der angegebene Benutzer über die Berechtigung verfügen, diesen Vorgang in LDAP durchzuführen. Am
unteren Rand dieses Abschnitts befindet sich eine Schaltfläche, um zu prüfen, ob eine LDAP-Verbindung
mit den angegebenen Parametern möglich ist. Es wird empfohlen, die Verbindung zu testen, bevor
Sie die Konfiguration fortsetzen.

.. NOTE::

   Wenn Ihr LDAP für die Verwendung mit anonymer Authentifizierung konfiguriert ist, können Sie
   die Attribute *UserDN* und *Password* leer lassen.

.. TIP::

   Bezüglich der *Active Directory (AD)*-Konfiguration muss das *Base*-Feld der
   genaue Speicherort sein, an dem sich der gebundene Benutzer in AD befindet.

   Beispiel: ``ou=organizational_unit,dc=example,dc=org``

Authentifizierung
=================

Hier können Sie die Eigenschaft in LDAP-Knoten konfigurieren, in der der angegebene Benutzername
gefunden werden soll. Die Eigenschaft *UserId* muss mit dem Namen der
Eigenschaft ausgefüllt werden, in der der Benutzername in LDAP gespeichert ist.

Das Kontrollkästchen *Passwörter in Datenbank speichern*, wenn aktiviert, bedeutet, dass das
Passwort auch in der LibrePlan-Datenbank gespeichert wird. Auf diese Weise können sich LDAP-Benutzer, wenn LDAP
offline oder nicht erreichbar ist, gegenüber der LibrePlan-Datenbank authentifizieren. Wenn es nicht aktiviert ist, können LDAP-Benutzer nur
gegenüber LDAP authentifiziert werden.

Autorisierung
=============

Dieser Abschnitt ermöglicht es Ihnen, eine Strategie zur Zuordnung von LDAP-Rollen zu
LibrePlan-Rollen zu definieren. Die erste Wahl ist die zu verwendende Strategie, abhängig von der
LDAP-Implementierung.

Gruppenstrategie
----------------

Wenn diese Strategie verwendet wird, bedeutet dies, dass LDAP eine Rollengruppenstrategie hat.
Das bedeutet, dass Benutzer in LDAP Knoten sind, die sich direkt unter einem Zweig befinden, der
die Gruppe repräsentiert.

Das folgende Beispiel stellt eine gültige LDAP-Struktur für die Verwendung der Gruppenstrategie dar.

* LDAP-Struktur::

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

In diesem Fall hat jede Gruppe ein Attribut, beispielsweise ``member`` genannt,
mit der Liste der zur Gruppe gehörenden Benutzer:

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

Die Konfiguration für diesen Fall ist wie folgt:

* Rollensuche-Strategie: ``Group strategy``
* Gruppenpfad: ``ou=groups``
* Rolleneigenschaft: ``member``
* Rollensuchabfrage: ``uid=[USER_ID],ou=people,dc=example,dc=org``

Und wenn Sie zum Beispiel einige Rollen zuordnen möchten:

* Administration: ``cn=admins;cn=itpeople``
* Webservice-Leser: ``cn=itpeople``
* Webservice-Schreiber: ``cn=itpeople``
* Alle Projekte leseberechtigt: ``cn=admins``
* Alle Projekte bearbeitungsberechtigt: ``cn=admins``
* Projekterstellung erlaubt: ``cn=workers``

Eigenschaftsstrategie
---------------------

Wenn ein Administrator sich entscheidet, diese Strategie zu verwenden, bedeutet dies, dass jeder Benutzer
ein LDAP-Knoten ist, und innerhalb des Knotens existiert eine Eigenschaft, die die
Gruppe(n) für den Benutzer repräsentiert. In diesem Fall erfordert die Konfiguration nicht den
*Gruppenpfad*-Parameter.

Das folgende Beispiel stellt eine gültige LDAP-Struktur für die Verwendung der Eigenschaftsstrategie dar.

* LDAP-Struktur::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Mit Attribut**

In diesem Fall hat jeder Benutzer ein Attribut, beispielsweise ``group`` genannt,
mit dem Namen der Gruppe, zu der er gehört:

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

   Diese Strategie hat eine Einschränkung: Jeder Benutzer kann nur einer Gruppe angehören.

Die Konfiguration für diesen Fall ist wie folgt:

* Rollensuche-Strategie: ``Property strategy``
* Gruppenpfad:
* Rolleneigenschaft: ``group``
* Rollensuchabfrage: ``[USER_ID]``

Und wenn Sie zum Beispiel einige Rollen zuordnen möchten:

* Administration: ``admins;itpeople``
* Webservice-Leser: ``itpeople``
* Webservice-Schreiber: ``itpeople``
* Alle Projekte leseberechtigt: ``admins``
* Alle Projekte bearbeitungsberechtigt: ``admins``
* Projekterstellung erlaubt: ``workers``

**Nach Benutzerkennung**

Sie können sogar einen Workaround verwenden, um LibrePlan-Rollen direkt Benutzern
zuzuweisen, ohne ein Attribut in jedem LDAP-Benutzer zu haben.

In diesem Fall geben Sie an, welche Benutzer die verschiedenen LibrePlan-Rollen
per ``uid`` haben.

Die Konfiguration für diesen Fall ist wie folgt:

* Rollensuche-Strategie: ``Property strategy``
* Gruppenpfad:
* Rolleneigenschaft: ``uid``
* Rollensuchabfrage: ``[USER_ID]``

Und wenn Sie zum Beispiel einige Rollen zuordnen möchten:

* Administration: ``admin1;it1``
* Webservice-Leser: ``it1;it2``
* Webservice-Schreiber: ``it1;it2``
* Alle Projekte leseberechtigt: ``admin1``
* Alle Projekte bearbeitungsberechtigt: ``admin1``
* Projekterstellung erlaubt: ``worker1;worker2;worker3``

Rollenzuordnung
---------------

Am unteren Rand dieses Abschnitts befindet sich eine Tabelle mit allen LibrePlan-Rollen
und einem Textfeld neben jeder. Dies dient zur Zuordnung von Rollen. Wenn zum Beispiel
ein Administrator entscheidet, dass die *Administration* LibrePlan-Rolle den Rollen
*admin* und *administrators* in LDAP entspricht, sollte das Textfeld enthalten:
"``admin;administrators``". Das Trennzeichen für Rollen ist "``;``".

.. NOTE::

   Wenn Sie angeben möchten, dass alle Benutzer oder alle Gruppen eine Berechtigung haben,
   können Sie ein Sternchen (``*``) als Platzhalter verwenden, um auf sie zu verweisen. Wenn Sie beispielsweise
   möchten, dass jeder die Rolle *Projekterstellung erlaubt* hat, konfigurieren Sie die Rollenzuordnung wie folgt:

   * Projekterstellung erlaubt: ``*``
