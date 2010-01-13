Avances
#######

.. contents::

El avance de un proyecto marca el grado en el que se está cumpliendo con el alcance estimado para la realización del mismo, además, el avance de una tarea indica ese mismo grado para el alcance estimado para dicha tarea.

Generalmente los avances no tienen un modo automático de ser medidos, y es una persona quien en base a la experiencia o a la realización de una lista de chequeo determina el grado de compleción de una tarea o un proyecto.

Cabe destacar que hay una diferencia importante entre el uso de horas asignadas a una tarea o proyecto, con el grado de avance en esa misma tarea o proyecto. Mientras que el uso de horas puede estar en desvío o no, el proyecto puede estar en un grado de avance inferior o superior al estimado para el día en el que se está controlando. Se producen, debido la estas dos medidas, varias posibles situaciones:

* Se consumieron menos horas de las estimadas para el elemento a medir y, al mismo tiempo, el proyecto está yendo más lento de lo estimado, porque el avance es inferior al estimado para el día de control.
* Se consumieron menos horas de las estimadas para el elemento a medir y, al mismo tiempo, el proyecto está yendo más rápido del estimado, porque el avance es inferior al estimado para el día de control.
* Se consumieron más horas de las estimadas y, al mismo tiempo, el proyecto está yendo más lento del estimado, porque el avance es inferior al estimado para el día de control.
* Se consumieron más horas de las estimadas y, al mismo, tiempo el proyecto está yendo más rápido del estimado, porque el avance es inferior al estimado para el día de control.

El contraste de estas posibles situaciones es posible realizarlo desde la propia planificación, utilizando información del grado de avance y por otro lado del grado de uso de horas. En este capítulo se tratará la introducción de la información para poder llevar un control del avance.

La filosofía implantada en el proyecto para el control del avance está basada en que el usuario divida hasta el punto en el que desea el control de avances de sus proyectos. En consecuencia, si el usuario desea controlar a nivel de pedido, sólo debe introducir información en los elementos de nivel 1, cuando se desea poder disponer de un control más fino sobre las tareas, debe introducir información de avances en niveles inferiores, siendo el sistema que propaga hacia arriba en la jerarquía todos los datos.

Gestión de tipos de avance
==========================

Cada empresa puede tener unas necesidades diferentes de control del avance de sus proyectos, y concretamente de las tareas que los componen. Por esta razón fue necesario contemplar la existencia de unas entidades en el sistema llamadas "tipos de avance". Los tipos de avance son diferentes tipologías que cada usuario puede dar de alta en el sistema para medir el avance de una tarea. Por ejemplo, una tarea puede ser medida porcentualmente, pero al mismo tiempo ese avance porcentual se traduce en un avance en *Toneladas* sobre lo acordado con el cliente.

Un tipo de avance está caracterizado por un nombre, un valor máximo y una precisión:

* Nombre: Será un nombre representativo que el usuario debe recordar para cuando seleccione la asignación de avances sea capaz de entender que tipo de avance está midiendo.
* Valor máximo: Es el valor máximo que se le permite a una tarea o proyecto establecer como medida total de avance. Por ejemplo, trabajando con *Toneladas*, si se considera que el máximo normal en toneladas es de 4000 y nunca va a haber tareas que requieran realizar más de 4000 toneladas de algún material, ese debería ser el valor máximo establecido.
* Precisión: Es el valor de los incrementos que se permiten para el tipo de avance creado. Por ejemplo, si el avance en *Toneladas* se va a medir en valores redondeados, podría ser 1 la precisión. Desde ese momento, sólo se podrían introducir medidas de avance con números enteros, por ejemplo, 1, 2, 300, etc.

El sistema cuenta con dos tipos de avance creados por defecto:

* Porcentual: Tipo de avance general que permite medir el avance de un proyecto o tarea en base al porcentaje que se estima de compleción, por ejemplo, una tarea está al 30% respeto al 100% estimado en un día concreto.
* Unidades: Tipo de avance general que permite medir el avance en unidades sin necesidad de especificar las unidades concretas. La tarea comprendía la creación de 3000 unidades y el avance son 500 unidades sobre las 3000 estimadas.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administración de tipos de avances

El usuario puede crear nuevos tipos de avance de la siguiente manera:

* El usuario accede a la sección de "Administración".
* Presiona en la opción del menú de segundo nivel "Gestionar tipos de avance".
* El sistema muestra un listado de tipos de avance existentes.
* Con cada tipo de avance el usuario puede:

  * Editar
  * Borrar

* A mayores, el usuario puede crear un tipo de avance nuevo.
* Con la edición o la creación, el sistema muestra un formulario con la siguiente información:

  * Nombre del tipo de avance.
  * Valor máximo que acepta el tipo de avance.
  * Precisión del tipo de avance.

Introducción de avances en base a tipo
======================================

La introducción de los avances se realiza sobre los elementos de pedido, sin embargo, es posible hacerlo con un atajo desde las tareas de planificación. Es responsabilidad del usuario la decisión sobre qué tipos de avance desea asociar a cada elemento de pedido.

Es posible introducir un tipo de avance único y defectivo para todo un pedido.

Antes de poder realizar medidas de avance, es necesario asociar el tipo elegido al pedido, por ejemplo, un avance de tipo porcentaje para medir porcentualmente los avances realizados sobre el total de la tarea, o un avance de tipo pactado por se se quieren introducir a futuro mediciones de avances pactadas con el cliente.

.. figure:: images/avance.png
   :scale: 40

   Pantalla de introducción de avances con visualización gráfica.

Para introducir mediciones de avance es necesario realizar el siguiente:

* Seleccionar el tipo de avance para lo cual desea introducir el avance.

  * Si no existe ningún tipo de avance es necesario añadir un tipo de avance.

* En el formulario que aparece debajo con los campos "Valor" y "Fecha" introducir el valor absoluto de la medida y la fecha que se le desea asignar a la medida tomada.
* El sistema almacena automáticamente los datos introducidos.


Contraste de avances sobre un elemento del pedido
=================================================

Es posible contrastar graficamente las evoluciones de los avances de los pedidos en base a las mediciones realizadas. Cada tipo de avance dispone de una columna con botones de chequeo (de título "Mostrar") el cual al ser marcado se muestra la gráfica de evoluciones de medidas realizadas sobre el elemento de pedido.

.. figure:: images/contraste-avance.png
   :scale: 40

   Contraste de varios avances.
