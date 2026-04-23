Configuració LDAP
#################

.. contents::

Aquesta pantalla us permet establir una connexió amb LDAP per delegar
l'autenticació i/o l'autorització.

Està dividida en quatre àrees diferents, que s'expliquen a continuació:

Activació
=========

Aquesta àrea s'utilitza per establir les propietats que determinen com *LibrePlan* utilitza
LDAP.

Si el camp *Habilitar l'autenticació LDAP* està marcat, *LibrePlan* consultarà
LDAP cada vegada que un usuari intenti iniciar sessió a l'aplicació.

El camp *Usar rols LDAP* marcat significa que s'estableix una correspondència entre els rols LDAP i els
rols de LibrePlan. En conseqüència, els permisos d'un usuari a
LibrePlan dependran dels rols que l'usuari tingui a LDAP.

Configuració
============

Aquesta secció conté els valors dels paràmetres per accedir a LDAP. *Base*, *UserDN* i
*Contrasenya* són paràmetres que s'utilitzen per connectar-se a LDAP i cercar usuaris. Per tant,
l'usuari especificat ha de tenir permís per realitzar aquesta operació a LDAP. A la
part inferior d'aquesta secció, hi ha un botó per comprovar si és possible una connexió LDAP
amb els paràmetres donats. És aconsellable provar la connexió abans de
continuar la configuració.

.. NOTE::

   Si el vostre LDAP està configurat per treballar amb autenticació anònima, podeu
   deixar els atributs *UserDN* i *Contrasenya* buits.

.. TIP::

   Pel que fa a la configuració d'*Active Directory (AD)*, el camp *Base* ha de ser la
   ubicació exacta on resideix l'usuari vinculat a AD.

   Exemple: ``ou=organizational_unit,dc=example,dc=org``

Autenticació
============

Aquí podeu configurar la propietat als nodes LDAP on s'ha de trobar el nom d'usuari
donat. La propietat *UserId* s'ha d'omplir amb el nom de la
propietat on s'emmagatzema el nom d'usuari a LDAP.

La casella de verificació *Desar contrasenyes a la base de dades*, quan està marcada, significa que la
contrasenya també s'emmagatzema a la base de dades de LibrePlan. D'aquesta manera, si LDAP és
fora de línia o inaccessible, els usuaris LDAP poden autenticar-se contra la base de dades de LibrePlan.
Si no està marcada, els usuaris LDAP només poden ser autenticats contra LDAP.

Autorització
============

Aquesta secció us permet definir una estratègia per fer coincidir els rols LDAP amb els rols de
LibrePlan. La primera elecció és l'estratègia a utilitzar, depenent de la
implementació de LDAP.

Estratègia de Grup
------------------

Quan s'utilitza aquesta estratègia, indica que LDAP té una estratègia de grups de rols.
Això significa que els usuaris a LDAP són nodes que es troben directament sota una branca que
representa el grup.

L'exemple següent representa una estructura LDAP vàlida per usar l'estratègia de grup.

* Estructura LDAP::

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

En aquest cas, cada grup tindrà un atribut, per exemple, anomenat ``member``,
amb la llista d'usuaris que pertanyen al grup:

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

La configuració per a aquest cas és la següent:

* Estratègia de cerca de rols: ``Estratègia de grup``
* Ruta del grup: ``ou=groups``
* Propietat del rol: ``member``
* Consulta de cerca de rols: ``uid=[USER_ID],ou=people,dc=example,dc=org``

I, per exemple, si voleu fer coincidir alguns rols:

* Administració: ``cn=admins;cn=itpeople``
* Lector de serveis web: ``cn=itpeople``
* Escriptor de serveis web: ``cn=itpeople``
* Lectura permesa de tots els projectes: ``cn=admins``
* Edició permesa de tots els projectes: ``cn=admins``
* Creació de projectes permesa: ``cn=workers``

Estratègia de Propietat
-----------------------

Quan un administrador decideix utilitzar aquesta estratègia, indica que cada usuari
és un node LDAP, i dins del node, hi ha una propietat que representa
el grup o grups per a l'usuari. En aquest cas, la configuració no requereix el
paràmetre *Ruta del grup*.

L'exemple següent representa una estructura LDAP vàlida per usar l'estratègia de propietat.

* Estructura LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Amb Atribut**

En aquest cas, cada usuari tindrà un atribut, per exemple, anomenat ``group``,
amb el nom del grup al qual pertany:

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

   Aquesta estratègia té una restricció: cada usuari només pot pertànyer a un grup.

La configuració per a aquest cas és la següent:

* Estratègia de cerca de rols: ``Estratègia de propietat``
* Ruta del grup:
* Propietat del rol: ``group``
* Consulta de cerca de rols: ``[USER_ID]``

I, per exemple, si voleu fer coincidir alguns rols:

* Administració: ``admins;itpeople``
* Lector de serveis web: ``itpeople``
* Escriptor de serveis web: ``itpeople``
* Lectura permesa de tots els projectes: ``admins``
* Edició permesa de tots els projectes: ``admins``
* Creació de projectes permesa: ``workers``

**Per Identificador d'Usuari**

Fins i tot podeu usar una solució alternativa per especificar els rols de LibrePlan directament als usuaris
sense tenir un atribut a cada usuari LDAP.

En aquest cas, especificareu quins usuaris tenen els diferents rols de LibrePlan
per ``uid``.

La configuració per a aquest cas és la següent:

* Estratègia de cerca de rols: ``Estratègia de propietat``
* Ruta del grup:
* Propietat del rol: ``uid``
* Consulta de cerca de rols: ``[USER_ID]``

I, per exemple, si voleu fer coincidir alguns rols:

* Administració: ``admin1;it1``
* Lector de serveis web: ``it1;it2``
* Escriptor de serveis web: ``it1;it2``
* Lectura permesa de tots els projectes: ``admin1``
* Edició permesa de tots els projectes: ``admin1``
* Creació de projectes permesa: ``worker1;worker2;worker3``

Correspondència de Rols
-----------------------

A la part inferior d'aquesta secció, hi ha una taula amb tots els rols de LibrePlan
i un camp de text al costat de cada un. Això és per fer coincidir els rols. Per exemple,
si un administrador decideix que el rol *Administració* de LibrePlan coincideix
amb els rols *admin* i *administrators* de LDAP, el camp de text ha de contenir:
"``admin;administrators``". El caràcter per separar els rols és "``;``".

.. NOTE::

   Si voleu especificar que tots els usuaris o tots els grups tenen un permís, podeu
   usar un asterisc (``*``) com a comodí per fer referència a ells. Per exemple, si
   voleu que tothom tingui el rol *Creació de projectes permesa*, configureu la
   correspondència de rols de la manera següent:

   * Creació de projectes permesa: ``*``
