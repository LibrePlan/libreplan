Configuracion LDAP
##################

.. contents::

Esta pantalla permite establecer unha conexión LDAP para delegar autenticación e/ou autorización.

Está dividida en catro áreas diferentes relacionadas que se explican debaixo:

Activación
==========


Esta área úsase para establecer as propiedades que configuran como *LibrePlan* usa LDAP.

Se o campo *Habilita autenticación LDAP* está marcado, *LibrePlan* utiliza o
LDAP para autenticar cada vez que o usuario tenta entrar na aplicación.

O campo *Usar LDAP roles* marcado significa que o mapping entre os roles LDAP
e os roles de LibrePlan está establecido, de maneira que os permisos en LibrePlan dependen
dos roles que o usuario ten no LDAP.

Configuración
=============

Esta sección ten os parámentros para o acceso ao LDAP. Os parámetros
*Basee, UserDN e Password* son os parámetros para conectar ao LDAP e buscar aos
usuarios. O usuario configurado debe ter permiso no
LDAP. Na última parte desta sección hai un botón para comprobar que a
conexión co LDAP é posible cos parámetros configurados. É unha boa idea
probar a conexión antes de continuar coa configuración.

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

Cando se usa esta estratexia, implica que o LDAP está a usar unha estratexia de grupo
de rol. Significa que os usuarios no LDAP son nodos que colgan
directamente dunha rama que representa o grupo. Un exemplo diso sería ter
un LDAP cunha rama (grupo) chamada *Admin* e que tivese debaixo dous nodos
(usuarios) chamados *John* e *William*. Neste caso un administrador de LibrePlan
podería asignar ambos usuarios a un rol en LibrePlan.

O único parámetro neste caso é o *Path de grupo* que representa a ruta en LDAP
para atopar as ramas que representan os grupos.

Estratexia de propiedade
------------------------

Cando un usuario administrador decide usar esta estratexia, implica que cada
usuario é un nodo de LDAP e que no nodo existe unha propiedade que representa
o grupo ou grupos ao que pertence o usuairo. Neste caso, a configuración
necesita dous parámetros:

   * *Propiedade do rol*. Representa a propiedade no nodo do usuario que
     contén todos os roles para o mesmo.
   * *Consulta para a procura de roles*. Representa a ruta no LDAP para
     atopar os nodos dos usuarios. Neste caso, hai que ter en conta que
     a cadea "[USER_IDE]" representa o lugar no que o nome do usuario
     proporcionado no formulario de entrada debe ser situado para obter o
     nodo apropiado correspondente ao usuario no LDAP.

No fondo da pantalla desta sección hai unha táboa con todos os roles de LibrePlan
e un campo de texto anexo a cada un deles. Esta área é para a
asociación dos roles. Por exemplo, se un usuario administrador de LibrePlan
decide que rol de LibrePlan *Administración* correspóndese cos roles
*admin* e *administrators* do LDAP no campo de texto hai que configurar:
"admin;administrators". O carácter de separación de roles é ";".
