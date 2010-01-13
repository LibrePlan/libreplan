Gestión de costes
#################

.. _costes:
.. contents::


Costes
======

La gestión de costes está planteada para poder realizar una previsión estimativa de los costes de los recursos de un proyecto. Para la gestión de costes se determinó la existencia de las siguientes entidades:

* Tipos de horas trabajadas: Indican los tipos de horas de trabajo de los recursos. Es posible incluir como tipos tanto los tipos de horas para máquinas como para trabajadores. Ejemplos de tipos de horas serían: Extraordinarias pagadas a 20 euros de manera genérica. Los campos que se pueden incluir en los tipos de horas trabajadas:

   * Código: Código externo del tipo de horas.
   * Nombre: Nombre del tipo de hora. Por ejemplo, extraordinaria.
   * Precio por defecto: Precio base por defecto para el tipo de horas.
   * Activado: Indica si el tipo de hora está activado.

* Categorías de coste. Las categorías de coste indican categorías que se utilizan para definir costes dependiendo de los tipos de horas durante unos períodos (estos períodos pueden ser indefinidos). Por ejemplo, el coste de las horas extraordinarias de los oficiales de 1ª durante el siguiente año es de 24 euros hora. Las categorías de coste están formadas por:

   * Nombre: Nombre de la categoría de coste.
   * Activado: Indica si la categoría está activada o no.
   * Listado de tipos de hora asignados a la categoría de coste. Indican diversos períodos y precios para los tipos de hora. Por ejemplo, cada año con cambio de precios se incluye como un período de tipo de hora en este listado. Por otro lado, para cada tipo de horas se mantiene un precio por hora (que puede ser diferente del precio por hora por defecto que se haya incluido para el tipo de hora).



Administración de tipos de horas trabajadas
-------------------------------------------

Para dar de alta tipos de horas trabajadas es necesario dar los siguientes pasos:

* Seleccionar la operación "Administrar tipos de hora de trabajo" en el menú de "Administración".
* La aplicación muestra el listado de tipos de hora existentes.

.. figure:: images/hour-type-list.png
   :scale: 35

   Lista de tipos de horas

* El usuario presiona en el icono de "Editar" o presiona en el botón "Crear".
* La aplicación muestra un formulario de edición del tipo de hora.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Edición de tipos de horas

* El usuario introduce o modifica:

   * El nombre del tipo de hora.
   * El código del tipo de hora.
   * El precio por defecto.
   * Activación/Desactivación del tipo de hora.

* El usuario presiona en "Guardar" o "Guardar y Continuar".

Categorías de coste
-------------------

Para dar de alta categorías de coste es necesario dar los siguientes pasos:

* Seleccionar la operación "Administrar categorías de coste" en el menú de "Administración".
* La aplicación muestra el listado de categorías existentes.

.. figure:: images/category-cost-list.png
   :scale: 50

   Lista de categorías de coste

* El usuario presiona en el icono de "Editar" o presiona en el botón "Crear".
* La aplicación muestra un formulario de edición de la categoría de coste.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Edición de categorías de coste

* El usuario introduce o modifica:

   * El nombre de la categoría de coste.
   * Activación/Desactivación del tipo de hora.
   * Listado de tipos de hora que forman parte de la categoría. Para cada tipo de hora:

      * Tipo de hora: Elegir uno de los tipos de hora existentes en el sistema. Si no existen ninguno es necesario crearlo (se explica en la subsección anterior).
      * Fecha de inicio y fecha fin (opcional esta segunda) del período en el que afecta la categoría de coste.
      * Precio por hora para esta categoría específicamente.

* El usuario presiona en "Guardar" o "Guardar y Continuar".


La asignación de categorías de coste a recursos puede verse en el capítulo de recursos. Acceder a la sección de "Recursos".
