Pedidos y elementos de pedidos
##############################

.. contents::

Los pedidos son las entidades que describen los trabajos a realizar por parte de los usuarios que utilicen la aplicación.
Cada pedido se corresponde con los proyectos que las empresas van a ofrecer a sus clientes.

Un pedido está formado por una o varias líneas de pedido. Cada línea de pedido se corresponde con la estruturación que se desee realizar sobre los trabajos dentro de ese pedido. Las líneas de pedido se organizan de modo jerárquica sin limitaciones de profundidad. El hecho de que las líneas de pedido se organicen de modo jerárquico marca el funcionamiento de diversas características heredables, tal como las etiquetas.

En las siguientes secciones se describirán las operaciones que un usuario puede realizar con los pedidos y las líneas de pedidos.

Pedido
======

Un pedido es un proyecto o trabajo que un cliente le solicita a una empresa. El pedido para lo planificador identifica la entidad de proyecto dentro de la empresa. La diferencia de las aplicaciones de gestión globales, "LibrePlan" sólo necesita utilizar ciertos datos de los pedidos. Estos datos son:

* Nombre del pedido
* Código del pedido.
* Importe total del pedido
* Fecha de comienzo estimada
* Fecha de finalización
* Responsable
* Descripción
* Calendario asignado.
* Autogeneración de códigos. Campo para indicarle al sistema que autogenere los códigos de los elementos de pedido y de los grupos de horas.
* Preferencia entre dependencias y restricciones. El usuario puede elegir dependencias o restricciones como prioritarios a la hora de ser aplicados cuendo entran en conflicto.

Sin embargo, el propio pedido está relacionado con otras entidades que finalmente lo dotan de una mayor riqueza:

* Horas asignadas al pedido.
* Avances imputados al pedido.
* Etiquetas.
* Criterios asignados al pedido.
* Materiales
* Formularios de calidad

Para editar o crear un pedido es posible realizarlo desde varios puntos de la aplicación:

* Accediendo a la perspectiva de "Lista de pedidos" dentro de la vista global de empresa.

   * La edición se realiza presionando en el botón de editar sobre lo pedido elegido.
   * La creación se realiza presionando en "Nuevo pedido".

* Accediendo a un pedido en el diagrama de Gantt y cambiando de perspectiva a la de detalles de pedido.


Desde la edición de pedido es posible acceder a las siguientes pestañas:

* Edición de los datos de pedidos. Desde dicha pantalla es posible editar los datos básicos del pedido. Actualmente:

  * Nombre
  * Código
  * Fecha de comienzo estimada
  * Fecha de finalización
  * Responsable
  * Cliente
  * Descripción

.. figure:: images/order-edition.png
   :scale: 50

   Edición de pedido

* Listado de elementos de pedido. Desde lo listado de elementos de pedido es posible realizar varias operaciones:

  * Crear nuevos elementos de pedido.
  * Subir en un mismo nivel de la jerarquía un elemento de pedido.
  * Bajar en un mismo nivel de la jerarquía un elemento de pedido.
  * Indentar un elemento de pedido, o lo que es lo mismo, mover hacia abajo en la jerarquía, cambiando de nivel del elemento.
  * Des-indentar un elemento de pedido, o lo que es lo mismo, mover hacia arriba en la jerarquía, cambiando de nivel del elemento.
  * Filtrar los elementos de pedido.
  * Borrar elementos de pedido.
  * Mover arrastrando y soltando un elemento de pedido en la jerarquía.

.. figure:: images/order-elements-list.png
   :scale: 40

   Listado de elementos de pedido

* Horas asignadas. Pantalla en la que se muestran las horas totales imputadas al proyecto, tratándose de una pantalla de visualización donde se agrupan las horas imputadas a los elementos de pedido.

.. figure:: images/order-assigned-hours.png
   :scale: 50

   Asignación de horas imputadas al pedido por los trabajadores

* Avances. Pantalla en la que se permiten imputar tipos de avances y medidas de avances al pedido. Ver sección de "Avances" para el funcionamiento de la misma.

* Etiquetas. Pantalla en la que se permite asignar etiquetas a un pedido y conocer las etiquetas directas e indirectas que ya fueron asignadas. Ver la siguiente sección de edición de elementos de pedido más en profundidad para conocer el procedimiento de gestionar las etiquetas.

