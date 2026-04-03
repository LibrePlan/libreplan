Konfigurace LDAP
################

.. contents::

Tato obrazovka umožňuje navázat připojení k LDAP za účelem delegování
ověřování a/nebo autorizace.

Je rozdělena do čtyř různých oblastí, které jsou vysvětleny níže:

Aktivace
========

Tato oblast slouží k nastavení vlastností, které určují, jak *LibrePlan* používá
LDAP.

Pokud je zaškrtnuto pole *Povolit ověřování LDAP*, bude *LibrePlan* dotazovat
LDAP pokaždé, když se uživatel pokusí přihlásit do aplikace.

Zaškrtnuté pole *Použít role LDAP* znamená, že je vytvořeno mapování mezi rolemi LDAP a
rolemi LibrePlan. V důsledku toho budou oprávnění uživatele v
LibrePlan záviset na rolích, které má uživatel v LDAP.

Konfigurace
===========

Tato část obsahuje hodnoty parametrů pro přístup k LDAP. *Base*, *UserDN* a
*Password* jsou parametry použité pro připojení k LDAP a vyhledání uživatelů. Proto
zadaný uživatel musí mít oprávnění k provedení této operace v LDAP. Ve
spodní části této sekce je tlačítko pro ověření, zda je připojení k LDAP
možné se zadanými parametry. Před pokračováním konfigurace je doporučeno otestovat připojení.

.. NOTE::

   Pokud je váš LDAP nakonfigurován pro práci s anonymním ověřováním, můžete
   ponechat atributy *UserDN* a *Password* prázdné.

.. TIP::

   Pokud jde o konfiguraci *Active Directory (AD)*, pole *Base* musí být
   přesné umístění, kde sídlí vázaný uživatel v AD.

   Příklad: ``ou=organizational_unit,dc=example,dc=org``

Ověřování
=========

Zde lze nakonfigurovat vlastnost v uzlech LDAP, kde by mělo být nalezeno zadané uživatelské jméno.
Vlastnost *UserId* musí být vyplněna názvem
vlastnosti, kde je v LDAP uloženo uživatelské jméno.

Zaškrtávací políčko *Ukládat hesla do databáze*, pokud je zaškrtnuto, znamená, že
heslo je také uloženo v databázi LibrePlan. Tímto způsobem, pokud je LDAP
offline nebo nedostupný, mohou se uživatelé LDAP ověřovat oproti databázi LibrePlan.
Pokud není zaškrtnuto, uživatelé LDAP se mohou ověřovat pouze oproti
LDAP.

Autorizace
==========

Tato část umožňuje definovat strategii pro přiřazení rolí LDAP k
rolím LibrePlan. Prvním výběrem je strategie, která se má použít, v závislosti na
implementaci LDAP.

Skupinová strategie
-------------------

Při použití této strategie to znamená, že LDAP má strategii rolí-skupin.
To znamená, že uživatelé v LDAP jsou uzly, které jsou přímo pod větví, která
představuje skupinu.

Následující příklad představuje platnou strukturu LDAP pro použití skupinové strategie.

* Struktura LDAP::

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

V tomto případě bude mít každá skupina atribut, například nazvaný ``member``,
se seznamem uživatelů patřících do skupiny:

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

Konfigurace pro tento případ je následující:

* Strategie vyhledávání rolí: ``Group strategy``
* Cesta skupiny: ``ou=groups``
* Vlastnost role: ``member``
* Vyhledávací dotaz role: ``uid=[USER_ID],ou=people,dc=example,dc=org``

A například, pokud chcete přiřadit některé role:

* Správa: ``cn=admins;cn=itpeople``
* Čtenář webových služeb: ``cn=itpeople``
* Zapisovatel webových služeb: ``cn=itpeople``
* Povoleno čtení všech projektů: ``cn=admins``
* Povolena editace všech projektů: ``cn=admins``
* Povoleno vytváření projektů: ``cn=workers``

Strategie vlastností
--------------------

Pokud se správce rozhodne použít tuto strategii, znamená to, že každý uživatel
je uzlem LDAP a v rámci uzlu existuje vlastnost, která představuje
skupinu(y) pro uživatele. V tomto případě konfigurace nevyžaduje
parametr *Cesta skupiny*.

Následující příklad představuje platnou strukturu LDAP pro použití strategie vlastností.

* Struktura LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**S atributem**

V tomto případě bude mít každý uživatel atribut, například nazvaný ``group``,
s názvem skupiny, do které patří:

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

   Tato strategie má omezení: každý uživatel může patřit pouze do jedné skupiny.

Konfigurace pro tento případ je následující:

* Strategie vyhledávání rolí: ``Property strategy``
* Cesta skupiny:
* Vlastnost role: ``group``
* Vyhledávací dotaz role: ``[USER_ID]``

A například, pokud chcete přiřadit některé role:

* Správa: ``admins;itpeople``
* Čtenář webových služeb: ``itpeople``
* Zapisovatel webových služeb: ``itpeople``
* Povoleno čtení všech projektů: ``admins``
* Povolena editace všech projektů: ``admins``
* Povoleno vytváření projektů: ``workers``

**Podle identifikátoru uživatele**

Můžete dokonce použít alternativní řešení pro přímé přiřazení rolí LibrePlan uživatelům
bez nutnosti mít atribut v každém uživateli LDAP.

V tomto případě zadáte, kteří uživatelé mají různé role LibrePlan
pomocí ``uid``.

Konfigurace pro tento případ je následující:

* Strategie vyhledávání rolí: ``Property strategy``
* Cesta skupiny:
* Vlastnost role: ``uid``
* Vyhledávací dotaz role: ``[USER_ID]``

A například, pokud chcete přiřadit některé role:

* Správa: ``admin1;it1``
* Čtenář webových služeb: ``it1;it2``
* Zapisovatel webových služeb: ``it1;it2``
* Povoleno čtení všech projektů: ``admin1``
* Povolena editace všech projektů: ``admin1``
* Povoleno vytváření projektů: ``worker1;worker2;worker3``

Přiřazení rolí
--------------

Ve spodní části této sekce je tabulka se všemi rolemi LibrePlan
a textové pole vedle každé z nich. Toto slouží pro přiřazení rolí. Například,
pokud správce rozhodne, že role *Správa* LibrePlan odpovídá
rolím *admin* a *administrators* v LDAP, textové pole by mělo obsahovat:
"``admin;administrators``". Oddělovačem rolí je "``;``".

.. NOTE::

   Pokud chcete specifikovat, že všichni uživatelé nebo všechny skupiny mají jedno oprávnění, můžete
   použít hvězdičku (``*``) jako zástupný znak pro jejich označení. Například, pokud
   chcete, aby každý měl roli *Povoleno vytváření projektů*, nakonfigurujete přiřazení role takto:

   * Povoleno vytváření projektů: ``*``
