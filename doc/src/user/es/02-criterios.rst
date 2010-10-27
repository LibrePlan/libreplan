Criterios
#########

.. contents::

Los criterios son los elementos que se utilizan en la aplicación para categorizar los recursos y las tareas. Las tareas requieren criterios y los recursos los satisfacen. Un ejemplo de utilización de criterios es la siguiente secuencia: un recurso es asignado con el criterio "soldador" (es decir, satisface el criterio "soldador") y una tarea requiere el criterio "soldador" para ser realizada, en consecuencia ante una asignación de recursos a tareas los trabajadores con criterio "soldador" son los utilizados a la hora de asignar recursos genéricamente (no aplica en las asignaciones específicas). Ver capítulo de asignación de recursos para entender los distintos tipos de asignaciones.

En el proyecto, existen varias operaciones que se pueden realizar con criterios:

* Administración de criterios
* Asignación de criterios a recursos.
* Asignación de criterios a tareas.
* Filtrado de entidades por criterios. Es posible filtrar tareas y elementos de pedido por criterios para realizar operaciones en la aplicación.

De las tres funcionalidades anteriores sólo se explicará la primera de ellas en esta sección dejando para secciones posteriores los dos tipos de asignación, la de recursos en el capitulo "Gestión de recursos" y la de filstrado en el capítulo "Planificación de tareas".


Administración de criterios
===========================
La administración de criterios es accesible desde el menú de administración:

.. figure:: images/menu.png
   :scale: 50

   Pestañas de menú de primer nivel

La operación específica para administrar criterios es *Gestionar Criterios*. A partir de dicha operación es posible listar los criterios disponibles en el sistema.

.. figure:: images/lista-criterios.png
   :scale: 50

   Listado de criterios

Presionando en el botón *Crear* se podrá acceder al formulario de creación/edición de un criterio. La edición de un criterio se realiza presionando en el icono de edición del incluso.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Edición de criterios

En el formulario de edición de criterios que se muestra en la imagen anterior se pueden realizar las siguientes operaciones:

* Edición del nombre del criterio
* Indicar si es posible asignar varios valores simultáneamente ó mesmo elemento para el tipo de criterio seleccionado. Por ejemplo, un recurso que satisface dos criterios, soldador y torneiro.
* Indicar el tipo del criterio:

  * Tipo genérico: Criterio que puede satisfacer indistintamente una máquina o un trabajador.
  * Tipo trabajador: Criterio que puede satisfacer un trabajador exclusivamente.
  * Tipo máquina: Criterio que puede satisfacer una máquina exclusivamente.

* Indicar si el criterio es jerárquico o no. Existen casos en los que los criterios deben ser tratados jerarquicamente, es decir, que el hecho de ser un criterio asignado a un elemento no hoja haga que este criterio esté asignado automáticamente a los hijos. Un ejemplo claro de jerarquización de criterios es el criterio localización, por ser jerárquico una persona que haya asignado Galicia como localización pertenecerá a la localización España.
* Indicar si el criterio está habilitado y deshabilitado. Esta es el modo de borrar criterios. Debido a que una vez creado un criterio, y utilizado en datos históricos, estos no pueden ser cambiados, el criterio debe existir en el sistema. Para evitar que este criterio salga en diferentes elementos de selección, puede ser invalidado.
* Realizar una descripción del criterio.
* Añadir nuevos valores. En la segunda parte del formulario aparece una entrada de texto con un botón *Nuevo Criterio*.
* Editar el nombre de los criterios existentes.
* Desplazar verticalmente los criterios en la lista de los existentes.
* Eliminar un valor de criterio de la lista.

El formulario de administración de criterios es un formulario que responde a las características de los formularios comentados en la introducción como de 3 operaciones (guardar, guardar y cerrar y cerrar).


