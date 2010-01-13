Etiquetas
#########

.. contents::

Las etiquetas son entidades que se utilizan en la aplicación para la organización conceptualmente de tareas o elementos de pedido.

Las etiquetas se categorizan según los tipos de etiquetas. Una etiqueta sólo pertenece a un tipo de etiqueta, sin embargo, nada impide crear tantas etiquetas similares que pertenezcan a tipos de etiquetas diferentes.

Tipos de etiquetas
==================

Los tipos de etiquetas se utilizan para agrupar tipologías de etiquetas que los usuarios deseen gestionar en la aplicación. Ejemplos de tipos de etiquetas posibles:

i. Cliente: El usuario podría estar interesado en etiquetar las tareas, pedidos o elementos de pedido en base al cliente que los solicitó.
ii. Zona: El usuario podría estar interesado en etiquetar las tareas, pedidos o elementos de pedido en base a la zona en la que se realizan.

La administración de tipos de etiquetas se gestionará desde la opción de menú de "Administración". Es desde esta opción, desde la que el usuario puede editar tipos de etiqueta, crear nuevos tipos de etiqueta o añadir etiquetas a tipos de etiquetas. Desde dicha operación puede accederse al listado de etiquetas.

.. figure:: images/tag-types-list.png
   :scale: 50

   Lista de tipos de etiquetas

Desde el listado de tipos de etiquetas es posible:

i. Crear nuevo tipo de etiquetas.
ii. Editar un tipo de etiquetas existente.
iii. Borrar un tipo de etiquetas con todas sus etiquetas.

Tanto la edición como la creación de etiquetas comparten formulario. Desde dicho formulario el usuario puede asignar un nombre al tipo de etiquetas, crear o borrar etiquetas y almacenar los cambios. Para realizar esto:

i. El usuario debería seleccionar una etiqueta a editar o presionar en el botón de creación de una nueva.
ii. El sistema muestra un formulario con una entrada de texto para el nombre y un listado de entradas de texto con las etiquetas existentes y asignadas.
iii. Si el usuario desea añadir una nueva etiqueta debe presionar en el botón "Etiqueta nueva".
iv. El sistema muestra una nueva fila al listado con una entrada de texto vacía que el usuario debe editar.
v. El usuario introduce un nombre para la etiqueta.
vi. El sistema añade el nombre al listado.
vii. El usuario presiona en "Guardar" para guardar y salir o "Guardar y Continuar" para guardar y continuar editando el formulario.

.. figure:: images/tag-types-edition.png
   :scale: 50

   Edición de tipos de etiquetas

Etiquetas
=========

Las etiquetas son entidades que pertenecen a un tipo de etiqueta. Estas entidades pueden ser asignadas a elementos de pedido. El hecho de asignar una etiqueta a un elemento de pedido hace que todos los elementos descendientes de dicho elemento hereden la etiqueta a la que pertenecen. El hecho de contar con una etiqueta asignada permite que esos elementos salgan filtrados en los puntos en los que se ofrece la posibilidad de búsqueda:

i. Búsqueda de tareas en el diagrama de Gantt.
ii. Búsqueda de elementos de pedido en el listado de elementos de pedido.
iii. Filtrados para informes.

La asignación de etiquetas a elementos de pedido es cubierta en el capítulo de pedidos.

