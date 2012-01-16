Configuracion LDAP
##################

.. contents::

Esta pantalla permite establecer unha conexión LDAP para delegar autenticación
e/ou autorización.

Está dividida en catro áreas diferentes relacionadas que se explican debaixo:

Activación
==========

Esta área úsase para establecer as propiedades que configuran como *LibrePlan*
usa LDAP.

Se o campo *Habilita autenticación LDAP* está marcado, *LibrePlan* utiliza o
LDAP para autenticar cada vez que o usuario tenta entrar na aplicación.

O campo *Usar LDAP roles* marcado significa que o mapping entre os roles LDAP
e os roles de LibrePlan está establecido, de maneira que os permisos en
LibrePlan dependen dos roles que o usuario ten no LDAP.

Configuración
=============

Esta sección ten os parámentros para o acceso ao LDAP. Os parámetros
*Base*, *UserDN* e *Contrasinal* son os parámetros para conectar ao LDAP e
buscar aos usuarios. O usuario configurado debe ter permiso no LDAP. Na última
parte desta sección hai un botón para comprobar que a conexión co LDAP é posible
cos parámetros configurados. É unha boa idea probar a conexión antes de
continuar coa configuración.

.. NOTE::

   Se o seu LDAP está configurado para traballar con autenticación anónima pode
   deixar baleiros oos atributos *UserDN* e *Contrasinal*.

Autenticación
=============

Nesta sección configúranse a propiedade dos nodos de LDAP onde se atopa
almacenada o login do usuario. A propiedade *UserId* debe ser
enchido co nome da propiedade onde o login está almacenado no
LDAP.

O checkbox *Almacenar contrasinais na base de datos* cando se atopa
marcado, indica que a contrasinal se almacena tamén na base de datos
LibrePlan. Desta forma, se o LDAP está offline ou non existe conectividad, os
usuarios do LDAP poden autenticarse contra a base de datos de LibrePlan. Se
non está marcado, o usuario de LDAP só poden ser autenticados contro o
LDAP.

Autorización
============

Esta sección permite definir a estratexia para asociar os roles de LDAP cos
roles de LibrePlan.

Estratexia de grupo
-------------------

Cando se usa esta estratexia, implica que o LDAP está a usar unha estratexia de
grupo de rol. Significa que os usuarios no LDAP son nodos que colgan
directamente dunha rama que representa o grupo.

O seguiente exemplo representa unha estrutura de LDAP válida para usar a
estratexia de grupo.

* Estrutura do LDAP::

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

Neste caso, cada grupo tenrá un atributo, por exemplo chamado ``member``,
coa lista de usuarios que pertencen ao grupo:

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

A configuración para este caso é a seguinte:

* Estratexia para a búsqueda de roles: ``Estratexia de grupo``
* Path do grupo: ``ou=groups``
* Propiedade do rol: ``member``
* Consulta para a búsqueda de roles: ``uid=[USER_ID],ou=people,dc=example,dc=org``

E por exemplo se quere facer algunha correspondencia de roles:

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

Estratexia de propiedade
------------------------

Cando o administrador decide usar esta estratexia, implica que cada usuario é
un nodo de LDAP e que no nodo existe unha propiedade que representa o grupo ou
grupos ao que pertence o usuairo. Neste caso, a configuración non necesita
o parámetro *Path do grupo*:

O seguiente exemplo representa unha estrutura de LDAP válida para usar a
estratexia de propiedade.

* Estrutura do LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

Con atributo
............

Neste caso, cada usuario tenrá un atributo, por exemplo chamado ``group```
co nome do grupo ao que pertence:

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

   Esta estratexia ten unha restricción, cada usuario pode pertencer só a un
   grupo.

A configuración para este caso é a seguinte:

* Estratexia para a búsqueda de roles: ``Estratexia de propiedade``
* Path do grupo:
* Propiedade do rol: ``group``
* Consulta para a búsqueda de roles: ``[USER_ID]``

E por exemplo se quere facer algunha correspondencia de roles:

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

Por identificador de usuario
............................

Incluso pode ter unha solución para especificar os roles de LibrePlan
directamente aos usuarios, sen ter un atributo en cada usuario de LDAP.

Neste caso, especificará que usuarios teñen os diferentes roles por ``uid``.

A configuración para este caso é a seguinte:

* Estratexia para a búsqueda de roles: ``Estratexia de propiedade``
* Path do grupo:
* Propiedade do rol: ``uid``
* Consulta para a búsqueda de roles: ``[USER_ID]``

E por exemplo se quere facer algunha correspondencia de roles:

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

Correspondencia de roles
------------------------

No fondo da pantalla desta sección hai unha táboa con todos os roles de
LibrePlan e un campo de texto anexo a cada un deles. Esta área é para a
asociación dos roles. Por exemplo, se un usuario administrador de LibrePlan
decide que rol de LibrePlan *Administración* correspóndese cos roles
*admin* e *administrators* do LDAP no campo de texto hai que configurar:
"``admin;administrators``". O carácter de separación de roles é "``;``".

.. NOTE::

   Se quere especificar que todos os usuarios ou todos os roles teñan un
   permiso pode usar un asterisco (``*``) coma comodín para referirse a eles.
   Por exemplo, se quere que todo o mundo teña o rol *Project creation
   allowed* configurará a correspondica de roles coma segue:

   * Project creation allowed: ``*``
