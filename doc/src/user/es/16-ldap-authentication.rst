Configuracion LDAP
##################

.. contents::

Esta pantalla permite establecer una conexión LDAP para delegar autenticación y/o autorización.

Está dividida en catro áreas diferentes relacionadas que se explican debajo:

Activación
==========


Esta área se usa para establecer las propiedades que configuran como *LibrePlan* usa LDAP.

Si el campo *Habilita autenticación LDAP* está marcado, *LibrePlan* utiliza el
LDAP para autenticar cada vez que el usuario intenta entrar en la aplicación.

El campo *Usar LDAP roles* marcado significa que el mapping entre el roles LDAP
y roles LibrePlan está establecido, de manera que los permisos en LibrePlan dependen
de los roles que el usuario tiene en el LDAP.

Configuración
=============

Esta sección tiene los parámentros para el acceso al LDAP. Los parámetros
*Base, UserDN y Password* son los parámetros para conectar al LDAP y buscar a
los usuarios. El usuario configurado debe tener permiso en
el LDAP. En la última parte de esta sección hay un botón para comprobar que la
conexión con el LDAP es posible con los parámetros configurados. Es una buena idea
probar la conexión antes de continuar con la configuración.

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
directamente de una rama que representa el grupo. Un ejemplo de ello sería tener
un LDAP con una rama (grupo) llamada *Admin* y que tuviera debajo dos nodos
(usuarios) llamados *John* y *William*. En este caso un administrador de
LibrePlan podría asignar ambos usuarios a un rol en LibrePlan.

El único parámetro en este caso es el *Path de grupo* que representa la ruta en
LDAP para encontras las ramas que representan los grupos.

Estrategia de propiedad
-----------------------

Cuando un usuario administrador decide usar esta estrategia, implica que cada
usuario es un nodo de LDAP y que en el nodo existe una propiedad que representa
el grupo o grupos al que pertenece el usuairo. En este caso, la configuración
necesita dos parámetros:

* *Propiedad del rol*. Representa la propiedad en el nodo del usuario que
  contiene todos los roles para el mismo.

* *Consulta para la búsqueda de roles*. Representa la ruta en el LDAP para
  encontrar los nodos de los usuarios. En este caso, hay que tener en cuenta que
  la cadena "[USER_ID]" representa el lugar en el que el nombre del usuario
  proporcionado en el formulario de entrada debe ser situado para obtener el
  nodo apropiado correspondiente al usuario en el LDAP.

En el fondo de la pantalla de esta sección hay una tabla con todos los roles de
LibrePlan y un campo de texto anexo a cada uno de ellos. Esta área es para la
asociación de los roles. Por ejemplo, si un usuario administrador de LibrePlan
decide que rol de LibrePlan *Administración* se corresponde con los roles
*admin* y *administrators* del LDAP en el campo de texto hay que configurar:
"admin;administrators". El carácter de separación de roles es ";".
