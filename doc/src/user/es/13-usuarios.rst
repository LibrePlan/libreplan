Usuarios
########

.. _tareas:
.. contents::


Administración de usuarios
==========================

El sistema de usuarios de "LibrePlan" permite gestionar perfiles, permisos y usuarios. Un usuario pertenece a un perfil de usuario y por otro lado los perfiles pueden tener una serie de roles predefinidos para el acceso a la aplicación. Los roles son los permisos definidos sobre "LibrePlan". Ejemplos de roles:

* Administración: Rol que los usuarios administradores deben tener asignados para poder realizar operaciones de administración.
* Lector de servicios web: Rol que los usuarios necesitan para poder consultar servicios web de la aplicación.
* Escritor de servicios web: Rol que los usuarios necesitan para poder escribir utilizando los servicios web de la aplicación.

Los roles están predefinidos en el sistema. Un perfil de usuario está compuesto por uno o varios roles, de modo que se comprueban roles a los que pertenecen los usuarios para realizar ciertas operaciones.

Los usuarios pertenecen a un o varios perfiles o directamente a un o varios roles, de modo que se puede asignar permisos específicos o un grupo de permisos genérico.

Para administrar usuarios es necesario realizar los siguientes pasos:

* Acceder a la operación de "Gestionar usuarios" del menú de "Administración".
* La aplicación muestra un formulario con el listado de usuarios.
* Presionar el botón de edición del usuario elegido o presionar en el botón "Crear".
* Mostrara un formulario con los siguientes campos:

   * Nombre de usuario.
   * Contraseña
   * Habilitado/Deshabilitado.
   * E-mail
   * Lista de roles asociados. Para añadir un nuevo rol es necesario buscar uno de los roles mostrados en la lista de selección y presionar en "Asignar".
   * Lista de perfiles asociados. Para añadir un nuevo perfil es necesario buscar uno de los perfiles mostrados en la lista de selección y presionar en "Asignar".

.. figure:: images/manage-user.png
   :scale: 50

   Administración de usuarios

* Presionar en "Guardar" o "Guardar y Continuar".


Administración de perfiles
--------------------------

Para administrar los perfiles de la aplicación es necesario dar los siguientes pasos:

* Acceder a la operación de "Gestionar perfiles de usuario" del menú de "Administración".
* La aplicación muestra un listado de perfiles.
* Presionar el botón de edición del perfil elegido o presionar en el botón "Crear".
* La aplicación muestra un formulario con los siguientes campos:

   * Nombre
   * Lista de roles (permisos) asociados al perfil. Para añadir un rol asociado al perfil se deberá seleccionar uno de la lista de roles y presionar en "Añadir".

.. figure:: images/manage-user-profile.png
   :scale: 50

   Gestión de perfiles de usuarios

* Presionar en "Guardar" o "Guardar y Continuar" y el sistema almacena el perfil creado o modificado.