.. figure:: images/order-labels.png
   :scale: 35

   Etiquetas de pedido

* Criterios. Pantalla en la que se pueden asignar los criterios que se desean aplicar globalmente a todas las tareas de un pedido. Estos criterios serían aplicados de manera automática a todos los elementos de pedido, excepto que estos fueran invalidados explícitamente. Asimismo, se pueden visualizar los grupos de horas de los elementos de pedidos agrupados por criterios, lo cual permite conocer los criterios que se exigen a lo largo de un pedido.

.. figure:: images/order-criterions.png
   :scale: 50

   Criterios de pedido

* Materiales. Pantalla en la que se puede asignar el material disponible al pedido. El material es seleccionable de entre las categorías de material que existen en la aplicación. Los materiales son gestionados del siguiente modo:

   * El usuario selecciona la pestaña que se muestra en la zona inferior de la pantalla llamada "Búsqueda de materiales".
   * El usuario puede introducir un texto para la búsqueda de materiales o seleccionar las categorías para las que se desea mostrar materiales.
   * El sistema filtra los resultados.
   * El usuario selecciona los materiales que desea (para seleccionar más de un material el usuario puede presionar en el botón "Ctrl" que permite la selección múltiple en el listado.
   * El usuario presiona en "Asignar".
   * El sistema muestra el listado de materiales ya asignado al pedido.
   * El usuario selecciona las unidades y el estado que desea asignarle al pedido.
   * El usuario presiona en "Guardar" o "Guardar y Continuar".
   * Si el usuario desea gestionar la recepción de material, el sistema permite presionar en "Dividir" para cambiar el estado de una cantidad parcial de materiales.

.. figure:: images/order-material.png
   :scale: 50

   Material asociado a un pedido

* Calidad. Es posible asignar un formulario de calidad al pedido con el objetivo de ser cumplimentado para asegurar la realización de ciertas actividades asociadas al pedido. Ver la sección siguiente sobre edición de elementos de pedido para conocer el procedimiento de gestionar el formulario de calidad.

.. figure:: images/order-quality.png
   :scale: 50

   Formulario de calidad asociado al pedido

Edición de elementos de pedido
==============================

La edición de elementos de pedido se realiza desde la pestaña de "Listado de elementos de pedido" a partir del icono de edición. Si el usuario presiona en el icono de edición, el sistema muestra una nueva pantalla desde la que el usuario puede realizar lo siguiente:

* Editar la información del elemento de pedido.
* Visualización de horas imputadas a elemento de pedido.
* Gestionar los avances de los elementos de pedido.
* Gestionar las etiquetas del pedido.
* Gestionar los criterios exigidos por el elemento de pedido.
* Gestionar los materiales.
* Gestionar los formularios de calidad.

Las siguientes subsecciones tratan cada uno de las operaciones en profundidad.

Edición de la información del elemento de pedido
------------------------------------------------

La edición de información de elemento de pedido incluye la edición de los siguientes datos:

* Nombre del elemento de pedido.
* Código del elemento de pedido.
* Fecha de inicio del elemento de pedido.
* Fecha estimada de fin del elemento de pedido.
* Horas totales del elemento de pedido. Estas horas pueden ser calculadas a partir de los grupos de horas añadidas o introducidas directamente en este punto que se habían repartido entre los grupos de horas, creando algún nuevo si los porcentajes no coinciden con los porcentajes iniciales.
* **Grupos de horas**: ES posible añadir uno o varios grupos de horas al elemento de pedido. **El significado disteis grupos de horas** es el establecimiento de los requerimientos que se le exigen a los recursos que vayan a ser asignados para realizarlas.
* Criterios: ES posible añadir criterios que se deben satisfacer para poder se asignado xenericamente para realizar dicho elemento de pedido.

.. figure:: images/order-element-edition.png
   :scale: 50

   Edición de elemento de pedido

Visualización de horas imputadas a elementos de pedido
------------------------------------------------------

La pestaña de "Horas asignadas" permite visualizar los partes de trabajo asociados a un elemento de pedido y al incluso tiempo permite visualizar cuantas horas de las presupuestadas están ya realizadas.

.. figure:: images/order-element-hours.png
   :scale: 50

   Horas asignadas a elementos de pedido

La pantalla está dividida en dos partes:

* Listado de partes de trabajo: El usuario ve el listado de partes de trabajo que están asociados al elemento de pedido pudiendo comprobar la fecha y hora, recurso y número de horas dedicadas a la tarea.
* Uso de las horas presupuestadas: El sistema calcula el total de horas dedicadas a la tarea y las contrasta con las que estaban presupuestadas.

Gestión de avances de los elementos de pedido
---------------------------------------------

La introducción de tipos de avances y gestión de los avances de los elementos de pedido fue descrita en el capítulo de "Avances".

Gestión de etiquetas del pedido
-------------------------------

Las etiquetas, tal y como se describen en el capítulo dedicado a las mismas, son entidades que permiten categorizar los elementos de pedido. De este modo, el usuario puede agrupar información de planificación o pedidos en base a ellas.

Un usuario puede asignar etiquetas directamente a un elemento de pedido o bien a un antecesor en la jerarquía del elemento de pedido. A partir del momento en el que se asigna una etiqueta de uno de las dos formas anteriores, tanto el elemento de pedido como la tarea de planificación asociada estan asociadas a dicha etiqueta, siendo utilizadas para posteriores filtrados.

.. figure:: images/order-element-tags.png
   :scale: 50

   Asignación de etiquetas para elementos de pedido

Tal y como se puede ver en la imagen, desde la pestaña de **etiquetas**, el usuario puede realizar las siguientes operaciones:

* Visualización de las etiquetas que un elemento del pedido tiene asociadas por herencia de un elemento de pedido superior en la jerarquía a la que le fue asignada directamente. La tarea de planificación asociada a cada elemento de pedido tiene las mismas etiquetas asociadas.
* Visualización de las etiquetas que un elemento del pedido tiene asociadas directamente a través del siguiente formulario de asignación de etiquetas inferior.
* Asignar etiquetas existentes: Un usuario puede asignar etiquetas a partir de la búsqueda de una entre las existentes en el formulario inferior al listado de etiquetas directas. Para buscar una etiqueta llega con presionar en el icono con la lupa o escribir el inicio de la etiqueta en la entrada de texto para que el sistema muestre las opciones disponibles.
* Crear y asignar etiquetas nuevas: Un usuario puede crear nuevas etiquetas asociadas a un tipo de etiquetas existente desde dicho formulario. Para realizar la operación es necesario que seleccione un tipo de etiqueta a la que se asocia y se introduzca el valor de la etiqueta para el tipo seleccionado. Presionando en "Crear y asignar" el sistema ya la crea automáticamente y la asigna al elemento de pedido.


Gestionar los criterios exigidos por el elemento de pedido y los grupos de horas
--------------------------------------------------------------------------------

Tanto un pedido como un elemento de pedido pueden tener asignados los criterios que se exigen para ser realizados. Los criterios pueden afectar de manera directa o de manera indirecta:

* Criterios directos: Son los que se asignan directamente al elemento de pedido. Son los criterios que se van a exigir a los grupos de horas que forman parte del elemento de pedido.
* Criterios indirectos: Son los criterios que se asignan en elementos de pedido superiores en la jerarquía y son heredados por el elemento en edición.

A mayores del criterio exigido, es posible definir uno o varios grupos de horas que forman parte del elemento de pedido. Dependiendo de se el elemento de pedido contiene otros elementos de pedido como hijos o es un nodo hoja. En el primero de los casos los datos de horas y grupos de horas son solo visualizables y en el caso de nodos hoja son editables. El funcionamiento en este segundo caso es el siguiente:

* Por defecto, el sistema crea un grupo de horas asociado al elemento de pedido. Los datos modificables para un grupo de horas son:

   * Código del grupo de horas, si no es autogenerado.
   * Tipo de criterio. El usuario puede elegir se desea asignar un criterio de tipo máquina o trabajador.
   * Número de horas del grupo de horas.
   * Lista de criterios que se aplican al grupo de horas. Para añadir nuevos criterios el usuario debe presionar en "Añadir criterio" y seleccionar uno en el buscador que aparece tras presionar en el botón.

* El usuario puede añadir nuevos grupos de horas con características diferentes que los grupos de horas anteriores. Ejemplo de esto sería que un elemento de pedido debe ser hecho por un soldador (30h) y por un pintor (40h).

.. figure:: images/order-element-criterion.png
   :scale: 50

   Asignación de criterios a elementos de pedidos

Gestionar los materiales
------------------------

Los materiales son gestionados en los proyectos como un listado asociado a cada línea de pedido o a un pedido globalmente. El listado de materiales está formado por los siguientes campos:

* Código
* Fecha
* Unidades: Unidades necesarias.
* Tipo de unidad: Tipo de unidad en el que se mide el material.
* Precio de la unidad: Precio unitario.
* Precio total: Precio resultante de multiplicar el precio unitario por las unidades.
* Categoría: Categoría de material a la que pertenece.
* Estado: Recibido, Solicitado, Pendiente, Procesando, Cancelado.

El modo de trabajar con los materiales es el siguiente:

* El usuario selecciona la pestaña de "Materiales" de un elemento de pedido.
* El sistema muestra dos subpestanas: "Materiales" y "Búsqueda de materiales".
* Si el elemento de pedido no tenía materiales asignados, la primera pestaña muestra un listado vacío.
* El usuario presiona en "Búsqueda de materiales" en la zona inferior izquierda de la ventana.
* El sistema muestra el listado de categorías disponibles y los materiales asociados.

.. figure:: images/order-element-material-search.png
   :scale: 50

   Búsqueda de material

* El usuario selecciona categorías en las que buscar para afinar la búsqueda de materiales.
* El sistema muestra los materiales pertenecientes a las categorías seleccionadas.
* El usuario selecciona en el listado de materiales aquellos que desea asignar al elemento de pedido.
* El usuario presiona en "Asignar".
* El sistema muestra el listado seleccionado de materiales en la pestaña de "Materiales" con nuevos campos por cubrir.

.. figure:: images/order-element-material-assign.png
   :scale: 50

   Asignación de material a elemento de pedido

* El usuario selecciona las unidades, estado y fecha de los materiales asignados.

Para control posterior de los materiales es posible cambiar el estado de un grupo de unidades del material recibido. Esta operación se realiza del siguiente modo:

* El usuario presiona en el botón "Dividir" que se muestra en el listado de materiales a la derecha de cada fila.
* El usuario selecciona el número de unidades para los que desea dividir la fila.
* La aplicación muestra dos filas con el material dividido.
* El usuario cambia el estado de la fila de material que desea.

La utilidad de esta operación de división es la de poder recibir entregas parciales de material sin necesidad de esperar a recibirlo todo para marcarlo cómo recibido.

Gestionar los formularios de calidad
------------------------------------

Existen elementos de pedido que deben certificar que ciertas tareas fueron realizados para poder ser marcadas cómo completadas. Es por eso que surgen los formularios de calidad, las cuales están formados por una lista de preguntas que pueden haber asignado un peso según sea contestada positivamente.

Es importante destacar que un formulario de calidad debe ser creado previamente para poder ser asignado al elemento de pedido.

Para gestionar los formulario de calidad:

* El usuario accede a la pestaña de "Formularios de calidad".

.. figure:: images/order-element-quality.png
   :scale: 50

   Asignación de formulario de calidad a elemento de pedido

* La aplicación muestra un buscador de formularios de calidad. Existen dos tipos de formularios de calidad: por elementos o porcentaje.

   * Por elementos: Cada elemento es independiente.
   * Por porcentaje: Cada pregunta incrementa el avance en el elemento de pedido en un porcentaje. Los porcentajes deben ser incrementales hasta el 100%.

* El usuario selecciona uno de los formularios dados de alta desde la interface de administración y presiona en "Asignar".
* La aplicación asigna el formulario elegido en el listado de formularios asignados al elemento de pedido.
* El usuario presiona en el botón "Editar" del elemento de pedido.
* La aplicación despliega las preguntas del formulario de calidad en el listado inferior.
* El usuario marca cómo conseguidas las preguntas que son realizadas.
   * Si el tipo de formulario de calidad es por porcentaje, las preguntas son contestadas por orden.
   * Si el tipo de formulario de calidad es por elementos, las preguntas son contestadas en cualquier orden.
