Konfiguracja LDAP
#################

.. contents::

Ten ekran umożliwia nawiązanie połączenia z LDAP w celu delegowania
uwierzytelniania i/lub autoryzacji.

Jest podzielony na cztery różne obszary, które są opisane poniżej:

Aktywacja
=========

Ten obszar służy do ustawiania właściwości określających, w jaki sposób *LibrePlan* używa
LDAP.

Jeśli pole *Włącz uwierzytelnianie LDAP* jest zaznaczone, *LibrePlan* będzie wysyłać zapytanie do
LDAP za każdym razem, gdy użytkownik spróbuje zalogować się do aplikacji.

Zaznaczone pole *Używaj ról LDAP* oznacza, że ustanawiane jest mapowanie między rolami LDAP a
rolami LibrePlan. W konsekwencji uprawnienia użytkownika w
LibrePlan będą zależeć od ról, które użytkownik ma w LDAP.

Konfiguracja
============

Ta sekcja zawiera wartości parametrów dostępu do LDAP. *Base*, *UserDN* i
*Password* to parametry używane do połączenia z LDAP i wyszukiwania użytkowników. Dlatego
podany użytkownik musi mieć uprawnienia do wykonania tej operacji w LDAP. Na dole
tej sekcji znajduje się przycisk do sprawdzenia, czy połączenie z LDAP jest możliwe
przy danych parametrach. Zaleca się przetestowanie połączenia przed
kontynuowaniem konfiguracji.

.. NOTE::

   Jeśli Twój LDAP jest skonfigurowany do pracy z uwierzytelnianiem anonimowym, możesz
   pozostawić atrybuty *UserDN* i *Password* puste.

.. TIP::

   W odniesieniu do konfiguracji *Active Directory (AD)*, pole *Base* musi być
   dokładną lokalizacją, w której znajduje się powiązany użytkownik w AD.

   Przykład: ``ou=organizational_unit,dc=example,dc=org``

Uwierzytelnianie
================

Tutaj można skonfigurować właściwość w węzłach LDAP, gdzie należy znaleźć podaną nazwę użytkownika.
Właściwość *UserId* musi być wypełniona nazwą
właściwości, w której nazwa użytkownika jest przechowywana w LDAP.

Pole wyboru *Zapisz hasła w bazie danych*, gdy jest zaznaczone, oznacza, że
hasło jest również przechowywane w bazie danych LibrePlan. W ten sposób, jeśli LDAP jest
niedostępny lub nieosiągalny, użytkownicy LDAP mogą uwierzytelniać się względem bazy danych
LibrePlan. Jeśli nie jest zaznaczone, użytkownicy LDAP mogą być uwierzytelniani tylko względem
LDAP.

Autoryzacja
===========

Ta sekcja umożliwia zdefiniowanie strategii dopasowywania ról LDAP do
ról LibrePlan. Pierwszym wyborem jest strategia do użycia, w zależności od
implementacji LDAP.

Strategia grupowa
-----------------

Gdy ta strategia jest używana, wskazuje, że LDAP ma strategię grupową roli.
Oznacza to, że użytkownicy w LDAP są węzłami bezpośrednio pod gałęzią reprezentującą
grupę.

Poniższy przykład przedstawia prawidłową strukturę LDAP do użycia strategii grupowej.

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

W tym przypadku każda grupa będzie miała atrybut, na przykład nazwany ``member``,
z listą użytkowników należących do grupy:

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

Konfiguracja dla tego przypadku jest następująca:

* Strategia wyszukiwania roli: ``Group strategy``
* Ścieżka grupy: ``ou=groups``
* Właściwość roli: ``member``
* Zapytanie wyszukiwania roli: ``uid=[USER_ID],ou=people,dc=example,dc=org``

Na przykład, jeśli chcesz dopasować niektóre role:

* Administracja: ``cn=admins;cn=itpeople``
* Odczyt usług sieciowych: ``cn=itpeople``
* Zapis usług sieciowych: ``cn=itpeople``
* Dozwolony odczyt wszystkich projektów: ``cn=admins``
* Dozwolona edycja wszystkich projektów: ``cn=admins``
* Dozwolone tworzenie projektów: ``cn=workers``

Strategia właściwości
---------------------

Gdy administrator zdecyduje się użyć tej strategii, wskazuje, że każdy użytkownik
jest węzłem LDAP, a w węźle istnieje właściwość reprezentująca
grupę (grupy) użytkownika. W tym przypadku konfiguracja nie wymaga parametru
*Ścieżka grupy*.

Poniższy przykład przedstawia prawidłową strukturę LDAP do użycia strategii właściwości.

* Struktura LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Z atrybutem**

W tym przypadku każdy użytkownik będzie miał atrybut, na przykład nazwany ``group``,
z nazwą grupy, do której należy:

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

   Ta strategia ma ograniczenie: każdy użytkownik może należeć tylko do jednej grupy.

Konfiguracja dla tego przypadku jest następująca:

* Strategia wyszukiwania roli: ``Property strategy``
* Ścieżka grupy:
* Właściwość roli: ``group``
* Zapytanie wyszukiwania roli: ``[USER_ID]``

Na przykład, jeśli chcesz dopasować niektóre role:

* Administracja: ``admins;itpeople``
* Odczyt usług sieciowych: ``itpeople``
* Zapis usług sieciowych: ``itpeople``
* Dozwolony odczyt wszystkich projektów: ``admins``
* Dozwolona edycja wszystkich projektów: ``admins``
* Dozwolone tworzenie projektów: ``workers``

**Według identyfikatora użytkownika**

Można nawet użyć obejścia, aby bezpośrednio przypisać role LibrePlan użytkownikom
bez posiadania atrybutu w każdym użytkowniku LDAP.

W tym przypadku określisz, którzy użytkownicy mają różne role LibrePlan
przez ``uid``.

Konfiguracja dla tego przypadku jest następująca:

* Strategia wyszukiwania roli: ``Property strategy``
* Ścieżka grupy:
* Właściwość roli: ``uid``
* Zapytanie wyszukiwania roli: ``[USER_ID]``

Na przykład, jeśli chcesz dopasować niektóre role:

* Administracja: ``admin1;it1``
* Odczyt usług sieciowych: ``it1;it2``
* Zapis usług sieciowych: ``it1;it2``
* Dozwolony odczyt wszystkich projektów: ``admin1``
* Dozwolona edycja wszystkich projektów: ``admin1``
* Dozwolone tworzenie projektów: ``worker1;worker2;worker3``

Dopasowywanie ról
-----------------

Na dole tej sekcji znajduje się tabela ze wszystkimi rolami LibrePlan
i polem tekstowym obok każdej z nich. Służy to do dopasowywania ról. Na przykład,
jeśli administrator zdecyduje, że rola *Administracja* LibrePlan odpowiada
rolom *admin* i *administrators* w LDAP, pole tekstowe powinno zawierać:
"``admin;administrators``". Znakiem rozdzielającym role jest "``;``".

.. NOTE::

   Jeśli chcesz określić, że wszyscy użytkownicy lub wszystkie grupy mają jedno uprawnienie, możesz
   użyć gwiazdki (``*``) jako symbolu wieloznacznego, aby się do nich odwołać. Na przykład, jeśli
   chcesz, aby wszyscy mieli rolę *Dozwolone tworzenie projektów*, skonfigurujesz dopasowanie ról
   w następujący sposób:

   * Dozwolone tworzenie projektów: ``*``
