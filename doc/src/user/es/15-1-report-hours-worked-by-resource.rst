Informe de horas trabajadas por recurso
#######################################

.. contents::

Propósito
=========

Este informe permite extraer una lista de tareas y tiempo dedicado por parte de los recursos de la organización en un período de tiempo. Hay varios filtros que permiten configurar la consulta para obtener la información deseada y evitar datos superfluos.

Datos de entrada y filtros
==========================

   * **Fechas**.
      * *Tipo*: Opcional.
      * *Dos campos de fecha*:
         * *Fecha de inicio.* Es la fecha mínima de los partes de trabajo que se desean. Los partes de trabajo con fecha inferior a la *fecha de inicio* se ignoran. Si no se especifica este parámetro, no hay fecha inferior de filtrado.

         * *Fecha de fin.* Es la fecha máxima de los los partes de trabajo que se incorporarán en los resultados del informe. Los partes de trabajo con una fecha posterior que la *fecha de fin* se obvian. Si no se cubre el parámetro, no existe fecha tope para los partes de trabajo a seleccionar.

   * **Filtrado por trabajadores**
      * *Tipo*: Opcional.
      * *Cómo funciona:* Se puede seleccionar un trabajador para restringir el conjunto de partes de trabajo a aquellos correspondientes al trabajador seleccionado. Si se deja en blanco, se seleccionan los partes de trabajo de forma independiente al trabajador al que pertenecen.

   * **Filtrado por etiquetas**
      * *Tipo:* Opcional.
      * *Cómo funciona:* Se puede seleccionar una o varias etiquetas a través del componente de interfaz para su búsqueda y pulsando en el botón *Añadir* para incorporarlas al filtro. Se usan para seleccionar las tareas que serán incluidas en los resultados del informe.

   * **Filtrado por criterio**
      * *Tipo:* Opcional.
      * *Cómo funciona:* Se puede seleccionar uno o varios criterios a través del componente de búsqueda y, después, mediante el pulsado del botón de *Añadir*. Estos criterios se usan para seleccionar los recursos que satisfagan al menos uno de ellos. El informe tendrá en cuenta el tiempo dedicado de los recursos que satisfagan al menos uno de los criterios añadidos en este filtro.

Salida
======

Cabecera
--------

En la cabecera del informe se indica qué filtros ha sido configurados y aplicados para la extracción del informe a la que corresponde una cabecera concreta.

Pie de página
-------------

Include la fecha en la que el reporte se sacó.

Cuerpo
------

El cuerpo del informe contiene los siguientes grupos de información:

* Hay un primer nivel de agregación de información por recurso. Todo el tiempo dedicado por un recurso se muestra junto debajo de la cabecera. Cada recurso se identifica por:

   * *Trabajador*: Apellidos, Nombre
   * *Máquina*: Nombre.

Se muestra una línea de resumen con el total de las  horas trabajadas por un recurso.

* Hay un segundo nivel de agrupamiento consistente en la *fecha*. Todos los partes de trabajo de un recurso concreto en el mismo día se muestra de forma conjunta.

Hay una línea de resumen con el total de las horas trabajadas por recurso.

* Hay un tercer y último nivel en el cual se listan los partes de trabajo del mismo día de un trabajador. La información que se muestra para cada línea de parte de trabajo de esta agrupación es:

   * *Código de tarea* al que las horas reportadas imputan tiempo.
   * *Nombre de la tarea* al que las horas reportadas imputan.
   * *Hora de inicio*. No es obligatorio. Es la hora de inicio a la que el recurso empezó a realizar el trabajo de la tarea.
   * *Hora de fin*- Es opcional. Es la hora de fin hasta la cual el recurso trabajó en la tarea en la fecha especificada.
   * *Campos de texto*. Es opcional. Si el tipo de parte de trabajo tiene campos de texto rellenados con valores, estos valores se muestrann en esta columna según el formato: <Nombre del campo de texto>:<Valor>
   * *Etiquetas*. Contiene valor dependiendo de si el tipo de parte de trabajo contiene al menos un campo de etiquetas en su definición. Si hay varias etiquetas se muestran en la misma columna. El formato es: <Nombre del tipo de etiqueta>.<Valor de la etiqueta asociada>.

