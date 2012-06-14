Configuración LDAP
##################

.. contents::

Esta pantalla permite establecer una conexión LDAP para delegar autenticación
y/o autorización.

Está dividida en catro áreas diferentes relacionadas que se explican debajo:

Activación
==========

Esta área se usa para establecer las propiedades que configuran como *LibrePlan*
usa LDAP.

Si el campo *Habilita autenticación LDAP* está marcado, *LibrePlan* utiliza el
LDAP para autenticar cada vez que el usuario intenta entrar en la aplicación.

El campo *Usar LDAP roles* marcado significa que el mapping entre el roles LDAP
y roles LibrePlan está establecido, de manera que los permisos en LibrePlan
dependen de los roles que el usuario tiene en el LDAP.

Configuración
=============

Esta sección tiene los parámentros para el acceso al LDAP. Los parámetros
*Base*, *UserDN* y *Contraseña* son los parámetros para conectar al LDAP y buscar
a los usuarios. El usuario configurado debe tener permiso en el LDAP. En la
última parte de esta sección hay un botón para comprobar que la conexión con el
LDAP es posible con los parámetros configurados. Es una buena idea probar la
conexión antes de continuar con la configuración.

.. NOTE::

   Si su LDAP está configurado para trabajar con autenticación anónima puede
   dejar vacíos los atributos *UserDN* y *Contraseña*.

Autenticación
=============

En esta sección se configuran la propiedad de los nodos de LDAP donde se
encuentra almacenada el login del usuario. La propiedad *UserId* debe ser
rellenado con el nombre de la propiedad donde el login está almacenado en el
LDAP.

El checkbox *Almacenar contraseñas en la base de datos* cuando se encuentra
marcado, indica que la contraseña se almacena también en la base de datos
LibrePlan. De esta forma, si el LDAP está offline o no existe conectividad, los
usuarios del LDAP pueden autenticarse contra la base de datos de LibrePlan. Si
no está marcado, los usuario de LDAP sólo pueden ser autenticados contro el
LDAP.

Autorización
============

Esta sección permite definir la estrategia para asociar los roles de LDAP con
los roles de LibrePlan.

Estrategia de grupo
-------------------

Cuando se usa esta estrategia, implica que el LDAP está usando una estrageia de
grupo de rol. Significa que los usuarios en el LDAP son nodos que cuelgan
directamente de una rama que representa el grupo.

El siguiente ejemplo representa una estructura de LDAP válida para usar la
estrategia de grupo.

* Estructura del LDAP::

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

En este caso, cada grupo tendrá un atributo, por ejemplo llamado ``member``,
con la lista de usuarios que pertenencen al grupo:

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

La configuración para este caso es la siguiente:

* Estrategia para la búsqueda de roles: ``Estrategia de grupo``
* Path del grupo: ``ou=groups``
* Propiedad del rol: ``member``
* Consulta para la búsqueda de roles: ``uid=[USER_ID],ou=people,dc=example,dc=org``

Y por ejemplo si quiere hacer alguna correspondencia de roles:

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

Estrategia de propiedad
-----------------------

Cuando el administrador decide usar esta estrategia, implica que cada usuario es
un nodo de LDAP y que en el nodo existe una propiedad que representa el grupo o
grupos al que pertenece el usuairo. En este caso, la configuración no necesita
el parámetro *Path del grupo*:

El siguiente ejemplo representa una estructura de LDAP válida para usar la
estrategia de propiedad.

* Estructura del LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Con atributo**

En este caso, cada usuario tendrá un atributo, por ejemplo llamado ``group```
con el nombre del grupo al que pertenece:

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

   Esta estrategia tienen una restricción, cada usuario puede pertenecer sólo a un
   grupo.

La configuración para este caso es la siguiente:

* Estrategia para la búsqueda de roles: ``Estrategia de propiedad``
* Path del grupo:
* Propiedad del rol: ``group``
* Consulta para la búsqueda de roles: ``[USER_ID]``

Y por ejemplo si quiere hacer alguna correspondencia de roles:

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

**Por identificador de usuario**

Incluso puede tener una solución para especificar los roles de LibrePlan
directamente a los usuarios, sin tener un atributo en cada usuario de LDAP.

En este caso, especificará que usuarios tienen los diferentes roles por ``uid``.

La configuración para este caso es la siguiente:

* Estrategia para la búsqueda de roles: ``Estrategia de propiedad``
* Path del grupo:
* Propiedad del rol: ``uid``
* Consulta para la búsqueda de roles: ``[USER_ID]``

Y por ejemplo si quiere hacer alguna correspondencia de roles:

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

Correspondencia de roles
------------------------

En el fondo de la pantalla de esta sección hay una tabla con todos los roles de
LibrePlan y un campo de texto anexo a cada uno de ellos. Esta área es para la
asociación de los roles. Por ejemplo, si un usuario administrador de LibrePlan
decide que rol de LibrePlan *Administración* se corresponde con los roles
*admin* y *administrators* del LDAP en el campo de texto hay que configurar:
"``admin;administrators``". El carácter de separación de roles es "``;``".

.. NOTE::

   Si quiere especificar que todos los usuarios o todos los roles tengan un
   permiso puede usar un asterisco (``*``) como comodín para referirse a ellos.
   Por ejemplo, si quiere que todo el mundo tenga el rol *Project creation
   allowed* configurará la correspondica de roles como sigue:

   * Project creation allowed: ``*``
